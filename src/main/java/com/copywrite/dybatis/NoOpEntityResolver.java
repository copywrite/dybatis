package com.copywrite.dybatis;


import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringBufferInputStream;

public class NoOpEntityResolver implements EntityResolver {
	@Override
	public InputSource resolveEntity(String arg0, String arg1)
			throws SAXException, IOException {
		return new InputSource(new StringBufferInputStream(""));
	}
}
