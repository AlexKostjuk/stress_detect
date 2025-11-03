"""add hypertable, compression, and prepare for dynamic retention

Revision ID: e876ac262572
Revises:
Create Date: 2025-10-25 18:01:19.580684
"""
from alembic import op

revision = 'e876ac262572'
down_revision = None  # ← Первая миграция
branch_labels = None
depends_on = None

def upgrade():
    # === 1. Создаём hypertable ===
    op.execute("""
        SELECT create_hypertable(
            'a_sensor_vectors',
            'timestamp',
            partitioning_column => 'user_id',
            number_partitions => 4
        );
    """)

    # === 2. Индексы ===
    op.execute("""
        CREATE INDEX IF NOT EXISTS ix_sensor_vectors_device_timestamp 
        ON a_sensor_vectors (device_id, timestamp DESC);
    """)

    # === 3. Включаем сжатие ===
    op.execute("""
        ALTER TABLE a_sensor_vectors SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'user_id',
            timescaledb.compress_orderby = 'timestamp DESC'
        );
    """)

    # === 4. Политика сжатия ===
    op.execute("""
        SELECT add_compression_policy('a_sensor_vectors', INTERVAL '7 days');
    """)

    # УДАЛЕНО: add_retention_policy('60 days')
    # → Переносим в следующую миграцию (динамическая логика)


def downgrade():
    op.execute("SELECT remove_compression_policy('a_sensor_vectors');")
    op.execute("SELECT drop_hypertable('a_sensor_vectors');")