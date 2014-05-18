package ru.jango.j0loader.test.image;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import java.net.URI;

import ru.jango.j0loader.Request;
import ru.jango.j0loader.test.Settings;
import ru.jango.j0loader.image.AsyncImageView;
import ru.jango.j0loader.image.ImageLoader;
import ru.jango.j0loader.image.cache.NullCache;

public class AsyncImageViewTestActivity extends Activity {

    private final String BASE = "http://192.168.1.4/j0Loader/";

    private final URI IMG_SMALL = URI.create(BASE + "small.jpg");
    private final URI IMG_NORMAL = URI.create(BASE + "normal.jpg");
    private final URI IMG_LARGE = URI.create(BASE + "large.jpg");
    private final URI IMG_HUGE = URI.create(BASE + "huge.jpg");
    private final URI IMG_FAKE = URI.create(BASE + "fake.jpg");

    private AsyncImageView img;
    private ImageLoader loader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loader = new ImageLoader();
        loader.setCache(new NullCache());

        img = new AsyncImageView(this);
        img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        img.setImageLoader(loader);

        testImageViewSettings();
        testImageURI();
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.addToQueue(new SlowRequest(img.getImageURI()));
                loader.start();
            }
        });

        setContentView(img);
    }

    private void testImageURI() {
        img.setImageURI(IMG_SMALL);
//        img.setImageURI(IMG_NORMAL);
//        img.setImageURI(IMG_LARGE);
//        img.setImageURI(IMG_HUGE);
//        img.setImageURI(IMG_FAKE);
    }

    private void testImageViewSettings() {
//        img.setLoadingIndicatorBackgroundColor(Color.RED);
//        img.setLoadingIndicatorColor(Color.RED);
//        img.setFailIndicatorColor(Color.GREEN);
//        img.setLoadingIndicatorBoldness(20);
//        img.setMaxIndicatorSize(300);
//        img.setShowIndicator(false);
    }

    private class SlowRequest extends Request {
        public SlowRequest(URI uri) { super(uri); }
        public long getResponseContentLength() {
            synchronized (this) { try { wait(1); } catch (Exception ignored) {} }
            return super.getResponseContentLength();
        }
    }

}
