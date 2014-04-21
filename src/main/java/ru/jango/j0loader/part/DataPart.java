package ru.jango.j0loader.part;

/**
 * A {@link ru.jango.j0loader.part.Part} wrapper for raw data. Default content type -
 * 'application/octet-stream'.
 */
public class DataPart extends Part {
	
	private String filename;
	
	public DataPart(String name, String filename, byte[] data) {
		setContentType("application/octet-stream");

		setRawData(data);
		setFilename(filename);
		setName(name);
	}
	
	@Override
	public Part setName(String name) {
		super.setName(name);
		setContentDisposition("form-data; name=\"" + name + "\"; filename=\"" + getFilename() + "\"");
		
		return this;
	}

	public Part setFilename(String filename) {
		this.filename = filename;
		setContentDisposition("form-data; name=\"" + getName() + "\"; filename=\"" + filename + "\"");
		
		return this;
	}

	public String getFilename() {
		return filename;
	}
}
