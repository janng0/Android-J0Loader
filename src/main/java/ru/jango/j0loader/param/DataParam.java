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
