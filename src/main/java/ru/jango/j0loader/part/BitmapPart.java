package ru.jango.j0loader.part;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import ru.jango.j0util.BmpUtil;

/**
 * A {@link ru.jango.j0loader.part.Part} wrapper for {@link android.graphics.Bitmap} object. Default content type -
 * 'image/jpeg' or 'image/png' (depends of the specified {@link android.graphics.Bitmap.CompressFormat}).
 * <br><br>
 *
 * Be aware, your {@link android.graphics.Bitmap} would be compressed into byte array according
 * to the specified {@link android.graphics.Bitmap.CompressFormat} in {@link #getFormat()}. That means,
 * that executing of some methods could take some time.
 */
public class BitmapPart extends Part {
	
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
	public BitmapPart(String name, CompressFormat format, Bitmap data) {
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
	public BitmapPart(String name, CompressFormat format, byte[] rawData) {
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
	public Part setName(String name) {
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
	public Part setFormat(CompressFormat format) {
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
	public Part setData(Bitmap data) {
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
