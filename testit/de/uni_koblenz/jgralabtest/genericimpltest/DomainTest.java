/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.genericimpltest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class DomainTest {

	// Create a schema and various domains for testing purposes
	private static Schema schema = new SchemaImpl("TempAttributeTest",
			"testprefix");
	private static EnumDomain enumDomain; // EnumDomain TestEnumDomain ( FIRST,
											// SECOND, THIRD );
	private static SetDomain simpleSetDomain; // Set<Boolean>
	private static ListDomain simpleListDomain; // List<String>
	private static MapDomain simpleMapDomain; // Map<Integer, Long>
	private static RecordDomain simpleRecordDomain; // RecordDomain
													// SimpleRecordDomain
													// (BoolComponent: Boolean,
													// DoubleComponent: Double,
													// EnumComponent:
													// TestEnumDomain,
													// IntComponent: Integer,
													// ListComponent:
													// List<String>,
													// LongComponent: Long,
													// MapComponent:
													// Map<Integer, Long>,
													// SetComponent:
													// Set<Boolean>,
													// StringComponent: String);
	private static SetDomain complexSetDomain; // Set<Map<Integer, Long>>
	private static ListDomain complexListDomain; // List<Map<Integer, Long>>
	private static MapDomain complexMapDomain; // Map<List<String>,
												// List<Map<Integer, Long>>>
	private static MapDomain complexMapDomain2; // Map<String,
												// *SimpleRecordDomain*>
	private static RecordDomain complexRecordDomain; // RecordDomain
														// ComplexRecordDomain
														// (ListComponent:
														// List<Map<Integer,
														// Long>>, MapComponent:
														// Map<List<String>,
														// List<Map<Integer,
														// Long>>>,
														// RecordComponent:
														// *SimpleRecordDomain*,
														// SetComponent:
														// Set<Map<Integer,
														// Long>>);
	static {
		enumDomain = schema.createEnumDomain("TestEnumDomain");
		enumDomain.addConst("FIRST");
		enumDomain.addConst("SECOND");
		enumDomain.addConst("THIRD");
		simpleSetDomain = schema.createSetDomain(schema.getBooleanDomain());
		simpleListDomain = schema.createListDomain(schema.getStringDomain());
		simpleMapDomain = schema.createMapDomain(schema.getIntegerDomain(),
				schema.getLongDomain());
		simpleRecordDomain = schema.createRecordDomain("SimpleRecordDomain");
		simpleRecordDomain.addComponent("BoolComponent",
				schema.getBooleanDomain());
		simpleRecordDomain.addComponent("IntComponent",
				schema.getIntegerDomain());
		simpleRecordDomain
				.addComponent("LongComponent", schema.getLongDomain());
		simpleRecordDomain.addComponent("DoubleComponent",
				schema.getDoubleDomain());
		simpleRecordDomain.addComponent("StringComponent",
				schema.getStringDomain());
		simpleRecordDomain.addComponent("EnumComponent", enumDomain);
		simpleRecordDomain.addComponent("SetComponent", simpleSetDomain);
		simpleRecordDomain.addComponent("ListComponent", simpleListDomain);
		simpleRecordDomain.addComponent("MapComponent", simpleMapDomain);
		complexSetDomain = schema.createSetDomain(simpleMapDomain);
		complexListDomain = schema.createListDomain(simpleMapDomain);
		complexMapDomain = schema.createMapDomain(simpleListDomain,
				simpleMapDomain);
		complexMapDomain2 = schema.createMapDomain(schema.getStringDomain(),
				simpleRecordDomain);
		complexRecordDomain = schema.createRecordDomain("ComplexRecordDomain");
		complexRecordDomain.addComponent("SetComponent", complexSetDomain);
		complexRecordDomain.addComponent("ListComponent", complexListDomain);
		complexRecordDomain.addComponent("MapComponent", complexMapDomain);
		complexRecordDomain.addComponent("RecordComponent", simpleRecordDomain);
	}

	// Serialized values to test serializing and parsing
	private static String serializedBoolDomainValue1 = "t";
	private static String serializedBoolDomainValue2 = "f";
	private static String serializedIntDomainMinValue = Integer
			.toString(Integer.MIN_VALUE);
	private static String serializedIntDomainMaxValue = Integer
			.toString(Integer.MAX_VALUE);
	private static String serializedIntDomainValue = "42";
	private static String serializedLongDomainMinValue = Long
			.toString(Long.MIN_VALUE);
	private static String serializedLongDomainMaxValue = Long
			.toString(Long.MAX_VALUE);
	private static String serializedLongDomainValue = "111";
	private static String serializedDoubleDomainValue1 = Double
			.toString(Double.MIN_VALUE);
	private static String serializedDoubleDomainValue2 = Double
			.toString(Double.MAX_VALUE);
	private static String serializedDoubleDomainValue3 = Double
			.toString(42.123456);
	private static String serializedStringDomainEmptyValue = "\"\"";
	private static String serializedStringDomainValue1 = "\"abcd efgh +-#'*~<>(){}[]?!\"";
	private static String serializedEnumDomainValue1 = "FIRST";
	private static String serializedEnumDomainValue2 = "SECOND";
	private static String serializedSetDomainValue1 = "{t f}"; // simpleSetDomain
	private static String serializedSetDomainValue2 = "{{123 - 4561 321 - 6541}{243 - 4151 312 - 6451}}"; // complexSetDomain
	private static String serializedSetDomainEmptyValue = "{}";
	private static String serializedListDomainValue1 = "[\"some string\"\"another one\"]"; // simpleListDomain
	private static String serializedListDomainValue2 = "[{}{123 - 4561 321 - 6541}{243 - 4151 312 - 6451}]"; // complexListDomain
	private static String serializedListDomainEmptyValue = "[]";
	private static String serializedMapDomainValue1 = "{24 - 0 -1000 - 9876543210 14 - 1234567890}"; // simpleMapDomain
	private static String serializedMapDomainValue2 = "{[\"a\"\"b\"\"c\"]-{1 - 123456 42 - 435782}[\"d\"\"e\"\"f\"]-{}[]-{}[\"abc\"]-{73 - 192837 84 - 46565}}"; // complexMapDomain:
																																									// Map<List<String>,
																																									// Map<Integer,
																																									// Long>>
	private static String serializedMapDomainEmptyValue = "{}";
	private static String serializedRecordDomainValue1 = "(t 3.1 SECOND 42[\"some string\"\"another one\"]1016{42 - 1016 1 - 39215 7 - 1234567890}{t f}\"somestring\")"; // simpleRecordDomain
	private static String serializedRecordDomainValue3 = "(f 1.3 FIRST 24[\"some other string\"\"yet another one\"]123456789{1 - 987654321 2 - 39215 3 - 1234567890}{t f}\"secondString\")";
	private static String serializedMapDomainValue3 = "{\"key one\"-"
			+ serializedRecordDomainValue1 + "\"key two\"-"
			+ serializedRecordDomainValue3 + "}"; // complexMapDomain2:
													// Map<String,
													// SimpleRecordDomain>
	private static String serializedRecordDomainValue2 = "("
			+ // complexRecordDomain
			"[{}{123 - 4561 321 - 6541}{243 - 4151 312 - 6451}]"
			+ // ListComponent: List<Map<Integer, Long>>
			"{[\"a\"\"b\"\"c\"]-{1 - 123456 42 - 435782 7 - 832 8 - 112345}[\"d\"\"e\"\"f\"]-{}[]-{73 - 192837 84 - 46565}}"
			+ // MapComponent: Map<List<String>, Map<Integer, Long>>
			serializedRecordDomainValue1 + // RecordComponent:
											// *SimpleRecordDomain*
			"{{123 - 4561 321 - 6541}{243 - 4151 312 - 6451}}" + // SetComponent:
																	// Set<Map<Integer,
																	// Long>>
			")";
	private static String serializedRecordDomainValue4 = "("
			+ GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + " "
			+ GraphIO.NULL_LITERAL + " " + GraphIO.NULL_LITERAL + ")"; // complexRecordDomain
																		// - all
																		// components
																		// are
																		// null
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
	private static Object stringDomainEmptyValue = "";
	private static Object stringDomainValue1 = "abcd efgh +-#'*~<>(){}[]?!";
	private static Object enumDomainValue1 = "FIRST";
	private static Object enumDomainValue2 = "SECOND";
	private static Object setDomainValue1 = JGraLab.set().plus(true)
			.plus(false);
	private static Object setDomainValue2 = JGraLab.set()
			.plus(JGraLab.map().plus(123, 4561l).plus(321, 6541l))
			.plus(JGraLab.map().plus(243, 4151l).plus(312, 6451l));
	private static Object setDomainEmptyValue = JGraLab.set();
	private static Object listDomainValue1 = JGraLab.vector()
			.plus("some string").plus("another one");
	private static Object listDomainValue2 = JGraLab
			.vector()
			.plus(JGraLab.map())
			.plus(JGraLab.map().plus(new Integer(123), new Long(4561))
					.plus(new Integer(321), new Long(6541)))
			.plus(JGraLab.map().plus(new Integer(243), new Long(4151))
					.plus(new Integer(312), new Long(6451)));
	private static Object listDomainEmptyValue = JGraLab.vector();
	private static Object mapDomainValue1 = JGraLab.map()
			.plus(new Integer(24), new Long(0))
			.plus(new Integer(-1000), new Long(9876543210l))
			.plus(new Integer(14), new Long(1234567890l));
	private static Object mapDomainValue2 = JGraLab
			.map()
			.plus(JGraLab.vector().plus("a").plus("b").plus("c"),
					JGraLab.map().plus(new Integer(1), new Long(123456))
							.plus(new Integer(42), new Long(435782))) // "{[\"a\"
																		// \"b\"
																		// \"c\"]
																		// - {1
																		// -
																		// 123456
																		// 42 -
																		// 435782}
																		// [\"d\"
																		// \"e\"
																		// \"f\"]
																		// - {}
																		// [] -
																		// {}
																		// [\"abc\"]
																		// - {73
																		// -
																		// 192837
																		// 84 -
																		// 46565}}
			.plus(JGraLab.vector().plus("d").plus("e").plus("f"), JGraLab.map()) // [\"d\"
																					// \"e\"
																					// \"f\"]
																					// -
																					// {}
			.plus(JGraLab.vector(), JGraLab.map()) // [] - {}
			.plus(JGraLab.vector().plus("abc"),
					JGraLab.map().plus(new Integer(73), new Long(192837))
							.plus(new Integer(84), new Long(46565))); // [\"abc\"]
																		// - {73
																		// -
																		// 192837
																		// 84 -
																		// 46565}
	private static Object mapDomainEmptyValue = JGraLab.map();
	private static Object recordDomainValue1 = de.uni_koblenz.jgralab.impl.RecordImpl
			.empty()
			.plus("BoolComponent", true)
			.plus("DoubleComponent", 3.1)
			.plus("EnumComponent", "SECOND")
			.plus("IntComponent", 42)
			.plus("ListComponent",
					JGraLab.vector().plus("some string").plus("another one"))
			.plus("LongComponent", 1016l)
			.plus("MapComponent",
					JGraLab.map().plus(42, 1016l).plus(1, 39215l)
							.plus(7, 1234567890l))
			.plus("SetComponent", JGraLab.set().plus(true).plus(false))
			.plus("StringComponent", "somestring");
	private static Object recordDomainValue2 = de.uni_koblenz.jgralab.impl.RecordImpl
			.empty()
			.plus("ListComponent",
					JGraLab.vector()
							.plus(JGraLab.map())
							.plus(JGraLab.map().plus(123, 4561l)
									.plus(321, 6541l))
							.plus(JGraLab.map().plus(243, 4151l)
									.plus(312, 6451l)))
			.plus("MapComponent",
					JGraLab.map()
							.plus(JGraLab.vector().plus("a").plus("b")
									.plus("c"),
									JGraLab.map().plus(1, 123456l)
											.plus(42, 435782l).plus(7, 832l)
											.plus(8, 112345l))
							.plus(JGraLab.vector().plus("d").plus("e")
									.plus("f"), JGraLab.map())
							.plus(JGraLab.vector(),
									JGraLab.map().plus(73, 192837l)
											.plus(84, 46565l)))
			.plus("RecordComponent", recordDomainValue1)
			.plus("SetComponent",
					JGraLab.set()
							.plus(JGraLab.map().plus(123, 4561l)
									.plus(321, 6541l))
							.plus(JGraLab.map().plus(243, 4151l)
									.plus(312, 6451l)));
	private static Object recordDomainValue3 = de.uni_koblenz.jgralab.impl.RecordImpl
			.empty()
			.plus("BoolComponent", false)
			.plus("DoubleComponent", 1.3)
			.plus("EnumComponent", "FIRST")
			.plus("IntComponent", 24)
			.plus("ListComponent",
					JGraLab.vector().plus("some other string")
							.plus("yet another one"))
			.plus("LongComponent", 123456789l)
			.plus("MapComponent",
					JGraLab.map().plus(1, 987654321l).plus(2, 39215l)
							.plus(3, 1234567890l))
			.plus("SetComponent", JGraLab.set().plus(true).plus(false))
			.plus("StringComponent", "secondString");
	private static Object mapDomainValue3 = JGraLab.map()
			.plus("key one", recordDomainValue1)
			.plus("key two", recordDomainValue3);
	private static Object recordDomainValue4 = de.uni_koblenz.jgralab.impl.RecordImpl
			.empty().plus("ListComponent", null).plus("MapComponent", null)
			.plus("RecordComponent", null).plus("SetComponent", null);

	@Test
	public void testSerializeAttribute() {
		try {
			GraphIO io = GraphIO.createStringWriter(schema);
			schema.getBooleanDomain().serializeGenericAttribute(io,
					boolDomainValue1);
			assertEquals(serializedBoolDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getBooleanDomain().serializeGenericAttribute(io,
					boolDomainValue2);
			assertEquals(serializedBoolDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getIntegerDomain().serializeGenericAttribute(io,
					intDomainMaxValue);
			assertEquals(serializedIntDomainMaxValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getIntegerDomain().serializeGenericAttribute(io,
					intDomainMinValue);
			assertEquals(serializedIntDomainMinValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getIntegerDomain().serializeGenericAttribute(io,
					intDomainValue);
			assertEquals(serializedIntDomainValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getLongDomain().serializeGenericAttribute(io,
					longDomainMaxValue);
			assertEquals(serializedLongDomainMaxValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getLongDomain().serializeGenericAttribute(io,
					longDomainMinValue);
			assertEquals(serializedLongDomainMinValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getLongDomain().serializeGenericAttribute(io,
					longDomainValue);
			assertEquals(serializedLongDomainValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getDoubleDomain().serializeGenericAttribute(io,
					doubleDomainValue1);
			assertEquals(serializedDoubleDomainValue1,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getDoubleDomain().serializeGenericAttribute(io,
					doubleDomainValue2);
			assertEquals(serializedDoubleDomainValue2,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getDoubleDomain().serializeGenericAttribute(io,
					doubleDomainValue3);
			assertEquals(serializedDoubleDomainValue3,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getStringDomain().serializeGenericAttribute(io,
					stringDomainValue1);
			assertEquals(serializedStringDomainValue1,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			schema.getStringDomain().serializeGenericAttribute(io,
					stringDomainEmptyValue);
			assertEquals(serializedStringDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			enumDomain.serializeGenericAttribute(io, enumDomainValue1);
			assertEquals(serializedEnumDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			enumDomain.serializeGenericAttribute(io, enumDomainValue2);
			assertEquals(serializedEnumDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleSetDomain.serializeGenericAttribute(io, setDomainValue1);
			assertEquals(serializedSetDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexSetDomain.serializeGenericAttribute(io, setDomainValue2);
			assertEquals(serializedSetDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleSetDomain.serializeGenericAttribute(io, setDomainEmptyValue);
			assertEquals(serializedSetDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexSetDomain.serializeGenericAttribute(io, setDomainEmptyValue);
			assertEquals(serializedSetDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleListDomain.serializeGenericAttribute(io, listDomainValue1);
			assertEquals(serializedListDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexListDomain.serializeGenericAttribute(io, listDomainValue2);
			assertEquals(serializedListDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleListDomain
					.serializeGenericAttribute(io, listDomainEmptyValue);
			assertEquals(serializedListDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexListDomain.serializeGenericAttribute(io,
					listDomainEmptyValue);
			assertEquals(serializedListDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleMapDomain.serializeGenericAttribute(io, mapDomainValue1);
			assertEquals(serializedMapDomainValue1, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain.serializeGenericAttribute(io, mapDomainValue2);
			assertEquals(serializedMapDomainValue2, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain2.serializeGenericAttribute(io, mapDomainValue3);
			assertEquals(serializedMapDomainValue3, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleMapDomain.serializeGenericAttribute(io, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain.serializeGenericAttribute(io, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain2
					.serializeGenericAttribute(io, mapDomainEmptyValue);
			assertEquals(serializedMapDomainEmptyValue,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleRecordDomain
					.serializeGenericAttribute(io, recordDomainValue1);
			assertEquals(serializedRecordDomainValue1,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexRecordDomain.serializeGenericAttribute(io,
					recordDomainValue2);
			assertEquals(serializedRecordDomainValue2,
					io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexRecordDomain.serializeGenericAttribute(io,
					recordDomainValue4);
			assertEquals(serializedRecordDomainValue4,
					io.getStringWriterResult());

			io = GraphIO.createStringWriter(schema);
			enumDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleSetDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexSetDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleListDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexListDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleMapDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexMapDomain2.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			simpleRecordDomain.serializeGenericAttribute(io, nullValue);
			assertEquals(serializedNullValue, io.getStringWriterResult());
			io = GraphIO.createStringWriter(schema);
			complexRecordDomain.serializeGenericAttribute(io, nullValue);
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
		try {
			Object result = schema.getBooleanDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedBoolDomainValue1,
							schema));
			assertEquals(boolDomainValue1, result);
			result = schema.getBooleanDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedBoolDomainValue2,
							schema));
			assertEquals(boolDomainValue2, result);
			result = schema.getIntegerDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedIntDomainMinValue,
							schema));
			assertEquals(intDomainMinValue, result);
			result = schema.getIntegerDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedIntDomainMaxValue,
							schema));
			assertEquals(intDomainMaxValue, result);
			result = schema.getIntegerDomain()
					.parseGenericAttribute(
							GraphIO.createStringReader(
									serializedIntDomainValue, schema));
			assertEquals(intDomainValue, result);
			result = schema.getLongDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedLongDomainMaxValue,
							schema));
			assertEquals(longDomainMaxValue, result);
			result = schema.getLongDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedLongDomainMinValue,
							schema));
			assertEquals(longDomainMinValue, result);
			result = schema.getLongDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedLongDomainValue,
							schema));
			assertEquals(longDomainValue, result);
			result = schema.getDoubleDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedDoubleDomainValue1,
							schema));
			assertEquals(doubleDomainValue1, result);
			result = schema.getDoubleDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedDoubleDomainValue2,
							schema));
			assertEquals(doubleDomainValue2, result);
			result = schema.getDoubleDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedDoubleDomainValue3,
							schema));
			assertEquals(doubleDomainValue3, result);
			result = schema.getStringDomain().parseGenericAttribute(
					GraphIO.createStringReader(serializedStringDomainValue1,
							schema));
			assertEquals(stringDomainValue1, result);
			result = schema.getStringDomain().parseGenericAttribute(
					GraphIO.createStringReader(
							serializedStringDomainEmptyValue, schema));
			assertEquals(stringDomainEmptyValue, result);
			result = enumDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedEnumDomainValue1, schema));
			assertEquals(enumDomainValue1, result);
			result = enumDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedEnumDomainValue2, schema));
			assertEquals(enumDomainValue2, result);
			result = simpleSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedSetDomainValue1, schema));
			assertEquals(setDomainValue1, result);
			result = complexSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedSetDomainValue2, schema));
			assertEquals(setDomainValue2, result);
			result = simpleSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedSetDomainEmptyValue, schema));
			assertEquals(setDomainEmptyValue, result);
			result = complexSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedSetDomainEmptyValue, schema));
			assertEquals(setDomainEmptyValue, result);
			result = simpleListDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedListDomainValue1, schema));
			assertEquals(listDomainValue1, result);
			result = complexListDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedListDomainValue2, schema));
			assertEquals(listDomainValue2, result);
			result = simpleListDomain
					.parseGenericAttribute(GraphIO.createStringReader(
							serializedListDomainEmptyValue, schema));
			assertEquals(listDomainEmptyValue, result);
			result = complexListDomain
					.parseGenericAttribute(GraphIO.createStringReader(
							serializedListDomainEmptyValue, schema));
			assertEquals(listDomainEmptyValue, result);
			result = simpleMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedMapDomainValue1, schema));
			assertEquals(mapDomainValue1, result);
			result = complexMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedMapDomainValue2, schema));
			assertEquals(mapDomainValue2, result);
			result = complexMapDomain2.parseGenericAttribute(GraphIO
					.createStringReader(serializedMapDomainValue3, schema));
			assertEquals(mapDomainValue3, result);
			result = simpleMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedMapDomainEmptyValue, schema));
			assertEquals(mapDomainEmptyValue, result);
			result = complexMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedMapDomainEmptyValue, schema));
			assertEquals(mapDomainEmptyValue, result);
			result = simpleRecordDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedRecordDomainValue1, schema));
			assertEquals(recordDomainValue1, result);
			result = complexRecordDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedRecordDomainValue2, schema));
			assertEquals(recordDomainValue2, result);
			result = complexRecordDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedRecordDomainValue4, schema));
			assertEquals(recordDomainValue4, result);

			result = simpleSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = complexSetDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = simpleListDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = complexListDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = simpleMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = complexMapDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = complexMapDomain2.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = simpleRecordDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
			result = complexRecordDomain.parseGenericAttribute(GraphIO
					.createStringReader(serializedNullValue, schema));
			assertEquals(nullValue, result);
		} catch (GraphIOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgenericIsConform() {
		Domain[] testDomains = new Domain[] { schema.getBooleanDomain(),
				schema.getIntegerDomain(), schema.getLongDomain(),
				schema.getDoubleDomain(), schema.getStringDomain(), enumDomain,
				simpleSetDomain, complexSetDomain, simpleListDomain,
				complexListDomain, simpleMapDomain, complexMapDomain,
				complexMapDomain2, simpleRecordDomain, complexRecordDomain };

		for (int i = 0; i < testDomains.length; i++) {
			if (i == 0) {
				assertTrue(testDomains[i].isConformValue(boolDomainValue1));
				assertTrue(testDomains[i].isConformValue(boolDomainValue2));
			} else {
				assertFalse(testDomains[i].isConformValue(boolDomainValue1));
				assertFalse(testDomains[i].isConformValue(boolDomainValue2));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 1) {
				assertTrue(testDomains[i].isConformValue(intDomainMaxValue));
				assertTrue(testDomains[i].isConformValue(intDomainMinValue));
				assertTrue(testDomains[i].isConformValue(intDomainValue));
			} else {
				assertFalse(testDomains[i].isConformValue(intDomainMaxValue));
				assertFalse(testDomains[i].isConformValue(intDomainMinValue));
				assertFalse(testDomains[i].isConformValue(intDomainValue));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 2) {
				assertTrue(testDomains[i].isConformValue(longDomainMaxValue));
				assertTrue(testDomains[i].isConformValue(longDomainMinValue));
				assertTrue(testDomains[i].isConformValue(longDomainValue));
			} else {
				assertFalse(testDomains[i].isConformValue(longDomainMaxValue));
				assertFalse(testDomains[i].isConformValue(longDomainMinValue));
				assertFalse(testDomains[i].isConformValue(longDomainValue));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 3) {
				assertTrue(testDomains[i].isConformValue(doubleDomainValue1));
				assertTrue(testDomains[i].isConformValue(doubleDomainValue2));
				assertTrue(testDomains[i].isConformValue(doubleDomainValue3));
			} else {
				assertFalse(testDomains[i].isConformValue(doubleDomainValue1));
				assertFalse(testDomains[i].isConformValue(doubleDomainValue2));
				assertFalse(testDomains[i].isConformValue(doubleDomainValue3));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 4) {
				assertTrue(testDomains[i].isConformValue(stringDomainValue1));
				assertTrue(testDomains[i]
						.isConformValue(stringDomainEmptyValue));
			} else {
				assertFalse(testDomains[i].isConformValue(stringDomainValue1));
				assertFalse(testDomains[i]
						.isConformValue(stringDomainEmptyValue));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 4 || i == 5) { // Enum-values are represented by Strings in
									// the generic implementation
				assertTrue(testDomains[i].isConformValue(enumDomainValue1));
				assertTrue(testDomains[i].isConformValue(enumDomainValue2));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(enumDomainValue1));
				assertFalse(testDomains[i].isConformValue(enumDomainValue2));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 6) {
				assertTrue(testDomains[i].isConformValue(setDomainValue1));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(setDomainValue1));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 7) {
				assertTrue(testDomains[i].isConformValue(setDomainValue2));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(setDomainValue2));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 8) {
				assertTrue(testDomains[i].isConformValue(listDomainValue1));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(listDomainValue1));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 9) {
				assertTrue(testDomains[i].isConformValue(listDomainValue2));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(listDomainValue2));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 10) {
				assertTrue(testDomains[i].isConformValue(mapDomainValue1));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(mapDomainValue1));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 11) {
				assertTrue(testDomains[i].isConformValue(mapDomainValue2));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(mapDomainValue2));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 12) {
				assertTrue(testDomains[i].isConformValue(mapDomainValue3));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				assertFalse(testDomains[i].isConformValue(mapDomainValue3));
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 13) {
				assertTrue(testDomains[i].isConformValue(recordDomainValue1));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				try {
					assertFalse(testDomains[i]
							.isConformValue(recordDomainValue1));
				} catch (NoSuchAttributeException e) {
				} // Desired behavior, if a record has components it shouldn't
					// have.
			}
		}
		for (int i = 0; i < testDomains.length; i++) {
			if (i == 14) {
				assertTrue(testDomains[i].isConformValue(recordDomainValue2));
				assertTrue(testDomains[i].isConformValue(nullValue));
			} else {
				try {
					assertFalse(testDomains[i]
							.isConformValue(recordDomainValue2));
				} catch (NoSuchAttributeException e) {
				} // Desired behavior, if a record has components it shouldn't
					// have.
			}
		}
	}
}
