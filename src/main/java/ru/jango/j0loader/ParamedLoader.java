package ru.jango.j0loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import ru.jango.j0loader.part.Part;

public abstract class ParamedLoader<T> extends DataLoader<T> {

	public ParamedLoader() {
		super();
	}

	@Override
	protected InputStream openInputStream(Request request) throws IOException, URISyntaxException {
		final HttpURLConnection urlConnection = (HttpURLConnection) request.getURL().openConnection();
		configHttpPOSTConnection(urlConnection);
		sendParams(request, urlConnection);

		request.setContentLength(urlConnection.getContentLength());
		return urlConnection.getInputStream();
	}

	private void configHttpPOSTConnection(HttpURLConnection urlConnection) throws ProtocolException {
		urlConnection.setUseCaches(false);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setConnectTimeout(15000);
		urlConnection.setReadTimeout(10000);
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		urlConnection.setRequestProperty("Cache-Control", "no-cache");
		urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + Part.BOUNDARY);
	}
	
	private void sendParams(Request request, HttpURLConnection urlConnection) throws IOException, URISyntaxException {
		final OutputStream out = urlConnection.getOutputStream();          
		out.write((Part.HYPHENS + Part.BOUNDARY + Part.RN).getBytes("UTF-8"));
		
		// сгенерить сущности; запомнить их, чтобы еще раз не генерить; 
		// посчитать общий объем данных по сгенеренным сущностям
        final ArrayList<byte[]> entities = new ArrayList<byte[]>();
        long totalBytes = 0;
		for (Part part : request.getRequestParams()) {
			entities.add(part.encodeEntity());
			totalBytes += entities.get(entities.size()-1).length;
		}
		
		logDebug("sendParams: " + request.getURI() + " : " 
					+ "entity count: " + entities.size() + "; " 
					+ "totalBytes: " + totalBytes + "bytes");
        
		writeEntities(request, out, entities, totalBytes);
        out.close();
	}
	
	/** 
	 * Залить сущности в выходной поток; параллельно пинговать слушателя.
	 */
	private void writeEntities(Request request, OutputStream out, ArrayList<byte[]> entities, long totalBytes) throws IOException {
		long progressLastUpdated = System.currentTimeMillis();
		int offset, count;
		int uploadedBytes = 0;
		boolean updateProgress;
		
		for (byte[] entity : entities) {
			offset = 0;
			while (offset < entity.length && canWork()) {
				count = Math.min(entity.length - offset, BUFFER_SIZE_BYTES);
				out.write(entity, offset, count);
				offset += count;
				uploadedBytes += count;
				
				updateProgress = System.currentTimeMillis() > progressLastUpdated + PROGRESS_UPDATE_INTERVAL_MS;
				if (updateProgress) {
					progressLastUpdated = System.currentTimeMillis();
					postMainUploadingUpdateProgress(request, uploadedBytes, totalBytes);
				}
			}
			
		logDebug("writeEntities: " + request.getURI() + " : " + "entity wrote: " 
				+ (new String(entity, "UTF-8")));
		}
		
        out.flush();
	}
}
