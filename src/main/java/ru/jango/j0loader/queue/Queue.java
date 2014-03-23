package ru.jango.j0loader.queue;

import java.net.URI;
import java.util.Collection;

import ru.jango.j0loader.Request;

public interface Queue {

	/**
     * Returns current elements (witch is processed now)
	 */
	public Request current();
	
	/**
	 * Returns next {@link Request} in queue and removes it from queue

	 * @return next {@link Request} in queue, or null
	 */
	public Request next();
	
	/**
	 * Removes first {@link Request} in queue
	 */
	public Request remove();

	public Request remove(int index);
	
	public boolean remove(Request request);
	
	public void add(Request request);

	public void addAll(Collection<? extends Request> requests);
	
	public boolean isEmpty();

	public void clear();
	
	public boolean contains(Request request);
	
	public int indexOf(Request request);
	
	public int indexOf(URI uri);
}
