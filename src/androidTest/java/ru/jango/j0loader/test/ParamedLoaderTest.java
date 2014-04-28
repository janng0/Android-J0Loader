package ru.jango.j0loader.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

import ru.jango.j0loader.ParamedLoader;
import ru.jango.j0loader.Request;
import ru.jango.j0loader.param.BitmapParam;
import ru.jango.j0loader.param.Param;
import ru.jango.j0util.LogUtil;

public class ParamedLoaderTest extends AndroidTestCase {

    public void testSimpleRequest() throws Exception {
        final ParamedLoaderWrapper loader = new ParamedLoaderWrapper();
        loader.addLoadingListener(new LoadingAdapter2<String>() {
            @Override
            public void processFinished(Request request, byte[] rawData, String data) {
                super.processFinished(request, rawData, data);

                assertTrue(request.getResponseContentLength() != -1);
                assertNotNull(rawData);
                assertEquals(Const.PART1.getData() + " -- " + Const.PART2.getData() +
                        " -- " + Const.PART3.getData(), data);

                LogUtil.d(ParamedLoaderTest.class, "content length = " + request.getResponseContentLength());
                LogUtil.d(ParamedLoaderTest.class, "content = " + data);
            }
        });

        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);
        request.setRequestParams(getParts(false));

        loader.addToQueue(request);
        loader.start();

        waitLoadingThreads(loader);
    }

    private List<Param> getParts(boolean includeBmp) {
        final List<Param> params = new ArrayList<Param>();

        params.add(Const.PART_TEST_SIMPLE_REQUEST);
        params.add(Const.PART1);
        params.add(Const.PART2);
        params.add(Const.PART3);
        if (includeBmp)
            params.add(new BitmapParam(Const.PARAM_BMP,
                    Bitmap.CompressFormat.JPEG,
                    getBitmap()));

        return params;
    }

    private Bitmap getBitmap() {
        //noinspection ConstantConditions
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.small);
    }

    private void waitLoadingThreads(ParamedLoaderWrapper loader) {
        //noinspection StatementWithEmptyBody
        while(loader.getLoaderThread2().isAlive()) {}
    }

    private class ParamedLoaderWrapper extends ParamedLoader<String> {

        public ParamedLoaderWrapper() {
            super();
            setDebug(true);
            setFullAsyncMode(true);
        }

        public Thread getLoaderThread2() { return getLoaderThread(); }

        @Override
        protected void loadInBackground(Request request) throws Exception {
            final byte[] response = load(request);
            postProcessFinished(request, response, new String(response, "UTF-8"));
        }
    }

}
