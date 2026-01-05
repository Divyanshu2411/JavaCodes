package customDS.hashmaps;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockConcurrentHashMap<K, V> extends CusHashMap<K, V> {

    // 1. Initialize the standard implementation
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    @Override
    public void add(K key, V val) {
        writeLock.lock(); // Exclusive access
        try {
            super.add(key, val); // Call your customDS.hashmaps.CusHashMap logic
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V get(K key) {
        readLock.lock(); // Shared access
        try {
            return super.get(key);
        } finally {
            readLock.unlock();
        }
    }
}