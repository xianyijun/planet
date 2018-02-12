package cn.xianyijun.planet.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Atomic positive integer.
 */
public class AtomicPositiveInteger extends Number {
    private final AtomicInteger i;

    /**
     * Instantiates a new Atomic positive integer.
     */
    public AtomicPositiveInteger() {
        i = new AtomicInteger();
    }

    /**
     * Instantiates a new Atomic positive integer.
     *
     * @param initialValue the initial value
     */
    public AtomicPositiveInteger(int initialValue) {
        i = new AtomicInteger(initialValue);
    }

    /**
     * Gets and increment.
     *
     * @return the and increment
     */
    public final int getAndIncrement() {
        for (; ; ) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE ? 0 : current + 1);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    /**
     * Gets and decrement.
     *
     * @return the and decrement
     */
    public final int getAndDecrement() {
        for (; ; ) {
            int current = i.get();
            int next = (current <= 0 ? Integer.MAX_VALUE : current - 1);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    /**
     * Increment and get int.
     *
     * @return the int
     */
    public final int incrementAndGet() {
        for (; ; ) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE ? 0 : current + 1);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * Decrement and get int.
     *
     * @return the int
     */
    public final int decrementAndGet() {
        for (; ; ) {
            int current = i.get();
            int next = (current <= 0 ? Integer.MAX_VALUE : current - 1);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * Get int.
     *
     * @return the int
     */
    public final int get() {
        return i.get();
    }

    /**
     * Set.
     *
     * @param newValue the new value
     */
    public final void set(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        i.set(newValue);
    }

    /**
     * Gets and set.
     *
     * @param newValue the new value
     * @return the and set
     */
    public final int getAndSet(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        return i.getAndSet(newValue);
    }

    /**
     * Gets and add.
     *
     * @param delta the delta
     * @return the and add
     */
    public final int getAndAdd(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        for (; ; ) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    /**
     * Add and get int.
     *
     * @param delta the delta
     * @return the int
     */
    public final int addAndGet(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        for (; ; ) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * Compare and set boolean.
     *
     * @param expect the expect
     * @param update the update
     * @return the boolean
     */
    public final boolean compareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return i.compareAndSet(expect, update);
    }

    /**
     * Weak compare and set boolean.
     *
     * @param expect the expect
     * @param update the update
     * @return the boolean
     */
    public final boolean weakCompareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return i.weakCompareAndSet(expect, update);
    }

    @Override
    public byte byteValue() {
        return i.byteValue();
    }

    @Override
    public short shortValue() {
        return i.shortValue();
    }

    @Override
    public int intValue() {
        return i.intValue();
    }

    @Override
    public long longValue() {
        return i.longValue();
    }

    @Override
    public float floatValue() {
        return i.floatValue();
    }

    @Override
    public double doubleValue() {
        return i.doubleValue();
    }

    @Override
    public String toString() {
        return i.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + i.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AtomicPositiveInteger)) {
            return false;
        }
        AtomicPositiveInteger other = (AtomicPositiveInteger) obj;
        return i.intValue() == other.i.intValue();
    }
}
