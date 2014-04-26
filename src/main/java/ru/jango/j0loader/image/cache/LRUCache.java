package ru.jango.j0loader.image.cache;

import android.support.v4.util.LruCache;

import java.net.URI;

/**
 * Special wrapper for {@link android.support.v4.util.LruCache}. That is also memory cache, but
 * with smarter algorithm.
 * <br><br>
 * Disk LRU cache incoming: http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#disk-cache
 */
public class LRUCache extends DefaultCache {

    private LruCache<URI, byte[]> cache;

    public LRUCache() {
        this((int) DEFAULT_MAX_CACHE_SIZE);
    }

    public LRUCache(int maxSize) {
        super();
        cache = new LruCache<URI, byte[]>(maxSize) {
            protected int sizeOf(URI key, byte[] value) {
                return value.length;
            }
        };
    }

    @Override
    public synchronized boolean put(URI uri, byte[] raw) {
        cache.put(uri, raw);
        return true;
    }

    @Override
    public synchronized byte[] get(URI uri) {
        return cache.get(uri);
    }

    @Override
    public synchronized byte[] remove(URI uri) {
        return cache.remove(uri);
    }

    @Override
    public synchronized boolean isCached(URI uri) {
        return cache.get(uri) != null;
    }

    @Override
    public synchronized long size() {
        return cache.size();
    }

    @Override
    public synchronized int count() {
        return cache.snapshot().size();
    }

    @Override
    public long getMaxCacheSize() {
        return cache.maxSize();
    }

    @Override
    public void setMaxCacheSize(long maxCacheSize) {
        throw new IllegalStateException("LRUCache couldn't be resized dynamically. " +
                "Create a new instance with new max size.");
    }

    public synchronized void clearCache() {
        cache.evictAll();
    }

}
