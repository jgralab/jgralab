package de.uni_koblenz.jgralabtest.eca;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.eca.Action;
import de.uni_koblenz.jgralab.eca.Condition;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.PrintAction;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.Book;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.Library;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.Loans;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.Magazin;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.MediaType;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.NewMedia;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.SimpleLibraryGraph;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.SimpleLibrarySchema;
import de.uni_koblenz.jgralabtest.eca.schemas.simplelibrary.User;
import de.uni_koblenz.jgralabtest.eca.useractions.CreateAVertexOfSameTypeAction;
import de.uni_koblenz.jgralabtest.eca.useractions.PrintNewAndOldAttributeValueAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingAction;
import de.uni_koblenz.jgralabtest.eca.useractions.RevertEdgeChangingOnHighesLevelAction;

public class ECATest {

	private static SimpleLibraryGraph simlibgraph;
	private static User user1;
	private static User user2;
	private static Book book1;
	private static NewMedia newmedia1;
	private static Loans loans_u1_b1;


	@BeforeClass
	public static void setUp() {
		System.out.println("Start ECA Test.");
		JGraLab.setLogLevel(Level.OFF);
		initGraph();
	}

	@AfterClass
	public static void tearDown() {
		System.out.println("Finish ECA Test.");
	}


	@Test
	public void testDeleteVertexEvent() {

		Book newBook = simlibgraph.createBook();

		EventDescription bef_ev = new DeleteVertexEventDescription(EventDescription.EventTime.BEFORE, Book.class);
		Action bef_act = new PrintAction(
				"ECA Test Message: Book Vertex will become deleted.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new DeleteVertexEventDescription(EventDescription.EventTime.AFTER, Book.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: Book Vertex is deleted.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.deleteVertex(newBook);

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);

	}

	@Test
	public void testCreateVertexEvent() {
		EventDescription bef_ev = new CreateVertexEventDescription(EventDescription.EventTime.BEFORE, Book.class);
		Action bef_act = new PrintAction(
				"ECA Test Message: New Book Vertex will become created.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER, Book.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: New Book Vertex is created.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.createBook();

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);

	}

	@Test
	public void testDeleteEdgeEvent() {
		EventDescription bef_ev = new DeleteEdgeEventDescription(EventDescription.EventTime.BEFORE, Loans.class);
		Action bef_act = new PrintAction(
				"ECA Test Message: Loans Edge will become deleted.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new DeleteEdgeEventDescription(EventDescription.EventTime.AFTER, Loans.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: Loans Edge is deleted.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		Loans newLoans = simlibgraph.createLoans(user1, newmedia1);
		simlibgraph.deleteEdge(newLoans);

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);

	}

	@Test
	public void testCreateEdgeEvent() {
		EventDescription bef_ev = new CreateEdgeEventDescription(EventDescription.EventTime.BEFORE, Loans.class);
		Action bef_act = new PrintAction(
				"ECA Test Message: New Loans Edge will become created.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new CreateEdgeEventDescription(EventDescription.EventTime.AFTER, Loans.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: New Loans Edge is created.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.createLoans(user1, newmedia1);

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);

	}

	@Test
	public void testChangeEdgeEvent() {
		EventDescription bef_ev = new ChangeEdgeEventDescription(EventDescription.EventTime.BEFORE, Loans.class);
		Action bef_act = new PrintAction(
				"ECA Test Message: Loans Edge will become changed.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new ChangeEdgeEventDescription(EventDescription.EventTime.AFTER, Loans.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: Loans Edge is changed.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		loans_u1_b1.setAlpha(user2);

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test
	public void testChangeAttributeEvent() {
		EventDescription bef_ev = new ChangeAttributeEventDescription(EventDescription.EventTime.BEFORE,
				Book.class, "title");
		Action bef_act = new PrintAction(
				"ECA Test Message: Title of Book Vertex will become changed.");
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		EventDescription aft_ev = new ChangeAttributeEventDescription(EventDescription.EventTime.AFTER,
				Book.class, "title");
		Action aft_act = new PrintAction(
				"ECA Test Message: Title of Book Vertex is changed.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		book1.set_title("The Return of the King");

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test
	public void testGrequlContextOnEvent() {
		EventDescription aft_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER, "V{Medium}");
		Action aft_act = new PrintAction(
				"ECA Test Message: New Medium created.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.createBook();

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);

	}

	@Test
	public void testCondition() {
		EventDescription aft_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER,
				NewMedia.class);
		Condition aft_cond = new Condition("count( V{NewMedia} ) = 2");
		Action aft_act = new PrintAction(
				"ECA Test Message: New Medium after Condition Test created. "
						+ "This message should appear only once.");
		ECARule aft_rule = new ECARule(aft_ev, aft_cond, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.createNewMedia();
		simlibgraph.createNewMedia();

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test(expected = RuntimeException.class)
	public void testAddEventTo2Graphs() {
		SimpleLibraryGraph newGraph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph();

		EventDescription aft_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER,
				Library.class);

		Action aft_act = new PrintAction(
				"ECA Test Message: Failure Test old Graph.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);

		Action aft_actN = new PrintAction(
				"ECA Test Message: Failure Test new Graph.");
		ECARule aft_ruleN = new ECARule(aft_ev, aft_actN);

		simlibgraph.getECARuleManager().addECARule(aft_rule);
		newGraph.getECARuleManager().addECARule(aft_ruleN);

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test(expected = RuntimeException.class)
	public void testAddRuleTo2Graphs() {
		SimpleLibraryGraph newGraph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph();

		EventDescription aft_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER,
				Library.class);
		Action aft_act = new PrintAction(
				"ECA Test Message: Failure Test two Graphs.");
		ECARule aft_rule = new ECARule(aft_ev, aft_act);

		simlibgraph.getECARuleManager().addECARule(aft_rule);
		newGraph.getECARuleManager().addECARule(aft_rule);

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}


	@Test
	public void testNeverEndingCreationStop() {
		EventDescription bef_ev = new CreateVertexEventDescription(EventDescription.EventTime.BEFORE, User.class);
		Action bef_act = new CreateAVertexOfSameTypeAction();
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		// allow only 5 nested triggers
		simlibgraph.getECARuleManager().setMaxNestedTriggerCalls(5);

		simlibgraph.createUser();

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
	}

	@Test
	public void testGenerationOf20Users() {
		EventDescription bef_ev = new CreateVertexEventDescription(EventDescription.EventTime.AFTER, User.class);
		Condition aft_cond = new Condition("count (V{User}) < 20");
		Action bef_act = new CreateAVertexOfSameTypeAction();
		ECARule bef_rule = new ECARule(bef_ev, aft_cond, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		simlibgraph.getECARuleManager().setMaxNestedTriggerCalls(50);

		simlibgraph.createUser();

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
	}

	@Test
	public void testGettingOldAndNewValueFromBeforeAttributeChanging() {
		EventDescription bef_ev = new ChangeAttributeEventDescription(EventDescription.EventTime.BEFORE,
				Book.class, "title");
		Action bef_act = new PrintNewAndOldAttributeValueAction();
		ECARule bef_rule = new ECARule(bef_ev, bef_act);
		simlibgraph.getECARuleManager().addECARule(bef_rule);

		book1.set_title("Silmarillion");

		simlibgraph.getECARuleManager().deleteECARule(bef_rule);
	}

	@Test
	public void testGettingOldAndNewValueFromAfterAttributeChanging() {

		EventDescription aft_ev = new ChangeAttributeEventDescription(EventDescription.EventTime.AFTER,
				Book.class, "title");
		Action aft_act = new PrintNewAndOldAttributeValueAction();
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		book1.set_title("The Hobbit");

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test
	public void testRevertChangedEdge() {
		EventDescription aft_ev = new ChangeEdgeEventDescription(EventDescription.EventTime.AFTER, Loans.class);
		Condition aft_cond = new Condition(
				"startVertex(context).name = \"Martin King\"");
		Action aft_act = new RevertEdgeChangingAction();
		ECARule aft_rule = new ECARule(aft_ev, aft_cond, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		loans_u1_b1.setAlpha(user1);

		assertEquals(loans_u1_b1.getAlpha(), user2);

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	@Test
	public void testRevertChangedEdge2() {
		EventDescription aft_ev = new ChangeEdgeEventDescription(EventDescription.EventTime.AFTER, Loans.class);
		Action aft_act = new RevertEdgeChangingOnHighesLevelAction();
		ECARule aft_rule = new ECARule(aft_ev, aft_act);
		simlibgraph.getECARuleManager().addECARule(aft_rule);

		loans_u1_b1.setAlpha(user1);

		assertEquals(loans_u1_b1.getAlpha(), user2);

		simlibgraph.getECARuleManager().deleteECARule(aft_rule);
	}

	static void initGraph() {
		SimpleLibraryGraph graph = SimpleLibrarySchema.instance()
				.createSimpleLibraryGraph();
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
		loans_u1_b1.set_date(graph.createDate(1, 1, 2011));

		// user 2 loans magazin1
		Loans loans_u2_m1 = graph.createLoans(user2, magazin1);
		loans_u2_m1.set_date(graph.createDate(5, 5, 2011));

		simlibgraph = graph;
	}

}
