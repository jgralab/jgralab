package de.uni_koblenz.jgralabtest.greql2.funlib;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class StringFunctionTest extends GenericTests {

	private static final String RE_MATCH = "ReMatch";
	private static final String SPLIT = "Split";
	private static final String CONCAT = "Concat";
	private static final String CAPITALIZE_FIRST = "CapitalizeFirst";

	@Test
	public void testConcat1() throws Exception {
		assertQueryEquals(CONCAT, "\"foo\" ++ \"bar\" ++ \"baz\"", "foobarbaz");
		assertQueryEquals(CONCAT, "'foo' ++ 'bar' ++ 'baz'", "foobarbaz");
	}

	@Test
	public void testConcat2() throws Exception {
		assertQueryEquals(CONCAT, "'' ++ '' ++ ''", "");
	}

	@Test
	public void testConcat3() throws Exception {
		assertQueryEquals(CONCAT, "'\n'", "\n");
	}

	@Test
	public void testConcat4() throws Exception {
		assertQueryEquals(CONCAT, "'g' ++ 'g'", "gg");
	}

	@Test
	public void testCapitalizeFirst1() throws Exception {
		assertQueryEquals(CAPITALIZE_FIRST, "capitalizeFirst('foobarbaz')",
				"Foobarbaz");
	}

	@Test
	public void testCapitalizeFirst2() throws Exception {
		assertQueryEquals(CAPITALIZE_FIRST, "capitalizeFirst('foo bar baz')",
				"Foo bar baz");
	}

	@Test
	public void testCapitalizeFirst3() throws Exception {
		assertQueryEquals(CAPITALIZE_FIRST, "capitalizeFirst(' oobarbaz')",
				" Oobarbaz");
	}

	@Test
	public void testReMatch1() throws Exception {
		assertQueryEquals(RE_MATCH, "reMatch('aaabbbb', '[a]+[b]+')", true);
		assertQueryEquals(RE_MATCH, "'aaabbbb' =~ '[a]+[b]+'", true);
	}

	@Test
	public void testReMatch2() throws Exception {
		assertQueryEquals(RE_MATCH, "reMatch('aaa', '[a]+[b]+')", false);
		assertQueryEquals(RE_MATCH, "'aaa' =~ '[a]+[b]+'", false);
	}

	@Test
	public void testReMatch3() throws Exception {
		assertQueryEquals(RE_MATCH, "reMatch('aaabc', '[a]+[b]+')", false);
		assertQueryEquals(RE_MATCH, "'aaabc' =~ '[a]+[b]+'", false);
	}

	@Test
	public void testSplitt1() throws Exception {

		assertQueryEquals(SPLIT, "split('aaabc', '[a]+[b]+')",
				Arrays.asList("", "c"));
	}

	@Test
	public void testSplitt2() throws Exception {

		assertQueryEquals(SPLIT, "split('Eckhard-Großmann', '-')",
				Arrays.asList("Eckhard", "Großmann"));
	}

	@Test
	public void testSplitt3() throws Exception {
		assertQueryEquals(SPLIT, "split('aaa bc', ' ')",
				Arrays.asList("aaa", "bc"));
	}

	@Test
	public void testSplitt4() throws Exception {
		assertQueryEquals(SPLIT, "split('Software-Technik', '[-e]')",
				Arrays.asList("Softwar", "", "T", "chnik"));
	}

	@Test
	public void testSplitt5() throws Exception {
		assertQueryEquals(SPLIT, "split('JGraLab', '\\p{javaLowerCase}')",
				Arrays.asList("JG", "", "L"));
	}
}
