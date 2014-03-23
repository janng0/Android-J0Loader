package ru.jango.j0loader.queue;

import java.net.URI;
import java.util.Collection;

import ru.jango.j0loader.Request;

public interface Queue {

	/**
	 * Возвращает текущий элемент (который сейчас обрабатывается) 
	 */
	public Request current();
	
	/**
	 * Берет следующий по очереди {@link Request} и удаляет его из очереди. 
	 * 
	 * @return следующий по очереди {@link Request}, либо null
	 */
	public Request next();
	
	/**
	 * Удаляет первый в очереди элемент. <br><br>
	 * 
	 * ВНИМАНИЕ. Не рекомендуется использовать в многопоточных системах - 
	 * могут возникнуть проблемы синхронизации.
	 */
	public Request remove();

	/**
	 * Удаляет указанный по номеру элемент из очереди. <br><br>
	 * 
	 * ВНИМАНИЕ. Не рекомендуется использовать в многопоточных системах - 
	 * могут возникнуть проблемы синхронизации.
	 * 
	 * @param index номер элемента в очереди
	 * @return удаленный элемент либо null
	 */
	public Request remove(int index);
	
	/**
	 * Удаляет указанный элемент из очереди. <br><br>
	 * 
	 * ВНИМАНИЕ. Не рекомендуется использовать в многопоточных системах - 
	 * могут возникнуть проблемы синхронизации.
	 * 
	 * @param request элемент в очереди
	 * @return удаленный элемент либо null
	 */
	public boolean remove(Request request);
	
	/**
	 * Добавляет {@link Request} в конец очереди.
	 * 
	 * @param request {@link Request} для добавления
	 */
	public void add(Request request);

	/**
	 * Добавляет все {@link Request} в конец очереди.
	 * 
	 * @param requests коллекция {@link Request} для добавления
	 */
	public void addAll(Collection<? extends Request> requests);
	
	/**
	 * @return TRUE, если очередь пуста
	 */
	public boolean isEmpty();

	/**
	 * Очищает очередь загрузки
	 */
	public void clear();
	
	/**
	 * Проверяет наличие элемента в очереди.
	 * 
	 * @param request элемент для проверки
	 */
	public boolean contains(Request request);
	
	/**
	 * Ищет определенный объект очереди
	 * 
	 * @param request {@link Request}, который надо искать
	 * @return порядковый номер указанного запроса в очереди, либо -1
	 */
	public int indexOf(Request request);
	
	/**
	 * Ищет определенный объект очереди по его {@link java.net.URI}
	 * 
	 * @param uri {@link java.net.URI}, запрос на который надо искать
	 * @return номер первого запроса на указанный URI, либо -1
	 */
	public int indexOf(URI uri);
}
