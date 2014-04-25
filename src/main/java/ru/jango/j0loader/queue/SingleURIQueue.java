package ru.jango.j0loader.queue;

import java.net.URI;

import ru.jango.j0loader.Request;

/**
 * Special queue for {@link ru.jango.j0loader.image.ImageLoader}. This loader doesn't need requests'
 * {@link ru.jango.j0loader.Request#params} - it needs only {@link java.net.URI}, so we can uniquely
 * identify a queue element by it's uri, not combination of uri-params.
 * <br><br>
 * That queue checks {@link java.net.URI} inside a {@link ru.jango.j0loader.Request} before adding
 * it, so it contains requests with unique URIs.
 */
public class SingleURIQueue extends DefaultQueue {

    /**
     * Removes from queue all requests with the specified {@link java.net.URI}. Actually there
     * should be only one element to remove, otherwise something somewhere went wrong some time ago.
     *
     * @param uri   {@link java.net.URI} to search and remove
     * @return      TRUE if the queue was modified
     */
    public synchronized boolean remove(URI uri) {
        boolean mod = false;
        for (int i=0; i<queue.size(); i++)
            if (queue.get(i).getURI().equals(uri)) {
                queue.remove(i);
                mod = true;
                i--;
            }

        return mod;
    }

    /**
     * Checks if a request with certain {@link java.net.URI} is in queue.
     */
    public synchronized boolean contains(URI uri) {
        for (Request request : queue)
            if (request.getURI().equals(uri))
                return true;

        return false;
    }

    @Override
    public synchronized void add(Request request) {
        if (request != null && !contains(request.getURI()))
            queue.add(request);
    }

    @Override
    public synchronized boolean insert(int pos, Request request) {
        if (request == null || pos < 0 || pos > queue.size() || contains(request.getURI()))
            return false;

        queue.add(pos, request);
        return true;
    }

}
