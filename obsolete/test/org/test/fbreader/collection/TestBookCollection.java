package org.test.fbreader.collection;

import java.util.ArrayList;
import java.util.Locale;

import junit.framework.TestCase;

import org.spireader.fbreader.collection.BookCollection;
import org.spireader.fbreader.description.Author;
import org.spireader.fbreader.description.BookDescription;
import org.spireader.zlibrary.core.resources.ZLResource;
import org.spireader.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.spireader.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestBookCollection extends TestCase  {
	private final String filename = "testfb2book.fb2";
	private final String filenameZip = "testbookZip.zip";	
	private String myDirectory = "test/data/fb2/filesystem";
	
	public void setUp() {
		new ZLSwingLibrary();
		new ZLOwnXMLProcessorFactory();
		//Locale.setDefault(Locale.ENGLISH);
		//ZLResource.setApplicationDirectory("test/data/resources/application/");
		//ZLResource.setZLibraryDirectory("test/data/resources/zlibrary/");
	}
	
	public void test() {
		BookCollection bc = new BookCollection();
		ArrayList authors = bc.authors();
		for (int i = 0; i < authors.size(); i++) {
			System.out.println(authors.get(i));
		}
	}
	
	public void testExternalBook() {
		BookDescription bd = BookDescription.getDescription(myDirectory+"/"+filename);
		assertTrue(bd != null);
		
		BookCollection bc = new BookCollection();
		bc.isBookExternal(bd);
		ArrayList authors = bc.authors();
		for (int i = 0; i < authors.size(); i++) {
			System.out.println(authors.get(i));
		}
	}

}
