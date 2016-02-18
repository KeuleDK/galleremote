package com.rene_arnold.galleremote.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utilities {

	public static String inputStream2string(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String s = reader.readLine();
		while (s != null) {
			sb.append(s);
			s = reader.readLine();
		}
		return sb.toString();
	}
}
