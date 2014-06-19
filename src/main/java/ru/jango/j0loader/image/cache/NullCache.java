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
