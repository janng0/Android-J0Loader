package ru.jango.j0loader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Special loader for handling JSON responses. Simply decodes response string with
 * {@link org.json.JSONArray} and {@link org.json.JSONObject}.
 * <br><br>
 *
 * <b>NOTE: ALWAYS</b> returns {@link org.json.JSONArray} to {@link ru.jango.j0loader.DataLoader.LoadingListener}.
 * If a response contains a {@link org.json.JSONObject}, then a {@link org.json.JSONArray} will be created
 * manually and listeners will receive a {@link org.json.JSONArray} with one {@link org.json.JSONObject} in it.
 */
public class JSONLoader extends ParamedLoader<JSONArray> {

	@Override
	protected void loadInBackground(Request request) throws Exception {
		final byte[] rawData = load(request);
		final String json = new String(rawData, "UTF-8");

		JSONArray array;
		try { array = new JSONArray(json); }
		catch(JSONException e) {  
			array = new JSONArray();
			array.put(new JSONObject(json));
		}
		
		logDebug("loadInBackground: " + request.getURI() + " : " + array);
		postMainLoadingFinished(request, rawData, array);
	}
}
