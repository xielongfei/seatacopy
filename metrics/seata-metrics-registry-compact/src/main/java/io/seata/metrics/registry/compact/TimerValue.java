package io.seata.metrics.registry.compact;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author: xielongfei
 * @date: 2021/05/08 14:24
 * @description:
 */
public class TimerValue {
    private final LongAdder count;

    private final LongAdder total;

    private final AtomicLong max;

    public long getCount() {
        return count.longValue();
    }

    public long getTotal() {
        return total.longValue();
    }

    public long getMax() {
        return max.get();
    }

    public double getAverage() {
        double count = this.count.doubleValue();
        double total = this.total.doubleValue();
        return count == 0 ? 0 : total / count;
    }

    public TimerValue() {
        this.count = new LongAdder();
        this.total = new LongAdder();
        this.max = new AtomicLong(0);
    }

    public void record(long value, TimeUnit unit) {
        if (value < 0) {
            return;
        }
        long changeValue = unit == TimeUnit.MICROSECONDS ? value : TimeUnit.MICROSECONDS.convert(value, unit);
        this.count.increment();
        this.total.add(changeValue);
        this.max.accumulateAndGet(changeValue, Math::max);
    }

}
