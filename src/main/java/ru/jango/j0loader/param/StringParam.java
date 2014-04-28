package ru.jango.j0loader.param;

/**
 * A {@link Param} wrapper for plain text data. Default content type -
 * 'text/plain; charset=UTF-8'.
 */
public class StringParam extends Param {
	
	private String data;
	
	public StringParam(String name, String data) {
		setContentType("text/plain; charset=UTF-8");

		setData(data);
		setName(name);
	}
	
	@Override
	public Param setName(String name) {
		super.setName(name);
		setContentDisposition("form-data; name=\"" + name + "\"");
		
		return this;
	}

	public Param setData(String data) {
		this.data = data;
		try { setRawData(data.getBytes("UTF-8")); } 
		catch (Exception e) { setRawData(data.getBytes()); }
		
		return this;
	}

	public String getData() {
		return data;
	}

}
