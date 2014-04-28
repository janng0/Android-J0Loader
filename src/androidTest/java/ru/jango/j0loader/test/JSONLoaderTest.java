package ru.jango.j0loader.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.jango.j0loader.JSONLoader;
import ru.jango.j0loader.Request;
import ru.jango.j0loader.param.BitmapParam;
import ru.jango.j0loader.param.Param;
import ru.jango.j0util.LogUtil;

public class JSONLoaderTest extends AndroidTestCase {

    public void testMixedRequest() throws Exception {
        final JSONLoaderWrapper loader = new JSONLoaderWrapper();
        loader.addLoadingListener(new LoadingAdapter2<JSONArray>() {
            @Override
            public void processFinished(Request request, byte[] rawData, JSONArray data) {
                super.processFinished(request, rawData, data);

                assertTrue(request.getResponseContentLength() != -1);
                assertNotNull(rawData);
                assertEquals(1, data.length());

                final JSONObject obj = data.optJSONObject(0);
                assertEquals(Settings.PARAM1.getData(), obj.optString(Settings.PARAM1.getName()));
                assertEquals(Settings.PARAM2.getData(), obj.optString(Settings.PARAM2.getName()));
                assertEquals(Settings.PARAM3.getData(), obj.optString(Settings.PARAM3.getName()));
                assertEquals(Settings.PARAM4_LONG.getRawData().length, obj.optInt(Settings.PARAM4_LONG.getName()));
                assertEquals("битмапа - ok", obj.optString(Settings.PARAM_BMP_NAME));

                LogUtil.d(ParamedLoaderTest.class, "content length = " + request.getResponseContentLength());
                LogUtil.d(ParamedLoaderTest.class, "content = " + data);
            }
        });

        final List<Param> params = new ArrayList<Param>();
        params.add(Settings.PARAM1);
        params.add(Settings.PARAM2);
        params.add(Settings.PARAM3);
        params.add(Settings.PARAM4_LONG);
        params.add(new BitmapParam(Settings.PARAM_BMP_NAME, Bitmap.CompressFormat.JPEG, getBitmap()));

        final Request request = new Request(Settings.JSON_LOADER_TEST_SCRIPT);
        request.setRequestParams(params);

        loader.addToQueue(request);
        loader.start();

        waitLoadingThreads(loader);
    }

    private Bitmap getBitmap() {
        //noinspection ConstantConditions
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.small);
    }

    private void waitLoadingThreads(JSONLoaderWrapper loader) {
        //noinspection StatementWithEmptyBody
        while (loader.getLoaderThread2().isAlive()) {
        }
    }

    private class JSONLoaderWrapper extends JSONLoader {

        public JSONLoaderWrapper() {
            super();
            setDebug(true);
            setFullAsyncMode(true);
        }

        public Thread getLoaderThread2() {
            return getLoaderThread();
        }
    }


}
