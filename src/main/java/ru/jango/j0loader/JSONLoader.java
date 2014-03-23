package ru.jango.j0loader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONLoader extends ParamedLoader<JSONArray> {

	private String json;

	public JSONLoader() {
		json = null;
	}

	public String getLastLoadedJSON() {
		synchronized (json) {
			return json;
		}
	}

	@Override
	protected void loadInBackground(Request request) throws Exception {
		final byte[] rawData = load(request);

		json = new String(rawData, "UTF-8");
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
