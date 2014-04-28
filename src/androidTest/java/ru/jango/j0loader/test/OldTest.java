package ru.jango.j0loader.test;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;

import ru.jango.j0loader.DataLoader;
import ru.jango.j0loader.JSONLoader;
import ru.jango.j0loader.ParamedLoader;
import ru.jango.j0loader.Request;
import ru.jango.j0loader.param.BitmapParam;
import ru.jango.j0loader.param.Param;
import ru.jango.j0loader.param.StringParam;
import ru.jango.j0util.BmpUtil;
import ru.jango.j0util.LogUtil;

public class OldTest extends AndroidTestCase {

    private static final String BASE = "http://funsellers.ru/libs_test/ffsLoader/";

    private static final String UPLOAD = BASE + "upload.php";
    private static final String JSON_LOAD = BASE + "json_load.php";

    public void testImageUpload() throws Exception {
        //noinspection ConstantConditions
        final Bitmap bmp1 = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        final Bitmap bmp2 = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        final byte[] bmp3 = BmpUtil.bmpToByte(bmp1, Bitmap.CompressFormat.PNG, 100);

        final ParamedLoader<String> loader = new ParamedLoader<String>() {
            @Override
            protected void loadInBackground(Request request) throws Exception {
                final byte[] responce = load(request);
                postProcessFinished(request, responce, new String(responce, "UTF-8"));
            }
        };

        loader.setDebug(true);
        loader.addLoadingListener(new DataLoader.LoadingListener<String>() {

            @Override
            public void processStarted(Request request) {
                LogUtil.d(OldTest.class, "loading started: "+request.getURI());
            }

            @Override
            public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "uploading progress: "+(uploadedBytes*100/totalBytes));
            }

            @Override
            public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "loading progress: "+(loadedBytes*100/totalBytes));
            }

            @Override
            public void processFinished(Request request, byte[] rawData, String data) {
                LogUtil.d(OldTest.class, "loading finished: "+request.getURI());

                Assert.assertNotNull(rawData);
                Assert.assertNotNull(data);
                // todo JSON is in data - more asserts
            }

            @Override
            public void processFailed(Request request, Exception e) {
                LogUtil.d(OldTest.class, "loading failled: "+request.getURI());
                LogUtil.d(OldTest.class, e.toString());
            }

        });

        final ArrayList<Param> params1 = new ArrayList<Param>();
        params1.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp1));

        final ArrayList<Param> params2 = new ArrayList<Param>();
        params2.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp2));

        final ArrayList<Param> params3 = new ArrayList<Param>();
        params3.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp3));

        final URI uri = new URI(UPLOAD);
        loader.addToQueue(new Request(uri, params1));
        loader.addToQueue(new Request(uri, params2));
        loader.addToQueue(new Request(uri, params3));
    }

    public void testTextUpload() throws Exception {
        final URI uri = new URI(UPLOAD);

        final ParamedLoader<String> loader = new ParamedLoader<String>() {
            @Override
            protected void loadInBackground(Request request) throws Exception {
                final byte[] responce = load(request);
                postProcessFinished(request, responce, new String(responce, "UTF-8"));
            }
        };

        loader.setDebug(true);
        loader.addLoadingListener(new DataLoader.LoadingListener<String>() {

            @Override
            public void processStarted(Request request) {
                LogUtil.d(OldTest.class, "loading started: "+request);
            }

            @Override
            public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "loading progress: "+(loadedBytes*100/totalBytes));
            }

            @Override
            public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "uploading progress: "+(uploadedBytes*100/totalBytes));
            }

            @Override
            public void processFinished(Request request, byte[] rawData, String data) {
                LogUtil.d(OldTest.class, "loading finished: "+request);

                Assert.assertNotNull(rawData);
                Assert.assertNotNull(data);
                // todo JSON is in data - more asserts
            }

            @Override
            public void processFailed(Request request, Exception e) {
                LogUtil.d(OldTest.class, "loading failled: "+request);
                LogUtil.d(OldTest.class, e.toString());
            }

        });

        final ArrayList<Param> params1 = new ArrayList<Param>();
        params1.add(new StringParam("txt", "ololo trololo"));

        final ArrayList<Param> params2 = new ArrayList<Param>();
        params2.add(new StringParam("txt", "ololo2 trololo2"));

        final ArrayList<Param> params3 = new ArrayList<Param>();
        params3.add(new StringParam("txt", "ololo3 trololo3"));

        loader.addToQueue(new Request(uri, params1));
        loader.addToQueue(new Request(uri, params2));
        loader.addToQueue(new Request(uri, params3));
    }

    public void testMixedUpload() throws Exception {
        //noinspection ConstantConditions
        final Bitmap bmp1 = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        final Bitmap bmp2 = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        final byte[] bmp3 = BmpUtil.bmpToByte(bmp1, Bitmap.CompressFormat.PNG, 100);

        final ParamedLoader<String> loader = new ParamedLoader<String>() {
            @Override
            protected void loadInBackground(Request request) throws Exception {
                final byte[] responce = load(request);
                postProcessFinished(request, responce, new String(responce, "UTF-8"));
            }
        };

        loader.setDebug(true);
        loader.addLoadingListener(new DataLoader.LoadingListener<String>() {

            @Override
            public void processStarted(Request request) {
                LogUtil.d(OldTest.class, "loading started: "+request);
            }

            @Override
            public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "loading progress: "+(loadedBytes*100/totalBytes));
            }

            @Override
            public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "uploading progress: "+(uploadedBytes*100/totalBytes));
            }

            @Override
            public void processFinished(Request request, byte[] rawData, String data) {
                LogUtil.d(OldTest.class, "loading finished: "+request);

                Assert.assertNotNull(rawData);
                Assert.assertNotNull(data);
                // todo JSON is in data - more asserts
            }

            @Override
            public void processFailed(Request request, Exception e) {
                LogUtil.d(OldTest.class, "loading failled: "+request);
                LogUtil.d(OldTest.class, e.toString());
            }

        });

        final ArrayList<Param> params1 = new ArrayList<Param>();
        params1.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp1));
        params1.add(new StringParam("txt", "ololo trololo"));

        final ArrayList<Param> params2 = new ArrayList<Param>();
        params2.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp2));
        params2.add(new StringParam("txt", "ololo2 trololo2"));

        final ArrayList<Param> params3 = new ArrayList<Param>();
        params3.add(new BitmapParam("image", Bitmap.CompressFormat.PNG, bmp3));
        params3.add(new StringParam("txt", "ololo3 trololo3"));

        final URI uri = new URI(UPLOAD);
        loader.addToQueue(new Request(uri, params1));
        loader.addToQueue(new Request(uri, params2));
        loader.addToQueue(new Request(uri, params3));
    }

    public void testJsonDownload() throws Exception {
        final URI uri = new URI(JSON_LOAD);
        final JSONLoader loader = new JSONLoader();
        loader.setDebug(true);
        loader.addLoadingListener(new DataLoader.LoadingListener<JSONArray>() {

            @Override
            public void processStarted(Request request) {
                LogUtil.d(OldTest.class, "loading started: "+request);
            }

            @Override
            public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "loading progress: "+(loadedBytes*100/totalBytes));
            }

            @Override
            public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {
                LogUtil.d(OldTest.class, "uploading progress: "+(uploadedBytes*100/totalBytes));
            }

            @Override
            public void processFinished(Request request, byte[] rawData, JSONArray data) {
                LogUtil.d(OldTest.class, "loading finished: "+request);

                Assert.assertNotNull(rawData);
                Assert.assertNotNull(data);
                // todo more asserts on data
            }

            @Override
            public void processFailed(Request request, Exception e) {
                LogUtil.d(OldTest.class, "loading failled: "+request);
                LogUtil.d(OldTest.class, e.toString());
            }
        });

        final ArrayList<Param> params1 = new ArrayList<Param>();
        params1.add(new StringParam("txt", "ololo trololo"));

        final ArrayList<Param> params2 = new ArrayList<Param>();
        params2.add(new StringParam("txt", "ololo2 trololo2"));

        final ArrayList<Param> params3 = new ArrayList<Param>();
        params3.add(new StringParam("txt", "ololo3 trololo3"));

        loader.addToQueue(new Request(uri, params1));
        loader.addToQueue(new Request(uri, params2));
        loader.addToQueue(new Request(uri, params3));
    }

}
