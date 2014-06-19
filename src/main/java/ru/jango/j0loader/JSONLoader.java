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
		onProcessFinished(request, rawData, array);
	}
}
