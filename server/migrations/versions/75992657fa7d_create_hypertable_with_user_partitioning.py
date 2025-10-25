"""create hypertable with user partitioning

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