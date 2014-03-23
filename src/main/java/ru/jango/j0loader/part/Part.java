package ru.jango.j0loader.part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class Part {

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

	public Part setRawData(byte[] rawData) {
		this.rawData = rawData;
		return this;
	}

	public Part setName(String name) {
		this.name = name;
		return this;
	}

	public Part setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public Part setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
		return this;
	}

	/**
	 * Пишет объект в указанный поток в виде HTTP-сущности (со всеми заголовками и нюансами).
	 * 
	 * @param out	поток, куда нужно выводить данные
	 * @throws java.io.IOException
	 */
	public void writeToStream(OutputStream out) throws IOException {
		out.write(encodeEntity());
	}

	/**
	 * Преобразует объект в HTTP-сущность (со всеми заголовками и нюансами).
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
		} catch (Exception e) {;}
		finally { try { out.close(); } catch(Exception e) {;} }
		
		return entity;
	}
	
}
