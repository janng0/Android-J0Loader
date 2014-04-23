package ru.jango.j0loader.test;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import ru.jango.j0loader.Request;
import ru.jango.j0loader.image.ImageLoader;
import ru.jango.j0util.LogUtil;

public class ImageLoaderTest extends AndroidTestCase {

    public void testSimpleDownload() throws Exception {
        final ImageLoaderWrapper loader = new ImageLoaderWrapper(new LoadingAdapter2<Bitmap>() {
            @Override
            public void processFinished(Request request, byte[] rawData, Bitmap data) {
                super.processFinished(request, rawData, data);
                LogUtil.d(ImageLoaderTest.class, "bitmap loaded " + data.getWidth() + "x" + data.getHeight());

                Const.Img img = null;
                for (Const.Img tImg : Const.Img.values())
                    if (tImg.getURI().equals(request.getURI())) {
                        img = tImg;
                        break;
                    }

                assertNotNull(img);
                switch (img) {
                    case SMALL:
                        assertEquals(425, data.getWidth());
                        assertEquals(554, data.getHeight());
                        assertEquals(80492, rawData.length);
                        break;

                    case NORMAL:
                        assertEquals(1600, data.getWidth());
                        assertEquals(1000, data.getHeight());
                        assertEquals(483849, rawData.length);
                        break;

                    case LARGE:
                        assertEquals(1999, data.getWidth());
                        assertEquals(1095, data.getHeight());
                        assertEquals(1978649, rawData.length);
                        break;

                    case HUGE:
                        assertEquals(2000, data.getWidth());
                        assertEquals(812, data.getHeight());
                        assertEquals(1688679, rawData.length);
                        break;

                    default:
                        throw new IllegalStateException("Should not be possible to load " + img.getURI());
                }
            }

            @Override
            public void processFailed(Request request, Exception e) {
                super.processFailed(request, e);
                assertTrue(request.getURI().equals(Const.IMG_FAKE));
            }
        });

        loader.setDebug(true);
        loader.setFullAsyncMode(true);

        for (Const.Img img : Const.Img.values())
            loader.addToQueue(new Request(img.getURI()));
        loader.start();

        while(loader.getLoaderThread2().isAlive()) {}
    }

    private class ImageLoaderWrapper extends ImageLoader {
        public ImageLoaderWrapper(LoadingListener<Bitmap> listener) { super(listener); }
        public Thread getLoaderThread2() { return getLoaderThread(); }
    }
}
