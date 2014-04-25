package ru.jango.j0loader.image.cache;

import android.graphics.Point;

import java.net.URI;

public interface Cache {

    public boolean put(URI uri, byte[] raw);

    public byte[] get(URI uri);

    public byte[] remove(URI uri);

    public boolean isCached(URI uri);

    public long size();

    public int count();

    public long getMaxCacheSize();

    public void setMaxCacheSize(long maxCacheSize);

    public void setScale(URI uri, Point scale);

    public Point getScale(URI uri);

    public Point resolveScale(URI uri, byte[] loadedData);

    public boolean hasScale(URI uri);

    public Point removeScale(URI uri);

    public int scalesCount();

    public void clear();

}
