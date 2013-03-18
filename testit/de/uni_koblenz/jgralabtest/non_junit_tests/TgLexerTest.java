package de.uni_koblenz.jgralabtest.non_junit_tests;

import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.TgLexer;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;

public class TgLexerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			System.out.println("Start...");

			// TgLexer lexer = new TgLexer(new FileInputStream(
			// "../jgstreetmap/OsmGraph.tg"));
			// TgLexer lexer = new TgLexer(new FileInputStream(
			// "src/de/uni_koblenz/jgralab/greql/GreqlSchema.tg"));

			// TgLexer lexer = new TgLexer(new FileInputStream(
			// "testit/testgraphs/citymapgraph.tg"));
			TgLexer lexer = new TgLexer(
					"this \"utf string\\nwith newline\" is f n a string");
			int n = 0;
			int s = 0;
			int i = 0;
			for (Token t = lexer.nextToken(); t != Token.EOF; t = lexer
					.nextToken()) {
				++n;
				if (t == Token.STRING) {
					++s;
				} else if (t == Token.INT) {
					++i;
				}
				if (t == Token.TEXT) {
					System.out.println(t + " '" + lexer.getLexem() + "'");
				} else if (t == Token.INT) {
					System.out.println(t + " " + lexer.getLong());
				} else {
					System.out.println(t);
				}
			}
			long stop = System.currentTimeMillis();
			System.out.println("Time: " + (stop - start) + "ms");
			System.out.println("n=" + n + ", s=" + s + ", i=" + i);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
