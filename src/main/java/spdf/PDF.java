package com.darwinsys.spdf;

import java.io.*;
import java.text.*;
import java.util.*;

/** Represent one PDF file. */
public class PDF {
	/** The output writer */
	protected PrintWriter out;

	/** The list of pages */
	protected ArrayList pages;

	/** The list of object xrefs */
	protected ArrayList xrefs;

	/** The root object */
	PDFObject rootObj = new RootObject(this);

	/** The Info object */
	InfoObject infoObj = new InfoObject(this);

	/** The outlines (outline font) object */
	OutlinesObject outlinesObj = new OutlinesObject(this);

	/** The Pages object */
	PagesObject pagesObj = new PagesObject(this);

	/** The object number of the current object */
	protected int currObj = 1;

	/** A flag to avoid writing twice */
	protected boolean startedWriting = false;

	/** A magic number that identifies the output as a PDF file */
	protected final static String PDF_MAGIC = "%PDF-1.0";

	/** Constructor */
	public PDF(PrintWriter o) {
		out = o;

		pages = new ArrayList();
		xrefs = new ArrayList();

	}

	public void addPage(Page p) {
		pages.add(p);
	}

	public void insertPage(int where, Page p) {
		pages.add(where, p);
	}

	// OUTPUT METHODS -- we provide our own print/println, so we
	// can keep track of file offsets (it was either that, or kludgily
	// use a RandomAccessFile and the getFilePointer() method).

	long offset = 0;

	/** Print a String */
	protected void print(String s){
		out.print(s);
		offset += s.length();
	}

	/** Println a String */
	protected void println(String s) {
		print(s);
		print("\n");
	}

	/** Print an Object */
	protected void print(Object o) {
		print(o.toString());
	}

	/** Println an Object */
	protected void println(Object o) {
		println(o.toString());
	}

	/** Print an int */
	protected void print(int i) {
		String s = Integer.toString(i);
		print(s);
	}

	/** Println an int */
	protected void println(int i) {
		String s = Integer.toString(i);
		print(s);
	}

	/** Println with no args */
	protected void println() {
		print("\n");
	}

	// END OF OUTPUT METHODS 

	/** Add an entry into the offset table */
	protected void addXref() {
		xrefs.add(new Long(offset));
	}

	/** Write the entire output */
	public void writePDF() {
		if (startedWriting) {
			throw new IllegalStateException(
				"writePDF() can only be called once.");
		}
		startedWriting = true;

		writePDFHeader();
		// for (int i=0; i<pages.size(); i++) {
		//	 ((Page)pages.get(i)).writePage(out);
		// }
		writePDFbody();
		writeXrefs();
		writePDFTrailer();
		out.flush();
		out.close();
	}

	protected void writePDFHeader() {
		println(PDF_MAGIC);

		rootObj.print();	// 1

		infoObj.print();	// 2

		outlinesObj.print(); // 3

		pagesObj.print();	// 4
	}

	protected void writePDFbody() {

		addXref();
		println("5 0 obj");
		println("<<");
		println("\t/Type /Page");
		println("\t/Parent 4 0 R");
		println("\t/Resources << /Font << /F1 8 0 R >> /ProcSet 7 0 R >>");
		println("\t/MediaBox [0 0 612 792]");
		println("\t/Contents 6 0 R");
		println(">>");
		println("endobj");
		
		new PDFText(this, 100, 600, "Printed live on the web for " + "Ian Darwin").
			print();

		addXref();
		println("7 0 obj");
		println("[/PDF /Text]");
		println("endobj");

		addXref();
		println("8 0 obj");
		println("<<");
		println("/Type /Font");
		println("/Subtype /Type1");
		println("/Name /F1");
		println("/BaseFont /Helvetica");
		println("/Encoding /MacRomanEncoding");
		println(">>");
		println("endobj");
	}

	DecimalFormat nf10 = new DecimalFormat("0000000000");
	DecimalFormat nf5 = new DecimalFormat("00000");

	/** Write one Xref, in the format 0000000000 65535 f */
	protected void printXref(long n, int where, char inUse) {
		println(nf10.format(n) + " " +  nf5.format(where) + " " + inUse);
	}

	long xrefStart;

	/** Write all the xrefs, using the format above */
	protected void writeXrefs() {
		xrefStart = offset;
		println("xref");
		print(0);
		print(" ");
		print(xrefs.size() + 1);
		println();
		// "fake" entry at 0, always 0, -1, f(free).
		printXref(0, 65535, 'f');
		// Remaining xref entries are for real objects.
		for (int i=0; i<xrefs.size(); i++) {
			Long lo = (Long)xrefs.get(i);
			long l = lo.longValue();
			printXref(l, 0, 'n');
		}

	}

	protected void writePDFTrailer() {
		println("trailer");
		println("<<");
		println("/Size " + (xrefs.size() + 1));
		println("/Root 1 0 R");
		println("/Info 2 0 R");
		println(">>");
		println("% startxref");
		println("% " + xrefStart);
		println("%%EOF");
	}

	class RootObject extends PDFDict {
		protected RootObject(PDF m) {
			super(m);
			dict.put("Type", "/Catalog");
			dict.put("Outlines", "3 0 R");
			dict.put("Pages", "4 0 R");
		}
	}

	class InfoObject extends PDFDict {
		protected InfoObject(PDF m) {
			super(m);
			dict.put("Title", "(Sample PDF by SPDF)");
			dict.put("Author", "(Ian F. Darwin)");
			dict.put("Creator", "(Darwin Open Systems SPDF Software)");
			dict.put("Created", "(D:20000516010203)");
		}
	}
	class OutlinesObject extends PDFDict {
		protected OutlinesObject(PDF m) {
			super(m);
			dict.put("Type", "/Outlines");
			dict.put("Count", "0");
		}
	}
	class PagesObject extends PDFDict {
		protected PagesObject(PDF m) {
			super(m);
			dict.put("Type", "/Pages");
			dict.put("Count", "1");
			dict.put("Kids", "[5 0 R]");
		}
	}
}