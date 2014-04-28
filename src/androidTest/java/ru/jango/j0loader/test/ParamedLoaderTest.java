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
import ru.jango.j0loader.param.StringParam;
import ru.jango.j0util.LogUtil;

public class ParamedLoaderTest extends AndroidTestCase {

    /**
     * Test downloading without sending any params.
     *
     * 1) create simple request
     * 2) send it (should be automatically send via GET)
     * 3) check response
     * 4) 2 beer + shashlik
     */
    public void testSimpleRequest() throws Exception {
        final String check = "no парамз!";
        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);

        doTest(request, check);
    }

    /**
     * Test sending short text params.
     *
     * 1) generate simple short params
     * 2) send them (should be automatically send via GET)
     * 3) simple check response
     * 4) still in progress...
     */
    public void testShortTextRequest() throws Exception {
        final String check = Const.PART1.getData() + " -- " + Const.PART2.getData() +
                " -- " + Const.PART3.getData();
        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);
        request.setRequestParams(getParts(Const.PARAM_SHORT_TEXT_REQUEST, false, false));

        doTest(request, check);
    }

    /**
     * Test sending short and long text params.
     *
     * 1) generate params with short and long strings
     * 2) send them (should be automatically send via POST)
     * 3) simple check response
     * 4) yet close...
     */
    public void testLongTextRequest() throws Exception {
        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);
        request.setRequestParams(getParts(Const.PARAM_LONG_TEXT_REQUEST, false, true));
        final String check = Const.PART1.getData() + " -- " + Const.PART2.getData() +
                " -- " + Const.PART3.getData() + " -- " +
                request.getRequestParams().get(4).getRawData().length;

        doTest(request, check);
    }

    /**
     * Test sending Bitmap.
     *
     * sure enough we all know how that should work...
     * 4) one more?
     */
    public void testBitmapRequest() throws Exception {
        final List<Param> params = new ArrayList<Param>();
        params.add(Const.PARAM_BITMAP_REQUEST);
        params.add(new BitmapParam("bmp", Bitmap.CompressFormat.JPEG, getBitmap()));

        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);
        request.setRequestParams(params);

        final String check = "битмапа - ok";
        doTest(request, check);
    }

    /**
     * Test sending all in one - short text, long text, bitmap.
     *
     * Yo! Who needs dat tests Oo I'm done!
     */
    public void testMixedRequest() throws Exception {
        final Request request = new Request(Const.PARAMED_LOADER_TEST_SCRIPT);
        request.setRequestParams(getParts(Const.PARAM_MIXED_REQUEST, true, true));
        final String check = Const.PART1.getData() + " -- " + Const.PART2.getData() +
                " -- " + Const.PART3.getData() + " -- " +
                request.getRequestParams().get(4).getRawData().length + " -- " +
                "битмапа - ok";

        doTest(request, check);
    }

    private void doTest(final Request request, final String check) throws Exception {
        final ParamedLoaderWrapper loader = new ParamedLoaderWrapper();
        loader.addLoadingListener(new LoadingAdapter2<String>() {
            @Override
            public void processFinished(Request request, byte[] rawData, String data) {
                super.processFinished(request, rawData, data);

                assertTrue(request.getResponseContentLength() != -1);
                assertNotNull(rawData);
                assertEquals(check, data);

                LogUtil.d(ParamedLoaderTest.class, "content length = " + request.getResponseContentLength());
                LogUtil.d(ParamedLoaderTest.class, "content = " + data);
            }
        });

        loader.addToQueue(request);
        loader.start();

        waitLoadingThreads(loader);
    }

    private List<Param> getParts(StringParam testName, boolean includeBmp, boolean includeLongStr) {
        final List<Param> params = new ArrayList<Param>();

        params.add(testName);
        params.add(Const.PART1);
        params.add(Const.PART2);
        params.add(Const.PART3);
        if (includeLongStr) params.add(Const.PART4_LONG);
        if (includeBmp)
            params.add(new BitmapParam("bmp",
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
        while (loader.getLoaderThread2().isAlive()) {
        }
    }

    private class ParamedLoaderWrapper extends ParamedLoader<String> {

        public ParamedLoaderWrapper() {
            super();
            setDebug(true);
            setFullAsyncMode(true);
        }

        public Thread getLoaderThread2() {
            return getLoaderThread();
        }

        @Override
        protected void loadInBackground(Request request) throws Exception {
            final byte[] response = load(request);
            postProcessFinished(request, response, new String(response, "UTF-8"));
        }
    }

}
