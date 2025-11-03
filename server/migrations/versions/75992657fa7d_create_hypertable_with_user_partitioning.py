"""dynamic retention: global 365 days + cleanup FREE users

Revision ID: 75992657fa7d
Revises: e876ac262572
"""

from alembic import op

revision = '75992657fa7d'
down_revision = 'e876ac262572'
branch_labels = None
depends_on = None


def upgrade():
    # === 1. Заполняем FREE ===
    op.execute("""
        INSERT INTO user_retention (user_id, retention_days)
        SELECT id, 60 FROM users
        ON CONFLICT (user_id) DO NOTHING;
    """)

    # === 2. Функция триггера ===
    op.execute("""
        CREATE OR REPLACE FUNCTION update_user_retention()
        RETURNS TRIGGER AS $$
        BEGIN
            INSERT INTO user_retention (user_id, retention_days, updated_at)
            VALUES (NEW.id, CASE WHEN NEW.user_type = 'premium' THEN 365 ELSE 60 END, now())
            ON CONFLICT (user_id) DO UPDATE
            SET retention_days = EXCLUDED.retention_days, updated_at = now();
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    """)

    # === 3. Безопасные триггеры через DO $$ ===
    op.execute("""
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_insert_retention'
            ) THEN
                CREATE TRIGGER trigger_insert_retention
                AFTER INSERT ON users
                FOR EACH ROW
                EXECUTE FUNCTION update_user_retention();
            END IF;
        END$$;
    """)
    op.execute("""
        DO $$
        BEGIN
            IF NOT EXISTS (
                SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_update_retention'
            ) THEN
                CREATE TRIGGER trigger_update_retention
                AFTER UPDATE OF user_type ON users
                FOR EACH ROW
                EXECUTE FUNCTION update_user_retention();
            END IF;
        END$$;
    """)

    # === 4. Политика 365 дней ===
    op.execute("""
        DO $$
        BEGIN
            -- Проверяем, есть ли уже retention policy для таблицы
            IF EXISTS (
                SELECT 1
                FROM timescaledb_information.jobs j
                JOIN timescaledb_information.job_stats js ON js.job_id = j.job_id
                WHERE j.proc_name = 'policy_retention'
                  AND js.hypertable_name = 'a_sensor_vectors'
            ) THEN
                PERFORM remove_retention_policy('a_sensor_vectors');
            END IF;
        END$$;
    """)

    op.execute("SELECT add_retention_policy('a_sensor_vectors', INTERVAL '365 days');")

    # === 5. Job очистки FREE ===
    op.execute("""
        CREATE OR REPLACE FUNCTION cleanup_free_users_data(job_id int, config jsonb)
        RETURNS void AS $$
        BEGIN
            DELETE FROM a_sensor_vectors v
            USING users u
            JOIN user_retention ur ON ur.user_id = u.id
            WHERE v.user_id = u.id
              AND u.user_type = 'free'
              AND v.timestamp < now() - (ur.retention_days || ' days')::interval;
        END;
        $$ LANGUAGE plpgsql;
    """)
    op.execute("SELECT add_job('cleanup_free_users_data', '1 day');")


def downgrade():
    op.execute("SELECT remove_retention_policy('a_sensor_vectors');")
    op.execute("DROP TRIGGER IF EXISTS trigger_update_retention ON users;")
    op.execute("DROP TRIGGER IF EXISTS trigger_insert_retention ON users;")
    op.execute("DROP FUNCTION IF EXISTS update_user_retention();")
    op.execute("DROP FUNCTION IF EXISTS cleanup_free_users_data(int, jsonb);")
    op.execute("DELETE FROM user_retention;")