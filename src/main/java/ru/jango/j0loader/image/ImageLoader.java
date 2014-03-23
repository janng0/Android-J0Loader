package ru.jango.j0loader.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;

import ru.jango.j0loader.DataLoader;
import ru.jango.j0loader.queue.DefaultQueue;
import ru.jango.j0loader.queue.Queue;
import ru.jango.j0loader.Request;
import ru.jango.j0util.LogUtil;

/**
 * Класс-загрузчик картинок. Загруженные картинки сохраняет внутри себя во внутреннем 
 * кэше в виде массивов байт (для облегчения). Так же можно задать размер, в котором 
 * нужно выдавать картинку после загрузки. 
 * <br><br>
 * -- для клиентов класса нет разграничения между загрузкой и выдачей из кэша - когда до 
 * картинки доходит очередь загрузки, она либо выдается быстро из кэша, либо долго 
 * загружается.
 * <br><br>
 * -- ограничений на размер кэша нет - он довольно компактный, но потенциально может 
 * уронить приложение при нехватке памяти
 * 
 *
 */
public class ImageLoader extends DataLoader<Bitmap> {
	
	public static final long DEFAULT_MAX_CACHE_SIZE = 5000000;
	
	private Thread cacheLoaderThread;
	private Queue cacheQueue;
	
	private final Map<URI, byte[]> cache;
	private final Map<URI, Rect> scales;
	private long maxCacheSize;
	
	public ImageLoader(LoadingListener<Bitmap> listener) {
		this();
		addLoadingListener(listener);
	}
	
	public ImageLoader() {
		super();
		
		cache = new HashMap<URI, byte[]>();
		scales = new HashMap<URI, Rect>();
		setMaxCacheSize(DEFAULT_MAX_CACHE_SIZE);
	}

	@Override
	public void addToQueue(Request request) {
		synchronized(cache) {
			if (cache.containsKey(request.getURI()))
				getCacheQueue().add(request);
			else getQueue().add(request);
		}
		
		executeQueue();
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
		
		executeQueue();
	}
	
	@Override
	public void removeFromQueue(Request request) {
		super.removeFromQueue(request);
		getCacheQueue().remove(request);
	}
	
	@Override
	public void executeQueue()  {
		super.executeQueue();
		
		final Thread thread = getCacheLoaderThread();
		if (!thread.isAlive()) thread.start();
	}
	
	/**
	 * Добавить изображение в очередь загрузки. Второй параметр задает
	 * размер, в котором нужно возвращать загруженное изображение (обработка изображения
	 * так же вынесена в отдельный поток).
	 * 
	 * @param request	{@link java.net.URI}, откуда грузить изображение
	 * @param scale	размер, в котором изображениие будет возвращено клиенту; максимум - 2048х2048
	 */
	public void addToQueue(Request request, Rect scale) {
		synchronized(scales) { 
			if (rectBigger(scale, scales.get(request.getURI()))) {
				scales.put(request.getURI(), scale);
				cache.remove(request.getURI());
			}
		}
		
		addToQueue(request);
	}
	
	private boolean rectBigger(Rect r1, Rect r2) {
		if (r1 == null && r2 == null) return false;
		if (r1 == null) return false;
		if (r2 == null) return true;
		
		return (r1.width() * r1.height()) - (r2.width() * r2.height()) > 100;
	}
	
	/**
	 * Вытаскивает изображение обычного из кэша, если оно там есть. 
	 * 
	 * @param request	{@link Request} изображения, которое нужно вытащить
	 * @return	изображение в виде массива байт, либо null
	 * @see android.graphics.BitmapFactory#decodeByteArray(byte[], int, int)
	 */
	public byte[] getCachedData(Request request) {
		synchronized(cache) { 
			return cache.get(request.getURI()); 
		}
	}
	
	/**
	 * Удаляет конкретный элемент из обычного кэша.
	 * 
	 * @param request {@link Request} картинки, которую нужно снести из кэша
	 * @return	только что удаленный элемент, либо null
	 */
	public byte[] removeFromCache(Request request) {
		return cache.remove(request.getURI());
	}
	
	/**
	 * Очищает обычный кэш.
	 */
	public void clearCache() {
		synchronized(cache) { 
			cache.clear(); 
		}
	}
	
	/**
	 * Возвращает загрузчик в исходное состояние: <br>
	 * -- отдает команду потоку загрузки остановиться (реально поток 
	 * может остановиться не сразу) <br>
	 * -- очищает кэш
	 * -- очищает очередь загрузки
	 * -- очищает очередь загрузки из кэша
	 */
	public void reset() {
		stopWorking();
		clearCache();
		clearQueue();
		clearCacheQueue();
		
		synchronized(scales) { 
			scales.clear(); 
		}
	}
	
	public boolean isCached(URI uri) {
		return cache.containsKey(uri);
	}
	
	/**
	 * Возвращает текущий размер кэша в байтах. 
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
	 * Возвращает максимальный размер кэша в байтах.
	 * @see #DEFAULT_MAX_CACHE_SIZE 
	 */
	public long getMaxCacheSize() {
		return maxCacheSize;
	}

	/**
	 * Устанавливает максимальный размер кэша в байтах. 
	 * @see #DEFAULT_MAX_CACHE_SIZE 
	 */
	public void setMaxCacheSize(long maxCacheSize) {
		this.maxCacheSize = maxCacheSize;
	}

	/**
	 * Проверяет, есть ли в очереди загрузки из кэша элементы.
	 */
	public boolean isCacheQueueEmpty() {
		return getCacheQueue().isEmpty();
	}
	
	/**
	 * Очищает очередь загрузки изображений из кэша.
	 */
	public void clearCacheQueue() {
		getCacheQueue().clear();
	}
	
	public Request getCurrentCacheQueueElement() {
		return getCacheQueue().current();
	}
	
	/**
	 * Фабричный метод для подстановки очереди загрузки закэшированных изображений. 
	 * По умолчанию {@link ImageLoader} создает {@link DefaultQueue} в качестве очереди.
	 * Если нужна другая очередь - в этом методе можно переписать поведение 
	 * (вернуть другую очередь).
	 * 
	 * @return очередь загрузки закэшированных изображений
	 */
	protected Queue getCacheQueue() {
		if (cacheQueue == null) cacheQueue = new DefaultQueue();
		return cacheQueue;
	}
	
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
	
	/**
	 * Максимальный размер изображения, который может прожевать Android - 
	 * 2048х2048, иначе при создании {@link android.graphics.Bitmap} получится {@link OutOfMemoryError}
	 * <br><br>
	 * Метод выясняет, во сколько раз нужно скукожить изображение (представленное в виде 
	 * массива байт), чтобы оно влезло в 2048х2048. Если размер и так нормальный - 
	 * вернет 1. 
	 */
	private int getScaleCritical(byte[] rawImageData) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeByteArray(rawImageData, 0, rawImageData.length, options);
	    
	    final int vScale = Math.round((options.outHeight)/2048.0f + 0.5f);
	    final int hScale = Math.round((options.outWidth) / 2048.0f + 0.5f);

	    return Math.max(1, Math.max(vScale, hScale));
	 	}

	/**
	 * Метод выясняет, во сколько раз нужно скукожить изображение (представленное в виде 
	 * массива байт), чтобы его максимальное измерение стало больше либо равно 
	 * минимальному измерению указанного {@link android.graphics.Rect}. То есть, максимально приблизить
	 * его размер к требуемому, но чтобы оно все равно осталось больше. Если размер и 
	 * так нормальный - вернет 1. 
	 */
	private int getScale(byte[] rawImageData, Request request) {
		Rect scale;
		synchronized(scales) { scale = scales.get(request.getURI()); }
		if (scale == null) return 1;

		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeByteArray(rawImageData, 0, rawImageData.length, options);
	    
	    final int vScale = Math.round((options.outHeight)/scale.height() - 0.5f);
	    final int hScale = Math.round((options.outWidth)/scale.width() - 0.5f);
	    
	    return Math.max(1, Math.min(vScale, hScale));
	}

	private byte[] bmpToByte(Bitmap bmp) throws IOException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, stream);
		stream.flush();
		byte[] data = stream.toByteArray();
		stream.close();
		
		return data;
	}

	private Rect getAccurateScale(Request request, Bitmap src) {
		Rect scale;
		synchronized(scales) { scale = scales.get(request.getURI()); }
		if (scale == null) return null;
		
		double factor = Math.min(((float) src.getWidth()) / ((float) scale.width()), 
				((float) src.getHeight()) / ((float) scale.height()));
		if (factor < 0) return null;
		
		return new Rect(0, 0, (int) (src.getWidth()/factor), (int) (src.getHeight()/factor));
	}
	
	private byte[] scaleBitmapData(Request request, byte[] raw) throws DataFormatException, IOException {
		int scale = Math.max(getScaleCritical(raw), getScale(raw, request));
		if (scale <= 1) return raw;
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = scale;
	
		final Bitmap bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length, options);
		if (bmp == null) throw new DataFormatException("That was not an image Oo");
		
		byte[] data;
		final Rect accurateScale = getAccurateScale(request, bmp);
		if (accurateScale == null) data = bmpToByte(bmp);
		else {
			final Bitmap scaled = Bitmap.createScaledBitmap(bmp, 
					accurateScale.width(), accurateScale.height(), true);
			
			data = bmpToByte(scaled);
			scaled.recycle();
		}
		
		bmp.recycle();
		return data;
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
	
	private Bitmap processBitmap(Request request, byte[] rawImageData) throws DataFormatException, IOException {
		addToCache(request, rawImageData);
		
		final Bitmap bmp = BitmapFactory.decodeByteArray(rawImageData, 0, rawImageData.length);
		if (bmp == null) throw new DataFormatException("That was not an image Oo");

		logDebug("processBitmapData: " + request.getURI());
		return bmp;
	}
	
	private boolean loadFromCache(Request request) {
		synchronized(cache) { 
			if (cache.containsKey(request.getURI())) {
				LogUtil.i(ImageLoader.class, "loading from cache: "+request.getURI());
				
				final byte[] raw = cache.get(request.getURI());
				postMainLoadingFinished(request, raw, BitmapFactory.decodeByteArray(raw, 0, raw.length));
				return true;
			}
		}		
		
		return false;
	}
	
	private void loadFromWWW(Request request) throws DataFormatException, IOException, URISyntaxException {
		LogUtil.i(ImageLoader.class, "loading from uri: "+request.getURI());

		final byte[] rawImageData = scaleBitmapData(request, load(request));
		postMainLoadingFinished(request, rawImageData, processBitmap(request, rawImageData));
	}
	
	@Override
	protected void loadInBackground(Request request) throws Exception {
		if (!loadFromCache(request)) loadFromWWW(request);
	}
	
	private Runnable cacheQueueRunnable = new Runnable() {
		@Override
		public void run()  {
			while (!isCacheQueueEmpty() && isWorking()) {
				final Request request = getCacheQueue().next();
				
				try {
					postMainLoadingStarted(request);
					loadInBackground(request);
				} catch (Exception e) { postMainLoadingFailed(request, e); }

				LogUtil.logMemoryUsage();
			}
		}
	};
	
}
