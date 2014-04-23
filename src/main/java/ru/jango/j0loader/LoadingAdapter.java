package ru.jango.j0loader;

public class LoadingAdapter<T> implements DataLoader.LoadingListener<T> {
    @Override
    public void processStarted(Request request) {}
    @Override
    public void uploadingUpdateProgress(Request request, long uploadedBytes, long totalBytes) {}
    @Override
    public void downloadingUpdateProgress(Request request, long loadedBytes, long totalBytes) {}
    @Override
    public void processFinished(Request request, byte[] rawData, T data) {}
    @Override
    public void processFailed(Request request, Exception e) {}
}

