package ru.jango.j0loader.image.cache;

import android.graphics.Point;

import java.net.URI;

public interface Cache {

    /**
     * Adds a chunk of data into cache. Also checks cache size limits before adding. Method is
     * called just after processing (loading and scaling) of next queue element.
     *
     * @param uri   key {@link java.net.URI} to retrieve data later
     * @param raw   an image compressed into a byte array
     * @return      TRUE if an data was put into cache; FALSE if cache is full - max
     *              cache size was reached
     */
    public boolean put(URI uri, byte[] raw);

    /**
     * Pulls an image data from the cache, if it was previously cached. Method is called when
     * loading from cache.
     *
     * @return  previously cached image data, or NULL
     * @see android.graphics.BitmapFactory#decodeByteArray(byte[], int, int)
     */
    public byte[] get(URI uri);

    /**
     * Removes an image from cache.
     *
     * @return  just removed image data, or NULL
     */
    public byte[] remove(URI uri);

    /**
     * Checks if an image was previously cached. Method is called to find out what loading should
     * be performed: loading from cache, or from {@link java.net.URI}.
     */
    public boolean isCached(URI uri);

    /**
     * Returns full cache size in bytes (sum of all elements' sizes).
     */
    public long size();

    /**
     * Returns number of elements in cache.
     */
    public int count();

    /**
     * Returns max allowed cache size in bytes.
     */
    public long getMaxCacheSize();

    /**
     * Sets max allowed cache size in bytes.
     */
    public void setMaxCacheSize(long maxCacheSize);

    /**
     * Sets scale for the specified image. It doesn't manage the cache itself - just saves the
     * scale. Method is called while adding a new request into queue.
     */
    public void setScale(URI uri, Point scale);

    /**
     * Returns previously remembered scale, or NULL.
     */
    public Point getScale(URI uri);

    /**
     * Returns appropriate scale for just loaded image. Not only returns previously saved scale,
     * but also applies some checks. For example, checks max texture size limits for second
     * parameter. Method is called during postprocessing an image just before scaling.
     *
     * @param uri           {@link java.net.URI} of an image
     * @param loadedData    just loaded not scaled image data
     * @return              final image scale, or NULL (that means image shouldn't be scaled)
     */
    public Point resolveScale(URI uri, byte[] loadedData);

    /**
     * Checks if scale was previously set for the specified image.
     */
    public boolean hasScale(URI uri);

    /**
     * Removes scale for the specified image.
     */
    public Point removeScale(URI uri);

    /**
     * Returns number of remembered scales.
     */
    public int scalesCount();

    /**
     * Fully clears the cache (both images data and scales).
     */
    public void clear();

}
