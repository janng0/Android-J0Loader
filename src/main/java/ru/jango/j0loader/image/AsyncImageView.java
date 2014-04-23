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

public class AsyncImageView extends ImageView {
	
	public static final boolean DEFAULT_DRAW_STATUS = false;
	public static final float DEFAULT_LOADING_INDICATOR_BOLDNESS = 20;
	public static final int DEFAULT_LOADING_INDICATOR_BACKGROUND_COLOR = Color.DKGRAY;
	public static final int DEFAULT_LOADING_INDICATOR_COLOR = Color.LTGRAY;
	public static final int DEFAULT_INDICAOTOR_MAX_SIZE = 120;

	private final RectF loadingIndicatorRect = new RectF();
	private final Paint loadingIndicatorBGPaint = new Paint();
	private final Paint loadingIndicatorPaint = new Paint();

	private boolean drawStatus;
	private float loadingIndBoldness;
	private int loadingIndBGColor;
	private int loadingIndColor;
	private int indMaxSize;

	private ImageLoader loader;
	private URI imageUri;
	private int progress;
	private boolean imageSet;

	private final DataLoader.LoadingListener<Bitmap> loadingListener = new DataLoader.LoadingListener<Bitmap>() {
		
		@Override
		public void processStarted(Request request) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (!imageSet) return;
			
			setImageBitmap(null);
			progress = 0;
			imageSet = false;
				
			invalidate();
		}

		@Override
		public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;
			
			progress =  (int) ((loadedBytes * 100) / totalBytes);
			invalidate();
		}

		@Override
		public void processFinished(Request request, byte[] rawData, Bitmap data) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;
			
			progress = 100;
			imageSet = true;
			
			setImageBitmap(data);
		}

		@Override
		public void processFailed(Request request, Exception e) {;}
		@Override
		public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {;}
	};
	
	public AsyncImageView(Context context) { super(context); init(context); }
	public AsyncImageView(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle);	init(context); }

	private void init(Context ctx) {
		drawStatus = DEFAULT_DRAW_STATUS;
		loadingIndBoldness = DEFAULT_LOADING_INDICATOR_BOLDNESS;
		loadingIndBGColor = DEFAULT_LOADING_INDICATOR_BACKGROUND_COLOR;
		loadingIndColor = DEFAULT_LOADING_INDICATOR_COLOR;
		indMaxSize = DEFAULT_INDICAOTOR_MAX_SIZE;

		imageSet = false;
		initPaints();
	}

	private void initPaints() {
		fillLoadingIndicatorBGPaint(loadingIndicatorBGPaint);
		fillLoadingIndicatorPaint(loadingIndicatorPaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!drawStatus || imageSet)
			return;

		drawArc(canvas, loadingIndicatorRect, 360, loadingIndicatorBGPaint);
		if (progress != 0) drawArc(canvas, loadingIndicatorRect, progress * 360 / 100, loadingIndicatorPaint);
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

	private void fillStandartPaint(float strokeWidth, int color, final Paint paint) {
		paint.setStrokeWidth(strokeWidth);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setDither(true);
		paint.setAntiAlias(true);
	}

	private Paint fillLoadingIndicatorBGPaint(final Paint paint) {
		fillStandartPaint(loadingIndBoldness, loadingIndBGColor, paint);
		return paint;
	}

	private Paint fillLoadingIndicatorPaint(final Paint paint) {
		fillStandartPaint(loadingIndBoldness, loadingIndColor, paint);
		return paint;
	}

	private void drawArc(Canvas canvas, RectF rect, float endAngle, Paint paint) {
		canvas.drawArc(rect, 0, endAngle, false, paint);
	}

	////////////////////////////////////////////////////////////////////////
	//
	// Setters and getters
	//
	////////////////////////////////////////////////////////////////////////

	public void setImageLoader(ImageLoader loader) {
		if (this.loader != null)
			this.loader.removeLoadingListener(loadingListener);
		
		if (loader != null)
			loader.addLoadingListener(loadingListener);
		
		this.loader = loader;
	}
	
	public ImageLoader getImageLoader() {
		return loader;
	}
	
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

	public URI getImageURI() {
		return imageUri;
	}

	public void setShouldDrawStatus(boolean drawStatus) {
		this.drawStatus = drawStatus;
	}

	public boolean shouldDrawStatus() {
		return drawStatus;
	}

	public void setLoadingIndicatorBoldness(float boldness) {
		loadingIndBoldness = boldness;
		getLoadingIndicatorRect(loadingIndicatorRect);
		invalidate();
	}

	public float getLoadingIndicatorBoldness() {
		return loadingIndBoldness;
	}

	public void setLoadingIndicatorBackgroundColor(int color) {
		loadingIndBGColor = color;
		fillLoadingIndicatorBGPaint(loadingIndicatorBGPaint);
		invalidate();
	}

	public int getLoadingIndicatorBackgroundColor() {
		return loadingIndBGColor;
	}

	public void setLoadingIndicatorColor(int color) {
		loadingIndColor = color;
		fillLoadingIndicatorPaint(loadingIndicatorPaint);
		invalidate();
	}

	public int getLoadingIndicatorColor() {
		return loadingIndColor;
	}

	public void setMaxIndicatorSize(int sizePx) {
		indMaxSize = sizePx;
		getLoadingIndicatorRect(loadingIndicatorRect);
		invalidate();
	}

	public int getMaxIndicatorSize() {
		return indMaxSize;
	}

}
