package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.impl.generic.GenericUtil;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class GenericUtilTest {
	
	// Create a schema and various domains for testing purposes
	private static Schema schema = new SchemaImpl("TempAttributeTest", "someprefix");
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
		simpleRecordDomain.addComponent("StringComponent", schema.getStringDomain());
		simpleRecordDomain.addComponent("EnumComponent", enumDomain);
		simpleRecordDomain.addComponent("SetComponent", simpleSetDomain);
		simpleRecordDomain.addComponent("ListComponent", simpleListDomain);
		simpleRecordDomain.addComponent("MapComponent", simpleMapDomain);
		complexSetDomain = schema.createSetDomain(simpleMapDomain);
		complexListDomain = schema.createListDomain(simpleMapDomain);
		complexMapDomain = schema.createMapDomain(simpleListDomain, simpleMapDomain);
		complexMapDomain2 = schema.createMapDomain(schema.getStringDomain(), simpleRecordDomain);
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
	private static String serializedDoubleDomainValue1 = Double.toString(Double.MIN_VALUE);
	private static String serializedDoubleDomainValue2 = Double.toString(Double.MAX_VALUE);
	private static String serializedDoubleDomainValue3 = Double.toString(42.123456);
	private static String serializedStringDomainEmptyValue = "\"\"";
	private static String serializedStringDomainValue1 = "\"abcd efgh +-#'*~<>(){}[]?!\"";
	private static String serializedEnumDomainValue1 = "FIRST";
	private static String serializedEnumDomainValue2 = "SECOND";
	private static String serializedSetDomainValue1 = "{t f}";	// simpleSetDomain
	private static String serializedSetDomainValue2 = "{{123 - 4561 321 - 6541} {243 - 4151 312 - 6451}}";	// complexSetDomain
	private static String serializedSetDomainEmptyValue = "{}";
	private static String serializedListDomainValue1 = "[\"some string\" \"another one\"]";	// simpleListDomain
	private static String serializedListDomainValue2 = "[{} {123 - 4561 321 - 6541} {243 - 4151 312 - 6451}]";	// complexListDomain
	private static String serializedListDomainEmptyValue = "[]";
	private static String serializedMapDomainValue1 = "{24 - 0 -1000 - 9876543210 14 - 1234567890}";	// simpleMapDomain
	private static String serializedMapDomainValue2 = "{[\"a\" \"b\" \"c\"] - {1 - 123456 42 - 435782} [\"d\" \"e\" \"f\"] - {} [] - {} [\"abc\"] - {73 - 192837 84 - 46565}}";	// complexMapDomain:   Map<List<String>, Map<Integer, Long>>
	private static String serializedMapDomainEmptyValue = "{}";
	private static String serializedRecordDomainValue1 = "(t 3.1 SECOND 42 [\"some string\" \"another one\"] 1016 {42 - 1016 1 - 39215 7 - 1234567890} {t f} \"somestring\")";	// simpleRecordDomain
//	private static String serializedMapDomainValue3 = "{\"key one\" - " + serializedRecordDomainValue1 + " \"key two\" - " + GraphIO.NULL_LITERAL + "}";	// complexMapDomain2: Map<String, SimpleRecordDomain>
	private static String serializedRecordDomainValue2 = "(" +				// complexRecordDomain
			"[{} {123 - 4561 321 - 6541} {243 - 4151 312 - 6451}] " +		//ListComponent: List<Map<Integer, Long>>
			"{[\"a\" \"b\" \"c\"] - {1 - 123456 42 - 435782 7 - 832 8 - 112345} [\"d\" \"e\" \"f\"] - {} [] - {73 - 192837 84 - 46565}} " +	// MapComponent: Map<List<String>, Map<Integer, Long>>
			serializedRecordDomainValue1 + " " +		// RecordComponent: *SimpleRecordDomain*
			"{{123 - 4561 321 - 6541} {243 - 4151 312 - 6451}}" +	// SetComponent: Set<Map<Integer, Long>>
			")";
//	private static String serializedRecordDomainValue3 = "(" + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + ")";	// complexRecordDomain - all components are null
	private static String serializedNullValue = GraphIO.NULL_LITERAL;
	
	// Values of various domains to test serializing and parsing
	private static Object nullValue = null;
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
	private static Object setDomainValue2 = JGraLab.set().plus(JGraLab.map().plus(123, 4561l).plus(321, 6541l)).plus(JGraLab.map().plus(243, 4151l).plus(312, 6451l));
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
	private static Object mapDomainEmptyValue = JGraLab.map();
	private static Object recordDomainValue1 = de.uni_koblenz.jgralab.impl.RecordImpl.empty()
			.plus("BoolComponent", true)
			.plus("DoubleComponent", 3.1)
			.plus("EnumComponent", "SECOND")
			.plus("IntComponent", 42)
			.plus("ListComponent", JGraLab.vector().plus("some string").plus("another one"))
			.plus("LongComponent", 1016l)
			.plus("MapComponent", JGraLab.map().plus(42, 1016l).plus(1, 39215l).plus(7, 1234567890l))
			.plus("SetComponent", JGraLab.set().plus(true).plus(false))
			.plus("StringComponent", "somestring");
//	private static Object mapDomainValue3 = JGraLab.map().plus("key one", recordDomainValue1).plus("key two", nullValue);	// TODO  "Can't add null to an ArrayPVector" Intended behavior?
	private static Object recordDomainValue2 = de.uni_koblenz.jgralab.impl.RecordImpl.empty()
			.plus("ListComponent", JGraLab.vector()
					.plus(JGraLab.map())
					.plus(JGraLab.map().plus(123, 4561l).plus(321, 6541l))
					.plus(JGraLab.map().plus(243, 4151l).plus(312, 6451l)))
			.plus("MapComponent", JGraLab.map()
					.plus(JGraLab.vector().plus("a").plus("b").plus("c"), JGraLab.map().plus(1, 123456l).plus(42, 435782l).plus(7, 832l).plus(8, 112345l))
					.plus(JGraLab.vector().plus("d").plus("e").plus("f"), JGraLab.map())
					.plus(JGraLab.vector(), JGraLab.map().plus(73, 192837l).plus(84, 46565l)))
			.plus("RecordComponent", recordDomainValue1)
			.plus("SetComponent", JGraLab.set()
					.plus(JGraLab.map().plus(123, 4561l).plus(321, 6541l))
					.plus(JGraLab.map().plus(243, 4151l).plus(312, 6451l)));
//	public static Object recordDomainValue3 = de.uni_koblenz.jgralab.impl.RecordImpl.empty()	// TODO Can't components in records be null? ArrayPVector prevents it!
//			.plus("ListComponent", null)
//			.plus("MapComponent", null)
//			.plus("RecordComponent", null)
//			.plus("SetComponent", null);
	
	
	@Test
	public void testSerializeAttribute() {
		try {
			GraphIO io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getBooleanDomain(), boolDomainValue1);
			assertEquals(serializedBoolDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getBooleanDomain(), boolDomainValue2);
			assertEquals(serializedBoolDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getIntegerDomain(), intDomainMaxValue);
			assertEquals(serializedIntDomainMaxValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getIntegerDomain(), intDomainMinValue);
			assertEquals(serializedIntDomainMinValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getIntegerDomain(), intDomainValue);
			assertEquals(serializedIntDomainValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getLongDomain(), longDomainMaxValue);
			assertEquals(serializedLongDomainMaxValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getLongDomain(), longDomainMinValue);
			assertEquals(serializedLongDomainMinValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getLongDomain(), longDomainValue);
			assertEquals(serializedLongDomainValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDoubleDomain(), doubleDomainValue1);
			assertEquals(serializedDoubleDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDoubleDomain(), doubleDomainValue2);
			assertEquals(serializedDoubleDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDoubleDomain(), doubleDomainValue3);
			assertEquals(serializedDoubleDomainValue3, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getStringDomain(), stringDomainValue1);
			assertEquals(serializedStringDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getStringDomain(), stringDomainEmpty);
			assertEquals(serializedStringDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDomain("TestEnumDomain"), enumDomainValue1);
			assertEquals(serializedEnumDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDomain("TestEnumDomain"), enumDomainValue2);
			assertEquals(serializedEnumDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleSetDomain, setDomainValue1);
			assertEquals(serializedSetDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexSetDomain, setDomainValue2);
			assertEquals(serializedSetDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleSetDomain, setDomainEmptyValue);
			assertEquals(serializedSetDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexSetDomain, setDomainEmptyValue);
			assertEquals(serializedSetDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleListDomain, listDomainValue1);
			assertEquals(serializedListDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexListDomain, listDomainValue2);
			assertEquals(serializedListDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleListDomain, listDomainEmptyValue);
			assertEquals(serializedListDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexListDomain, listDomainEmptyValue);
			assertEquals(serializedListDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleMapDomain, mapDomainValue1);
			assertEquals(serializedMapDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexMapDomain, mapDomainValue2);
			assertEquals(serializedMapDomainValue2, io.getStringWriterResult());
//			io = GraphIO.createStringWriter(schema);
//			GenericUtil.serializeGenericAttribute(io, complexMapDomain2, mapDomainValue3);
//			assertEquals(serializedMapDomainValue3, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleMapDomain, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexMapDomain, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexMapDomain2, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleRecordDomain, recordDomainValue1);
			assertEquals(serializedRecordDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexRecordDomain, recordDomainValue2);
			assertEquals(serializedRecordDomainValue2, io.getStringWriterResult());
			
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, schema.getDomain("TestEnumDomain"), nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleSetDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexSetDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleListDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexListDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleMapDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexMapDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexMapDomain2, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, simpleRecordDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			GenericUtil.serializeGenericAttribute(io, complexRecordDomain, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testParseAttribute() {
		// TODO
	}
	
	@Test
	public void testDomainConformity() {
		// TODO
	}
}
