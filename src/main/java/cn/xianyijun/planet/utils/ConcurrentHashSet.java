package cn.xianyijun.planet.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Concurrent hash set.
 *
 * @param <E> the type parameter
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>,Serializable {
    private static final Object PRESENT = new Object();

    private final ConcurrentHashMap<E, Object> map;

    /**
     * Instantiates a new Concurrent hash set.
     */
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Instantiates a new Concurrent hash set.
     *
     * @param initialCapacity the initial capacity
     */
    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<>(initialCapacity);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        map.clear();
    }

}
