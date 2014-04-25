package ru.jango.j0loader.image.cache;

import android.graphics.Point;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import ru.jango.j0util.BmpUtil;

/**
 * Default cache implementation.
 * <ul>
 * <li>data is stored in memory</li>
 * <li>data is stored as encoded byte arrays</li>
 * <li>default cache size - 5M</li>
 * </ul>
 */
public class DefaultCache implements Cache {

    public static final long DEFAULT_MAX_CACHE_SIZE = 5000000;

    private Map<URI, byte[]> cache;
	private Map<URI, Point> scales;
	private long maxCacheSize;

    public DefaultCache() {
        cache = new HashMap<URI, byte[]>();
		scales = new HashMap<URI, Point>();
        setMaxCacheSize(DEFAULT_MAX_CACHE_SIZE);
    }

    public synchronized void clear() {
        clearCache();
        clearScales();
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //		Cache controlling methods
    //
    ////////////////////////////////////////////////////////////////////////

    public synchronized boolean put(URI uri, byte[] raw) {
        if (size() <= getMaxCacheSize()) {
            cache.put(uri, raw);
            return true;
        }

        return false;
    }

    /**
     * Pulls an image data from the cache, if it was previously cached.
     *
     * @return  previously cached image data, or NULL
     * @see android.graphics.BitmapFactory#decodeByteArray(byte[], int, int)
     */
    public synchronized byte[] get(URI uri) {
        return cache.get(uri);
    }

    /**
     * Removes an image from cache.
     *
     * @return  just removed image data, or NULL
     */
    public synchronized byte[] remove(URI uri) {
        return cache.remove(uri);
    }

    /**
     * Clears cache.
     */
    public synchronized void clearCache() {
        cache.clear();
    }

    /**
     * Checks if an image was previously cached.
     */
    public synchronized boolean isCached(URI uri) {
        return cache.containsKey(uri);
    }

    /**
     * Returns full cache size in bytes.
     */
    public synchronized long size() {
        long size = 0;
        for (URI uri : cache.keySet())
            size += cache.get(uri).length;

        return size;
    }

    public synchronized int count() {
        return cache.size();
    }

    /**
     * Returns max allowed cache size in bytes.
     * @see #DEFAULT_MAX_CACHE_SIZE
     */
    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Sets max allowed cache size in bytes.
     * @see #DEFAULT_MAX_CACHE_SIZE
     */
    public void setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //		Scales controlling methods
    //
    ////////////////////////////////////////////////////////////////////////

    public synchronized void setScale(URI uri, Point scale) {
        scales.put(uri, scale);
    }

    public synchronized Point getScale(URI uri) {
        return scales.get(uri);
    }

    public synchronized Point resolveScale(URI uri, byte[] loadedData) {
        final boolean tooBig = BmpUtil.isTooBig(loadedData);
        if (hasScale(uri)) return getScale(uri);
        else if (tooBig) return new Point(BmpUtil.MAX_TEXTURE_SIZE, BmpUtil.MAX_TEXTURE_SIZE);
        else return null;
    }

    public synchronized boolean hasScale(URI uri) {
        return scales.containsKey(uri);
    }

    public synchronized Point removeScale(URI uri) {
        return scales.remove(uri);
    }

    public synchronized int scalesCount() {
        return scales.size();
    }

    public synchronized void clearScales() {
        scales.clear();
    }

}
