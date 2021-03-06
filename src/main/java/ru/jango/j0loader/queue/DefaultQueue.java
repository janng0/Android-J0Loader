/*
 * The MIT License Copyright (c) 2014 Krayushkin Konstantin (jangokvk@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.jango.j0loader.queue;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import ru.jango.j0loader.Request;

/**
 * Default implementation of the {@link ru.jango.j0loader.queue.Queue} interface. All methods are
 * declared as 'synchronized' for safer asynchronous use.
 * <br><br>
 * In a queue could exist only unique requests (couldn't be added same instance twice).
 */
public class DefaultQueue implements Queue, Iterable<Request> {

	protected LinkedList<Request> queue;
    protected Request current;
	
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
        boolean mod = false;
        while(queue.remove(request)) { mod = true; }
		return mod;
	}
	
	@Override
	public synchronized void add(Request request) {
		if (request != null && !contains(request))
            queue.add(request);
	}

	@Override
	public synchronized void addAll(Collection<? extends Request> requests) {
		if (requests != null && !requests.isEmpty())
            for (Request request : requests)
			    add(request);
	}

    @Override
    public synchronized boolean insert(int pos, Request request) {
        if (request == null || pos < 0 || pos > queue.size() || contains(request))
            return false;

        queue.add(pos, request);
        return true;
    }

    @Override
    public synchronized int size() {
        return queue.size();
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
