/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.eca;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.Condition;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.ECARuleManager;
import de.uni_koblenz.jgralab.eca.GreqlCondition;
import de.uni_koblenz.jgralab.eca.PrintAction;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription.EdgeEnd;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.std.EdgeImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.eca.useractions.CreateAVertexOfSameTypeAction;
import de.uni_koblenz.jgralabtest.eca.useractions.PrintAlphaAndOmegaOfDeletedEdge;
import de.uni_koblenz.jgralabtest.eca.useractions.PrintNewAndOldAttributeValueAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingOnHighesLevelAction;
import de.uni_koblenz.jgralabtest.eca.userconditions.IsGreaterThan2012;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Loans;


public class ECAGenericTest {

	private static Schema schema;
	private static VertexClass vcBook;
	private static VertexClass vcNewMedia;
	private static VertexClass vcMagazin;
	private static VertexClass vcLibrary;
	private static VertexClass vcUser;
	private static EdgeClass ecLoans;
	
	private static Graph simlibgraph;
	private static Vertex user1;
	private static Vertex user2;
	private static Vertex book1;
	private static Vertex newmedia1;
	private static Edge loans_u1_b1;

	@BeforeClass
	public static void setUp() throws GraphIOException {
		System.out.println("Start ECA Test.\n");
		JGraLab.setLogLevel(Level.OFF);
		initGraph();
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("Finish ECA Test.\n");
		System.out.println("-----------------------------------------------");
		System.out.println();
	}

	@Test
	public void testDeleteVertexEvent() {
		System.out.println("Single Tests to check if Events are recognized:");

		Vertex newBook = 	simlibgraph.createVertex(schema.getGraphClass().getVertexClass("Book"));



		EventDescription<VertexClass> bef_ev = new DeleteVertexEventDescription(
				EventDescription.EventTime.BEFORE, vcBook);
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: Book Vertex will become deleted.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new DeleteVertexEventDescription(
				EventDescription.EventTime.AFTER, vcBook);
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: Book Vertex is deleted.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.deleteVertex(newBook);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testCreateVertexEvent() {
		EventDescription<VertexClass> bef_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.BEFORE, vcBook);
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Book Vertex will become created.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, vcBook);
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Book Vertex is created.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("Book"));

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testDeleteEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new DeleteEdgeEventDescription(
				EventDescription.EventTime.BEFORE, ecLoans);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge will become deleted.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new DeleteEdgeEventDescription(
				EventDescription.EventTime.AFTER, ecLoans);
		Action<EdgeClass> aft_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge is deleted.");
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);

		Edge newLoans = simlibgraph.createEdge(ecLoans,user1, newmedia1);
		simlibgraph.deleteEdge(newLoans);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testCreateEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new CreateEdgeEventDescription(
				EventDescription.EventTime.BEFORE, ecLoans);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: New Loans Edge will become created.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new CreateEdgeEventDescription(
				EventDescription.EventTime.AFTER, ecLoans);
		Action<EdgeClass> aft_act = new PrintAction<EdgeClass>(
				"ECA Test Message: New Loans Edge is created.");
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createEdge(schema.getGraphClass().getEdgeClass("Loans"),user1, newmedia1);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testChangeEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.BEFORE, ecLoans, EdgeEnd.ANY);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge will become changed.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.AFTER, ecLoans, EdgeEnd.ANY);
		Action<EdgeClass> aft_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge is changed.");
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);
		
		loans_u1_b1.setAlpha(user2);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);
	}

	@Test
	public void testChangeAttributeEvent() {
		EventDescription<VertexClass> bef_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.BEFORE, vcBook, "title");
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: Title of Book Vertex will become changed.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, vcBook, "title");
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: Title of Book Vertex is changed.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ecaRuleManager.addECARule(aft_rule);

		book1.setAttribute("title","The Return of the King");

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testAfterDeleteEdgeEvent() {
		
		System.out.println("Test if old alpha and omega are accessable in after delete edge event:");
		
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();

		EventDescription<EdgeClass> aft_ev = new DeleteEdgeEventDescription(
				EventDescription.EventTime.AFTER, Loans.EC);
		Action<EdgeClass> aft_act = new PrintAlphaAndOmegaOfDeletedEdge();
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);

		Edge newLoans = simlibgraph.createEdge(ecLoans,user1, newmedia1);
		simlibgraph.deleteEdge(newLoans);

		ecaRuleManager.deleteECARule(aft_rule);
		
		System.out.println();

	}
	
	@Test
	public void testGrequlContextOnEvent() {
		System.out.println("Test Grequl Context:");
		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, "V{Medium}");
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Medium created.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("Book"));

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testCondition() {
		System.out.println("Test Condition:");
		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, vcNewMedia);
		Condition<VertexClass> aft_cond = new GreqlCondition<VertexClass>(
				"count( V{NewMedia} ) = 2");
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Medium after Condition Test created. "
						+ "This message should appear only once.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_cond, aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("NewMedia"));
		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("NewMedia"));

		ecaRuleManager.deleteECARule(aft_rule);
		System.out.println();
	}

	@Test(expected = RuntimeException.class)
	public void testAddEventTo2Graphs() {
		System.out
				.println("Test if an Exception occurs when an Event is assigned to two Graphs");
		Graph newGraph = schema.createGraph(ImplementationType.GENERIC);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, vcLibrary);

		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: Failure Test old Graph.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);

		Action<VertexClass> aft_actN = new PrintAction<VertexClass>(
				"ECA Test Message: Failure Test new Graph.");
		ECARule<VertexClass> aft_ruleN = new ECARule<VertexClass>(aft_ev,
				aft_actN);

		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);
		((ECARuleManager) newGraph.getECARuleManager()).addECARule(aft_ruleN);

		ecaRuleManager.deleteECARule(aft_rule);
		System.out.println();
	}

	@Test(expected = RuntimeException.class)
	public void testAddRuleTo2Graphs() {
		System.out
				.println("Test if an Exception occurs when a Rule is assigned to two Graphs");
		Graph newGraph = schema.createGraph(ImplementationType.GENERIC);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER,vcLibrary);
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: Failure Test two Graphs.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);

		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);
		((ECARuleManager) newGraph.getECARuleManager()).addECARule(aft_rule);

		ecaRuleManager.deleteECARule(aft_rule);
		System.out.println();
	}

	@Test
	public void testNeverEndingCreationStop() {
		System.out
				.println("\nTest if the prevention of never ending nested calls works:");
		EventDescription<VertexClass> bef_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.BEFORE, vcUser);
		Action<VertexClass> bef_act = new CreateAVertexOfSameTypeAction();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		// allow only 5 nested triggers
		ecaRuleManager.setMaxNestedTriggerCalls(5);

		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("User"));

		ecaRuleManager.deleteECARule(bef_rule);
		System.out.println();
	}

	@Test
	public void testGenerationOf20Users() {
		System.out
				.println("Test to create Users until an count of 20 is reached:");
		EventDescription<VertexClass> bef_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, vcUser);
		Condition<VertexClass> aft_cond = new GreqlCondition<VertexClass>(
				"count (V{User}) < 20");
		Action<VertexClass> bef_act = new CreateAVertexOfSameTypeAction();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				aft_cond, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		ecaRuleManager.setMaxNestedTriggerCalls(50);

		simlibgraph.createVertex(schema.getGraphClass().getVertexClass("User"));

		ecaRuleManager.deleteECARule(bef_rule);

		System.out.println();
	}

	@Test
	public void testGettingOldAndNewValueFromBeforeAttributeChanging() {
		System.out
				.println("Test Action that uses old and new Attribute value of ChangeAttributeEvent:");
		EventDescription<VertexClass> bef_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.BEFORE, vcBook, "title");
		Action<VertexClass> bef_act = new PrintNewAndOldAttributeValueAction<VertexClass>();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		book1.setAttribute("title","Silmarillion");

		ecaRuleManager.deleteECARule(bef_rule);

		System.out.println();
	}

	@Test
	public void testGettingOldAndNewValueFromAfterAttributeChanging() {
		System.out
				.println("Test Action that uses old and new Vertex of ChangeEdgeEvent:");
		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, vcBook, "title");
		Action<VertexClass> aft_act = new PrintNewAndOldAttributeValueAction<VertexClass>();
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		book1.setAttribute("title","The Hobbit");

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testRevertChangedEdge() {
		System.out
				.println("Test Action that reverts the change of an Edge if the condition is true:");
		EventDescription<EdgeClass> aft_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.AFTER, ecLoans, EdgeEnd.ANY);
		Condition<EdgeClass> aft_cond = new GreqlCondition<EdgeClass>(
				"startVertex(context).name = 'Martin King'");
		Action<EdgeClass> aft_act = new RevertEdgeChangingAction();
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_cond,
				aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		System.out.println(loans_u1_b1.getAttributedElementClass());
		System.out.println(ecLoans);
		
		loans_u1_b1.setAlpha(user1);

		assertEquals(loans_u1_b1.getAlpha(), user2);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testRevertChangedEdge2() {
		System.out
				.println("Test Action that reverts the change of an Edge if it is on the highest nested call level");
		EventDescription<EdgeClass> aft_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.AFTER, ecLoans, EdgeEnd.ANY);
		Action<EdgeClass> aft_act = new RevertEdgeChangingOnHighesLevelAction();
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		loans_u1_b1.setAlpha(user1);

		assertEquals(loans_u1_b1.getAlpha(), user2);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testUserCondition1() {
		System.out
				.println("Test Condition if the new set year of Magazin is greater than 2012.");
		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, vcMagazin, "year");
		Condition<VertexClass> cond = new IsGreaterThan2012<VertexClass>();
		Action<VertexClass> act = new PrintAction<VertexClass>(
				"new year is greater than 2012");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev, cond,
				act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.getFirstVertex(schema.getGraphClass().getVertexClass("Magazin")).setAttribute("year",2014);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testUserCondition2() {
		System.out
				.println("Test Condition if the new set year of Magazin is greater than 2012.");
		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, vcMagazin, "year");
		Condition<VertexClass> cond = new IsGreaterThan2012<VertexClass>();
		Action<VertexClass> act = new PrintAction<VertexClass>(
				"new year is greater than 2012");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev, cond,
				act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.getFirstVertex(schema.getGraphClass().getVertexClass("Magazin")).setAttribute("year",2001);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	static void initGraph() throws GraphIOException {
		
		schema = GraphIO.loadSchemaFromFile("testit/testschemas/eca/SimpleLibrarySchema.tg");
		
		Graph graph = schema.createGraph(ImplementationType.GENERIC);
		graph.setAttribute("version", "v1.0");

		// Library
		vcLibrary = schema.getGraphClass().getVertexClass("Library");
		Vertex lib = graph.createVertex(vcLibrary);
		lib.setAttribute("name", "Bibliothequa");

		// Users
		vcUser = schema.getGraphClass().getVertexClass("User");
		user1 = graph.createVertex(vcUser);
		user1.setAttribute("name", "Martin King");

		user2 = graph.createVertex(schema.getGraphClass().getVertexClass("User"));
		user2.setAttribute("name","Stephanie Plum");

		// Media
		vcBook = schema.getGraphClass().getVertexClass("Book");
		book1 = graph.createVertex(vcBook);
		book1.setAttribute("title","Lord of the Rings");
		book1.setAttribute("author", "J.R.R. Tokien");

		vcMagazin = schema.getGraphClass().getVertexClass("Magazin");
		Vertex magazin1 =  graph.createVertex(vcMagazin);
		magazin1.setAttribute("title", "CT");
		magazin1.setAttribute("publisher", "Blub");
		magazin1.setAttribute("year",2011);

		vcNewMedia = schema.getGraphClass().getVertexClass("NewMedia");
		newmedia1 =  graph.createVertex(vcNewMedia);
		newmedia1.setAttribute("title","Rush Hour 1");
		newmedia1.setAttribute("type", graph.getEnumConstant((EnumDomain) schema.getDomain("MediaType"), "DVD"));

		// Media are in Library
		lib.addAdjacence("media", book1);
		lib.addAdjacence("media",magazin1);
		lib.addAdjacence("media", newmedia1);

		// user1 loans book1
		ecLoans = schema.getGraphClass().getEdgeClass("Loans");
		loans_u1_b1 = graph.createEdge(ecLoans, user1, book1);
		Map<String,Object> values = new HashMap<String, Object>();
		values.put("day", 1);
		values.put("month", 1);
		values.put("year", 2011);
		loans_u1_b1.setAttribute("date", graph.createRecord((RecordDomain)schema.getDomain("Date"), values));

		// user 2 loans magazin1
		Edge loans_u2_m1 = graph.createEdge(ecLoans, user1, magazin1);
		Map<String,Object> values1 = new HashMap<String, Object>();
		values1.put("day", 5);
		values1.put("month", 5);
		values1.put("year", 2011);
		loans_u2_m1.setAttribute("date",graph.createRecord((RecordDomain)schema.getDomain("Date"), values1));

		simlibgraph = graph;
	}
}
