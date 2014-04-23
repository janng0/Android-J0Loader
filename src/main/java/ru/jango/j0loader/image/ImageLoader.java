package ru.jango.j0loader.image;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import ru.jango.j0loader.DataLoader;
import ru.jango.j0loader.queue.DefaultQueue;
import ru.jango.j0loader.queue.Queue;
import ru.jango.j0loader.Request;
import ru.jango.j0util.BmpUtil;
import ru.jango.j0util.LogUtil;

/**
 * Advanced lightweight image loader. Main features:
 * <ul>
 * <li>internal cache - by default images are cached in memory as encoded byte arrays (should be
 * transformed into {@link android.graphics.Bitmap} with
 * {@link android.graphics.BitmapFactory#decodeByteArray(byte[], int, int, android.graphics.BitmapFactory.Options)});
 * it takes some time to retrieve an image from cache then, but on the other hand pretty much data
 * could be cached (encoded data is much smaller, than raw pixel data)</li>
 * <li>smart scaling - loader's clients could specify the desired image size and loader will
 * automatically and asynchronously (in the downloading thread) scale images before passing it to
 * clients; for scaling are used algorithms from {@link ru.jango.j0util.BmpUtil}</li>
 * </ul>
 * <br>
 *
 * Other features:
 * <ul>
 * <li>images are cached in already scaled size</li>
 * <li>only one instance of a single image could be cached - if clients try to load same image with
 * different scales, would be selected a larger one and an image will be cached in this larger scale;
 * if clients try to load an image that is already cached but in smaller scale, then the image
 * would be reloaded, rescaled in new large scale and recached
 * {@link #addToQueue(ru.jango.j0loader.Request, android.graphics.Point)}</li>
 * <li>due to that, images could be retrieved from cache by it's {@link java.net.URI}, not a
 * {@link ru.jango.j0loader.Request}</li>
 * <li>cache is separated as a standalone class, so you can different caching strategies,
 * or create your own (default is simple memory cache)</li>
 * <li>by default cache size is limited by {@link #DEFAULT_MAX_CACHE_SIZE}; be aware, that huge
 * cache can cause {@link java.lang.OutOfMemoryError}</li>
 * <li>loading from cache is transparent for clients - clients don't know was requested image
 * cached or not (if it was, client just get required image faster)</li>
 * <li>loading is separated into default and cache threads and queues - defaults come from
 * {@link ru.jango.j0loader.DataLoader} and cache thread and queue are created and managed by
 * {@link ru.jango.j0loader.image.ImageLoader} itself</li>
 * </ul>
 */
public class ImageLoader extends DataLoader<Bitmap> {

    // TODO separate cache as a standalone class

	public static final long DEFAULT_MAX_CACHE_SIZE = 5000000;
	
	private Thread cacheLoaderThread;
	private Queue cacheQueue;
	
	private final Map<URI, byte[]> cache;
	private final Map<URI, Point> scales;
	private long maxCacheSize;
	
	public ImageLoader() {
		super();
		
		cache = new HashMap<URI, byte[]>();
		scales = new HashMap<URI, Point>();
		setMaxCacheSize(DEFAULT_MAX_CACHE_SIZE);
	}

    public ImageLoader(LoadingListener<Bitmap> listener) {
        this();
        addLoadingListener(listener);
    }

    @Override
	public void addToQueue(Request request) {
		synchronized(cache) {
			if (cache.containsKey(request.getURI()))
				getCacheQueue().add(request);
			else getQueue().add(request);
		}
	}
	
	@Override
	public void addToQueue(Collection<Request> requests) {
		for (Request request : requests) {
			synchronized(cache) {
				if (cache.containsKey(request.getURI()))
					getCacheQueue().add(request);
				else getQueue().add(request);
			}
		}
	}

    /**
     * Adds an element into loading queues. Automatically checks cache and chooses a queue. Second
     * parameter specifies a size, in witch image should be cached and returned to the client. <br><br>
     * If clients try to load a cached image, but with smaller scale, loader will return a cached
     * image. <br><br>
     * If clients try to load a cached image, but with larger scale, loader will remove that image
     * from cache, then reload it from URI, rescale into size, put again in cache and return it to
     * the client.
     *
     * @param request   a {@link java.net.URI} where to take the image
     * @param scale	    a size, in witch image should be cached and returned to the client; be aware,
     *                  that max texture size in Android is 2048x2048, loader automatically handles
     *                  appropriate scaling
     */
    public void addToQueue(Request request, Point scale) {
        synchronized(scales) {
            if (scaleLarger(scale, scales.get(request.getURI()))) {
                scales.put(request.getURI(), scale);
                cache.remove(request.getURI());
            }
        }

        addToQueue(request);
    }

	@Override
	public void removeFromQueue(Request request) {
		super.removeFromQueue(request);
		getCacheQueue().remove(request);
	}
	
	@Override
	public void start()  {
		super.start();
		
		final Thread thread = getCacheLoaderThread();
		if (!thread.isAlive()) thread.start();
	}

    /**
     * Resets the loader to it's initial clear state: <br>
     * <ul>
     * <li>stop operations by {@link #stopWorking()}</li>
     * <li>clear queue by {@link #clearQueue()}</li>
     * <li>clear cache by {@link #clearCache()}</li>
     * <li>clear queue queue by {@link #clearCacheQueue()}</li>
     * </ul>
     */
    @Override
    public void reset() {
        stopWorking();
        clearCache();
        clearQueue();
        clearCacheQueue();

        synchronized(scales) {
            scales.clear();
        }
    }

	private boolean scaleLarger(Point p1, Point p2) {
		if (p1 == null && p2 == null) return false;
		if (p1 == null) return false;
		if (p2 == null) return true;
		
		return (p1.x * p1.y) - (p2.x * p2.y) > 100;
	}

    ////////////////////////////////////////////////////////////////////////
    //
    //		Cache controlling methods
    //
    ////////////////////////////////////////////////////////////////////////

	/**
     * Pulls an image data from the cache, if it was previously cached.
	 *
     * @return  previously cached image data, or NULL
	 * @see android.graphics.BitmapFactory#decodeByteArray(byte[], int, int)
	 */
	public byte[] getCachedData(Request request) {
		synchronized(cache) { 
			return cache.get(request.getURI()); 
		}
	}
	
	/**
	 * Removes an image from cache.
	 * 
	 * @return  kust removed image data, or NULL
	 */
	public byte[] removeFromCache(Request request) {
		return cache.remove(request.getURI());
	}
	
	/**
	 * Clears cache.
	 */
	public void clearCache() {
		synchronized(cache) { 
			cache.clear(); 
		}
	}

    /**
     * Checks if an image was previously cached.
     */
	public boolean isCached(Request request) {
		return cache.containsKey(request.getURI());
	}
	
	/**
	 * Returns full cache size in bytes.
	 */
	public long getCacheSize() {
		long size = 0;
		synchronized(cache) {
			for (URI uri : cache.keySet())
				size += cache.get(uri).length;
		}
		
		return size;
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
    //		Cache queue controlling methods
    //
    ////////////////////////////////////////////////////////////////////////

    /**
     * Removes a {@link Request} from the cache loading queue. If this {@link ru.jango.j0loader.Request} is
     * already being processed, it could be retrieved by {@link #getCurrentCacheQueueElement()} and the
     * procession could be stopped by {@link #cancelCurrent()} (stops only the current, not all queue).
     *
     * @param request   a {@link Request} to remove
     */
    public void removeFromCacheQueue(Request request) {
        getCacheQueue().remove(request);
    }

	/**
	 * Checks if the cache queue has elements.
	 */
	public boolean isCacheQueueEmpty() {
		return getCacheQueue().isEmpty();
	}
	
	/**
	 * Clears cache queue.
	 */
	public void clearCacheQueue() {
		getCacheQueue().clear();
	}

    /**
     * Returns current element in cache queue (witch is processed now) or null.
     */
	public Request getCurrentCacheQueueElement() {
		return getCacheQueue().current();
	}
	
	/**
     * Special method for queue configuration. By default {@link ru.jango.j0loader.image.ImageLoader}
     * creates an instance of {@link ru.jango.j0loader.queue.DefaultQueue}, but if a queue with
     * different logic is required, it could be substituted here.
     * <br><br>
     * This method with conjunction of {@link ru.jango.j0loader.queue.Queue} hierarchy defines a
     * usual Iterator pattern.
     *
     * @return  cache loading queue instance
	 */
	protected Queue getCacheQueue() {
		if (cacheQueue == null) cacheQueue = new DefaultQueue();
		return cacheQueue;
	}

    private void addToCache(Request request, byte[] raw) {
        synchronized(cache) {
            if (!cache.containsKey(request.getURI()) && getCacheSize()<=maxCacheSize) {
                cache.put(request.getURI(), raw);
                LogUtil.i(ImageLoader.class, "added to cache; " +
                        "cache size bytes: " + getCacheSize());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //		Loading methods
    //
    ////////////////////////////////////////////////////////////////////////

    /**
	 * Возвращает поток {@link Thread}, в котором исполняется очередь загрузки 
	 * изображений из кэша. В подклассах можно переписать метод, подставив другой поток.
	 * 
	 * @return	поток {@link Thread}, в котором исполняется очередь загрузки 
	 * 			изображений из кэша
	 */
	protected Thread getCacheLoaderThread() {
		if (!(cacheLoaderThread!=null && cacheLoaderThread.isAlive())) 
			return cacheLoaderThread = new Thread(cacheQueueRunnable);
		
		return cacheLoaderThread;
	}

    private Point getScale(Request request, byte[] loadedData) {
        synchronized(scales) {
            final boolean tooBig = BmpUtil.isTooBig(loadedData);
            if (scales.containsKey(request.getURI())) return scales.get(request.getURI());
            else if (tooBig) return new Point(BmpUtil.MAX_TEXTURE_SIZE, BmpUtil.MAX_TEXTURE_SIZE);
            else return null;
        }
    }

    private boolean processFromCache(Request request) {
		synchronized(cache) { 
			if (cache.containsKey(request.getURI())) {
				LogUtil.i(ImageLoader.class, "loading from cache: "+request.getURI());
				
				final byte[] raw = cache.get(request.getURI());
				postProcessFinished(request, raw, BitmapFactory.decodeByteArray(raw, 0, raw.length));
				return true;
			}
		}		
		
		return false;
	}

	private void processFromURI(Request request) throws DataFormatException, IOException, URISyntaxException {
		LogUtil.i(ImageLoader.class, "loading from uri: "+request.getURI());
        final byte[] loadedData = load(request);
        final Point scale = getScale(request, loadedData);

        Bitmap bmp;
        byte[] rawData;
        if (scale == null) {
            bmp = BitmapFactory.decodeByteArray(loadedData, 0, loadedData.length);
            rawData = loadedData;
        } else {
            bmp = BmpUtil.scale(loadedData, BmpUtil.ScaleType.PROPORTIONAL_FIT, scale.x, scale.y);
            rawData = BmpUtil.bmpToByte(bmp, Bitmap.CompressFormat.PNG, 100);
        }

        addToCache(request, rawData);
		postProcessFinished(request, rawData, bmp);
	}
	
	@Override
	protected void loadInBackground(Request request) throws Exception {
		if (!processFromCache(request)) processFromURI(request);
	}
	
	private Runnable cacheQueueRunnable = new Runnable() {
		@Override
		public void run()  {
			while (!isCacheQueueEmpty() && canWork()) {
				final Request request = getCacheQueue().next();
				
				try {
					postLoadingStarted(request);
					loadInBackground(request);
				} catch (Exception e) { postProcessFailed(request, e); }

				LogUtil.logMemoryUsage();
			}
		}
	};
	
}