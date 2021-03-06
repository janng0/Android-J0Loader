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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import ru.jango.j0loader.param.Param;

/**
 * Parametrized loader. A loader, witch allows not only to download data, but also to upload it.
 * Parameters for sending should be backed as a {@link java.util.List}<{@link ru.jango.j0loader.param.Param}> and
 * passed inside a {@link ru.jango.j0loader.Request} object.
 *
 * @param <T>   after postprocessing of the loaded data an object of type T will be created and passed into
 *              {@link ru.jango.j0loader.DataLoader.LoadingListener#processFinished(Request, byte[], Object)}
 */
public abstract class ParamedLoader<T> extends DataLoader<T> {

	@Override
	protected InputStream openInputStream(Request request) throws IOException, URISyntaxException {
		final HttpURLConnection urlConnection = (HttpURLConnection) request.getComposedURL().openConnection();
        configURLConnection(request, urlConnection);

        if (request.getMethod() == Request.Method.POST) sendParams(request, urlConnection);
		request.setResponseContentLength(urlConnection.getContentLength());
		return urlConnection.getInputStream();
	}

    /**
     * Applies configurations to specified {@link java.net.HttpURLConnection}.
     */
	protected void configURLConnection(Request request, HttpURLConnection urlConnection) throws ProtocolException {
        super.configURLConnection(urlConnection);
        request.getMethod().configURLConnection(urlConnection);

        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Cache-Control", "no-cache");
	}

    protected void sendParams(Request request, HttpURLConnection urlConnection) throws IOException, URISyntaxException {
		final OutputStream out = urlConnection.getOutputStream();
		out.write((Param.HYPHENS + Param.BOUNDARY + Param.RN).getBytes("UTF-8"));
		
		// generate entities and count content length for uploading
        final ArrayList<byte[]> entities = new ArrayList<byte[]>();
        long totalBytes = 0;
		for (Param param : request.getRequestParams()) {
			entities.add(param.encodeEntity());
			totalBytes += entities.get(entities.size()-1).length;
		}
		
		logDebug("sendParams: " + request.getURI() + " : " 
					+ "entity count: " + entities.size() + "; " 
					+ "totalBytes: " + totalBytes + "bytes");
        
		writeEntities(request, out, entities, totalBytes);
        try { out.close(); } catch(Exception ignored) {}
	}
	
	/**
     * Write entities into specified {@link java.io.OutputStream}. Also automatically calls
     * {@link #onUploadingUpdateProgress(Request, long, long)} during the work; handles
     * {@link #canWork()} and {@link #isCurrentCancelled()} flags.
	 */
	protected void writeEntities(Request request, OutputStream out, ArrayList<byte[]> entities, long totalBytes) throws IOException {
		long progressLastUpdated = System.currentTimeMillis();
		int offset, count, uploadedBytes = 0;
		boolean updateProgress;

        final BufferedOutputStream output = new BufferedOutputStream(out);
		for (byte[] entity : entities) {
			offset = 0;
			while (offset < entity.length && canWork() && !isCurrentCancelled()) {
				count = Math.min(entity.length - offset, BUFFER_SIZE_BYTES);
                output.write(entity, offset, count);
				offset += count;
				uploadedBytes += count;
				
				updateProgress = System.currentTimeMillis() > progressLastUpdated + PROGRESS_UPDATE_INTERVAL_MS;
				if (updateProgress) {
					progressLastUpdated = System.currentTimeMillis();
					onUploadingUpdateProgress(request, uploadedBytes, totalBytes);
				}
			}
			
		logDebug("writeEntities: " + request.getURI() + " : " + "entity wrote: " 
				+ (new String(entity, "UTF-8")));
		}

        output.flush();
	}
}
