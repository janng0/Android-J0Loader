package ru.jango.j0loader.param;

/**
 * A {@link Param} wrapper for raw data. Default content type -
 * 'application/octet-stream'.
 */
public class DataParam extends Param {
	
	private String filename;
	
	public DataParam(String name, String filename, byte[] data) {
		setContentType("application/octet-stream");

		setRawData(data);
		setFilename(filename);
		setName(name);
	}
	
	@Override
	public Param setName(String name) {
		super.setName(name);
		setContentDisposition("form-data; name=\"" + name + "\"; filename=\"" + getFilename() + "\"");
		
		return this;
	}

	public Param setFilename(String filename) {
		this.filename = filename;
		setContentDisposition("form-data; name=\"" + getName() + "\"; filename=\"" + filename + "\"");
		
		return this;
	}

	public String getFilename() {
		return filename;
	}
}
