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

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import ru.jango.j0util.BmpUtil;

/**
 * A {@link Param} wrapper for {@link android.graphics.Bitmap} object. Default content type -
 * 'image/jpeg' or 'image/png' (depends of the specified {@link android.graphics.Bitmap.CompressFormat}).
 * <br><br>
 *
 * Be aware, your {@link android.graphics.Bitmap} would be compressed into byte array according
 * to the specified {@link android.graphics.Bitmap.CompressFormat} in {@link #getFormat()}. That means,
 * that executing of some methods could take some time.
 */
public class BitmapParam extends Param {
	
	private CompressFormat format;
	private Bitmap data;

    /**
     * Be aware, your {@link android.graphics.Bitmap} would be compressed into byte array according
     * to the specified {@link android.graphics.Bitmap.CompressFormat}. That means, that executing
     * of this method could take some time.
     *
     * @param name      name of the part - needed for HTTP; set name <b>WITHOUT AN EXTENSION</b> - it
     *                  would be applied automatically according to specified
     *                  {@link android.graphics.Bitmap.CompressFormat}
     * @param format    {@link android.graphics.Bitmap.CompressFormat} that will be accepted by your server
     * @param data      your {@link android.graphics.Bitmap}
     */
	public BitmapParam(String name, CompressFormat format, Bitmap data) {
		this.data = data;
		
		setFormat(format);
		setName(name);
	}

    /**
     * Simple constructor.
     *
     * @param name      name of the part - needed for HTTP; set name <b>WITHOUT AN EXTENSION</b> - it
     *                  would be applied automatically according to specified
     *                  {@link android.graphics.Bitmap.CompressFormat}
     * @param format    {@link android.graphics.Bitmap.CompressFormat} that will be accepted by your server
     * @param rawData   your bitmap as a byte array
     */
	public BitmapParam(String name, CompressFormat format, byte[] rawData) {
		this.data = null;

		setRawData(rawData);
		setFormat(format);
		setName(name);
	}

    /**
     * Sets name of the part - needed for HTTP. Set name <b>WITHOUT AN EXTENSION</b> - it
     * would be applied automatically according to specified
     * {@link android.graphics.Bitmap.CompressFormat}.
     */
	@Override
	public Param setName(String name) {
		super.setName(name);
		setContentDisposition("form-data; name=\"" + name + "\"; filename=\"" + getFilename() + "\"");
		
		return this;
	}
	
	public CompressFormat getFormat() {
		return format;
	}

    /**
     * Be aware, your {@link android.graphics.Bitmap} would be compressed into byte array according
     * to the specified {@link android.graphics.Bitmap.CompressFormat}. That means, changing format
     * could take some time.
     */
	public Param setFormat(CompressFormat format) {
		this.format = format;
		
		if (data != null) setData(data); 
		setContentType("image/"+formatToExt(format));
		setContentDisposition("form-data; name=\"" + getName() + "\"; filename=\"" + getFilename() + "\"");
		
		return this;
	}
	
	public Bitmap getData() {
		return data;
	}

    /**
     * Be aware, your {@link android.graphics.Bitmap} would be compressed into byte array according
     * to the specified {@link android.graphics.Bitmap.CompressFormat} in {@link #getFormat()}. That means,
     * that executing of this method could take some time.
     */
	public Param setData(Bitmap data) {
		this.data = data;
		setRawData(BmpUtil.bmpToByte(data, format, 100));
		
		return this;
	}
	
	public String getFilename() {
		return getName() + "." + formatToExt(format);
	}
	
	private static String formatToExt(CompressFormat format) {
		switch (format) {
			case JPEG: return "jpeg";
			default: return "png";
		}
	}

}
