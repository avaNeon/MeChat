package com.neon.mechat.support;

public class SnowflakeIdGenerator
{
    private static final long EPOCH = 1735689600000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long datacenterId;
    private long sequence;
    private long lastTimestamp = -1L;

    /**
     * 初始化雪花 ID 生成器，校验机器号和数据中心号是否在 5 bit 可表达范围内。
     *
     * @param workerId 机器号，范围 0-31
     * @param datacenterId 数据中心号，范围 0-31
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId)
    {
        if (workerId < 0 || workerId > MAX_WORKER_ID)
        {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER_ID);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID)
        {
            throw new IllegalArgumentException("datacenterId must be between 0 and " + MAX_DATACENTER_ID);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个雪花 ID，同一毫秒内通过 sequence 保证递增，检测到时钟回拨时拒绝生成。
     *
     * @return 全局唯一 ID
     */
    public synchronized long nextId()
    {
        long timestamp = currentTimeMillis();
        if (timestamp < lastTimestamp)
        {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate id.");
        }

        if (timestamp == lastTimestamp)
        {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0)
            {
                timestamp = waitNextMillis(lastTimestamp);
            }
        }
        else
        {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 当前毫秒序列号耗尽时等待下一毫秒，避免同一毫秒内 sequence 溢出导致 ID 冲突。
     *
     * @param lastTimestamp 上一次生成 ID 使用的时间戳
     * @return 下一毫秒时间戳
     */
    private long waitNextMillis(long lastTimestamp)
    {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp)
        {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前系统时间戳，单独抽出便于后续测试或替换时间源。
     *
     * @return 当前毫秒时间戳
     */
    private long currentTimeMillis()
    {
        return System.currentTimeMillis();
    }
}
