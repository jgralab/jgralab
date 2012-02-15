package de.uni_koblenz.jgralabtest.eca;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
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
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralabtest.eca.useractions.CreateAVertexOfSameTypeAction;
import de.uni_koblenz.jgralabtest.eca.useractions.PrintNewAndOldAttributeValueAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingOnHighesLevelAction;
import de.uni_koblenz.jgralabtest.eca.userconditions.IsGreaterThan2012;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Book;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Date;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Library;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Loans;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.Magazin;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.MediaType;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.NewMedia;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.SimpleLibraryGraph;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.SimpleLibrarySchema;
import de.uni_koblenz.jgralabtest.schemas.eca.simplelibrary.User;

public class ECATest {

	private static SimpleLibraryGraph simlibgraph;
	private static User user1;
	private static User user2;
	private static Book book1;
	private static NewMedia newmedia1;
	private static Loans loans_u1_b1;

	@BeforeClass
	public static void setUp() {
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

		Book newBook = simlibgraph.createBook();

		EventDescription<VertexClass> bef_ev = new DeleteVertexEventDescription(
				EventDescription.EventTime.BEFORE, Book.VC);
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: Book Vertex will become deleted.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new DeleteVertexEventDescription(
				EventDescription.EventTime.AFTER, Book.VC);
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
				EventDescription.EventTime.BEFORE, Book.VC);
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Book Vertex will become created.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, Book.VC);
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: New Book Vertex is created.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createBook();

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testDeleteEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new DeleteEdgeEventDescription(
				EventDescription.EventTime.BEFORE, Loans.EC);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge will become deleted.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new DeleteEdgeEventDescription(
				EventDescription.EventTime.AFTER, Loans.EC);
		Action<EdgeClass> aft_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge is deleted.");
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);

		Loans newLoans = simlibgraph.createLoans(user1, newmedia1);
		simlibgraph.deleteEdge(newLoans);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testCreateEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new CreateEdgeEventDescription(
				EventDescription.EventTime.BEFORE, Loans.EC);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: New Loans Edge will become created.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new CreateEdgeEventDescription(
				EventDescription.EventTime.AFTER, Loans.EC);
		Action<EdgeClass> aft_act = new PrintAction<EdgeClass>(
				"ECA Test Message: New Loans Edge is created.");
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_act);
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.createLoans(user1, newmedia1);

		ecaRuleManager.deleteECARule(bef_rule);
		ecaRuleManager.deleteECARule(aft_rule);

	}

	@Test
	public void testChangeEdgeEvent() {
		EventDescription<EdgeClass> bef_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.BEFORE, Loans.EC, EdgeEnd.ANY);
		Action<EdgeClass> bef_act = new PrintAction<EdgeClass>(
				"ECA Test Message: Loans Edge will become changed.");
		ECARule<EdgeClass> bef_rule = new ECARule<EdgeClass>(bef_ev, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<EdgeClass> aft_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.AFTER, Loans.EC, EdgeEnd.ANY);
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
				EventDescription.EventTime.BEFORE, Book.VC, "title");
		Action<VertexClass> bef_act = new PrintAction<VertexClass>(
				"ECA Test Message: Title of Book Vertex will become changed.");
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, Book.VC, "title");
		Action<VertexClass> aft_act = new PrintAction<VertexClass>(
				"ECA Test Message: Title of Book Vertex is changed.");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ecaRuleManager.addECARule(aft_rule);

		book1.set_title("The Return of the King");

		ecaRuleManager.deleteECARule(bef_rule);
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

		simlibgraph.createBook();

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testCondition() {
		System.out.println("Test Condition:");
		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, NewMedia.VC);
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

		simlibgraph.createNewMedia();
		simlibgraph.createNewMedia();

		ecaRuleManager.deleteECARule(aft_rule);
		System.out.println();
	}

	@Test(expected = RuntimeException.class)
	public void testAddEventTo2Graphs() {
		System.out
				.println("Test if an Exception occurs when an Event is assigned to two Graphs");
		SimpleLibraryGraph newGraph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph(ImplementationType.STANDARD);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, Library.VC);

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
		SimpleLibraryGraph newGraph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph(ImplementationType.STANDARD);

		EventDescription<VertexClass> aft_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, Library.VC);
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
				EventDescription.EventTime.BEFORE, User.VC);
		Action<VertexClass> bef_act = new CreateAVertexOfSameTypeAction();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		// allow only 5 nested triggers
		ecaRuleManager.setMaxNestedTriggerCalls(5);

		simlibgraph.createUser();

		ecaRuleManager.deleteECARule(bef_rule);
		System.out.println();
	}

	@Test
	public void testGenerationOf20Users() {
		System.out
				.println("Test to create Users until an count of 20 is reached:");
		EventDescription<VertexClass> bef_ev = new CreateVertexEventDescription(
				EventDescription.EventTime.AFTER, User.VC);
		Condition<VertexClass> aft_cond = new GreqlCondition<VertexClass>(
				"count (V{User}) < 20");
		Action<VertexClass> bef_act = new CreateAVertexOfSameTypeAction();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				aft_cond, bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		ecaRuleManager.setMaxNestedTriggerCalls(50);

		simlibgraph.createUser();

		ecaRuleManager.deleteECARule(bef_rule);

		System.out.println();
	}

	@Test
	public void testGettingOldAndNewValueFromBeforeAttributeChanging() {
		System.out
				.println("Test Action that uses old and new Attribute value of ChangeAttributeEvent:");
		EventDescription<VertexClass> bef_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.BEFORE, Book.VC, "title");
		Action<VertexClass> bef_act = new PrintNewAndOldAttributeValueAction<VertexClass>();
		ECARule<VertexClass> bef_rule = new ECARule<VertexClass>(bef_ev,
				bef_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(bef_rule);

		book1.set_title("Silmarillion");

		ecaRuleManager.deleteECARule(bef_rule);

		System.out.println();
	}

	@Test
	public void testGettingOldAndNewValueFromAfterAttributeChanging() {
		System.out
				.println("Test Action that uses old and new Vertex of ChangeEdgeEvent:");
		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, Book.VC, "title");
		Action<VertexClass> aft_act = new PrintNewAndOldAttributeValueAction<VertexClass>();
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev,
				aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		book1.set_title("The Hobbit");

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testRevertChangedEdge() {
		System.out
				.println("Test Action that reverts the change of an Edge if the condition is true:");
		EventDescription<EdgeClass> aft_ev = new ChangeEdgeEventDescription(
				EventDescription.EventTime.AFTER, Loans.EC, EdgeEnd.ANY);
		Condition<EdgeClass> aft_cond = new GreqlCondition<EdgeClass>(
				"startVertex(context).name = 'Martin King'");
		Action<EdgeClass> aft_act = new RevertEdgeChangingAction();
		ECARule<EdgeClass> aft_rule = new ECARule<EdgeClass>(aft_ev, aft_cond,
				aft_act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		System.out.println(loans_u1_b1.getSchemaClass());
		System.out.println(Loans.EC);
		
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
				EventDescription.EventTime.AFTER, Loans.EC, EdgeEnd.ANY);
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
				EventDescription.EventTime.AFTER, Magazin.VC, "year");
		Condition<VertexClass> cond = new IsGreaterThan2012<VertexClass>();
		Action<VertexClass> act = new PrintAction<VertexClass>(
				"new year is greater than 2012");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev, cond,
				act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.getFirstMagazin().set_year(2014);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	@Test
	public void testUserCondition2() {
		System.out
				.println("Test Condition if the new set year of Magazin is greater than 2012.");
		EventDescription<VertexClass> aft_ev = new ChangeAttributeEventDescription<VertexClass>(
				EventDescription.EventTime.AFTER, Magazin.VC, "year");
		Condition<VertexClass> cond = new IsGreaterThan2012<VertexClass>();
		Action<VertexClass> act = new PrintAction<VertexClass>(
				"new year is greater than 2012");
		ECARule<VertexClass> aft_rule = new ECARule<VertexClass>(aft_ev, cond,
				act);
		ECARuleManager ecaRuleManager = (ECARuleManager) simlibgraph
				.getECARuleManager();
		ecaRuleManager.addECARule(aft_rule);

		simlibgraph.getFirstMagazin().set_year(2001);

		ecaRuleManager.deleteECARule(aft_rule);

		System.out.println();
	}

	static void initGraph() {
		SimpleLibraryGraph graph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph(ImplementationType.STANDARD);
				
		graph.set_version("v1.0");

		// Library
		Library lib = graph.createLibrary();
		lib.set_name("Bibliothequa");

		// Users
		user1 = graph.createUser();
		user1.set_name("Martin King");

		user2 = graph.createUser();
		user2.set_name("Stephanie Plum");

		// Media
		book1 = graph.createBook();
		book1.set_title("Lord of the Rings");
		book1.set_author("J.R.R. Tokien");

		Magazin magazin1 = graph.createMagazin();
		magazin1.set_title("CT");
		magazin1.set_publisher("Blub");
		magazin1.set_year(2011);

		newmedia1 = graph.createNewMedia();
		newmedia1.set_title("Rush Hour 1");
		newmedia1.set_type(MediaType.DVD);

		// Media are in Library
		lib.add_media(book1);
		lib.add_media(magazin1);
		lib.add_media(newmedia1);

		// user1 loans book1
		loans_u1_b1 = graph.createLoans(user1, book1);
		loans_u1_b1.set_date(new Date(1, 1, 2011));

		// user 2 loans magazin1
		Loans loans_u2_m1 = graph.createLoans(user2, magazin1);
		loans_u2_m1.set_date(new Date(5, 5, 2011));

		simlibgraph = graph;
	}

}
