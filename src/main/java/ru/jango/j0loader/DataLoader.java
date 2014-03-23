package ru.jango.j0loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import ru.jango.j0loader.queue.DefaultQueue;
import ru.jango.j0loader.queue.Queue;
import ru.jango.j0util.LogUtil;

/**
 * Базовый класс-загрузчик. 
 * <br><br>
 * - умеет собственно загружать данные (HTTP GET без параметров) <br>
 * - умеет рапортовать в главный поток через {@link ru.jango.j0loader.DataLoader.LoadingListener} <br>
 * - хранит состояние процесса {@link #isWorking()} <br>
 * - управляет очередью загрузки (см. {@link #getQueue()}) <br>
 * - обеспечивает асинхронность в {@link #loadInBackground(Request)}
 */
public abstract class DataLoader<T> {
	protected final int PROGRESS_UPDATE_INTERVAL_MS = 200;
	protected final int BUFFER_SIZE_BYTES = 50;

	private Handler mainThreadHandler;
	private Set<LoadingListener<T>> listeners;

	private Thread loaderThread;
	private Queue queue;
	private boolean working;
	private boolean cancelled;
	private boolean loading;
	private boolean debug;
	
	public DataLoader() {
		mainThreadHandler = new Handler();
		listeners = new HashSet<LoadingListener<T>>();
	}
	
	protected void logDebug(String message) {
		if (isDebug()) 
			LogUtil.d(getClass(), message);
	}
	
	////////////////////////////////////////////////////////////////////////
    //
    //		Setters and getters
    //
    ////////////////////////////////////////////////////////////////////////
	
	/**
	 * Добавить слушателя загрузки
	 * 
	 * @param listener новый слушатель загрузки
	 */
	public void addLoadingListener(LoadingListener<T> listener) {
		listeners.add(listener);
	}
	
	/**
	 * Удалить слушателя загрузки
	 * 
	 * @param listener слушатель загрузки
	 */
	public void removeLoadingListener(LoadingListener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * Добавляет {@link Request} в конец очереди загрузки.
	 * 
	 * @param request	{@link Request} для добавления
	 */
	public void addToQueue(Request request) {
		getQueue().add(request);
		executeQueue();
	}
	
	/**
	 * Добавляет все {@link Request} в конец очереди загрузки.
	 * 
	 * @param requests	пачка {@link Request} для добавления
	 */
	public void addToQueue(Collection<Request> requests) {
		getQueue().addAll(requests);
		executeQueue();
	}
	
	/**
	 * Удаляет {@link Request} из очереди загрузки.
	 * 
	 * @param request {@link Request} для удаления
	 */
	public void removeFromQueue(Request request) {
		getQueue().remove(request);
	}

	public Request getCurrentQueueElement() {
		return getQueue().current();
	}
	
	/**
	 * Очищает очередь загрузки.
	 */
	public void clearQueue() {
		getQueue().clear();
	}
	
	/**
	 * Проверяет, есть ли в очереди элементы.
	 */
	public boolean isQueueEmpty() {
		return getQueue().isEmpty();
	}

	/**
	 * Устанавливает внутреннюю переменную в false, а разные 
	 * методы периодически опрашивают эту переменную в процессе работы.
	 * То бишь, после вызова метода работа останавливается почти сразу,
	 * но скорее всего не совсем сразу. 
	 */
	public void stopWorking() {
		working = false;
	}
	
	/**
	 * Устанавливает внутреннюю переменную в true. Метод не запускает процесс
	 * загрузки, а только разрешает его.
	 */
	public void doWork() {
		working = true;
	}
	
	/**
	 * Устанавливает внутреннюю переменную в true. Метод не запускает процесс
	 * загрузки, а только разрешает его.
	 */
	public boolean isWorking() {
		return working;
	}

	/**
	 * Останавливает процесс загрузки текущего элемента очереди.
	 */
	public void cancelCurrent() {
		if (!loading) return;
		cancelled = true;
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

    ////////////////////////////////////////////////////////////////////////
    //
    //		Loading methods
    //
    ////////////////////////////////////////////////////////////////////////
	
	/**
	 * Запускает очередь загрузки в отдельном потоке.
	 */
	public void executeQueue()  {
		doWork();
		
		final Thread thread = getLoaderThread();
		if (!thread.isAlive()) thread.start();
	}

	/**
	 * Вызывается из отдельного потока для загрузки выполнения конкретного
	 * {@link Request} из очереди.
	 * 
	 * @param request	{@link Request} из очереди
	 */
	protected abstract void loadInBackground(Request request) throws Exception;
	
	/**
	 * Возвращает поток {@link Thread}, в котором исполняется очередь загрузки. В подклассах
	 * можно переписать метод, подставив другой поток.
	 * 
	 * @return	поток {@link Thread}, в котором исполняется очередь загрузки
	 */
	protected Thread getLoaderThread() {
		if (!(loaderThread!=null && loaderThread.isAlive())) 
			return loaderThread = new Thread(queueRunnable);
		
		return loaderThread;
	}
	
	/**
	 * Фабричный метод для подстановки очереди. По умолчанию {@link ru.jango.j0loader.DataLoader}
	 * создает {@link DefaultQueue} в качестве очереди. Если нужна другая очередь - 
	 * в этом методе можно переписать поведение (вернуть другую очередь).
	 * 
	 * @return очередь загрузки
	 */
	protected Queue getQueue() {
		if (queue == null) queue = new DefaultQueue();
		return queue;
	}
	
	/**
	 * Проверяет подключение к интернетам 
	 */
	protected boolean isOnline(Context context)  {
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo nInfo = cm.getActiveNetworkInfo();
		 
		return (nInfo != null && nInfo.isConnected());
	}
	
	/**
	 * Открывает {@link java.io.InputStream} и запускает собственно загрузку.
	 * 
	 * @return "сырые" загруженные данные
	 */
	protected byte[] load(Request request) throws IOException, URISyntaxException {
		InputStream in = null;
		try {
			in = openInputStream(request);
			return doLoad(request,in);
		} finally { try { in.close(); } catch(Exception ignored) {} }
	}
	
	/**
	 * Собственно загрузка. Рапортует о прогрессе и завершении. 
	 */
	protected byte[] doLoad(Request request, InputStream in) throws IOException {
		loading = true;
		
		long progressLastUpdated = System.currentTimeMillis();
		int nRead = 0;
		int totalRead = 0;
		byte[] data = new byte[BUFFER_SIZE_BYTES];

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while ((nRead = in.read(data, 0, data.length))!=-1 && isWorking() && !cancelled)  {
			buffer.write(data, 0, nRead);
			totalRead += nRead;
			
			boolean updateProgress = System.currentTimeMillis() > progressLastUpdated + PROGRESS_UPDATE_INTERVAL_MS;
			if (request.getContentLength()!=-1 && updateProgress) {
				progressLastUpdated = System.currentTimeMillis();
				postMainLoadingUpdateProgress(request,totalRead,request.getContentLength());
			}
		}
		buffer.flush();
		
		final byte[] ret = buffer.toByteArray();
		buffer.close();

		loading = false;
		cancelled = false;
		logDebug("doLoad: " + request.getURI() + " : " + (new String(ret,"UTF-8")));
		return ret;
	}

	/**
	 * Открывает {@link java.io.InputStream} и в {@link Request} прописывает объем данных, которые
	 * в этом {@link java.io.InputStream} лежат.
	 */
	protected InputStream openInputStream(Request request) throws IOException, URISyntaxException {
		final URLConnection urlConnection = request.getURL().openConnection();
		urlConnection.setUseCaches(false);
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(false);
		urlConnection.setConnectTimeout(15000);
		urlConnection.setReadTimeout(10000);
		
		request.setContentLength(urlConnection.getContentLength());
		return urlConnection.getInputStream();
	}
	
    ////////////////////////////////////////////////////////////////////////
    //
    //		Crossthread communication methods
    //
    ////////////////////////////////////////////////////////////////////////
	
	/**
	 * Рапортует о начале загрузки. 
	 */
	protected void postMainLoadingStarted(final Request request) {
		if (!isWorking()) return;
		logDebug("postMainLoadingStarted: " + request.getURI());
		
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() { 
				for (LoadingListener<T> listener : listeners)
					listener.processStarted(request); 
			}
		});
	}
		
	/**
	 * Рапортует о прогрессе загрузки. 
	 */
	protected void postMainLoadingUpdateProgress(final Request request, final long loadedBytes, final long totalBytes) {
		if (!isWorking()) return;
		logDebug("postMainLoadingUpdateProgress: " + request.getURI() + " : " 
					+ "downloaded " + loadedBytes + "bytes; "
					+ "total " + totalBytes + "bytes");
		
		mainThreadHandler.post(new Runnable()  {
			@Override
			public void run()  { 
				for (LoadingListener<T> listener : listeners)
					listener.loadingUpdateProgress(request, loadedBytes, totalBytes); 
			}
		});
	}
	
	/**
	 * Рапортует о прогрессе отправки. 
	 */
	protected void postMainUploadingUpdateProgress(final Request request, final long uploadedBytes, final long totalBytes) {
		if (!isWorking()) return;
		logDebug("postMainUploadingUpdateProgress: " + request.getURI() + " : " 
					+ "uploaded " + uploadedBytes + "bytes; "
					+ "total " + totalBytes + "bytes");
		
		mainThreadHandler.post(new Runnable()  {
			@Override
			public void run()  { 
				for (LoadingListener<T> listener : listeners)
					listener.uploadingUpdateProgress(request, uploadedBytes, totalBytes); 
			}
		});
	}
	
	/**
	 * Рапортует об успешном завершении загрузки. 
	 */
	protected void postMainLoadingFinished(final Request request, final byte[] rawData, final T data) {
		if (!isWorking()) return;
		logDebug("postMainLoadingFinished: " + request.getURI() + " : " 
					+ rawData.length + "bytes");
		
		mainThreadHandler.post(new Runnable()  {
			@Override
			public void run()  { 
				for (LoadingListener<T> listener : listeners)
					listener.loadingFinished(request,rawData,data); 
			}
		});
	}
	
	/**
	 * Рапортует о неудачном завершении загрузки. 
	 */
	protected void postMainLoadingFailed(final Request request, final Exception e) {
		if (!isWorking()) return;
		if (isDebug()) e.printStackTrace();
		logDebug("postMainLoadingFailed: " + request.getURI() + " : " + e);
		
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() { 
				for (LoadingListener<T> listener : listeners)
					listener.processFailed(request,e); 
			}
		});
	}
	
    ////////////////////////////////////////////////////////////////////////
    //
    //		Crossthread communication staff
    //
    ////////////////////////////////////////////////////////////////////////
	
	/**
	 * Слушатель загрузки. 
	 */
	public interface LoadingListener<T> {
		/**
		 * Вызывается перед началом процесса. Процесс может состоять из
		 * отправки и загрузки данных, либо только из загрузки.
		 * 
		 * @param request {@link Request}, который начал выполняться
		 */
		public void processStarted(Request request);

		/**
		 * Вызывается в процессе отправки данных.
		 * 
		 * @param request		{@link Request}, который выполняется
		 * @param uploadedBytes	объем отправленных данных в байтах
		 * @param totalBytes	общий объем загружаемых данных
		 */
		public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes);
		
		/**
		 * Вызывается в процессе загрузки.
		 * 
		 * @param request		{@link Request}, который выполняется
		 * @param loadedBytes	объем загруженных данных в байтах
		 * @param totalBytes	общий объем загружаемых данных
		 */
		public void loadingUpdateProgress(Request request, long loadedBytes, long totalBytes);

		/**
		 * Вызывается после успешной загрузки.
		 * 
		 * @param request	{@link Request}, который был выполнен
		 * @param rawData	"сырые" загруженные данные - необработанный массив байт 
		 * 					с загруженными данными
		 * @param data		обработанные загруженные данные
		 */
		public void loadingFinished(Request request, byte[] rawData, T data);
		
		/**
		 * Вызывается после неудачного завершения процесса (отправки или загрузки).
		 *  
		 * @param request	{@link Request}, который не удалось выполнить
		 * @param e			подкласс от {@link Exception} с ошибкой
		 */
		public void processFailed(Request request, Exception e);
	}
	
	private Runnable queueRunnable = new Runnable() {
		@Override
		public void run()  {
			while (!isQueueEmpty() && isWorking()) {
				final Request request = getQueue().next();

				try {
					postMainLoadingStarted(request);
					loadInBackground(request);
				} catch (Exception e) { postMainLoadingFailed(request, e); }

				LogUtil.logMemoryUsage();
			}
		}
	};
}
