package ru.jango.j0loader;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import ru.jango.j0loader.part.Part;
import ru.jango.j0loader.part.StringPart;
import ru.jango.j0util.PathUtil;

/**
 * Class-container for information, needed for loaders to work: <br>
 * <ul>
 * <li>{@link java.net.URI} of the request destination</li>
 * <li>{@link java.util.List}<{@link ru.jango.j0loader.part.Part}> with request parameters
 * (for HTTP POST and GET)</li>
 * <li>HTTP method - "POST" or "GET"</li>
 * <li>size of the response data in bytes (value of HTTP response 'content-length' header,
 * file size, etc.; -1 if the data size is unknown or unavailable)</li>
 * </ul>
 */
public class Request {

    private URI uri;
    private Method method;
    private long responseContentLength;
    private List<Part> params;

    public Request(URI uri) {
        this(uri, -1, null);
    }

    public Request(URI uri, long responseContentLength) {
        this(uri, responseContentLength, null);
    }

    public Request(URI uri, List<Part> params) {
        this(uri, -1, params);
    }

    public Request(URI uri, long responseContentLength, List<Part> params) {
        this.uri = PathUtil.safeStringToURI(uri.toString());

        setResponseContentLength(responseContentLength);
        setRequestParams(params);
    }

    public void setResponseContentLength(long responseContentLength) {
        this.responseContentLength = responseContentLength;
    }

    public long getResponseContentLength() {
        return responseContentLength;
    }

    public void setRequestParams(List<Part> params) {
        this.params = params;
        this.method = getRecommendedMethod();
    }

    public List<Part> getRequestParams() {
        return params;
    }

    public void setMethod(Method method) throws IllegalStateException {
        final Method recommended = getRecommendedMethod();
        if (recommended.equals(Method.POST) && method.equals(Method.GET))
            throw new IllegalStateException("This parameters could be sent only by POST method.");

        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    private boolean canComposeParams() {
        if (params == null)
            return false;

        for (Part part : params)
            if (!(part instanceof StringPart && ((StringPart) part).getData().length() < 255))
                return false;

        return true;
    }

    private String getComposedParams() {
        if (!canComposeParams())
            return null;

        final StringBuilder sb = new StringBuilder();
        StringPart stringPart;
        for (Part part : params) {
            stringPart = (StringPart) part;

            sb.append(stringPart.getName());
            sb.append("=");
            sb.append(Uri.encode(stringPart.getData()));
            sb.append("&");
        }

        if (sb.length() != 0)
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    public URI getComposedURI() {
        final String composedParams = getComposedParams();
        if (composedParams == null || (composedParams.length() + uri.toString().length()) > 250)
            return URI.create(uri.toString());
        else if (!uri.toString().contains("?"))
            return URI.create(uri.toString() + "?" + composedParams);
        else return URI.create(uri.toString() + "&" + composedParams);
    }

    public Method getRecommendedMethod() {
        final URI composedURI = getComposedURI();
        if (params == null) return Method.GET;
        else if (composedURI == null || composedURI.equals(uri)) return Method.POST;
        else return Method.GET;
    }

    public URI getURI() {
        return uri;
    }

    public URL getURL() throws URISyntaxException, MalformedURLException {
        return getURI().toURL();
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    public enum Method {
        POST, GET
    }
}
