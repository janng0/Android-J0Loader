/*
 * The MIT License Copyright (c) 2014 Krayushkin Konstantin (jangokvk@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
