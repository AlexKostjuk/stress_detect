"""create hypertable with user partitioning and fix retention trigger

Revision ID: 75992657fa7d
Revises: e876ac262572
Create Date: 2025-10-25 18:08:46.082218
"""

from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '75992657fa7d'
down_revision: Union[str, None] = 'e876ac262572'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade():
    # === 1. Заполняем FREE пользователей по умолчанию ===
    op.execute("""
        INSERT INTO user_retention (user_id, retention_days)
        SELECT id, 60 FROM users
        ON CONFLICT (user_id) DO NOTHING;
    """)

    # === 2. Функция для создания/обновления retention записи ===
    op.execute("""
        CREATE OR REPLACE FUNCTION update_user_retention()
        RETURNS TRIGGER AS $$
        BEGIN
            IF NEW.user_type = 'premium' THEN
                INSERT INTO user_retention (user_id, retention_days, updated_at)
                VALUES (NEW.id, 365, now())
                ON CONFLICT (user_id) DO UPDATE
                SET retention_days = 365, updated_at = now();
            ELSE
                INSERT INTO user_retention (user_id, retention_days, updated_at)
                VALUES (NEW.id, 60, now())
                ON CONFLICT (user_id) DO UPDATE
                SET retention_days = 60, updated_at = now();
            END IF;
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    """)

    # === 3. Триггеры на INSERT и UPDATE user_type в таблице users ===
    op.execute("""
        CREATE TRIGGER trigger_insert_retention
        AFTER INSERT ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_user_retention();
    """)

    op.execute("""
        CREATE TRIGGER trigger_update_retention
        AFTER UPDATE OF user_type ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_user_retention();
    """)

    # === 4. Удаляем старую retention policy (если была) ===
    op.execute("SELECT remove_retention_policy('a_sensor_vectors');")

    # === 5. Добавляем новую динамическую retention policy (TimescaleDB) ===
    op.execute("""
        SELECT add_retention_policy('a_sensor_vectors', INTERVAL '1 day');
    """)

    # === 6. Настраиваем job для ежедневной проверки ===
    op.execute("""
        SELECT alter_job(
            (SELECT job_id FROM timescaledb_information.jobs WHERE proc_name = 'policy_retention'),
            schedule_interval => INTERVAL '1 day'
        );
    """)


def downgrade():
    op.execute("SELECT remove_retention_policy('a_sensor_vectors');")
    op.execute("DROP TRIGGER IF EXISTS trigger_update_retention ON users;")
    op.execute("DROP TRIGGER IF EXISTS trigger_insert_retention ON users;")
    op.execute("DROP FUNCTION IF EXISTS update_user_retention();")
    op.execute("DELETE FROM user_retention;")