package de.uni_koblenz.jgralab.eca;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription.EdgeEnd;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.gretl.eca.GretlTransformAction;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class ECAIO {

	// ////////////////////////////////////////////////////////////////////////////
	// // -- Only for testing -- /////////////////////////////////////////////

	public static void main(String[] args) throws GraphIOException,
			ECAIOException {
		Schema schema = GraphIO
				.loadSchemaFromFile("testit/testschemas/eca/SimpleLibrarySchema.tg");
		schema.compile(CodeGeneratorConfiguration.NORMAL);
		List<ECARule> list = ECAIO.loadECArules(schema, "../../rule1test.txt");
		ECARule rule = list.get(0);
		// Creating a graph
		Method graphCreateMethod = schema
				.getGraphCreateMethod(ImplementationType.STANDARD);
		Object[] a = { "ExampleGraph", 40, 50 };
		try {
			Graph graph = (Graph) graphCreateMethod.invoke(null, a);

			ECARuleManager ecaRuleManager = (ECARuleManager) graph
					.getECARuleManager();
			ecaRuleManager.addECARule(rule);
			ecaRuleManager.addECARule(list.get(1));

			VertexClass vc = (VertexClass) schema
					.getAttributedElementClass("Book");

			graph.createVertex(vc.getM1Class());

			ECAIO.saveECArules(schema, "../../rule2test.txt", list);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	// #########################################################################
	// ++++++++ public static Methods - behavior to the outside ++++++++++++++++
	// #########################################################################

	/**
	 * Loads ECARules from a given File using a schema to get Vertex- and
	 * EdgeClasses
	 * 
	 * @param schema
	 * @param filename
	 * @return
	 * @throws ECAIOException
	 */
	public static List<ECARule> loadECArules(Schema schema, String filename)
			throws ECAIOException {

		try {
			FileInputStream fileStream = new FileInputStream(filename);
			BufferedInputStream inputStream = new BufferedInputStream(
					fileStream, BUFFER_SIZE);

			ECAIO ecaLoader = new ECAIO();

			ecaLoader.inStream = inputStream;
			ecaLoader.schema = schema;

			ecaLoader.load();

			return ecaLoader.rules;

		} catch (IOException e) {
			throw new ECAIOException("Error while reading file " + filename);
		}
	}

	/**
	 * Save ECA rules to file
	 * 
	 * @param filename
	 * @param rules
	 * @throws ECAIOException
	 */
	public static void saveECArules(Schema schema, String filename,
			List<ECARule> rules) throws ECAIOException {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(filename), BUFFER_SIZE));
			ECAIO ecaSaver = new ECAIO();
			ecaSaver.rules = rules;
			ecaSaver.schema = schema;
			ecaSaver.outStream = out;
			ecaSaver.save();
		} catch (IOException ex) {
			throw new ECAIOException("Error while saving ECA rules to "
					+ filename);
		} finally {
			close(out);
		}
	}

	// #########################################################################
	// ++++++++ Members ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	private static final int BUFFER_SIZE = 65536;

	/**
	 * Schema to get Vertex- and EdgeClasses by name
	 */
	private Schema schema;

	/**
	 * List with loaded rules or to be saved rules
	 */
	private List<ECARule> rules;

	/**
	 * InputStream to read
	 */
	private BufferedInputStream inStream;

	/**
	 * OutputStream to write
	 */
	private DataOutputStream outStream;

	/**
	 * last character read from inputStream
	 */
	int la;

	// #########################################################################
	// ++++++++ Constructor ++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################
	/**
	 * 
	 * @param in
	 */
	private ECAIO() {
		rules = new ArrayList<ECARule>();
	}

	// #########################################################################
	// ++++++++ Saving ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	private void save() throws ECAIOException {
		for (ECARule rule : rules) {
			saveRule(rule);
		}
	}

	/**
	 * Save an ECARule to output stream
	 * 
	 * @param rule
	 * @throws ECAIOException
	 */
	private void saveRule(ECARule rule) throws ECAIOException {
		saveEventDescriptionToStream(rule.getEventDescription());
		if (rule.getCondition() != null) {
			saveConditionToStream(rule.getCondition());
		}
		saveActionToStream(rule.getAction());
	}

	/**
	 * Save an EventDescription to output stream
	 * 
	 * @param ev
	 * @throws ECAIOException
	 */
	private void saveEventDescriptionToStream(EventDescription ev)
			throws ECAIOException {
		String eventstring = "";
		if (ev.getContext().equals(EventDescription.Context.EXPRESSION)) {
			eventstring += "<\"";
			eventstring += ev.getContextExpression();
			eventstring += "\"> ";
		}
		if (ev.getTime().equals(EventTime.AFTER)) {
			eventstring += "after ";
		} else {
			eventstring += "before ";
		}

		eventstring += getEventDescriptionType(ev);

		writeToStream(eventstring);
	}

	/**
	 * Determines the String representation of an EventDescription
	 * 
	 * @param ev
	 *            the EventDescription
	 * @return the EventDescription as String
	 */
	private String getEventDescriptionType(EventDescription ev) {
		if (ev instanceof CreateVertexEventDescription) {
			return "createVertex(" + getEventElementTypeString(ev) + ") ";
		} else if (ev instanceof DeleteVertexEventDescription) {
			return "deleteVertex(" + getEventElementTypeString(ev) + ") ";
		} else if (ev instanceof ChangeAttributeEventDescription) {
			return "updatedAttributeValue("
					+ getEventElementTypeString(ev)
					+ ", <"
					+ ((ChangeAttributeEventDescription) ev)
							.getConcernedAttribute();
		} else if (ev instanceof ChangeEdgeEventDescription) {
			if (((ChangeEdgeEventDescription) ev).getEdgeEnd().equals(
					EdgeEnd.ALPHA)) {
				return "updatedStartVertex(";
			} else if (((ChangeEdgeEventDescription) ev).getEdgeEnd().equals(
					EdgeEnd.OMEGA)) {
				return "updatedEndVertex(";
			} else {
				// TODO decide whether BOTH is although possible
				return "";
			}
		} else if (ev instanceof CreateEdgeEventDescription) {
			return "createEdge(" + getEventElementTypeString(ev) + ") ";
		} else {
			return "deleteEdge(" + getEventElementTypeString(ev) + ") ";
		}

	}

	/**
	 * Determines the type that a given EventDescription monitors
	 * 
	 * @param ev
	 *            EventDescription to get the type
	 * @return the type as string or "" if the elements are filtered by context
	 */
	private String getEventElementTypeString(EventDescription ev) {
		String eventstring = "";
		if (ev.getContext().equals(EventDescription.Context.TYPE)) {
			eventstring += "<";
			eventstring += ev.getType().getName().replace(
					schema.getPackagePrefix() + ".", "");
			eventstring += ">";
		}
		return eventstring;
	}

	/**
	 * Save Condition as String to output stream
	 * 
	 * @param cond
	 * @throws ECAIOException
	 */
	private void saveConditionToStream(Condition cond) throws ECAIOException {
		writeToStream("with <\"" + cond.getConditionExpression() + "\"> ");
	}

	/**
	 * Save Action as String to output stream
	 * 
	 * @param act
	 * @throws ECAIOException
	 */
	private void saveActionToStream(Action act) throws ECAIOException {
		String actionstring = "do ";
		if (act instanceof PrintAction) {
			actionstring += "<\"";
			actionstring += ((PrintAction) act).getMessage();
			actionstring += "\">";
			actionstring += "\n";
		} else if (act instanceof GretlTransformAction) {
			GretlTransformAction gta = ((GretlTransformAction) act);
			actionstring += gta.getTransformationClass().getName();
		} else {
			actionstring += act.getClass().getName();
		}

		writeToStream(actionstring);
	}

	/**
	 * Write a given text to output stream
	 * 
	 * @param text
	 * @throws ECAIOException
	 */
	private void writeToStream(String text) throws ECAIOException {
		try {
			outStream.writeChars(text);
		} catch (IOException e) {
			throw new ECAIOException("Error while writing " + text
					+ " to stream.");
		}
	}

	// #########################################################################
	// ++++++++ Loading ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	/**
	 * Internal load Method
	 * 
	 * @throws ECAIOException
	 */
	private void load() throws ECAIOException {

		try {

			la = inStream.read();
			// parse Rules until the Stream is finished
			while (la != -1) {
				parseRule();
				skipWs();
			}
			inStream.close();

		} catch (IOException e) {
			throw new ECAIOException("Error while reading File.");
		}

	}

	// ######################################################################

	/**
	 * Match with parsing Event, Condition and Action
	 * 
	 * @throws ECAIOException
	 */
	private void parseRule() throws ECAIOException {
		EventDescription ed = parseEventDescription();
		Condition cond = parseCondition();
		Action action = parseAction();

		if (cond == null) {
			rules.add(new ECARule(ed, action));
		} else {
			rules.add(new ECARule(ed, cond, action));
		}
	}

	// ######################################################################

	/**
	 * 
	 * @return
	 * @throws ECAIOException
	 */
	private EventDescription parseEventDescription() throws ECAIOException {

		// Check whether a context is given
		String next = nextToken();
		String context = null;
		if (next.equals("<")) {
			context = nextToken();
			match(">");
			match(":");
			next = nextToken();
		}

		// Get the EventTime
		EventTime et = getEventTime(next);

		// Get the Type of the EventDescription
		String eventdestype = nextToken();

		// Get the Type of the AttributedElement if there is one
		String type = null;
		match("(");
		String test = nextToken();
		if (isMatching(test, "<")) {
			type = nextToken();
			match(">");
			test = nextToken();
		}
		if (!isMatching(test, ")") && !isMatching(test, ",")) {
			throw new ECAIOException(
					"Error while parsing Event. ')' or ',' expected, found '"
							+ test + "'.");
		}

		// Create an EventDescription depending on the Type
		// -- CreateVertexEventDescription
		if (eventdestype.equals("createVertex")) {
			return finishCreateVertexEvent(context, et, type);
		}
		// -- CreateEdgeEventDescription
		else if (eventdestype.equals("createEdge")) {
			return finishCreateEdgeEventDescription(context, et, type);
		}
		// -- ChangeAttributeEventDescription
		else if (eventdestype.equals("updatedAttributeValue")) {
			return finishChangeAttributeEventDescription(context, et, type);
		}
		// -- ChangeEdgeEventDescription
		else if (eventdestype.equals("updatedStartVertex")) {
			return finishChangeAlphaOfEdgeEventDescription(context, et, type);
		}
		// -- ChangeEdgeEventDescription
		else if (eventdestype.equals("updatedEndVertex")) {
			return finishChangeOmegaOfEdgeEventDescription(context, et, type);
		}
		// -- DeleteVertexEventDescription
		else if (eventdestype.equals("deleteVertex")) {
			return finishDeleteVertexEventDescription(context, et, type);
		}
		// -- DeleteEdgeEventDescription
		else if (eventdestype.equals("deleteEdge")) {
			return finishDeleteEdgeEventDescription(context, et, type);
		}
		// -- wrong syntax
		else {
			throw new ECAIOException(
					"Type of EventDescription not recognized. Found "
							+ eventdestype
							+ " Possible are \"createVertex\", \"deleteVertex\", "
							+ "\"createEdge\", \"deleteEdge\", "
							+ "\"updatedStartVertex\", \"updatedEndVertex\", "
							+ "\"changeAttributeValue");
		}
	}

	private EventDescription finishDeleteEdgeEventDescription(String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new DeleteEdgeEventDescription(et, context);
		} else if (context == null && type != null) {
			return new DeleteEdgeEventDescription(et,
					getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishDeleteVertexEventDescription(String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new DeleteVertexEventDescription(et, context);
		} else if (context == null && type != null) {
			return new DeleteVertexEventDescription(et,
					getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishChangeAlphaOfEdgeEventDescription(
			String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new ChangeEdgeEventDescription(et, context);
		} else if (context == null && type != null) {
			return new ChangeEdgeEventDescription(et,
					getAttributedElement(type), EdgeEnd.ALPHA);
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishChangeOmegaOfEdgeEventDescription(
			String context, EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new ChangeEdgeEventDescription(et, context);
		} else if (context == null && type != null) {
			return new ChangeEdgeEventDescription(et,
					getAttributedElement(type), EdgeEnd.OMEGA);
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishChangeAttributeEventDescription(
			String context, EventTime et, String type) throws ECAIOException {
		match("<");
		String name = nextToken();
		match(">");
		match(")");

		if (context != null && type == null) {
			return new ChangeAttributeEventDescription(et, context, name);
		} else if (context == null && type != null) {
			return new ChangeAttributeEventDescription(et,
					getAttributedElement(type), name);
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishCreateEdgeEventDescription(String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new CreateEdgeEventDescription(et, context);
		} else if (context == null && type != null) {
			return new CreateEdgeEventDescription(et,
					getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishCreateVertexEvent(String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new CreateVertexEventDescription(et, context);
		} else if (context == null && type != null) {
			return new CreateVertexEventDescription(et,
					getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	/**
	 * Determines the EventTime
	 * 
	 * @param next
	 * @return
	 * @throws ECAIOException
	 */
	private EventTime getEventTime(String next) throws ECAIOException {
		EventTime et;
		if (next.equals("after")) {
			et = EventTime.AFTER;
		} else if (next.equals("before")) {
			et = EventTime.BEFORE;
		} else {
			String before = "before";
			for (int i = 0; i < next.length(); i++) {
				System.out.println(i + ":  " + (int) next.charAt(i));
			}
			for (int i = 0; i < before.length(); i++) {
				System.out.println(i + ":  " + (int) before.charAt(i));
			}
			throw new ECAIOException(
					"EventTime expected. Possible are \"before\" and \"after\". Found: \""
							+ next + "\" " + next.equals("before")
							+ next.concat("before") + next.hashCode()
							+ before.hashCode());
		}
		return et;
	}

	// ######################################################################

	/**
	 * Parses the condition
	 * 
	 * @return the condition or null if there is no
	 * @throws ECAIOException
	 */
	private Condition parseCondition() throws ECAIOException {
		String next = nextToken();
		if (isMatching(next, "do")) {
			return null;
		} else if (isMatching(next, "with")) {
			match("<");
			String condexpr = nextToken();
			match(">");
			match("do");
			return new Condition(condexpr);
		} else {
			throw new ECAIOException(
					"Parsing Error. Expected \"do\" or \"with\". Found: \""
							+ next + "\"");
		}
	}

	// ######################################################################

	/**
	 * Parses the Action
	 * 
	 * @return the resulting Action
	 * @throws ECAIOException
	 */
	@SuppressWarnings("unchecked")
	private Action parseAction() throws ECAIOException {
		String next = nextToken();
		if (isMatching("<", next)) {
			String print = nextToken();
			match(">");
			return new PrintAction(print);
		} else {
			try {
				Class<?> actionclass = Class.forName(next);
				if (actionclass.getSuperclass().equals(Transformation.class)) {
					return new GretlTransformAction(
							(Class<? extends Transformation<Graph>>) actionclass);
				} else {
					return (Action) actionclass.newInstance();
				}

			} catch (ClassNotFoundException e) {
				throw new ECAIOException("Specified Action " + next
						+ " not found.");
			} catch (InstantiationException e) {
				throw new ECAIOException("Error while instanciating Action "
						+ next);
			} catch (IllegalAccessException e) {
				throw new ECAIOException("Error while instanciating Action "
						+ next);
			}
		}

	}

	// #########################################################################
	// ++++++++ Help-Methods for parsing +++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	private boolean isMatching(String one, String two) {
		if (one.equals(two)) {
			return true;
		} else {
			return false;
		}
	}

	private void match(String expected) throws ECAIOException {
		String token = nextToken();
		if (!token.equals(expected)) {
			throw new ECAIOException("Parsing Error: Expected \"" + expected
					+ "\" Found: \"" + token + "\"");
		}
	}

	// #########################################################################
	// ++++++++ Tokenizing ++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	/**
	 * @return the next Token from the inputString
	 */
	private String nextToken() {
		StringBuilder out = new StringBuilder();

		try {
			skipWs();
			if (la == '"') {
				readUtfString(out);
			} else if (isBracket(la)) {
				System.out.println("bracket " + la);
				out.append((char) la);
				la = inStream.read();
			} else {
				if (la != -1) {
					do {
						out.append((char) la);
						la = inStream.read();
					} while (!isWs(la) && !isBracket(la) && (la != -1));
				}
			}
		} catch (IOException e) {

		}

		System.out.println("CURRENT_TOKEN: " + out.toString());
		return myTrim0(out.toString());
	}

	private final void skipWs() throws IOException {
		while (isWs(la) || la == 0) {
			la = inStream.read();
		}
	}

	private String myTrim0(String x) {
		char[] ar = x.toCharArray();
		String ex = "";
		for (int i = 0; i < ar.length; i++) {
			if (ar[i] != 0) {
				ex += ar[i];
			}
		}
		return ex;

	}

	private boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	private boolean isBracket(int c) {
		return (c == '>') || (c == '<') || (c == '(') || (c == ')')
				|| (c == ',');
	}

	/**
	 * Reads String in quotes as one token - copied from GraphIO
	 * 
	 * @param out
	 * @throws IOException
	 */
	private final void readUtfString(StringBuilder out) throws IOException {
		la = inStream.read();
		LOOP: while ((la != -1) && (la != '"')) {
			if ((la < 32) || (la > 127)) {
				throw new RuntimeException("invalid character '" + (char) la);
			}
			if (la == '\\') {
				la = inStream.read();
				if (la == -1) {
					break LOOP;
				}
				switch (la) {
				case '\\':
					la = '\\';
					break;
				case '"':
					la = '"';
					break;
				case 'n':
					la = '\n';
					break;
				case 'r':
					la = '\r';
					break;
				case 't':
					la = '\t';
					break;
				case 'u':
					la = inStream.read();
					if (la == -1) {
						break LOOP;
					}
					String unicode = "" + (char) la;
					la = inStream.read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					la = inStream.read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					la = inStream.read();
					if (la == -1) {
						break LOOP;
					}
					unicode += (char) la;
					try {
						la = Integer.parseInt(unicode, 16);
					} catch (NumberFormatException e) {
						throw new RuntimeException(
								"invalid unicode escape sequence '\\u"
										+ unicode);
					}
					break;
				default:
					throw new RuntimeException(
							"invalid escape sequence in string");
				}
			}
			out.append((char) la);
			la = inStream.read();
		}

		la = inStream.read();
	}

	/**
	 * Gets the given AttributedElement
	 * 
	 * @param name
	 * @return
	 */
	private Class<? extends AttributedElement> getAttributedElement(String name) {
		Class<? extends AttributedElement> aecl;
		AttributedElementClass aeclo = schema.getAttributedElementClass(name);
		aecl = aeclo.getM1Class();
		return aecl;
	}

	private static void close(Closeable stream) throws ECAIOException {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ex) {
			throw new ECAIOException("Exception while closing the stream.", ex);
		}
	}

}
