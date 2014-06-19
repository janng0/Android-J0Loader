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

package ru.jango.j0loader;

import android.net.Uri;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import ru.jango.j0loader.param.Param;
import ru.jango.j0loader.param.StringParam;

/**
 * Class-container for information, needed for loaders to work: <br>
 * <ul>
 * <li>{@link java.net.URI} of the request destination</li>
 * <li>{@link java.util.List}<{@link ru.jango.j0loader.param.Param}> with request parameters
 * (for HTTP POST and GET)</li>
 * <li>HTTP method - basically it is determined automatically, GET is in priority</li>
 * <li>size of the response data in bytes (value of HTTP response 'content-length' header,
 * file size, etc.; -1 if the data size is unknown or unavailable)</li>
 * </ul>
 */
public class Request {

    private URI uri;
    private Method method;
    private long responseContentLength;
    private List<Param> params;

    /**
     * Constructs new request with some internal variables. HTTP method will be set automatically.
     *
     * @param uri                       request destination
     *
     * @see #getComposedURI()
     * @see #getRecommendedMethod()
     * @see #Request(java.net.URI, long, java.util.List)
     */
    public Request(URI uri) {
        this(uri, -1, null);
    }

    /**
     * Constructs new request with some internal variables. HTTP method will be set automatically.
     *
     * @param uri                       request destination
     * @param responseContentLength     response "content-length" header value, file size, etc.
     *
     * @see #getComposedURI()
     * @see #getRecommendedMethod()
     * @see #Request(java.net.URI, long, java.util.List)
     */
    public Request(URI uri, long responseContentLength) {
        this(uri, responseContentLength, null);
    }

    /**
     * Constructs new request with some internal variables. HTTP method will be set automatically.
     *
     * @param uri                       request destination
     * @param params                    list of HTTP parameters to send with request
     *
     * @see #getComposedURI()
     * @see #getRecommendedMethod()
     * @see #Request(java.net.URI, long, java.util.List)
     */
    public Request(URI uri, List<Param> params) {
        this(uri, -1, params);
    }

    /**
     * Constructs new request with all internal variables. HTTP method will be set automatically.
     *
     * @param uri                       request destination
     * @param responseContentLength     response "content-length" header value, file size, etc.
     * @param params                    list of HTTP parameters to send with request
     *
     * @see #getComposedURI()
     * @see #getRecommendedMethod()
     */
    public Request(URI uri, long responseContentLength, List<Param> params) {
        this.uri = uri;

        setResponseContentLength(responseContentLength);
        setRequestParams(params);
    }

    public void setResponseContentLength(long responseContentLength) {
        this.responseContentLength = responseContentLength;
    }

    public long getResponseContentLength() {
        return responseContentLength;
    }

    /**
     * Sets HTTP parameters for this request. This method also determines optimal HTTP method and
     * applies it. That is - rewrites previously set HTTP method.
     *
     * @see #getRecommendedMethod()
     */
    public void setRequestParams(List<Param> params) {
        this.params = params;
        this.method = getRecommendedMethod();
    }

    public List<Param> getRequestParams() {
        return params;
    }

    /**
     * Manually sets the HTTP method for sending request. Basically this is unnecessary because
     * {@link ru.jango.j0loader.Request} automatically determines optimal method when setting params
     * list.
     *
     * @param method    desired {@link ru.jango.j0loader.Request.Method} for sending HTTP request
     * @throws IllegalStateException    if parameters, specified previously, couldn't be sent
     *                                  by passed into <code>setMethod</code> method
     * @see #getRecommendedMethod()
     * @see #setRequestParams(java.util.List)
     */
    public void setMethod(Method method) throws IllegalStateException {
        final Method recommended = getRecommendedMethod();
        if (recommended.equals(Method.POST) && method.equals(Method.GET))
            throw new IllegalStateException("This parameters could be sent only by POST method.");

        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Checks if params list could be composed in a single URI for sending via HTTP GET.
     */
    protected boolean canComposeParams() {
        if (params == null)
            return false;

        for (Param param : params)
            if (!(param instanceof StringParam && ((StringParam) param).getData().length() < 255))
                return false;

        return true;
    }

    /**
     * Composes params list string witch could be appended to the request URI for HTTP GET usage.
     *
     * @return  string, composed from params list, or NULL, if it is impossible - than only HTTP
     *          POST could be used
     */
    protected String getComposedParams() {
        if (!canComposeParams())
            return null;

        final StringBuilder sb = new StringBuilder();
        StringParam stringPart;
        for (Param param : params) {
            stringPart = (StringParam) param;

            sb.append(stringPart.getName());
            sb.append("=");
            sb.append(Uri.encode(stringPart.getData()));
            sb.append("&");
        }

        if (sb.length() != 0)
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    /**
     * Composes params list string and appends it to the request URI for HTTP GET usage. Always
     * returns new {@link java.net.URI} instance.
     *
     * @return  if HTTP GET could be used - returns ready to use {@link java.net.URI} (initial one
     *          with appended params), else returns copy of the initial {@link java.net.URI} as a
     *          new instance (params the should be send using HTTP POST)
     */
    public URI getComposedURI() {
        final String composedParams = getComposedParams();
        if (composedParams == null || (composedParams.length() + uri.toString().length()) > 250)
            return URI.create(uri.toString());
        else if (!uri.toString().contains("?"))
            return URI.create(uri.toString() + "?" + composedParams);
        else return URI.create(uri.toString() + "&" + composedParams);
    }

    /**
     * Same as {@link #getComposedURI()} but already transformed into {@link java.net.URL}.
     */
    public URL getComposedURL() throws URISyntaxException, MalformedURLException {
        return getComposedURI().toURL();
    }

    /**
     * Looks through request params list and returns desired method for sending them. HTTP GET is
     * in priority, so if it could be used, then {@link ru.jango.j0loader.Request.Method#GET} will
     * be returned.
     * <br><br>
     * HTTP GET could be used if:
     * <ul>
     * <li>params were not set at all</li>
     * <li>params list contains <b>only</b> {@link ru.jango.j0loader.param.StringParam}s</li>
     * <li>final uri-string composed by initial {@link java.net.URI} and all params is not longer
     * the 255 chars - limitation of URI standards and HTTP protocol itself</li>
     * </ul>
     * <br><br>
     * This method is used in all {@link ru.jango.j0loader.Request} constructors,
     * {@link #setMethod(ru.jango.j0loader.Request.Method)} and
     * {@link #setRequestParams(java.util.List)}.
     */
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
        POST {
            @Override
            public void configURLConnection(HttpURLConnection urlConnection)  throws ProtocolException {
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + Param.BOUNDARY);
            }
        },

        GET {
            @Override
            public void configURLConnection(HttpURLConnection urlConnection)  throws ProtocolException {
                urlConnection.setDoOutput(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
        };

        public abstract void configURLConnection(HttpURLConnection urlConnection) throws ProtocolException;
    }
}
