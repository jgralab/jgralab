package de.uni_koblenz.jgralabtest.non_junit_tests;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralabtest.schemas.commenttest.CommentTestSchema;

public class CommentTest {
	public static void main(String[] args) {
		System.out.println("Writing schema file CommentTestOutput.tg...");
		try {
			GraphIO.saveSchemaToFile("CommentTestOutput.tg",
					CommentTestSchema.instance());
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fini.");
	}
}
