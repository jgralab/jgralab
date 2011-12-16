package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.impl.generic.GenericUtil;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class GenericUtilTest {
	
	// Create a schema and various domains for testing purposes
	private static Schema schema = new SchemaImpl("tempAttributeTest", "someprefix");
	private static EnumDomain enumDomain;				// EnumDomain TestEnumDomain ( FIRST, SECOND, THIRD );
	private static SetDomain simpleSetDomain;			// Set<Boolean>
	private static ListDomain simpleListDomain;			// List<String>
	private static MapDomain simpleMapDomain;			// Map<Integer, Long>
	private static RecordDomain simpleRecordDomain;		// RecordDomain SimpleRecordDomain (BoolComponent: Boolean, DoubleComponent: Double, EnumComponent: TestEnumDomain, IntComponent: Integer, ListComponent: List<String>, LongComponent: Long, MapComponent: Map<Integer, Long>, SetComponent: Set<Boolean>, StringComponent: String);
	private static SetDomain complexSetDomain;			// Set<Map<Integer, Long>>
	private static ListDomain complexListDomain;		// List<Map<Integer, Long>>
	private static MapDomain complexMapDomain;			// Map<List<String>, List<Map<Integer, Long>>>
	private static MapDomain complexMapDomain2;			// Map<String, *SimpleRecordDomain*>
	private static RecordDomain complexRecordDomain;	// RecordDomain ComplexRecordDomain (ListComponent: List<Map<Integer, Long>>, MapComponent: Map<List<String>, List<Map<Integer, Long>>>, RecordComponent: *SimpleRecordDomain*, SetComponent: Set<Map<Integer, Long>>);
	static {
		enumDomain = schema.createEnumDomain("TestEnumDomain");
		enumDomain.addConst("FIRST");
		enumDomain.addConst("SECOND");
		enumDomain.addConst("THIRD");
		simpleSetDomain = schema.createSetDomain(schema.getBooleanDomain());
		simpleListDomain = schema.createListDomain(schema.getStringDomain());
		simpleMapDomain = schema.createMapDomain(schema.getIntegerDomain(), schema.getLongDomain());
		simpleRecordDomain = schema.createRecordDomain("SimpleRecordDomain");
		simpleRecordDomain.addComponent("BoolComponent", schema.getBooleanDomain());
		simpleRecordDomain.addComponent("IntComponent", schema.getIntegerDomain());
		simpleRecordDomain.addComponent("LongComponent", schema.getLongDomain());
		simpleRecordDomain.addComponent("DoubleComponent", schema.getDoubleDomain());
		simpleRecordDomain.addComponent("EnumComponent", enumDomain);
		simpleRecordDomain.addComponent("SetComponent", simpleSetDomain);
		simpleRecordDomain.addComponent("ListComponent", simpleListDomain);
		simpleRecordDomain.addComponent("MapComponent", simpleMapDomain);
		complexSetDomain = schema.createSetDomain(simpleMapDomain);
		complexListDomain = schema.createListDomain(simpleMapDomain);
		complexMapDomain = schema.createMapDomain(simpleListDomain, simpleMapDomain);
		complexMapDomain2 = schema.createMapDomain(schema.getStringDomain(), simpleRecordDomain);	// TODO!
		complexRecordDomain = schema.createRecordDomain("ComplexRecordDomain");
		complexRecordDomain.addComponent("SetComponent", complexSetDomain);
		complexRecordDomain.addComponent("ListComponent", complexListDomain);
		complexRecordDomain.addComponent("MapComponent", complexMapDomain);
		complexRecordDomain.addComponent("RecordComponent", simpleRecordDomain);
	}

	// Serialized values to test serializing and parsing
	private static String serializedBoolDomainValue1 = "t";
	private static String serializedBoolDomainValue2 = "f";
	private static String serializedIntDomainMinValue = Integer.toString(Integer.MIN_VALUE);
	private static String serializedIntDomainMaxValue = Integer.toString(Integer.MAX_VALUE);
	private static String serializedIntDomainValue = "42";
	private static String serializedLongDomainMinValue = Long.toString(Long.MIN_VALUE);
	private static String serializedLongDomainMaxValue = Long.toString(Long.MAX_VALUE);
	private static String serializedLongDomainValue = "111";
	private static String serializedDoubleDomainValue1 = Double.toString(Double.MAX_VALUE);
	private static String serializedDoubleDomainValue2 = Double.toString(Double.MIN_VALUE);
	private static String serializedDoubleDomainValue3 = Double.toString(42.123456);
	private static String serializedStringDomainEmpty = "";
	private static String serializedStringDomainValue1 = "abcd efgh +-#'*~<>(){}[]?!";
	private static String serializedEnumDomainValue1 = "FIRST";
	private static String serializedEnumDomainValue2 = "SECOND";
	private static String serializedSetDomainValue1 = "{t f}";	// simpleSetDomain
	private static String serializedSetDomainValue2 = "{{123 - 4561 321 - 654l} {243 - 4151 312 - 6451}}";	// complexSetDomain
	private static String serializedSetDomainEmpty = "{}";
	private static String serializedListDomainValue1 = "[\"some string\" \"another one\"]";	// simpleListDomain
	private static String serializedListDomainValue2 = "[{} {123 - 4561 321 - 654l} {243 - 4151 312 - 6451}]";	// complexListDomain
	private static String serializedListDomainEmpty = "[]";
	private static String serializedMapDomainValue1 = "{24 - 0 -1000 - 9876543210 14 - 1234567890}";	// simpleMapDomain
	private static String serializedMapDomainValue2 = "{[\"a\" \"b\" \"c\"] - {1 - 123456 42 - 435782} [\"d\" \"e\" \"f\"] - {} [] - {} [\"abc\"] - {73 - 192837 84 - 46565}}";	// complexMapDomain:   Map<List<String>, Map<Integer, Long>>
	private static String serializedMapDomainValue3 = "";	// complexMapDomain2 TODO Map<String, SimpleRecordDomain>
	private static String serializedMapDomainEmpty = "{}";
	private static String serializedRecordDomainValue1 = "(t 3.1 SECOND 42 [\"some string\" \"another one\"] 1016 {42 - 1016 1 - 39215 7 - 1234567890} {t f} \"somestring\")";	// simpleRecordDomain
	private static String serializedRecordDomainValue2 = "(" +				// complexRecordDomain
			"[{} {123 - 4561 321 - 654l} {243 - 4151 312 - 6451}] " +		//ListComponent: List<Map<Integer, Long>>
			"{[\"a\" \"b\" \"c\"] - [{1 - 123456 42 - 435782} {7 - 832 8 - 11235}] [\"d\" \"e\" \"f\"] - [] [] - [{} {73 - 192837 84 - 46565}]} " +	// MapComponent: Map<List<String>, List<Map<Integer, Long>>>
			serializedRecordDomainValue1 + " " +		// RecordComponent: *SimpleRecordDomain*
			"{{123 - 4561 321 - 654l} {243 - 4151 312 - 6451}}" +	// SetComponent: Set<Map<Integer, Long>>
			")";
	private static String serializedRecordDomainValue3 = "(" + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + ")";	// complexRecordDomain - all components are null
	private static String serializedNullValue = GraphIO.NULL_LITERAL;
	
	// Values of various domains to test serializing and parsing
	private static Object boolDomainValue1 = new Boolean(true);
	private static Object boolDomainValue2 = new Boolean(false);
	private static Object intDomainMinValue = new Integer(Integer.MIN_VALUE);
	private static Object intDomainMaxValue = new Integer(Integer.MAX_VALUE);
	private static Object intDomainValue = new Integer(42);
	private static Object longDomainMinValue = new Long(Long.MIN_VALUE);
	private static Object longDomainMaxValue = new Long(Long.MAX_VALUE);
	private static Object longDomainValue = new Long(111);
	private static Object doubleDomainValue1 = new Double(Double.MIN_VALUE);
	private static Object doubleDomainValue2 = new Double(Double.MAX_VALUE);
	private static Object doubleDomainValue3 = new Double(42.123456);
	private static Object stringDomainEmpty = "";
	private static Object stringDomainValue1 = "abcd efgh +-#'*~<>(){}[]?!";
	private static Object enumDomainValue1 = "FIRST";
	private static Object enumDomainValue2 = "SECOND";
	private static Object setDomainValue1 = JGraLab.set().plus(true).plus(false);
	private static Object setDomainValue2 = JGraLab.set().plus(JGraLab.map().plus(123, 456l).plus(321, 654l)).plus(JGraLab.map().plus(243, 415l).plus(312, 645l));
	private static Object setDomainEmptyValue = JGraLab.set();
	private static Object listDomainValue1 = JGraLab.vector().plus("some string").plus("another one");
	private static Object listDomainValue2 = JGraLab.vector()
			.plus(JGraLab.map())
			.plus(JGraLab.map().plus(new Integer(123), new Long(4561)).plus(new Integer(321), new Long(6541)))
			.plus(JGraLab.map().plus(new Integer(243), new Long(4151)).plus(new Integer(312), new Long(6451)));
	private static Object listDomainEmptyValue = JGraLab.vector();
	private static Object mapDomainValue1 = JGraLab.map()
			.plus(new Integer(24), new Long(0))
			.plus(new Integer(-1000), new Long(9876543210l))
			.plus(new Integer(14), new Long(1234567890l));
	private static Object mapDomainValue2 = JGraLab.map()
			.plus(JGraLab.vector().plus("a").plus("b").plus("c"), JGraLab.map().plus(new Integer(1), new Long(123456)).plus(new Integer(42), new Long(435782))) // "{[\"a\" \"b\" \"c\"] - {1 - 123456 42 - 435782} [\"d\" \"e\" \"f\"] - {} [] - {} [\"abc\"] - {73 - 192837 84 - 46565}}
			.plus(JGraLab.vector().plus("d").plus("e").plus("f"), JGraLab.map()) // [\"d\" \"e\" \"f\"] - {}
			.plus(JGraLab.vector(), JGraLab.map())	// [] - {}	
			.plus(JGraLab.vector().plus("abc"), JGraLab.map().plus(new Integer(73), new Long(192837)).plus(new Integer(84), new Long(46565)));	// [\"abc\"] - {73 - 192837 84 - 46565}
	private static Object mapDomainVlaue3 = JGraLab.map();	// TODO
	private static Object mapDomainEmptyValue = JGraLab.map();
	private static Object recordDomainValue1;
	private static Object recordDomainValue2;
	private static Object nullValue = null;
	
	
	@Test
	public void testSerializeAttribute() {
		try {
			GenericUtil.serializeGenericAttribute(GraphIO.createStringWriter(schema), schema.getDomain("TestEnumDomain"), (Object) "FIRST");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testParseAttribute() {
		
	}
	
	@Test
	public void testDomainConformity() {
		// TODO
	}
}
