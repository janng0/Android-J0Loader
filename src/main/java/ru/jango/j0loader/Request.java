package ru.jango.j0loader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;


import android.net.Uri;

import ru.jango.j0loader.part.Part;

/**
 *	Класс-контейнер для необходимой для загрузки информации: <br>
 *	-- хранит в себе собственно саму {@link java.net.URI} <br>
 *	-- хранит параметры запроса для нее <br>
 *	-- хранит объем данных в байтах, которые находятся по адресу, 
 *		описанному этим {@link java.net.URI} (типа content-length в случае
 *		HTTP-запросов, или еще что-то в противном случае)
 */
public class Request {
	
	private URI uri;
	private long contentLength;
	private List<Part> params;
	
	public Request(URI uri) throws URISyntaxException  {
		this(uri, -1, null);
	}

	public Request(URI uri, long contentLength) throws URISyntaxException  {
		this(uri, contentLength, null);
	}
	
	public Request(URI uri, List<Part> params) throws URISyntaxException  {
		this(uri, -1, params);
	}
	
	public Request(URI uri, long contentLength, List<Part> params) throws URISyntaxException  {
		this.uri = new URI(Uri.encode(Uri.decode(uri.toString()),":/?="));;
		this.contentLength = contentLength;
		this.params = params;
	}
	
	public void setContentLength(long contentLength) { this.contentLength = contentLength; }
	public void setRequestParams(List<Part> params) { this.params = params; }
	
	public long getContentLength() { return contentLength; }
	public List<Part> getRequestParams() { return params; }
	
	public URI getURI() { return uri; }
	public URL getURL() throws URISyntaxException, MalformedURLException { return getURI().toURL(); }
	@Override
	public String toString() { return uri.toString(); }
}
