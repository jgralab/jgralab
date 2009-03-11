package de.uni_koblenz.jgralabtest.codegeneratortest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.codegenerator.ImportCodeSnippet;

public class CodeListTest{
	
	@Test
	public void testCodeList(){
		//only possible case using this constructor
		CodeList cl=new CodeList();
		assertEquals(0, cl.size());
		assertEquals("", cl.getCode());
	}
	
	@Test
	public void testCodeList2(){
		//border cases
		CodeList cl1=new CodeList();
		cl1=new CodeList(cl1);
		CodeList cl2=new CodeList(cl1);
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
		//CodeSnippets will be added and removed
		CodeSnippet cs1=new CodeSnippet("Schweden", "Norwegen", "Finnland");
		CodeSnippet cs2=new CodeSnippet("Dänemark", "Island");
		CodeSnippet cs3=new CodeSnippet("Großbritannien", "", "Wales", "England", "Schottland", "Nordirland");
		CodeList cl=new CodeList();
		cl.add(cs1);
		cl.add(cs2);
		cl.add(cs3);
		cl.remove(cs2);
		assertEquals("\tSchweden\n\tNorwegen\n\tFinnland\n\tGroßbritannien\n\t\n" +
				"\tWales\n\tEngland\n\tSchottland\n\tNordirland\n", cl.getCode());
		assertEquals(null, cs2.getParent());
		
		cl.remove(cs3);
		assertEquals("\tSchweden\n\tNorwegen\n\tFinnland\n", cl.getCode());
		assertEquals(null, cs3.getParent());
		
		cl.add(cs2);
		cl.remove(cs1);
		assertEquals("\tDänemark\n\tIsland\n", cl.getCode());
		assertEquals(null, cs1.getParent());
		
		cl.remove(cs2);
		assertEquals("", cl.getCode());
		
		//ImportCodeSnippets will be added and removed
		ImportCodeSnippet ics1=new ImportCodeSnippet();
		ics1.add("Westeuropa.Frankreich", "Südeuropa.Spanien", "Südeuropa.Portugal");
		ImportCodeSnippet ics2=new ImportCodeSnippet();
		ics2.add("Südeuropa.Italien", "Mitteleuropa.Österreich", "Mitteleuropa.Schweiz");
		cl.add(ics1);
		cl.add(ics2);
		cl.remove(ics1);
		assertEquals("\n\timport Mitteleuropa.Schweiz;\n\timport Mitteleuropa." +
				"Österreich;\n\t\n\timport Südeuropa.Italien;\n", cl.getCode());
		cl.remove(ics2);
		assertEquals("", cl.getCode());
		
		//CodeLists will be added and removed
		CodeList cl2=new CodeList();
		CodeList cl3=new CodeList();
		cl.add(ics1);
		cl2.add(cs1);
		cl3.add(cl);
		cl3.add(cl2);
		cl3.remove(cl);
		assertEquals("\t\tSchweden\n\t\tNorwegen\n\t\tFinnland\n", cl3.getCode());
		cl3.remove(cl2);
		assertEquals("", cl3.getCode());		
		
		//a mixture of all kinds of CodeBlocks will be added and removed
		cl2.add(ics1);
		cl2.add(cs2);
		cl2.add(cs1);
		cl2.remove(cs1);
		assertEquals("\n\timport Südeuropa.Portugal;\n\timport Südeuropa.Spanien;" +
				"\n\t\n\timport Westeuropa.Frankreich;\n\tDänemark\n\tIsland\n",
				cl2.getCode());
		
		cl2.remove(ics1);
		assertEquals("\tDänemark\n\tIsland\n", cl2.getCode());
		
		cl2.remove(cs2);
		assertEquals("", cl2.getCode());

		//border cases
		//tests if it works to remove sth. which is not part of the CodeList anymore
		cl.add(cs2);
		cl.remove(cs1);
		assertEquals("\tDänemark\n\tIsland\n", cl.getCode());
		assertEquals(null, cs1.getParent());
		
		cl2.clear();
		cl.add(ics1);
		cl3.add(cl);
		cl3.add(cl2);
		cl.remove(ics1);
		assertEquals("\t\tDänemark\n\t\tIsland\n", cl3.getCode());
		cl3.remove(cl);
		assertEquals("", cl3.getCode());
		cl3.remove(cl2);
		assertEquals("", cl3.getCode());
		
		cl.remove(cs2);
		assertEquals("", cl.getCode());
		assertEquals(null, cs1.getParent());
		
		cs1=new CodeSnippet();
		cs2=new CodeSnippet("");
		cs3=new CodeSnippet();
		cl.add(cs1);
		cl.add(cs3);
		cl.remove(cs1);
		assertEquals("", cl.getCode());
		assertEquals(null, cs1.getParent());
		
		cl.add(cs2);
		cl.remove(cs2);
		cl.remove(cs3);
		assertEquals("", cl.getCode());
		assertEquals(null, cs2.getParent());
		assertEquals(null, cs3.getParent());
		
		cs3=new CodeSnippet("Irland");
		cl.add(cs3);
		cl.remove(cs3);
		assertEquals(0, cl.size());
		assertEquals(null, cs3.getParent());
		
		cl.remove(cl);
		assertEquals(0, cl.size());
	}
	
	@Test
	public void testAdd(){
		//border cases
		CodeList cl1=new CodeList();
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
		
		ImportCodeSnippet ics1=new ImportCodeSnippet();
		cl1.clear();
		cl1.add(ics1);
		assertEquals("", cl1.getCode());
		
		CodeList cl2=new CodeList();
		ics1.add("Klassik.");
		cl2.add(cl1);
		assertEquals("\n\t\timport Klassik.;\n", cl2.getCode());
		cl1.clear();
		assertEquals("", cl2.getCode());
		
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
		cl2.clear();
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
		
		cl2.add(cl1);
		assertEquals("\tImpressionismus\n\tExpressionismus\n\tJugendstil\n\t" +
				"Klassizismus\n\tRenaissance\n\tPointillismus\n\t\n\tRomantik" +
				"\n\tRokoko\n\t\n\tRealismus\n\t\n\tBarock\n\tNeoklassizismus\n\t" +
				"\n\tSpätromantik\n", cl2.getCode());
		ics1.add("Epochen.Kunst");
		cl2.clear();
		cl2.add(ics1);
		assertEquals("\n\timport Epochen.Kunst;\n\t\n\timport Klassik.;\n", cl2.getCode());
	}
	
	@Test
	public void testAdd2(){
		//border cases
		CodeList cl1=new CodeList();
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
		
		CodeSnippet cs2=new CodeSnippet();
		CodeList cl3=new CodeList();
		ImportCodeSnippet ics=new ImportCodeSnippet();
		cl3.add(ics, 0);
		assertEquals("", cl3.getCode());

		ics.add("Epochen.Literatur");
		assertEquals("\n\timport Epochen.Literatur;\n", cl3.getCode());

		CodeList cl4=new CodeList();
		cl4.add(cl3,0);
		assertEquals("\n\t\timport Epochen.Literatur;\n", cl4.getCode());
		
		ics.add("Epochen.Kunst");
		cl4.add(ics,0);
		cl4.add(cs2,1);
		cs2.add("Neogotik");
		assertEquals("\n\timport Epochen.Kunst;\n\timport Epochen.Literatur;\n\t\t" +
				"Neogotik\n", cl4.getCode());
				
		//normal cases
		cs.add("Gotik");
		CodeList cl2=new CodeList();
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
		
		CodeSnippet cs3=new CodeSnippet("Biedermeier", "", "Manierismus");
		cl1.add(cs3, 1);
		assertEquals("\t\t\t\tSymbolismus\n\t\t\t\t\n\t\t\t\tNeue Sachlichkeit\n" +
				"\t\t\t\tRestauration\n\t\tBiedermeier\n\t\t\n\t\tManierismus\n", 
				cl1.getCode());
		assertEquals(cl1, cs3.getParent());
		
		ics.add("Epochen.Musik");
		cl1.clear();
		cl1.add(ics,2);
		assertEquals("\n\t\t\timport Epochen.Kunst;\n\t\t\timport Epochen.Literatur;" +
				"\n\t\t\timport Epochen.Musik;\n", cl1.getCode());
		
		cl2.clear();
		cl2.add(cl1,4);
		assertEquals("\n\t\t\t\t\t\t\t\timport Epochen.Kunst;\n\t\t\t\t\t\t\t\t" +
				"import Epochen.Literatur;\n\t\t\t\t\t\t\t\timport Epochen.Musik;\n",
				cl2.getCode());
	}
	
	@Test
	public void testAddNoIndent(){
		//normal cases
		CodeList cl1=new CodeList();
		CodeSnippet cs1=new CodeSnippet("Spanien", "Portugal", "", "Frankreich");
		CodeSnippet cs2=new CodeSnippet("", "Estland", "Litauen", "Lettland", "");
		CodeList cl2=new CodeList();
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
		
		ImportCodeSnippet ics=new ImportCodeSnippet();
		ics.add("Westeuropa.Benelux", "Benelux.Belgien");
		cl1.clear();
		cl1.addNoIndent(ics);
		assertEquals("\nimport Benelux.Belgien;\n\nimport Westeuropa.Benelux;\n", 
				cl1.getCode());
		cl2.clear();
		
		cl2.addNoIndent(cl1);
		assertEquals("\nimport Benelux.Belgien;\n\nimport Westeuropa.Benelux;\n",
			cl2.getCode());
		
		ImportCodeSnippet ics2=new ImportCodeSnippet();
		ics2.add("Benelux.Niederlande", "Benelux.Luxemburg");
		cl2.addNoIndent(ics2);
		assertEquals("\nimport Benelux.Belgien;\n\nimport Westeuropa.Benelux;\n\n" +
				"import Benelux.Luxemburg;\nimport Benelux.Niederlande;\n", cl2.getCode());
		
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
		
		ics.clear();
		cl1.clear();
		cl1.addNoIndent(ics);
		assertEquals("", cl1.getCode());
		
		ics.add("Osteuropa.BaltischeStaaten");
		assertEquals("\nimport Osteuropa.BaltischeStaaten;\n", cl1.getCode());
		
		cl2.clear();
		cl1.clear();
		cl2.addNoIndent(cl1);
		assertEquals("", cl2.getCode());
		
		cl1.addNoIndent(ics);
		assertEquals("\nimport Osteuropa.BaltischeStaaten;\n", cl1.getCode());
	}
	
	@Test
	public void testGetCode(){
		//normal cases
		CodeList cl1=new CodeList();
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
		CodeList cl2=new CodeList();
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
		//border cases
		CodeList cl=new CodeList();
		assertEquals("", cl.getCode());
		CodeSnippet cs=new CodeSnippet("");
		cl.add(cs);
		assertEquals("\t\n", cl.getCode());
		cs.add("Marokko");
		assertEquals("\t\n\tMarokko\n", cl.getCode());
		cl.clear();
		
		ImportCodeSnippet ics=new ImportCodeSnippet();
		cl.add(ics);
		assertEquals("", cl.getCode());
		ics.add("Afrika.");
		assertEquals("\n\timport Afrika.;\n", cl.getCode());
		
		//normal cases
		ics.add("Fluss.Afrika.Nil");
		assertEquals("\n\timport Afrika.;\n\t\n\timport Fluss.Afrika.Nil;\n", cl.getCode());
		ics.add("Land.Afrika.Südafrika", "Land.Afrika.Zaire", "Afrika.", "Land.Afrika.Ghana");
		assertEquals("\n\timport Afrika.;\n\t\n\timport Fluss.Afrika.Nil;\n\t\n\timport " +
				"Land.Afrika.Ghana;\n\timport Land.Afrika.Südafrika;\n\timport " +
				"Land.Afrika.Zaire;\n", cl.getCode());
		ics.add("Fluss.Afrika.Niger", "Land.Afrika.Elfenbeinküste", "Land.");
		assertEquals("\n\timport Afrika.;\n\t\n\timport Fluss.Afrika.Niger;\n\t" +
				"import Fluss.Afrika.Nil;\n\t\n\timport Land.;\n\timport " +
				"Land.Afrika.Elfenbeinküste;\n\timport Land.Afrika.Ghana;\n\t" +
				"import Land.Afrika.Südafrika;\n\timport Land.Afrika.Zaire;\n", 
				cl.getCode());
		cs.add("Ägypten", "Sudan");
		cl.add(cs);		
		assertEquals("\n\timport Afrika.;\n\t\n\timport Fluss.Afrika.Niger;\n\t" +
				"import Fluss.Afrika.Nil;\n\t\n\timport Land.;\n\timport " +
				"Land.Afrika.Elfenbeinküste;\n\timport Land.Afrika.Ghana;\n\t" +
				"import Land.Afrika.Südafrika;\n\timport Land.Afrika.Zaire;\n\t" +
				"\n\tMarokko\n\tÄgypten\n\tSudan\n", cl.getCode());
	}
	
	@Test
	public void testClear(){
		//border cases
		CodeList cl=new CodeList();
		cl.clear();
		assertEquals(0, cl.size());
		CodeSnippet cs=new CodeSnippet();
		cl.add(cs);
		cl.clear();
		assertEquals(0, cl.size());
		cs.add("Monday");
		cl.add(cs);
		cl.clear();
		assertEquals(0, cl.size());
		
		//normal cases
		cs.add("Tuesday", "Wednesday");
		cl.add(cs);
		cl.clear();
		assertEquals(0, cl.size());
		cs.add("Thursday", "Friday", "Saturday", "Sunday");
		cl.add(cs);
		cl.clear();
		assertEquals(0, cl.size());
	}
	
	@Test
	public void testSize(){
		//border cases
		CodeList cl=new CodeList();
		assertEquals(0, cl.size());
		cl=new CodeList(cl);
		assertEquals(0, cl.size());
		CodeList cl2=new CodeList();
		cl=new CodeList(cl2);
		assertEquals(0, cl.size());
		CodeSnippet cs1=new CodeSnippet("January");
		cl.add(cs1);
		assertEquals(1, cl.size());
		
		//normal cases
		cs1.add("February", "March", "April");
		cl.add(cs1);
		assertEquals(4, cl.size());
		cs1.add("May");
		assertEquals(5, cl.size());
		CodeSnippet cs2=new CodeSnippet("June", "July", "August");
		CodeSnippet cs3=new CodeSnippet("September", "October", "November", "December");
		cl.add(cs2);
		assertEquals(8, cl.size());
		cl.add(cs3);
		assertEquals(12, cl.size());
	}
	
	@Test
	public void testAddAndGetVariables(){
		//border cases
		CodeList cl=new CodeList();
		Map<String, String> testMap=new HashMap<String, String>();
		testMap.put(null,null);		
		cl.addVariables(testMap);
		assertEquals(null, cl.getVariable(null));
		testMap.put("", "");
		cl.addVariables(testMap);
		assertEquals("", cl.getVariable(""));
		testMap.put(null, "");
		cl.addVariables(testMap);
		assertEquals("", cl.getVariable(null));
		testMap.put("", null);
		cl.addVariables(testMap);
		assertEquals(null, cl.getVariable(""));
		testMap.put(null, "b");
		cl.addVariables(testMap);
		assertEquals("b", cl.getVariable(null));
		testMap.put("b", null);
		cl.addVariables(testMap);
		assertEquals(null, cl.getVariable("b"));
		testMap.put("", "b");
		cl.addVariables(testMap);
		assertEquals("b", cl.getVariable(""));
		assertEquals("*UNDEFINED:a*", cl.getVariable("a"));
		testMap.put("b", "");
		cl.addVariables(testMap);
		assertEquals("", cl.getVariable("b"));
		testMap.put("a", "Andorra");
		cl.addVariables(testMap);
		assertEquals("Andorra", cl.getVariable("a"));
		testMap.put("a", "Albanien");
		cl.addVariables(testMap);
		assertEquals("Albanien", cl.getVariable("a"));
		testMap.put(null,null);
		cl.addVariables(testMap);
		assertEquals(null, cl.getVariable(null));
		
		//tests some undefined keys
		assertEquals("*UNDEFINED:c*", cl.getVariable("c"));
		assertEquals("*UNDEFINED:z*", cl.getVariable("z"));
		
		//normal cases
		testMap.put("n", "Norwegen");
		cl.addVariables(testMap);
		assertEquals("Norwegen", cl.getVariable("n"));
		testMap.put("m", "Malaysia");
		cl.addVariables(testMap);
		assertEquals("Malaysia", cl.getVariable("m"));
		testMap.put("k", "Kenia");
		cl.addVariables(testMap);
		assertEquals("Kenia", cl.getVariable("k"));
		testMap.put("K", "Kanada");
		cl.addVariables(testMap);
		assertEquals("Kanada", cl.getVariable("K"));
		assertEquals("Kenia", cl.getVariable("k"));
	}
	
	@Test
	public void testSetVariable(){
		//border cases
		CodeList cl=new CodeList();
		cl.setVariable(null, null);
		assertEquals(null, cl.getVariable(null));
		cl.setVariable(null, "");
		assertEquals("", cl.getVariable(null));
		cl.setVariable("", null);
		assertEquals(null, cl.getVariable(""));
		cl.setVariable("", "");
		assertEquals("", cl.getVariable(""));
		cl.setVariable("y", "");
		assertEquals("", cl.getVariable("y"));
		cl.setVariable("x", null);
		assertEquals(null, cl.getVariable("x"));
		cl.setVariable(null, "w");
		assertEquals("w", cl.getVariable(null));
		cl.setVariable("", "q");
		assertEquals("q", cl.getVariable(""));
		
		//normal cases
		cl.setVariable("d", "Dänemark");
		assertEquals("Dänemark", cl.getVariable("d"));
		cl.setVariable("d", "Dominikanische Republik");
		assertEquals("Dominikanische Republik", cl.getVariable("d"));
		cl.setVariable("e", "Estland");
		assertEquals("Estland", cl.getVariable("e"));
		cl.setVariable("j", "Japan");
		assertEquals("Japan", cl.getVariable("j"));
		cl.setVariable("c", "China");
		assertEquals("China", cl.getVariable("c"));
	}
	
	@Test
	public void testGetParent(){
		CodeList cl1=new CodeList();
		assertEquals(null, cl1.getParent());
		
		CodeList cl2=new CodeList(cl1);
		assertEquals(cl1, cl2.getParent());
		
		CodeList cl3=new CodeList(cl2);
		assertEquals(cl2, cl3.getParent());
		assertEquals(cl1, cl3.getParent().getParent());
		assertEquals(null, cl3.getParent().getParent().getParent());

		cl1.setVariable("1", "cl1");
		cl2.setVariable("2", "cl2");
		cl3.setVariable("3", "cl3");
		cl2=new CodeList(cl3);
		cl2.setVariable("4", "");
		assertEquals(cl3, cl2.getParent());
		//tests things that are happening when a circle is created
		assertEquals("cl1", cl2.getParent().getParent().getVariable("1"));
		assertEquals("cl2", cl2.getParent().getParent().getVariable("2"));
		assertEquals("*UNDEFINED:3*", cl2.getParent().getParent().getVariable("3"));
		assertEquals("*UNDEFINED:4*", cl2.getParent().getParent().getVariable("4"));
		
	}
}
