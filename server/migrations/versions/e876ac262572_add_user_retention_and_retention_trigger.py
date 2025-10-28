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


import sqlalchemy as sa

from alembic import op

def upgrade():
    # Создаём hypertable с партиционированием
    op.execute("""
        SELECT create_hypertable(
            'a_sensor_vectors',
            'timestamp',
            partitioning_column => 'user_id',
            number_partitions => 4
        );
    """)

    # Индексы (опционально, но полезно)
    op.execute("""
        CREATE INDEX IF NOT EXISTS ix_sensor_vectors_device_timestamp 
        ON a_sensor_vectors (device_id, timestamp DESC);
    """)

    # Включаем сжатие (compression)
    op.execute("""
        ALTER TABLE a_sensor_vectors SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'user_id',
            timescaledb.compress_orderby = 'timestamp DESC'
        );
    """)

    # Политика сжатия: сжимать чанки старше 7 дней
    op.execute("""
        SELECT add_compression_policy('a_sensor_vectors', INTERVAL '7 days');
    """)

    # Политика удаления: удалять данные старше 60 дней (FREE)
    op.execute("""
        SELECT add_retention_policy('a_sensor_vectors', INTERVAL '60 days');
    """)


def downgrade():
    # Отключаем политики
    op.execute("SELECT remove_retention_policy('a_sensor_vectors');")
    op.execute("SELECT remove_compression_policy('a_sensor_vectors');")
    # Удаляем hypertable
    op.execute("SELECT drop_hypertable('a_sensor_vectors');")