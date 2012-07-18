package de.uni_koblenz.jgralabtest.temporary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pcollections.ArrayPMap;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TemporaryEdge;
import de.uni_koblenz.jgralab.TemporaryGraphElementBlessingException;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.impl.RecordImpl;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.Date;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.Month;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteMap;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteSchema;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Street;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.CountyTags;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Town;

public class AttributeConversionTest {

	private RouteSchema schema;
	private RouteMap graph;

	@Before
	public void setUp() {
		schema = RouteSchema.instance();
		graph = schema.createRouteMap(ImplementationType.STANDARD);
	}

	@Test
	public void testConvertString() {
		TemporaryVertex v = graph.createTemporaryVertex();
		v.setAttribute("name", "Koblenz");
		Town town = (Town) v.bless(Town.VC);
		assertEquals("Koblenz", town.get_name());
		assertTrue(town.isValid());
		assertFalse(v.isValid());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertStringFail() {
		TemporaryVertex v = graph.createTemporaryVertex();
		v.setAttribute("name", 123);
		v.bless(Town.VC);
	}

	@Test
	public void testConvertInteger() {
		TemporaryVertex v = graph.createTemporaryVertex();
		v.setAttribute("inhabitants", 1234);
		Town town = (Town) v.bless(Town.VC);
		assertEquals(1234, town.get_inhabitants());
		assertTrue(town.isValid());
		assertFalse(v.isValid());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertIntegerFail() {
		TemporaryVertex v = graph.createTemporaryVertex();
		v.setAttribute("inhabitants", "1234");
		v.bless(Town.VC);
	}

	@Test
	public void testConvertDouble() {
		Crossroad v1 = graph.createCrossroad();
		Crossroad v2 = graph.createCrossroad();

		TemporaryEdge tempe = graph.createTemporaryEdge(v1, v2);
		tempe.setAttribute("length", 34.21);
		Street street = (Street) tempe.bless(Street.EC);
		assertEquals(34.21, street.get_length(), 0.0);
		assertTrue(street.isValid());
		assertFalse(tempe.isValid());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertDoubleFail() {
		Crossroad v1 = graph.createCrossroad();
		Crossroad v2 = graph.createCrossroad();

		TemporaryEdge tempe = graph.createTemporaryEdge(v1, v2);
		tempe.setAttribute("length", "hugo");
		tempe.bless(Street.EC);
	}

	@Test
	public void testConvertBoolean() {
		Crossroad v1 = graph.createCrossroad();
		Crossroad v2 = graph.createCrossroad();

		TemporaryEdge tempe = graph.createTemporaryEdge(v1, v2);
		tempe.setAttribute("oneway", true);
		Street street = (Street) tempe.bless(Street.EC);
		assertTrue(street.isValid());
		assertFalse(tempe.isValid());
		assertTrue(street.is_oneway());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertBooleanFail() {
		Crossroad v1 = graph.createCrossroad();
		Crossroad v2 = graph.createCrossroad();

		TemporaryEdge tempe = graph.createTemporaryEdge(v1, v2);
		tempe.setAttribute("oneway", 123);
		tempe.bless(Street.EC);
	}

	@Test
	public void testConvertMap() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		PMap<CountyTags, Double> map = ArrayPMap.empty();
		map = map.plus(CountyTags.AREA, 0.12);
		tempV.setAttribute("tags", map);
		County county = (County) tempV.bless(County.VC);
		assertEquals(map, county.get_tags());
		assertTrue(county.isValid());
		assertFalse(tempV.isValid());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertMapFail1() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		tempV.setAttribute("tags", "Hugo");
		tempV.bless(County.VC);
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertMapFail2() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		PMap<CountyTags, String> map = ArrayPMap.empty();
		map = map.plus(CountyTags.AREA, "0.12");
		tempV.setAttribute("tags", map);
		tempV.bless(County.VC);
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertMapFail3() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		PMap<String, Double> map = ArrayPMap.empty();
		map = map.plus("AREAS", 0.12);
		tempV.setAttribute("tags", map);
		tempV.bless(County.VC);
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertMapFail4() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		PMap<Month, Double> map = ArrayPMap.empty();
		map = map.plus(Month.MAY, 0.12);
		tempV.setAttribute("tags", map);
		County v = (County) tempV.bless(County.VC);
		assertTrue(v.isValid());
		assertFalse(tempV.isValid());
	}

	@Test
	public void testEnumAcceptString() {
		TemporaryVertex tempV = graph.createTemporaryVertex();
		PMap<String, Double> map = ArrayPMap.empty();
		map = map.plus("AREA", 0.12);
		tempV.setAttribute("tags", map);
		tempV.bless(County.VC);
	}

	@Test
	public void testConvertRecord() {
		TemporaryVertex v = graph.createTemporaryVertex();
		PMap<String, Object> componentValues = ArrayPMap.empty();
		componentValues = componentValues.plus("day", 2);
		componentValues = componentValues.plus("month", Month.JAN);
		componentValues = componentValues.plus("year", 2012);
		Date date = new Date(componentValues);
		v.setAttribute("foundingDate", date);
		Town town = (Town) v.bless(Town.VC);
		assertEquals(date, town.get_foundingDate());
		assertTrue(town.isValid());
		assertFalse(v.isValid());
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertRecordFail1() {
		TemporaryVertex v = graph.createTemporaryVertex();
		v.setAttribute("foundingDate", "date");
		v.bless(Town.VC);
	}

	@Test(expected = TemporaryGraphElementBlessingException.class)
	public void testConvertRecordFail2() {
		TemporaryVertex v = graph.createTemporaryVertex();
		PMap<String, Object> componentValues = ArrayPMap.empty();
		componentValues = componentValues.plus("days", 2);
		componentValues = componentValues.plus("month", Month.JAN);
		componentValues = componentValues.plus("year", 2012);
		RecordImpl record = RecordImpl.empty();
		for (RecordComponent c : ((RecordDomain) schema.getDomain("Date"))
				.getComponents()) {
			record = record.plus(c.getName(), componentValues.get(c.getName()));
		}

		v.setAttribute("foundingDate", record);
		v.bless(Town.VC);
	}
}
