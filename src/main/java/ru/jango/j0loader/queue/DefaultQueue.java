package ru.jango.j0loader.queue;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import ru.jango.j0loader.Request;

/**
 * Default implementation of the {@link ru.jango.j0loader.queue.Queue} interface. All methods are
 * declared as 'synchronized' for safer asynchronous use.
 */
public class DefaultQueue implements Queue, Iterable<Request> {

	private LinkedList<Request> queue;
	private Request current;
	
	public DefaultQueue() {
		queue = new LinkedList<Request>();
	}
	
	@Override
	public synchronized Request current() {
		return current;
	}
	
	@Override
	public synchronized Request next() {
		if (!queue.isEmpty()) {
			current = remove();
			return current;
		}
		
		return null;
	}
	
	@Override
	public synchronized Request remove() {
		if (isEmpty()) return null;
		return queue.remove(0);
	}
	
	@Override
	public synchronized Request remove(int index) {
		return queue.remove(index);
	}

	@Override
	public synchronized boolean remove(Request request) {
		return queue.remove(request);
	}
	
	@Override
	public synchronized void add(Request request) {
		if (request != null) queue.add(request);
	}

	@Override
	public synchronized void addAll(Collection<? extends Request> requests) {
		if (requests != null && !requests.isEmpty()) 
			queue.addAll(requests);
	}

    @Override
    public synchronized boolean insert(int pos, Request request) {
        if (request == null || pos < 0 || pos > queue.size())
            return false;

        queue.add(pos, request);
        return true;
    }

	@Override
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public synchronized void clear() {
		queue.clear();
        current = null;
	}
	
	@Override
	public synchronized boolean contains(Request request) {
		return queue.contains(request);
	}
	
	@Override
	public synchronized int indexOf(Request request) {
		return queue.indexOf(request);
	}

	@Override
	public synchronized int indexOf(URI uri) {
		for (int i=0; i<queue.size(); i++)
			if (queue.get(i).getURI().equals(uri))
				return i;
		
		return -1;
	}

	@Override
	public Iterator<Request> iterator() {
		return queue.iterator();
	}
}
