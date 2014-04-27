package ru.jango.j0loader.test;

import ru.jango.j0loader.LoadingAdapter;
import ru.jango.j0loader.Request;
import ru.jango.j0util.LogUtil;

public class LoadingAdapter2<T> extends LoadingAdapter<T> {

    @Override
    public void processStarted(Request request) {
        LogUtil.d(LoadingAdapter.class, "loading started: " + request.getURI());
    }

    @Override
    public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {
        LogUtil.d(LoadingAdapter.class, "uploading progress: " + (uploadedBytes * 100 / totalBytes));
    }

    @Override
    public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {
        LogUtil.d(LoadingAdapter.class, "loading progress: " + (loadedBytes * 100 / totalBytes));
    }

    @Override
    public void processFinished(Request request, byte[] rawData, T data) {
        LogUtil.d(LoadingAdapter.class, "loading finished: " + request.getURI());
    }

    @Override
    public void processFailed(Request request, Exception e) {
        LogUtil.d(LoadingAdapter.class, "loading failed: " + request.getURI());
        LogUtil.d(LoadingAdapter.class, e.toString());
    }
}
