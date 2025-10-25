"""add user_retention and retention trigger

Revision ID: e876ac262572
Revises: 
Create Date: 2025-10-25 18:01:19.580684

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'e876ac262572'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


from alembic import op
import sqlalchemy as sa

def upgrade():
    # === 1. Создаём таблицу user_retention ===
    op.create_table(
        'user_retention',
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), primary_key=True),
        sa.Column('retention_days', sa.Integer(), nullable=False, server_default='60'),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), onupdate=sa.func.now()),
    )

    # === 2. Заполняем FREE по умолчанию ===
    op.execute("""
        INSERT INTO user_retention (user_id, retention_days)
        SELECT id, 60 FROM users
        ON CONFLICT (user_id) DO NOTHING;
    """)

    # === 3. Создаём триггер-функцию ===
    op.execute("""
        CREATE OR REPLACE FUNCTION update_user_retention()
        RETURNS TRIGGER AS $$
        BEGIN
            IF NEW.user_type = 'premium' THEN
                NEW.retention_days := 365;
            ELSE
                NEW.retention_days := 60;
            END IF;
            RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;
    """)

    # === 4. Триггер на UPDATE user_type ===
    op.execute("""
        CREATE TRIGGER trigger_update_retention
        BEFORE UPDATE OF user_type ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_user_retention();
    """)

    # === 5. Триггер на INSERT ===
    op.execute("""
        CREATE TRIGGER trigger_insert_retention
        BEFORE INSERT ON users
        FOR EACH ROW
        EXECUTE FUNCTION update_user_retention();
    """)

    # === 6. Удаляем старую retention policy (если была) ===
    op.execute("SELECT remove_retention_policy('a_sensor_vectors');")

    # === 7. Добавляем динамическую политику через job (TimescaleDB) ===
    op.execute("""
        SELECT add_retention_policy('a_sensor_vectors', INTERVAL '1 day');
    """)

    # === 8. Создаём job для ежедневной проверки ===
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
    op.drop_table('user_retention')