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

package ru.jango.j0loader.param;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for managing HTTP request params. You can use this class and it's subclasses to
 * construct and manipulate HTTP-entities that you need. All aspects (including ContentType) could
 * be configured as you need.
 * <br><br>
 *
 * If you want a {@link Param}-like wrapper (like
 * {@link BitmapParam}) for some complicated structure (like
 * {@link android.graphics.Bitmap}), it's a good idea to subclass {@link Param}.
 */
public class Param {

    public static final String RN = "\r\n";
    public static final String HYPHENS = "--";
    public static final String BOUNDARY = "******";

    private byte[] rawData;
	private String name;
	private String contentType;
	private String contentDisposition;

	public byte[] getRawData() {
		return rawData;
	}

	public String getName() {
		return name;
	}

	public String getContentType() {
		return contentType;
	}

	public int getContentLength() {
		if (rawData == null) return 0;
		return rawData.length;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}

	public Param setRawData(byte[] rawData) {
		this.rawData = rawData;
		return this;
	}

	public Param setName(String name) {
		this.name = name;
		return this;
	}

	public Param setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public Param setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
		return this;
	}

	/**
	 * Writes this {@link Param} into the specified {@link java.io.OutputStream}
     * as an HTTP-entity (with all headers, Black-Jack and others...)
     *
     * @see #encodeEntity()
	 */
	public void writeToStream(OutputStream out) throws IOException {
		out.write(encodeEntity());
	}

	/**
	 * Transforms this {@link Param} into an HTTP-entity of full value - adds all
     * needed headers and boundaries before and after and transforms all this into a byte array.
	 */
	public byte[] encodeEntity() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] entity = null;
		
		try {
			out.write(("Content-Disposition: " + getContentDisposition() + RN).getBytes("UTF-8"));
			out.write(("Content-Type: " + getContentType() + RN).getBytes("UTF-8"));
			out.write(("Content-Length: " + getContentLength() + RN).getBytes("UTF-8"));
			out.write(RN.getBytes("UTF-8"));
			out.write(rawData);
			out.write(RN.getBytes("UTF-8"));
			out.write((HYPHENS + BOUNDARY + RN).getBytes("UTF-8"));
			out.flush();
			
			entity = out.toByteArray();
		} catch (Exception ignored) {}
		finally { try { out.close(); } catch(Exception ignored) {} }
		
		return entity;
	}
	
}
