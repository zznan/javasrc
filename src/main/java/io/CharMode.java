package io;

import java.io.*;

/**
 * Read a file in character mode - maximally inefficient.
 * @author Ian F. Darwin, http://www.darwinsys.com/
 * @version $Id$
 */
public class CharMode {
	public static void main(String[] argv) throws IOException {
		//+
		BufferedReader is = new BufferedReader(
			new InputStreamReader(System.in));

		int c;
		while ((c=is.read()) != -1) {
			System.out.print((char)c);
		}
		//-
	}
}
