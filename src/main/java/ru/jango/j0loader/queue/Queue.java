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

import ru.jango.j0loader.Request;

/**
 * Base interface for queueing {@link ru.jango.j0loader.Request} objects for loaders. Used
 * directly in {@link ru.jango.j0loader.DataLoader#createQueue()} as a part of Iterator pattern.
 */
public interface Queue {

	/**
     * Returns current element (witch is being processed now) or null.
	 */
	public Request current();
	
	/**
	 * Returns next {@link Request} in queue, or null. <b>Also removes returned request from queue.</b>
	 */
	public Request next();
	
	/**
	 * Removes next {@link Request} from queue.
	 */
	public Request remove();

    /**
     * Removes a {@link Request} from queue at a specified index.
     */
	public Request remove(int index);

    /**
     * Removes a certain {@link Request} from queue. If a queue contains
     * more than one pointer to the same {@link ru.jango.j0loader.Request} object,
     * all of them will be removed.
     *
     * @return  TRUE if the queue was modified
     */
	public boolean remove(Request request);

    /**
     * Adds a {@link Request} into queue.
     */
	public void add(Request request);

    /**
     * Adds all {@link Request}s into queue.
     */
	public void addAll(Collection<? extends Request> requests);

    /**
     * Inserts a {@link Request} into the specified position.
     */
    public boolean insert(int pos, Request request);

    /**
     * Returns current size of the queue.
     */
    public int size();

    /**
     * Checks if the queue is empty.
     */
	public boolean isEmpty();

    /**
     * Removes all elements from queue.
     */
	public void clear();

    /**
     * Checks if a certain request is in queue.
     */
	public boolean contains(Request request);

    /**
     * Searches the queue for the specified {@link ru.jango.j0loader.Request} and returns the
     * index of the first occurrence.
     *
     * @param request   the {@link ru.jango.j0loader.Request} to search for
     * @return          found index or -1
     */
	public int indexOf(Request request);

    /**
     * Searches the queue for the specified {@link ru.jango.j0loader.Request} by it's
     * {@link java.net.URI} and returns the index of the first occurrence.
     *
     * @param uri   the {@link java.net.URI} to search for
     * @return      found index or -1
     */
	public int indexOf(URI uri);
}
