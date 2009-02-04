package de.uni_koblenz.jgralabtest.codegeneratortest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Assert;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

//TODO überall wo CodeBlock übergibt, auch anderes als nur CodeSnippet bzw. 
// CodeSnippet und CodeList mal übergeben & dafür Testfälle

public class CodeListTest extends CodeBlockTest{
	
	private CodeList cl1;
	private CodeList cl2;
	
	@Before
	public void init(){
		super.init();
	}
	
	@Test
	public void testCodeList(){
		//only possible case using this constructor
		cl1=new CodeList();
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
	}
	
	@Test
	public void testCodeList2(){
		//border cases
		cl1=new CodeList();
		cl1=new CodeList(cl1);
		cl2=new CodeList(cl2);
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
		assertEquals(0, cl2.size());
		assertEquals("", cl2.getCode());
		cl2=new CodeList(null);
		assertEquals(0, cl2.size());
		
		//normal cases
		CodeSnippet cs=new CodeSnippet("Englisch", "Französisch");
		cl1.add(cs);
		cl2=new CodeList(cl1);
		//no matter what cl1 contains, the usage of the constructor will always result
		//in an empty CodeList => getSize() will always return 0, getCode always an empty
		//String
		assertEquals(0, cl2.size());
		assertEquals("", cl2.getCode());
		cs.add("Spanisch", "Russisch", "Finnisch");
		cl1.add(cs);
		cl2=new CodeList(cl1);
		assertEquals(0, cl2.size());
		cs.clear();
		cl1.clear();
		cs.add("Italienisch", "Griechisch");
		cl1.add(cs);
		cl2=new CodeList(cl1);
		assertEquals(0, cl2.size());
		cs.add("Chinesisch", "Japanisch", "Koreanisch", "Schwedisch");
		cl1.add(cs);
		cl2=new CodeList(cl1);
		assertEquals(0, cl2.size());
	}
	
	@Test
	public void testRemove(){
		//normal cases
		CodeSnippet cs1=new CodeSnippet("Schweden", "Norwegen", "Finnland");
		CodeSnippet cs2=new CodeSnippet("Dänemark", "Island");
		CodeSnippet cs3=new CodeSnippet("Großbritannien", "", "Wales", "England", "Schottland", "Nordirland");
		cl1=new CodeList();
		cl1.add(cs1);
		cl1.add(cs2);
		cl1.add(cs3);
		cl1.remove(cs2);
		assertEquals("\tSchweden\n\tNorwegen\n\tFinnland\n\tGroßbritannien\n\t\n" +
				"\tWales\n\tEngland\n\tSchottland\n\tNordirland\n", cl1.getCode());
		assertEquals(null, cs2.getParent());
		cl1.remove(cs3);
		assertEquals("\tSchweden\n\tNorwegen\n\tFinnland\n", cl1.getCode());
		assertEquals(null, cs3.getParent());
		cl1.add(cs2);
		cl1.remove(cs1);
		assertEquals("\tDänemark\n\tIsland\n", cl1.getCode());
		assertEquals(null, cs1.getParent());
		
		//border cases
		
		//tests if it works to remove sth. which is not part of the CodeList anymore
		cl1.remove(cs1);
		assertEquals("\tDänemark\n\tIsland\n", cl1.getCode());
		assertEquals(null, cs1.getParent());
		cl1.remove(cs2);
		assertEquals("", cl1.getCode());
		assertEquals(null, cs1.getParent());
		cs1=new CodeSnippet();
		cs2=new CodeSnippet("");
		cs3=new CodeSnippet();
		cl1.add(cs1);
		cl1.add(cs3);
		cl1.remove(cs1);
		assertEquals("", cl1.getCode());
		assertEquals(null, cs1.getParent());
		cl1.add(cs2);
		cl1.remove(cs2);
		cl1.remove(cs3);
		assertEquals("", cl1.getCode());
		assertEquals(null, cs2.getParent());
		assertEquals(null, cs3.getParent());
		
		cs3=new CodeSnippet("Irland");
		cl1.add(cs3);
		cl1.remove(cs3);
		assertEquals(0, cl1.size());
		assertEquals(null, cs3.getParent());
		cl1.remove(cl1);
		assertEquals(0, cl1.size());
	}
	
	@Test
	public void testAdd(){
		//border cases
		cl1=new CodeList();
		cl1.add(null);
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
		CodeSnippet cs=new CodeSnippet("");
		cl1.add(cs);
		assertEquals(1, cl1.size());
		assertEquals("\t\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		cs.clear();
		cs.add("Impressionismus");
		cl1.add(cs);
		assertEquals(1, cl1.size());
		assertEquals("\tImpressionismus\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		CodeSnippet cs2=new CodeSnippet();
		cl1.add(cs2);
		assertEquals("\tImpressionismus\n", cl1.getCode());
		assertEquals(cl1, cs2.getParent());
		cs2.add("");
		cl1.add(cs2);
		assertEquals("\tImpressionismus\n\t\n", cl1.getCode());
		assertEquals(cl1, cs2.getParent());
		
		//normal cases
		cl1.clear();
		cs.add("Expressionismus", "Jugendstil");
		cl1.add(cs);
		assertEquals(3, cl1.size());
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		cs.add("Klassizismus", "Renaissance", "Pointillismus");
		cl1.add(cs);
		assertEquals(6, cl1.size());
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n" +
				"\tKlassizismus\n\tRenaissance\n\tPointillismus\n", cl1.getCode());
		cs.add("", "Romantik", "Rokoko", "");
		assertEquals(cl1, cs.getParent());
		cl1.add(cs);
		assertEquals(10, cl1.size());		
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n" +
				"\tKlassizismus\n\tRenaissance\n\tPointillismus\n\t\n\tRomantik\n" +
				"\tRokoko\n\t\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		cl2=new CodeList();
		cs.add("Realismus", "");
		cl2.add(cs);
		assertEquals(12, cl2.size());		
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n" +
				"\tKlassizismus\n\tRenaissance\n\tPointillismus\n\t\n\tRomantik\n" +
				"\tRokoko\n\t\n\tRealismus\n\t\n", cl2.getCode());
		assertEquals(cl2, cs.getParent());
		cs2.clear();
		cs2.add("Barock", "Neoklassizismus", "", "Spätromantik");
		cl2.add(cs2);
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n" +
				"\tKlassizismus\n\tRenaissance\n\tPointillismus\n\t\n\tRomantik\n" +
				"\tRokoko\n\t\n\tRealismus\n\t\n\tBarock\n\tNeoklassizismus\n\t\n" +
				"\tSpätromantik\n", cl2.getCode());
		assertEquals(cl2, cs2.getParent());
	}
	
	@Test
	public void testAdd2(){
		//border cases
		cl1=new CodeList();
		cl1.add(null, 0);
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
		cl1.add(null, -4);
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
		CodeSnippet cs=new CodeSnippet();
		cl1.add(cs, -1);
		assertEquals(0, cl1.size());
		assertEquals("", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		cs.add("");
		cl1.add(cs, 1);
		assertEquals(1, cl1.size());
		assertEquals("\t\t\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		
		//normal cases
		cs.add("Gotik");
		cl2=new CodeList();
		cl2.add(cs, 7);
		assertEquals("\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\tGotik\n", cl2.getCode());
		assertEquals(cl2, cs.getParent());
		cs.add("Kubismus", "", "Moderne", "Postmoderne", "");
		cl2.add(cs, 2);
		assertEquals("\t\t\t\n\t\t\tGotik\n\t\t\tKubismus\n\t\t\t\n\t\t\tModerne\n" +
				"\t\t\tPostmoderne\n\t\t\t\n", cl2.getCode());
		assertEquals(cl2, cs.getParent());
		cs.clear();
		cs.add("Symbolismus", "", "Neue Sachlichkeit", "Restauration");
		cl1.clear();
		cl1.add(cs, 3);
		assertEquals("\t\t\t\tSymbolismus\n\t\t\t\t\n\t\t\t\tNeue Sachlichkeit\n" +
				"\t\t\t\tRestauration\n", cl1.getCode());
		assertEquals(cl1, cs.getParent());
		CodeSnippet cs2=new CodeSnippet("Biedermeier", "", "Manierismus");
		cl1.add(cs2, 1);
		assertEquals("\t\t\t\tSymbolismus\n\t\t\t\t\n\t\t\t\tNeue Sachlichkeit\n" +
				"\t\t\t\tRestauration\n\t\tBiedermeier\n\t\t\n\t\tManierismus\n", 
				cl1.getCode());
		assertEquals(cl1, cs2.getParent());
	}
	
	@Test
	public void testAddNoIndent(){
		//normal cases
		cl1=new CodeList();
		CodeSnippet cs1=new CodeSnippet("Spanien", "Portugal", "", "Frankreich");
		CodeSnippet cs2=new CodeSnippet("", "Estland", "Litauen", "Lettland", "");
		cl2=new CodeList();
		cl1.addNoIndent(cs1);
		assertEquals("Spanien\nPortugal\n\nFrankreich\n", cl1.getCode());
		cl1.addNoIndent(cs2);
		assertEquals("Spanien\nPortugal\n\nFrankreich\n\nEstland\nLitauen\n" +
				"Lettland\n\n", cl1.getCode());
		cl2.addNoIndent(cl1);
		assertEquals("Spanien\nPortugal\n\nFrankreich\n\nEstland\nLitauen\n" +
				"Lettland\n\n", cl2.getCode());
		cl2.addNoIndent(cl1);
		assertEquals("Spanien\nPortugal\n\nFrankreich\n\nEstland\nLitauen\n" +
				"Lettland\n\n", cl2.getCode());
		cl2.addNoIndent(cs1);
		assertEquals("\nEstland\nLitauen\nLettland\n\nSpanien\nPortugal\n\n" +
				"Frankreich\n", cl2.getCode());
		
		//border cases
		cl1.clear();
		cl2.clear();
		cl1.addNoIndent(null);
		assertEquals("", cl1.getCode());
		cl1.addNoIndent(cl2);
		assertEquals("", cl1.getCode());
		cs1.clear();
		cs1.add("");
		cl1.addNoIndent(cs1);
		assertEquals("\n", cl1.getCode());
	}
	
	@Test
	public void testGetCode(){
		//normal cases
		cl1=new CodeList();
		CodeSnippet cs=new CodeSnippet("Niederlande", "Belgien", "Luxemburg");
		cl1.add(cs);
		assertEquals("\t\t\t\t\t\t\tNiederlande\n\t\t\t\t\t\t\tBelgien\n" +
				"\t\t\t\t\t\t\tLuxemburg\n", cl1.getCode(6));
		CodeSnippet cs2=new CodeSnippet("Schweiz", "", "Liechtenstein", "Österreich");
		cl1.add(cs2, 3);
		assertEquals("\t\t\tNiederlande\n\t\t\tBelgien\n\t\t\tLuxemburg\n" +
				"\t\t\t\t\t\tSchweiz\n\t\t\t\t\t\t\n\t\t\t\t\t\tLiechtenstein\n" +
				"\t\t\t\t\t\tÖsterreich\n", cl1.getCode(2));
		cs.add("Andorra", "Monaco", "");
		cl1.add(cs);
		assertEquals("\t\t\t\t\t\t\t\tSchweiz\n\t\t\t\t\t\t\t\t\n" +
				"\t\t\t\t\t\t\t\tLiechtenstein\n\t\t\t\t\t\t\t\tÖsterreich\n" +
				"\t\t\t\t\tNiederlande\n\t\t\t\t\tBelgien\n\t\t\t\t\tLuxemburg\n" +
				"\t\t\t\t\tAndorra\n\t\t\t\t\tMonaco\n\t\t\t\t\t\n", cl1.getCode(4));
		cs.clear();
		cs.add("Griechenland", "Moldawien");
		cl1.clear();
		cl1.add(cs);
		assertEquals("\t\t\t\t\t\tGriechenland\n\t\t\t\t\t\tMoldawien\n", cl1.getCode(5));
		cl2=new CodeList();
		cl2.add(cl1);
		assertEquals("\t\t\t\t\t\t\t\t\t\t\tGriechenland\n\t\t\t\t\t\t\t\t\t\t\tMoldawien\n", cl2.getCode(9));		
		
		//border cases
		cs2.clear();
		cl1.clear();
		cl1.add(cs2);
		assertEquals("", cl1.getCode(1));
		cs2.add("");
		cl1.add(cs2);
		assertEquals("\t\t\n", cl1.getCode(1));
		assertEquals("\t\n", cl1.getCode(0));
		assertEquals("\n", cl1.getCode(-1));
		assertEquals("\n", cl1.getCode(-104));
		cs.clear();
		cs.add("Ungarn");
		cl1.add(cs);
		assertEquals("\t\t\n\t\tUngarn\n", cl1.getCode(1));
		assertEquals("\nUngarn\n", cl1.getCode(-42));
	}
	
	@Test
	//tests the inherited getCode()-method
	public void testGetCode2(){
		
	}
	
	@Test
	public void testClear(){
		//border cases
		cl1=new CodeList();
		cl1.clear();
		assertEquals(0, cl1.size());
		CodeSnippet cs=new CodeSnippet();
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		cs.add("Monday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		
		//normal cases
		cs.add("Tuesday", "Wednesday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
		cs.add("Thursday", "Friday", "Saturday", "Sunday");
		cl1.add(cs);
		cl1.clear();
		assertEquals(0, cl1.size());
	}
	
	@Test
	public void testSize(){
		//border cases
		cl1=new CodeList();
		assertEquals(0, cl1.size());
		cl1=new CodeList(cl1);
		assertEquals(0, cl1.size());
		cl2=new CodeList();
		cl1=new CodeList(cl2);
		assertEquals(0, cl1.size());
		CodeSnippet cs1=new CodeSnippet("January");
		cl1.add(cs1);
		assertEquals(1, cl1.size());
		
		//normal cases
		cs1.add("February", "March", "April");
		cl1.add(cs1);
		assertEquals(4, cl1.size());
		cs1.add("May");
		assertEquals(5, cl1.size());
		CodeSnippet cs2=new CodeSnippet("June", "July", "August");
		CodeSnippet cs3=new CodeSnippet("September", "October", "November", "December");
		cl1.add(cs2);
		assertEquals(8, cl1.size());
		cl1.add(cs3);
		assertEquals(12, cl1.size());
	}
	
	@Test
	public void testAddVariables(){
		
	}
	
	@Test
	public void testSetVariable(){
		
	}
	
	@Test
	public void testGetVariable(){
		
	}
	
	@Test
	public void testReplaceVariables(){
		
	}
	
	@Test
	public void testGetParent(){
		
	}
	
	@Test
	public void testSetParent(){
		
	}
}
