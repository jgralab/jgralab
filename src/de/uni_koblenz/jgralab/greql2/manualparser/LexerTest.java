package de.uni_koblenz.jgralab.greql2.manualparser;

import junit.framework.TestCase;

import org.junit.Test;


public class LexerTest extends TestCase {

	@Test
	public void testLexer() {
		String query = "from v:V{MyVertexType} report v.attribute end";
		runLexer(query);
	}
	
	public void runLexer(String query) {
		ManualGreqlLexer lexer = new ManualGreqlLexer(query);
		while(lexer.hasNextToken()) {
			Token nextToken = lexer.getNextToken();
			System.out.print(nextToken.type.toString());
			if (nextToken instanceof ComplexToken) {
				System.out.print(" : " + ((ComplexToken)nextToken).value);
			}
			System.out.println();
		}
	}
	
	@Test
	public void testParserImports() {
		String query = "import a.b.c.D";
	}
	
}
