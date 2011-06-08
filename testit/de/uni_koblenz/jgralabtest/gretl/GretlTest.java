/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.gretl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.SerializableGreql2Impl;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Schema;
import de.uni_koblenz.jgralab.gretl.AddSuperClass;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.CopyTransformation;
import de.uni_koblenz.jgralab.gretl.CreateVertexClass;
import de.uni_koblenz.jgralab.gretl.ExecuteTransformation;
import de.uni_koblenz.jgralab.gretl.MatchReplace;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.schemacompare.SchemaCompare;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralabtest.gretl.schemas.addressbook.AddressBook;
import de.uni_koblenz.jgralabtest.gretl.schemas.addressbook.AddressBookGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.addressbook.AddressBookSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.addressbook.Contact;
import de.uni_koblenz.jgralabtest.gretl.schemas.bedsl.BedslGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.bedsl.BedslSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.bedsl.Entity;
import de.uni_koblenz.jgralabtest.gretl.schemas.bedsl.ReferenceAttribute;
import de.uni_koblenz.jgralabtest.gretl.schemas.bedsl.SimpleAttribute;
import de.uni_koblenz.jgralabtest.gretl.schemas.copy.CopyGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.copy.CopySchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.copy.Part;
import de.uni_koblenz.jgralabtest.gretl.schemas.copy.Whole;
import de.uni_koblenz.jgralabtest.gretl.schemas.copy.WholePart;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.Family;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.FamilyGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.FamilySchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.HasFather;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.HasMother;
import de.uni_koblenz.jgralabtest.gretl.schemas.families.Member;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.Card;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.Chassis;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.Configuration;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.PddslGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.PddslSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.pddsl.Slot;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simplerdbms.SimpleRDBMSSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Association;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.PrimitiveDataType;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.SimpleUMLGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.SimpleUMLSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.BasicService;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.ComposedService;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.Database;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.ProcessService;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.ServiceGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.services.ServiceSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.varro.UMLGraph;
import de.uni_koblenz.jgralabtest.gretl.schemas.varro.UMLSchema;
import de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.Assoc;
import de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.AssocEnd;
import de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.Attribute;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class GretlTest {
	static {
		JGraLab.setLogLevel(Level.OFF);
		Logger logger = JGraLab.getLogger(Transformation.class.getPackage()
				.getName());
		logger.setLevel(Level.INFO);

		Greql2Schema
				.instance()
				.getGraphFactory()
				.setGraphImplementationClass(Greql2.class,
						SerializableGreql2Impl.class);
	}

	public static void main(String[] args) {
		JUnitCore.runClasses(GretlTest.class);
	}

	private static Graph sourceAddressBookGraph = null;
	private static Graph sourceFamilyGraph = null;
	private static Graph sourceVarroUMLGraph = null;
	private static Graph sourceBEDSLGraph = null;
	private static Graph sourcePDDSLGraph = null;
	private static Graph sourceServiceGraph = null;
	private static Graph sourceCopyGraph = null;
	private static SimpleUMLGraph sourceSimpleUMLGraph = null;
	private static String tmpDir;

	private String targetFileName;
	private Context context = null;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUpClass() throws GraphIOException, IOException {
		tmpDir = null;
		try {
			tmpDir = File.createTempFile("test", null).getParentFile()
					.getAbsolutePath()
					+ File.separator;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (tmpDir == null) {
			tmpDir = "/tmp/";
		}

		initAddressBookGraph();
		initFamilyGraph();
		initVarroUMLGraph();
		initBedslAndPddslGraphs();
		initServiceGraph();
		initCopyGraph();
		initSimpleUMLGraph();

		dotty(sourceAddressBookGraph, tmpDir + "sourceAddressBookGraph.pdf");
		dotty(sourceFamilyGraph, tmpDir + "sourceFamilyGraph.pdf",
				HasMother.class, HasFather.class);
		dotty(sourceVarroUMLGraph, tmpDir + "sourceVarroUMLGraph.pdf");
		dotty(sourceBEDSLGraph, tmpDir + "sourceBEDSLGraph.pdf");
		dotty(sourcePDDSLGraph, tmpDir + "sourcePDDSLGraph.pdf");
		dotty(sourceServiceGraph, tmpDir + "sourceServiceGraph.pdf");
		dotty(sourceCopyGraph, tmpDir + "sourceCopyGraph.pdf");
		dotty(sourceSimpleUMLGraph, tmpDir + "sourceSimpleUMLGraph.pdf");
	}

	@Before
	public void setUp() {
		// We use that Graph in in-place transforms, so recreate it between
		// tests
		initFamilyGraph();
	}

	private static void initSimpleUMLGraph() {
		SimpleUMLGraph g = SimpleUMLSchema.instance().createSimpleUMLGraph();

		// data types...
		PrimitiveDataType string = g.createPrimitiveDataType();
		string.set_name("string");

		PrimitiveDataType doub = g.createPrimitiveDataType();
		doub.set_name("double");

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class clsGeoLoc = g
				.createClass();
		clsGeoLoc.set_name("GeoLoc");

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrGeoLocLat = g
				.createAttribute();
		attrGeoLocLat.set_name("lat");
		g.createHasType(attrGeoLocLat, doub);
		g.createHasAttribute(clsGeoLoc, attrGeoLocLat);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrGeoLocLon = g
				.createAttribute();
		attrGeoLocLon.set_name("lon");
		g.createHasType(attrGeoLocLon, doub);
		g.createHasAttribute(clsGeoLoc, attrGeoLocLon);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class clsAddress = g
				.createClass();
		clsAddress.set_name("Address");

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrAddressStreet = g
				.createAttribute();
		attrAddressStreet.set_name("street");
		g.createHasType(attrAddressStreet, string);
		g.createHasAttribute(clsAddress, attrAddressStreet);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrAddressTown = g
				.createAttribute();
		attrAddressTown.set_name("town");
		g.createHasType(attrAddressTown, string);
		g.createHasAttribute(clsAddress, attrAddressTown);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrAddressLoc = g
				.createAttribute();
		attrAddressLoc.set_name("loc");
		g.createHasType(attrAddressLoc, clsGeoLoc);
		g.createHasAttribute(clsAddress, attrAddressLoc);

		// entity classes
		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class clsAddressBook = g
				.createClass();
		clsAddressBook.set_name("AddressBook");
		clsAddressBook.set_kind("persistent");

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrAddressBookName = g
				.createAttribute();
		attrAddressBookName.set_name("abName");
		attrAddressBookName.set_kind("primary");
		g.createHasType(attrAddressBookName, string);
		g.createHasAttribute(clsAddressBook, attrAddressBookName);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Class clsPerson = g
				.createClass();
		clsPerson.set_name("Person");
		clsPerson.set_kind("persistent");

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrPersonAddress = g
				.createAttribute();
		attrPersonAddress.set_name("address");
		g.createHasType(attrPersonAddress, clsAddress);
		g.createHasAttribute(clsPerson, attrPersonAddress);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Attribute attrPersonName = g
				.createAttribute();
		attrPersonName.set_name("pName");
		attrPersonName.set_kind("primary");
		g.createHasType(attrPersonName, string);
		g.createHasAttribute(clsPerson, attrPersonName);

		Association assocAddressBookContainsPerson = g.createAssociation();
		assocAddressBookContainsPerson.set_name("AddressBookContainsPerson");
		assocAddressBookContainsPerson.addAdjacence("source", clsAddressBook);
		assocAddressBookContainsPerson.addAdjacence("target", clsPerson);

		// packages
		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Package pkgEntities = g
				.createPackage();
		pkgEntities.set_name("entities");
		g.createContains(pkgEntities, clsAddressBook);
		g.createContains(pkgEntities, clsPerson);
		g.createContains(pkgEntities, assocAddressBookContainsPerson);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Package pkgData = g
				.createPackage();
		pkgData.set_name("data");
		g.createContains(pkgData, clsAddress);
		g.createContains(pkgData, clsGeoLoc);

		de.uni_koblenz.jgralabtest.gretl.schemas.qvt.simpleuml.Package pkgDefault = g
				.createPackage();
		pkgDefault.set_name("");
		g.createContains(pkgDefault, pkgData);
		g.createContains(pkgDefault, pkgEntities);

		sourceSimpleUMLGraph = g;
	}

	private static void initVarroUMLGraph() {
		UMLGraph g = UMLSchema.instance().createUMLGraph();

		de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.Package p = g
				.createPackage();
		p.set_name("p");

		Assoc left = g.createAssoc();
		left.set_name("left");
		AssocEnd ae1 = g.createAssocEnd();
		ae1.set_name("ae1");
		AssocEnd ae2 = g.createAssocEnd();
		ae2.set_name("ae2");
		g.createHasAssocEnd(left, ae1);
		g.createHasAssocEnd(left, ae2);
		g.createContainsAssoc(p, left);

		Assoc right = g.createAssoc();
		right.set_name("right");
		AssocEnd ae3 = g.createAssocEnd();
		ae3.set_name("ae3");
		AssocEnd ae4 = g.createAssocEnd();
		ae4.set_name("ae4");
		g.createHasAssocEnd(right, ae3);
		g.createHasAssocEnd(right, ae4);
		g.createContainsAssoc(p, right);

		Assoc hold = g.createAssoc();
		hold.set_name("hold");
		AssocEnd ae5 = g.createAssocEnd();
		ae5.set_name("ae5");
		AssocEnd ae6 = g.createAssocEnd();
		ae6.set_name("ae6");
		g.createHasAssocEnd(hold, ae5);
		g.createHasAssocEnd(hold, ae6);
		g.createContainsAssoc(p, hold);

		de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.Class phil = g
				.createClass();
		phil.set_name("phil");
		g.createContainsClass(p, phil);
		g.createHasType(ae1, phil);
		g.createHasType(ae3, phil);
		g.createHasType(ae5, phil);

		de.uni_koblenz.jgralabtest.gretl.schemas.varro.uml.Class fork = g
				.createClass();
		fork.set_name("fork");
		g.createContainsClass(p, fork);
		g.createHasType(ae2, fork);
		g.createHasType(ae4, fork);
		g.createHasType(ae6, fork);

		Attribute status = g.createAttribute();
		status.set_name("status");
		g.createHasAttribute(phil, status);

		sourceVarroUMLGraph = g;
	}

	private static void initCopyGraph() {
		CopyGraph g = CopySchema.instance().createCopyGraph();

		Whole w1 = g.createWholeOne();
		Whole w2 = g.createWholeOne();
		Whole w3 = g.createWholeTwo();

		w1.set_uid(1);
		w2.set_uid(2);
		w3.set_uid(3);
		w1.set_name("One");
		w2.set_name("Two");
		w3.set_name("Three");

		WholePart wp1 = g.createWholePart();
		WholePart wp2 = g.createWholePart();
		WholePart wp3 = g.createWholePart();
		WholePart wp4 = g.createWholePart();
		WholePart wp5 = g.createWholePart();
		WholePart wp6 = g.createWholePart();

		wp1.set_uid(4);
		wp2.set_uid(5);
		wp3.set_uid(6);
		wp4.set_uid(7);
		wp5.set_uid(8);
		wp6.set_uid(9);
		wp1.set_name("Four");
		wp2.set_name("Five");
		wp3.set_name("Six");
		wp4.set_name("Seven");
		wp5.set_name("Eight");
		wp6.set_name("Nine");

		Part p1 = g.createPart();
		Part p2 = g.createPart();
		Part p3 = g.createPart();
		Part p4 = g.createPart();
		Part p5 = g.createPart();
		Part p6 = g.createPart();

		p1.set_uid(10);
		p2.set_uid(11);
		p3.set_uid(12);
		p4.set_uid(13);
		p5.set_uid(14);
		p6.set_uid(15);
		p1.set_name("Ten");
		p2.set_name("Eleven");
		p3.set_name("Twelve");
		p4.set_name("Thirteen");
		p5.set_name("Fourteen");
		p6.set_name("Sixteen");

		g.createIsPartOfWhole(wp1, w1);
		g.createIsPartOfWhole(wp2, w1);
		g.createIsPartOfWhole(wp3, w1);
		g.createIsPartOfWhole(wp4, w1);
		g.createIsPartOfWhole(wp5, w2);
		g.createIsPartOfWhole(wp6, w2);

		g.createIsPartOfWholePart(p1, wp1);
		g.createIsPartOfWholePart(p2, wp1);
		g.createIsPartOfWholePart(p3, wp2);
		g.createIsPartOfWholePart(p4, wp3);
		g.createIsPartOfWholePart(p5, wp3);
		g.createIsPartOfWholePart(p6, wp5);

		sourceCopyGraph = g;
	}

	private static void initFamilyGraph() {
		FamilyGraph g = FamilySchema.instance().createFamilyGraph();

		// Family Smith (2 sons, 1 daughter)
		Family smith = g.createFamily();
		smith.set_lastName("Smith");
		smith.set_street("Smith Avenue 4");
		smith.set_town("Smithtown");

		Member steve = g.createMember();
		steve.set_firstName("Steve");
		g.createHasFather(smith, steve);
		steve.set_age(66);

		Member stephanie = g.createMember();
		stephanie.set_firstName("Stephanie");
		g.createHasMother(smith, stephanie);
		stephanie.set_age(61);

		Member stu = g.createMember();
		stu.set_firstName("Stu");
		g.createHasSon(smith, stu);
		stu.set_age(27);

		Member sven = g.createMember();
		sven.set_firstName("Sven");
		g.createHasSon(smith, sven);
		sven.set_age(31);

		Member stella = g.createMember();
		stella.set_firstName("Stella");
		g.createHasDaughter(smith, stella);
		stella.set_age(29);

		// Family Carter (3 daughters)
		Family carter = g.createFamily();
		carter.set_lastName("Carter");
		carter.set_street("Carter Street 2");
		carter.set_town("Cartertown");

		Member chris = g.createMember();
		chris.set_firstName("Chris");
		g.createHasFather(carter, chris);
		chris.set_age(51);

		Member christy = g.createMember();
		christy.set_firstName("Christy");
		g.createHasMother(carter, christy);
		christy.set_age(49);

		Member carol = g.createMember();
		carol.set_firstName("Carol");
		g.createHasDaughter(carter, carol);
		carol.set_age(25);

		Member conzuela = g.createMember();
		conzuela.set_firstName("Conzuela");
		g.createHasDaughter(carter, conzuela);
		conzuela.set_age(17);

		// Family Smith number 2 (1 daughter, 1 son)
		Family smith2 = g.createFamily();
		smith2.set_lastName("Smith");
		smith2.set_street("Smithway 17");
		smith2.set_town("Smithtown");

		Member dennis = g.createMember();
		dennis.set_firstName("Dennis");
		g.createHasFather(smith2, dennis);
		// Dennis Smith is a son of the Smith 1 Family
		g.createHasSon(smith, dennis);
		dennis.set_age(37);

		Member debby = g.createMember();
		debby.set_firstName("Debby");
		g.createHasMother(smith2, debby);
		// Debby Doe is a daughter of the Carter Family
		g.createHasDaughter(carter, debby);
		debby.set_age(33);

		Member diana = g.createMember();
		diana.set_firstName("Diana");
		g.createHasDaughter(smith2, diana);
		diana.set_age(9);

		Member doug = g.createMember();
		doug.set_firstName("Doug");
		g.createHasSon(smith2, doug);
		doug.set_age(12);

		sourceFamilyGraph = g;
	}

	private static void initBedslAndPddslGraphs() {
		// The BEDSL Graph
		BedslGraph g = BedslSchema.instance().createBedslGraph();
		Entity bCisco = g.createEntity();
		bCisco.set_name("Cisco");
		Entity bCisco7603 = g.createEntity();
		bCisco7603.set_name("Cisco7603");
		g.createHasSupertype(bCisco7603, bCisco);

		SimpleAttribute bPrice = g.createSimpleAttribute();
		bPrice.set_name("price");
		bPrice.set_value("1299 EUR");
		g.createHasAttribute(bCisco7603, bPrice);

		Entity bCiscoConfig = g.createEntity();
		bCiscoConfig.set_name("CiscoConfiguration");
		ReferenceAttribute bHasConfig1 = g.createReferenceAttribute();
		bHasConfig1.set_name("HasConfig");
		g.createHasAttribute(bCisco, bHasConfig1);
		g.createReferences(bHasConfig1, bCiscoConfig);
		Entity bCiscoConfig7603 = g.createEntity();
		bCiscoConfig7603.set_name("CiscoConfiguration7603");
		ReferenceAttribute bHasConfig2 = g.createReferenceAttribute();
		bHasConfig2.set_name("HasConfig");
		g.createHasAttribute(bCisco7603, bHasConfig2);
		g.createReferences(bHasConfig2, bCiscoConfig7603);
		g.createHasSupertype(bCiscoConfig7603, bCiscoConfig);

		Entity bCiscoCard = g.createEntity();
		bCiscoCard.set_name("CiscoCard");
		Entity bHotSwappable = g.createEntity();
		bHotSwappable.set_name("HotSwappable");
		g.createHasSupertype(bHotSwappable, bCiscoCard);
		Entity bSPAInterface = g.createEntity();
		bSPAInterface.set_name("SPAInterface");
		g.createHasSupertype(bSPAInterface, bCiscoCard);
		Entity bSupervisor = g.createEntity();
		bSupervisor.set_name("Supervisor");
		g.createHasSupertype(bSupervisor, bCiscoCard);

		ReferenceAttribute bNotWorkWith = g.createReferenceAttribute();
		bNotWorkWith.set_name("DoesNotWorkWith");
		g.createHasAttribute(bCisco7603, bNotWorkWith);
		g.createReferences(bNotWorkWith, bHotSwappable);

		sourceBEDSLGraph = g;

		// The PDDSL Graph
		PddslGraph h = PddslSchema.instance().createPddslGraph();
		Chassis pCisco = h.createChassis();
		pCisco.set_name("Cisco");

		Configuration pConfig = h.createConfiguration();
		pConfig.set_name("CiscoConfiguration");
		h.createHasConfig(pCisco, pConfig);

		Slot pSlot = h.createSlot();
		pSlot.set_name("CiscoSlot");
		h.createHasSlot(pConfig, pSlot);

		Card pCard = h.createCard();
		pCard.set_name("CiscoCard");
		h.createHasCard(pSlot, pCard);

		Slot pSlot2 = h.createSlot();
		pSlot2.set_name("CiscoSlot2");
		h.createHasSlot(pConfig, pSlot2);

		Card pCard2 = h.createCard();
		pCard2.set_name("CiscoCard2");
		h.createHasCard(pSlot2, pCard2);

		sourcePDDSLGraph = h;
	}

	private static void initAddressBookGraph() {
		AddressBookGraph g = AddressBookSchema.instance()
				.createAddressBookGraph();
		AddressBook ab1 = g.createAddressBook();
		ab1.set_name("Democrats");
		Contact c1 = g.createContact();
		c1.set_name("Barack Obama");
		c1.set_address("Honolulu, Hawaii, USA");
		g.createContains(ab1, c1);
		Contact c2 = g.createContact();
		c2.set_name("Bill Clinton");
		c2.set_address("Hope, Arkansas, USA");
		g.createContains(ab1, c2);

		AddressBook ab2 = g.createAddressBook();
		ab2.set_name("Republicans");
		Contact c3 = g.createContact();
		c3.set_name("George Bush");
		c3.set_address("New Haven, Connecticut, USA");
		g.createContains(ab2, c3);
		Contact c4 = g.createContact();
		c4.set_name("Ronald Reagan");
		c4.set_address("Tampico, Illinois, USA");
		g.createContains(ab2, c4);

		sourceAddressBookGraph = g;
	}

	private static void initServiceGraph() {
		String myShopOwner = "MyShop Inc.";

		ServiceGraph g = ServiceSchema.instance().createServiceGraph();
		BasicService customerDataService = g.createBasicService();
		customerDataService.set_name("CustomerDataService");
		customerDataService.set_owner("CustomerPool Inc.");
		Database customerDB = g.createDatabase();
		customerDB.set_lastAccessTime(System.currentTimeMillis());
		customerDB.set_name("CustomerDB");
		customerDB.set_owner("CustomerPool Inc.");
		g.createAccesses(customerDataService, customerDB);

		BasicService placeOrderService = g.createBasicService();
		placeOrderService.set_name("PlaceOrderService");
		placeOrderService.set_owner(myShopOwner);
		Database orderDB = g.createDatabase();
		orderDB.set_lastAccessTime(System.currentTimeMillis());
		orderDB.set_name("OrderDB");
		orderDB.set_owner(myShopOwner);
		g.createAccesses(placeOrderService, orderDB);

		BasicService validateEmailService = g.createBasicService();
		validateEmailService.set_name("ValidateEmailService");
		validateEmailService.set_owner("Validation Inc.");

		ComposedService loginService = g.createComposedService();
		loginService.set_name("LoginService");
		loginService.set_owner(myShopOwner);
		g.createCalls(loginService, validateEmailService);
		g.createCalls(loginService, customerDataService);

		ComposedService orderService = g.createComposedService();
		orderService.set_name("OrderService");
		orderService.set_owner(myShopOwner);
		g.createCalls(orderService, customerDataService);
		g.createCalls(orderService, placeOrderService);

		ProcessService shoppingService = g.createProcessService();
		shoppingService.set_name("ShoppingService");
		shoppingService.set_owner(myShopOwner);
		g.createCalls(shoppingService, loginService);
		g.createCalls(shoppingService, orderService);

		sourceServiceGraph = g;
	}

	@SuppressWarnings("unchecked")
	@After
	public void tearDown() throws Exception {
		if (context.getTargetGraph() == null) {
			return;
		}

		String graphFile = tmpDir + targetFileName;

		try {
			GraphIO.saveGraphToFile(graphFile + ".tg",
					context.getTargetGraph(), null);
		} catch (GraphIOException e) {
			e.printStackTrace();
		}
		System.out.println("Dotting target graph to " + graphFile + ".pdf...");
		if (context.getTargetSchema() == FamilySchema.instance()) {
			dotty(context.getTargetGraph(), graphFile + ".pdf",
					HasFather.class, HasMother.class);
		} else {
			dotty(context.getTargetGraph(), graphFile + ".pdf");
		}

		String traceFile = tmpDir + targetFileName + ".gretltrace";
		context.storeTrace(traceFile);

		// Access those private fields
		Field arch = Context.class.getDeclaredField("archMap");
		Field img = Context.class.getDeclaredField("imgMap");
		arch.setAccessible(true);
		img.setAccessible(true);

		JValueMap oldArch = (JValueMap) arch.get(context);
		JValueMap oldImg = (JValueMap) img.get(context);

		context.restoreTrace(traceFile);

		if (!oldArch.equals(arch.get(context))) {
			System.err
					.println("Stored/reloaded archMap doesn't equal context's archMap");
		} else {
			System.out.println("Stored and reloaded archMap match!");
		}
		if (!oldImg.equals(img.get(context))) {
			System.err
					.println("Stored/reloaded imgMap doesn't equal context's imgMap");
		} else {
			System.out.println("Stored and reloaded imgMap match!");
		}

		context = null;
		GreqlEvaluator.resetGraphIndizes();
		GreqlEvaluator.resetOptimizedSyntaxGraphs();
	}

	private static void dotty(Graph g, String file,
			java.lang.Class<? extends Edge>... reversedEdgeTypes)
			throws IOException {
		Tg2Dot.convertGraph(g, file, false, GraphVizOutputFormat.PDF,
				reversedEdgeTypes);
	}

	@Test
	public void matchReplace1() {
		targetFileName = "matchReplace1";
		System.out.println(">>> " + targetFileName);
		context = new Context(sourceFamilyGraph);
		// make all Fathers Dieter
		new MatchReplace(
				context,
				"('$[0]') -->{'$[1]'} (Member '$[2]' | firstName = '\"Dieter\"')",
				"from hf: E{HasFather} reportSet startVertex(hf), hf, endVertex(hf) end")
				.execute();
		assertEquals(15, context.getTargetGraph().getECount());
	}

	@Test
	public void matchReplace2() {
		targetFileName = "matchReplace2";
		System.out.println(">>> " + targetFileName);
		context = new Context(sourceFamilyGraph);
		// Reset all families, the edges should be preserved!
		new MatchReplace(context, "(Family '$' | ...)", "V{Family}").execute();
		assertEquals(15, context.getTargetGraph().getECount());
	}

	@Test
	public void matchReplace3() {
		targetFileName = "matchReplace3";
		System.out.println(">>> " + targetFileName);
		context = new Context(sourceFamilyGraph);
		// Make all Mothers Mom and all Daughters Jenny...
		new MatchReplace(
				context,
				"(Member '$[3]' | firstName = '\"Mom\"') <--{'$[0]'} ('$[1]') "
						+ "-->{'$[2]'} (Member '$[4]' | firstName = '\"Jenny\"')",
				"from f: V{Family}, m, d: V{Member}, hm: E{HasMother}, hd: E{HasDaughter} "
						+ "with m <-hm-- f --hd-> d "
						+ "reportSet hm, f, hd, m, d end").execute();
		assertEquals(15, context.getTargetGraph().getECount());
	}

	@Test
	public void matchReplace4() {
		targetFileName = "matchReplace4";
		System.out.println(">>> " + targetFileName);
		context = new Context(sourceFamilyGraph);
		// Reset all families, the edges should be preserved! Here, the type is
		// also specified as greql expression.
		new MatchReplace(context, "(#'$[1]' '$[0]')",
				"from f: V{Family} reportSet f, typeName(f) end").execute();
		assertEquals(15, context.getTargetGraph().getECount());
	}

	@Test
	public void addressBookTransformation() {
		targetFileName = "addressBookTransformation";
		System.out.println(">>> " + targetFileName);
		context = new Context(
				"de.uni_koblenz.new_addressbookschema.AddressBookSchema",
				"AddressBookGraph");
		context.setSourceGraph(sourceAddressBookGraph);

		AddressBookTransformation t = new AddressBookTransformation(context);
		Graph targetGraph = t.execute();

		// context.printMappings();
		validateTargetAddressBookGraph(targetGraph);
	}

	private File getTransformationFile(String relativePath) {
		return new File(getClass().getResource(relativePath).getFile());
	}

	@Test
	public void addressBookTransformationConcreteSyntax() {
		targetFileName = "addressBookTransformationConcreteSyntax";
		System.out.println(">>> " + targetFileName);
		// We use the schema created in the addressBookTransformation() test
		// case, so the schema creation should be skipped here.
		context = new Context(
				"de.uni_koblenz.new_addressbookschema.AddressBookSchema",
				"AddressBookGraph");
		context.setSourceGraph(sourceAddressBookGraph);

		ExecuteTransformation t = new ExecuteTransformation(
				context,
				getTransformationFile("transforms/AddressBookRefactoring.gretl"));
		Graph targetGraph = t.execute();

		// context.printMappings();
		validateTargetAddressBookGraph(targetGraph);
	}

	private void validateTargetAddressBookGraph(Graph targetGraph) {
		Object[][] ary = {
				{
						1,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("AddressBook")) },
				{
						2,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Category")) },
				{
						4,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Contact")) },
				{
						4,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Address")) },
				{
						4,
						targetGraph.edges(targetGraph.getSchema()
								.getGraphClass().getEdgeClass("Contains")) },
				{
						2,
						targetGraph.edges(targetGraph.getSchema()
								.getGraphClass().getEdgeClass("HasCategory")) },
				{
						4,
						targetGraph.edges(targetGraph.getSchema()
								.getGraphClass().getEdgeClass("HasAddress")) } };
		for (Object[] pair : ary) {
			int count = 0;
			for (@SuppressWarnings("unused")
			Object o : (Iterable<?>) pair[1]) {
				count++;
			}
			assertEquals(pair[0], count);
		}
	}

	@Test
	public void familyGraph2Genealogy() throws Exception {
		targetFileName = "familyGraph2Genealogy";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uni_koblenz.genealogy.GenealogySchema",
				"Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new FamilyGraph2Genealogy(context).execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
		context.printImgMappings();
	}

	@Test
	public void familyGraph2GenealogyWithHelpers() throws Exception {
		targetFileName = "familyGraph2GenealogyWithHelpers";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uni_koblenz.genealogy.GenealogySchema",
				"Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new FamilyGraph2GenealogyWithHelpers(context).execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
		// context.printImgMappings();
	}

	@Test
	public void familyGraph2GenealogyWithHelpersConcreteSyntax()
			throws Exception {
		targetFileName = "familyGraph2GenealogyWithHelpersConcreteSyntax";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uko.genealogy.GenealogySchema", "Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new ExecuteTransformation(
				context,
				getTransformationFile("transforms/Families2GenealogyWithHelpers.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void familyGraph2GenealogyICMT2011() throws Exception {
		targetFileName = "familyGraph2GenealogyICMT2011";

		System.out.println(">>> " + targetFileName);

		context = new Context("icmt2011.GenealogySchema", "Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new ExecuteTransformation(context,
				getTransformationFile("transforms/F2G_ICMT2011.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(53, tg.getECount());
		assertEquals(16, tg.getVCount());
	}

	@Test
	public void familyGraph2GenealogyConcreteSyntax() throws Exception {
		targetFileName = "familyGraph2GenealogyConcreteSyntax";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uko.genealogy.GenealogySchema", "Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new ExecuteTransformation(context,
				getTransformationFile("transforms/Families2Genealogy.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void familyGraph2GenealogyConcreteSyntaxIndirect() throws Exception {
		targetFileName = "familyGraph2GenealogyConcreteSyntaxIndirect";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uko.genealogyindirect.GenealogySchema",
				"Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new ExecuteTransformation(
				context,
				getTransformationFile("transforms/Families2GenealogyIndirect.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void familyGraph2GenealogyConcreteSyntaxVeryIndirect()
			throws Exception {
		targetFileName = "familyGraph2GenealogyConcreteSyntaxVeryIndirect";

		System.out.println(">>> " + targetFileName);

		context = new Context("de.uko.genealogyveryindirect.GenealogySchema",
				"Genealogy");
		context.setSourceGraph(sourceFamilyGraph);
		Graph tg = new FamilyGraph2GenealogyByUsingConcreteSyntax(context)
				.execute();
		assertNotNull(tg);
		assertEquals(21, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void varroUML2RDBSConcreteSyntax() throws Exception {
		targetFileName = "varroUML2RDBSConcreteSyntax";

		System.out.println(">>> " + targetFileName);

		context = new Context("varro.RDBSchema", "DBSchemaGraph");
		context.setSourceGraph(sourceVarroUMLGraph);
		Graph tg = new ExecuteTransformation(context,
				getTransformationFile("transforms/VarroUML2RDBS.gretl"))
				.execute();
		assertNotNull(tg);
		assertTrue(tg.getECount() > 0);
		assertTrue(tg.getVCount() > 0);
		// TODO: Check the target graph!
	}

	@Test
	public void bedslPddslMerge_plain() throws Exception {
		targetFileName = "bedslPddslMerge_plain";

		System.out.println(">>> " + targetFileName);

		context = new Context("bedsl_pddsl_merged.MergedBedslPddslSchema",
				"MergedBedslPddslGraph");
		context.addSourceGraph("bedsl", sourceBEDSLGraph);
		context.addSourceGraph("pddsl", sourcePDDSLGraph);
		Graph tg = new ExecuteTransformation(context,
				getTransformationFile("transforms/BedslPddslMerge-plain.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(14, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void bedslPddslMerge_merge_ops() throws Exception {
		targetFileName = "bedslPddslMerge_merge_ops";

		System.out.println(">>> " + targetFileName);

		context = new Context("bedsl_pddsl_merged.MergedBedslPddslSchema",
				"MergedBedslPddslGraph");
		context.addSourceGraph("bedsl", sourceBEDSLGraph);
		context.addSourceGraph("pddsl", sourcePDDSLGraph);
		Graph tg = new ExecuteTransformation(
				context,
				getTransformationFile("transforms/BedslPddslMerge-merge-ops.gretl"))
				.execute();
		assertNotNull(tg);
		assertEquals(14, tg.getECount());
		assertEquals(13, tg.getVCount());
	}

	@Test
	public void serviceTransformation() {
		targetFileName = "serviceTransformation";
		context = new Context("de.uni_koblenz.new_services.ServiceSchema",
				"ServiceGraph");
		context.setSourceGraph(sourceServiceGraph);

		ServiceTransformation t = new ServiceTransformation(context);
		Graph targetGraph = t.execute();

		EdgeClass iob = (EdgeClass) targetGraph.getSchema()
				.getAttributedElementClass("IsOwnedBy");
		// check if the default values for that composition were set correctly
		assertEquals(1, iob.getTo().getMin());
		assertEquals(1, iob.getTo().getMax());
		assertEquals(0, iob.getFrom().getMin());
		assertEquals(Integer.MAX_VALUE, iob.getFrom().getMax());

		Object[][] ary = {
				{
						6,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Service")) },
				{
						2,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Database")) },
				{
						1,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass()
								.getVertexClass("ProcessService")) },
				{
						2,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass()
								.getVertexClass("ComposedService")) },
				{
						3,
						targetGraph
								.vertices(targetGraph.getSchema()
										.getGraphClass()
										.getVertexClass("BasicService")) },
				{
						3,
						targetGraph.vertices(targetGraph.getSchema()
								.getGraphClass().getVertexClass("Owner")) },
				{
						8,
						targetGraph.edges(targetGraph.getSchema()
								.getGraphClass().getEdgeClass("IsOwnedBy")) },
				{
						2,
						targetGraph.edges(targetGraph.getSchema()
								.getGraphClass().getEdgeClass("Accesses")) } };

		for (Object[] pair : ary) {
			int count = 0;
			for (@SuppressWarnings("unused")
			Object o : (Iterable<?>) pair[1]) {
				count++;
			}
			assertEquals(pair[0], count);
		}
	}

	@Test
	public void copyTransformation1() {
		targetFileName = "copyTransformation1";
		context = new Context("de.uni_koblenz.copy1.CopySchema", "CopyGraph");
		context.setSourceGraph(sourceCopyGraph);

		Graph targetGraph = new CopyTransformation(context).execute();

		SchemaCompare sc = new SchemaCompare(sourceCopyGraph.getSchema(),
				targetGraph.getSchema());
		assertEquals(0, sc.compareSchemas());
		assertEquals(sourceCopyGraph.getECount(), targetGraph.getECount());
		assertEquals(sourceCopyGraph.getVCount(), targetGraph.getVCount());
	}

	@Test
	public void copyTransformation2() {
		JGraLab.getLogger("de.uni_koblenz.jgralab.gretl.transformations")
				.setLevel(Level.ALL);
		targetFileName = "copyTransformation2";
		context = new Context("de.uni_koblenz.copy2.CopySchema", "CopyGraph");
		context.setSourceGraph(sourceCopyGraph);

		// In the new schema, Whole is not abstract and both subclasses
		// are removed. Each of those instances will be a Whole instance
		// in the new graph.
		Graph targetGraph = new CopyTransformation(context,
				Pattern.compile("(Whole|WholeOne|WholeTwo)"), null) {

			@Override
			protected void transformVertexClasses() {
				VertexClass nWhole = new CreateVertexClass(context, "Whole",
						"V{Whole}").execute();
				new AddSuperClass(context, nWhole, vc("CopyVertex")).execute();
			}
		}.execute();

		// context.printImgMappings();

		SchemaCompare sc = new SchemaCompare(sourceCopyGraph.getSchema(),
				targetGraph.getSchema());
		assertEquals(5, sc.compareSchemas());
		assertEquals(sourceCopyGraph.getECount(), targetGraph.getECount());
		assertEquals(sourceCopyGraph.getVCount(), targetGraph.getVCount());
	}

	@Test
	public void copyTransformation3() {
		targetFileName = "copyTransformation3";
		context = new Context("de.uni_koblenz.copy3.CopySchema", "CopyGraph");
		context.setSourceGraph(sourceCopyGraph);
		// Exclude the uid attribute of CopyVertex by excluding all its
		// attributes and including only the name attribute.
		new CopyTransformation(context, Pattern.compile("CopyVertex\\..*"),
				Pattern.compile("CopyVertex\\.name")).execute();
		Graph targetGraph = context.getTargetGraph();
		// context.printMappings();

		SchemaCompare sc = new SchemaCompare(sourceCopyGraph.getSchema(),
				targetGraph.getSchema());
		assertEquals(1, sc.compareSchemas());
		assertEquals(sourceCopyGraph.getECount(), targetGraph.getECount());
		assertEquals(sourceCopyGraph.getVCount(), targetGraph.getVCount());
	}

	@Test
	public void simpleCopyTransformation1() {
		targetFileName = "simpleCopyTransformation1";
		context = new Context("de.uni_koblenz.simplecopy1.CopySchema",
				"CopyGraph");
		context.setSourceGraph(sourceCopyGraph);

		Graph targetGraph = new SimpleCopyTransformation(context).execute();

		SchemaCompare sc = new SchemaCompare(sourceCopyGraph.getSchema(),
				targetGraph.getSchema());
		assertEquals(0, sc.compareSchemas());
		assertEquals(sourceCopyGraph.getECount(), targetGraph.getECount());
		assertEquals(sourceCopyGraph.getVCount(), targetGraph.getVCount());
	}

	@Test
	public void simpleUML2SimpleRDBMS() throws Exception {
		targetFileName = "simpleUML2SimpleRDBMS";

		System.out.println(">>> " + targetFileName);

		context = new Context(SimpleRDBMSSchema.instance());
		context.setSourceGraph(sourceSimpleUMLGraph);
		new SimpleUML2SimpleRDBMS(context).execute();

		// TODO: Check the target graph!
	}

}
