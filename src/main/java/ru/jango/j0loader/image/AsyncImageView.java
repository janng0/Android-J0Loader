package ru.jango.j0loader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.net.URI;

import ru.jango.j0loader.DataLoader;
import ru.jango.j0loader.Request;
import ru.jango.j0util.PathUtil;

/**
 * Special extension of {@link android.widget.ImageView} that could be used with
 * {@link ru.jango.j0loader.image.ImageLoader}. Also it automatically draws a special drawing indicator while listening to
 * {@link ru.jango.j0loader.DataLoader.LoadingListener#downloadingUpdateProgress(ru.jango.j0loader.Request, long, long)}.
 * <p>
 * <b>ATTENTION</b>: Automatically removes itself from {@link ru.jango.j0loader.image.ImageLoader}'s listeners
 * when is removed from interface (in {@link #onDetachedFromWindow()} calls {@link #setImageLoader(ImageLoader)} with NULL).
 * So when using it in {@link android.widget.ListView} basically everything goes well, but just in case pay attention and
 * use {@link #setAutoDetachLoader(boolean)} or manually reset {@link ru.jango.j0loader.image.ImageLoader} via
 * {@link #setImageLoader(ImageLoader)} on each {@link android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)}
 * call.
 */
public class AsyncImageView extends ImageView {
	
	public static final float DEFAULT_LOADING_INDICATOR_BOLDNESS = 7;
	public static final int DEFAULT_LOADING_INDICATOR_BACKGROUND_COLOR = Color.DKGRAY;
	public static final int DEFAULT_LOADING_INDICATOR_COLOR = Color.LTGRAY;
    public static final int DEFAULT_FAIL_INDICATOR_COLOR = Color.rgb(255, 100, 100);
	public static final int DEFAULT_INDICATOR_MAX_SIZE = 120;

	private final RectF loadingIndicatorRect = new RectF();
	private final Paint loadingIndicatorBGPaint = new Paint();
	private final Paint loadingIndicatorPaint = new Paint();
    private final Paint failIndicatorPaint = new Paint();

	private Status status;
    private boolean autoDetachLoader;
    private boolean showIndicator;
	private float loadingIndBoldness;
	private int loadingIndBGColor;
	private int loadingIndColor;
    private int failIndColor;
	private int indMaxSize;

	private ImageLoader loader;
	private URI imageUri;
	private int progress;
	private boolean imageSet;

    public enum Status {
        UNKNOWN, LOADING, LOADED, FAILED
    }

    private final DataLoader.LoadingListener<Bitmap> loadingListener = new DataLoader.LoadingListener<Bitmap>() {
		
		@Override
		public void processStarted(Request request) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (!imageSet) return;
			
			setImageBitmap(null);
			progress = 0;
			imageSet = false;

            status = Status.LOADING;
			invalidate();
		}

		@Override
		public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;
			
			progress =  (int) ((loadedBytes * 100) / totalBytes);
            status = Status.LOADING;
			invalidate();
		}

		@Override
		public void processFinished(Request request, byte[] rawData, Bitmap data) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;
			
			progress = 100;
			imageSet = true;

            status = Status.LOADED;
			setImageBitmap(data);
		}

		@Override
		public void processFailed(Request request, Exception e) {
            if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
            if (imageSet) return;

            progress = 0;
            imageSet = false;

            status = Status.FAILED;
            invalidate();
        }

		@Override
		public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {}
	};
	
	public AsyncImageView(Context context) { super(context); init(); }
	public AsyncImageView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle);	init(); }

	private void init() {
		loadingIndBoldness = DEFAULT_LOADING_INDICATOR_BOLDNESS;
		loadingIndBGColor = DEFAULT_LOADING_INDICATOR_BACKGROUND_COLOR;
		loadingIndColor = DEFAULT_LOADING_INDICATOR_COLOR;
        failIndColor = DEFAULT_FAIL_INDICATOR_COLOR;
		indMaxSize = DEFAULT_INDICATOR_MAX_SIZE;

        status = Status.UNKNOWN;
        autoDetachLoader = true;
        showIndicator = true;
		imageSet = false;
		initPaints();
	}

	private void initPaints() {
		fillLoadingIndicatorBGPaint(loadingIndicatorBGPaint);
		fillLoadingIndicatorPaint(loadingIndicatorPaint);
        fillFailIndicatorPaint(failIndicatorPaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!showIndicator || imageSet)
			return;

        switch (status) {
            case LOADING:
                canvas.drawArc(loadingIndicatorRect, 0, 360, false, loadingIndicatorBGPaint);
                if (progress != 0)
                    canvas.drawArc(loadingIndicatorRect, 0, progress * 360 / 100, false, loadingIndicatorPaint);
                break;

            case FAILED:
                canvas.drawArc(loadingIndicatorRect, 0, 360, false, failIndicatorPaint);
                break;
        }
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		super.onSizeChanged(w, h, oldW, oldH);
		initPaints();

		getLoadingIndicatorRect(loadingIndicatorRect);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (autoDetachLoader)
            setImageLoader(null);
	}
	
	////////////////////////////////////////////////////////////////////////
	//
	// Drawing methods
	//
	////////////////////////////////////////////////////////////////////////

	private int getIndicatorSize() {
		return Math.min(Math.min(getWidth(), getHeight()), indMaxSize);
	}

    /**
     * Just centers rect inside view.
     */
	private RectF getIndicatorRect(final RectF ret) {
		ret.set((getMeasuredWidth() - getIndicatorSize()) / 2,
                (getMeasuredHeight() - getIndicatorSize()) / 2,
                (getMeasuredWidth() + getIndicatorSize()) / 2,
                (getMeasuredHeight() + getIndicatorSize()) / 2);

		return ret;
	}

	private RectF getLoadingIndicatorRect(final RectF ret) {
		getIndicatorRect(ret);
		ret.inset(loadingIndBoldness / 2, loadingIndBoldness / 2);

		return ret;
	}

	private void fillStandardPaint(float strokeWidth, int color, final Paint paint) {
		paint.setStrokeWidth(strokeWidth);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setDither(true);
		paint.setAntiAlias(true);
	}

	private Paint fillLoadingIndicatorBGPaint(final Paint paint) {
		fillStandardPaint(loadingIndBoldness, loadingIndBGColor, paint);
		return paint;
	}

	private Paint fillLoadingIndicatorPaint(final Paint paint) {
		fillStandardPaint(loadingIndBoldness, loadingIndColor, paint);
		return paint;
	}

    private Paint fillFailIndicatorPaint(final Paint paint) {
        fillStandardPaint(loadingIndBoldness, failIndColor, paint);
        return paint;
    }

	////////////////////////////////////////////////////////////////////////
	//
	// Setters and getters
	//
	////////////////////////////////////////////////////////////////////////

    public Status getStatus() {
        return status;
    }

    /**
     * Switches view on new {@link ru.jango.j0loader.image.ImageLoader}. Automatically removes itself
     * from old loader's listeners collection and adds itself to new loader's listeners collection.
     *
     * @param loader new {@link ru.jango.j0loader.image.ImageLoader}, may be NULL
     */
    public void setImageLoader(ImageLoader loader) {
		if (this.loader != null)
			this.loader.removeLoadingListener(loadingListener);
		
		if (loader != null)
			loader.addLoadingListener(loadingListener);
		
		this.loader = loader;
	}

    /**
     * @see #setImageLoader(ImageLoader)
     */
	public ImageLoader getImageLoader() {
		return loader;
	}

    /**
     * Just saves {@link URI} inside itself - doesn't start loading itself.
     *
     * @see ru.jango.j0loader.image.ImageLoader#addToQueue(ru.jango.j0loader.Request)
     * @see ImageLoader#start()
     */
	public void setImageURI(URI uri) {
		if (PathUtil.uriEquals(uri, this.imageUri))
			return;

		imageUri = uri;
		progress = 0;

		if (imageSet) {
			setImageBitmap(null);
			imageSet = false;
		}
		
		invalidate();
	}

    /**
     * @see #setImageURI(java.net.URI)
     */
	public URI getImageURI() {
		return imageUri;
	}

    /**
     * Sets boldness of the line of the indicator torus.
     */
	public void setLoadingIndicatorBoldness(float boldness) {
		loadingIndBoldness = boldness;
		getLoadingIndicatorRect(loadingIndicatorRect);
		invalidate();
	}

    /**
     * Returns boldness of the line of the indicator torus.
     */
	public float getLoadingIndicatorBoldness() {
		return loadingIndBoldness;
	}

    /**
     * Sets color of the background torus.
     */
	public void setLoadingIndicatorBackgroundColor(int color) {
		loadingIndBGColor = color;
		fillLoadingIndicatorBGPaint(loadingIndicatorBGPaint);
		invalidate();
	}

    /**
     * Returns color of the background torus.
     */
	public int getLoadingIndicatorBackgroundColor() {
		return loadingIndBGColor;
	}

    /**
     * Sets color of the foreground torus.
     */
	public void setLoadingIndicatorColor(int color) {
		loadingIndColor = color;
		fillLoadingIndicatorPaint(loadingIndicatorPaint);
		invalidate();
	}

    /**
     * Returns color of the foreground torus.
     */
	public int getLoadingIndicatorColor() {
		return loadingIndColor;
	}

    /**
     * Sets color of the fail indicator torus.
     */
    public int getFailIndicatorColor() {
        return failIndColor;
    }

    /**
     * Returns color of the fail indicator torus.
     */
    public void setFailIndicatorColor(int failIndColor) {
        this.failIndColor = failIndColor;
    }

    /**
     * Indicator size is calculated based on current view size, but it could be
     * limited by this method.
     */
    public void setMaxIndicatorSize(int sizePx) {
		indMaxSize = sizePx;
		getLoadingIndicatorRect(loadingIndicatorRect);
		invalidate();
	}

    /**
     * Indicator size is calculated based on current view size, but it could be
     * limited by this value.
     */
	public int getMaxIndicatorSize() {
		return indMaxSize;
	}

    /**
     * Just hides indicator at all.
     */
    public boolean shouldShowIndicator() {
        return showIndicator;
    }

    /**
     * @see #shouldShowIndicator()
     */
    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
    }

    /**
     * AsyncImageView automatically removes itself from {@link ru.jango.j0loader.image.ImageLoader}'s listeners when
     * is removed from interface (in {@link #onDetachedFromWindow()} calls {@link #setImageLoader(ImageLoader)}
     * with NULL). To avoid this - use {@link #setAutoDetachLoader(boolean)} with FALSE param.
     */
    public boolean shouldAutoDetachLoader() {
        return autoDetachLoader;
    }

    /**
     * AsyncImageView automatically removes itself from {@link ru.jango.j0loader.image.ImageLoader}'s listeners when
     * is removed from interface (in {@link #onDetachedFromWindow()} calls {@link #setImageLoader(ImageLoader)}
     * with NULL). To avoid this - use this method with FALSE param.
     */
    public void setAutoDetachLoader(boolean autoDetachLoader) {
        this.autoDetachLoader = autoDetachLoader;
    }

}
