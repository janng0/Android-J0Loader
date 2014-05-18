package ru.jango.j0loader.image.cache;

import android.graphics.Point;

import java.net.URI;

import ru.jango.j0util.BmpUtil;

/**
 * The simplest {@link ru.jango.j0loader.image.cache.Cache} implementation - does nothing. Also
 * could be named as fake cache. If you don't need to cache anything at all you can use this class.
 */
public class NullCache implements Cache {
    @Override
    public boolean put(URI uri, byte[] raw) {
        return false;
    }

    @Override
    public byte[] get(URI uri) {
        return null;
    }

    @Override
    public byte[] remove(URI uri) {
        return null;
    }

    @Override
    public boolean isCached(URI uri) {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public long getMaxCacheSize() {
        return 0;
    }

    @Override
    public void setMaxCacheSize(long maxCacheSize) {
    }

    @Override
    public void setScale(URI uri, Point scale) {
    }

    @Override
    public Point getScale(URI uri) {
        return null;
    }

    @Override
    public Point resolveScale(URI uri, byte[] loadedData) {
        if (BmpUtil.isTooBig(loadedData))
            return new Point(BmpUtil.MAX_TEXTURE_SIZE, BmpUtil.MAX_TEXTURE_SIZE);

        return null;
    }

    @Override
    public boolean hasScale(URI uri) {
        return false;
    }

    @Override
    public Point removeScale(URI uri) {
        return null;
    }

    @Override
    public int scalesCount() {
        return 0;
    }

    @Override
    public void clear() {
    }
}
