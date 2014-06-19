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

package ru.jango.j0loader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.net.URI;

import ru.jango.j0loader.DataLoader;
import ru.jango.j0loader.Request;
import ru.jango.j0util.PathUtil;
import ru.jango.j0widget.TorusIndicator;

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
public class AsyncImageView extends RelativeLayout {

    private ImageView imageView;
    private TorusIndicator indicator;

	private Status status;
    private boolean autoDetachLoader;
    private boolean showIndicator;

	private ImageLoader loader;
	private URI imageUri;
	private boolean imageSet;

    public enum Status {
        UNKNOWN, LOADING, LOADED, FAILED
    }

    private final DataLoader.LoadingListener<Bitmap> loadingListener = new DataLoader.LoadingListener<Bitmap>() {
		
		@Override
		public void processStarted(Request request) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (!imageSet) return;
			
			imageView.setImageBitmap(null);
            indicator.setProgress(0);
			imageSet = false;

            status = Status.LOADING;
            if (showIndicator) indicator.setVisibility(View.VISIBLE);
		}

		@Override
		public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;

            indicator.setProgress((int) ((loadedBytes * 100) / totalBytes));
            status = Status.LOADING;
            if (showIndicator) indicator.setVisibility(View.VISIBLE);
		}

		@Override
		public void processFinished(Request request, byte[] rawData, Bitmap data) {
			if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
			if (imageSet) return;

            indicator.setProgress(100);
			imageSet = true;

            status = Status.LOADED;
			imageView.setImageBitmap(data);
            indicator.setVisibility(View.GONE);
        }

		@Override
		public void processFailed(Request request, Exception e) {
            if (!PathUtil.uriEquals(imageUri, request.getURI())) return;
            if (imageSet) return;

            indicator.setProgress(-1);
            imageSet = false;

            status = Status.FAILED;
            if (showIndicator) indicator.setVisibility(View.VISIBLE);
        }

		@Override
		public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {}
	};
	
	public AsyncImageView(Context context) { super(context); init(context); }
	public AsyncImageView(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle);	init(context); }

	private void init(Context ctx) {
        status = Status.UNKNOWN;
        autoDetachLoader = true;
		imageSet = false;
        showIndicator = true;

        initLayout(ctx);
	}

    private void initLayout(Context ctx) {
        imageView = new ImageView(ctx);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        indicator = new TorusIndicator(ctx);
        indicator.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        addView(imageView);
        addView(indicator);
    }

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (autoDetachLoader)
            setImageLoader(null);
	}
	
	////////////////////////////////////////////////////////////////////////
	//
	// Setters and getters
	//
	////////////////////////////////////////////////////////////////////////

    public Status getStatus() {
        return status;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TorusIndicator getLoadingIndicator() {
        return indicator;
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
		indicator.setProgress(0);

		if (imageSet) {
			imageView.setImageBitmap(null);
			imageSet = false;
		}
	}

    /**
     * @see #setImageURI(java.net.URI)
     */
	public URI getImageURI() {
		return imageUri;
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
        indicator.setVisibility(status != Status.LOADED && showIndicator ? View.VISIBLE : View.GONE);
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
