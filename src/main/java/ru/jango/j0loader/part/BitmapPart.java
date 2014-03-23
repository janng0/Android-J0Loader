package ru.jango.j0loader.part;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class BitmapPart extends Part {
	
	private CompressFormat format;
	private Bitmap data;
	
	public BitmapPart(String name, CompressFormat format, Bitmap data) {
		this.data = data;
		
		setFormat(format);
		setName(name);
	}

	public BitmapPart(String name, CompressFormat format, byte[] rawData) {
		this.data = null;
		
		setRawData(rawData);
		setFormat(format);
		setName(name);
	}

	@Override
	public Part setName(String name) {
		super.setName(name);
		setContentDisposition("form-data; name=\"" + name + "\"; filename=\"" + getFilename() + "\"");
		
		return this;
	}
	
	public CompressFormat getFormat() {
		return format;
	}
	
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
	
	public Part setData(Bitmap data) {
		this.data = data;
		setRawData(bitmapToRaw(format, data));
		
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
	
	private byte[] bitmapToRaw(CompressFormat format, Bitmap bmp) {
		if (format != CompressFormat.JPEG && format != CompressFormat.PNG)
			format = CompressFormat.PNG;
		
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bmp.compress(format,100,bos); 

		return bos.toByteArray();
	}
	
}
