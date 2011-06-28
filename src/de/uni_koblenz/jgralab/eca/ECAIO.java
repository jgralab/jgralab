package de.uni_koblenz.jgralab.eca;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
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

			graph.getECARuleManager().addECARule(rule);
			graph.getECARuleManager().addECARule(list.get(1));

			VertexClass vc = (VertexClass) schema
					.getAttributedElementClass("Book");

			graph.createVertex(vc.getM1Class());

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
			// TODO Auto-generated catch block e.printStackTrace();
		}
		return null;
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
		this.rules = new ArrayList<ECARule>();
	}

	// #########################################################################
	// ++++++++ Loading ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	/**
	 * Internal load Method
	 * 
	 * @throws ECAIOException
	 * 
	 * @throws IOException
	 */
	private void load() throws ECAIOException {

		try {

			this.la = inStream.read();
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
	 * Match the "Rule := " and then parse Event, Condition and Action
	 * 
	 * @throws ECAIOException
	 */
	private void parseRule() throws ECAIOException {
		match("Rule");
		match(":=");

		EventDescription ed = parseEventDescription();
		Condition cond = parseCondition();
		Action action = parseAction();

		if (cond == null) {
			this.rules.add(new ECARule(ed, action));
		} else {
			this.rules.add(new ECARule(ed, cond, action));
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
		String next = this.nextToken();
		String context = null;
		if (next.equals("<")) {
			context = this.nextToken();
			match(">");
			match(":");
			next = this.nextToken();
		}

		// Get the EventTime
		EventTime et = this.getEventTime(next);

		// Get the Type of the EventDescription
		String eventdestype = this.nextToken();

		// Get the Type of the AttributedElement if there is one
		String type = null;
		match("(");
		String test = this.nextToken();
		if (isMatching(test, "<")) {
			type = this.nextToken();
			match(">");
			test = this.nextToken();
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
			return finishChangeEdgeEventDescription(context, et, type);
		}
		// -- ChangeEdgeEventDescription
		else if (eventdestype.equals("updatedEndVertex")) {
			return finishChangeEdgeEventDescription(context, et, type);
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
					this.getAttributedElement(type));
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
					this.getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishChangeEdgeEventDescription(String context,
			EventTime et, String type) throws ECAIOException {
		if (context != null && type == null) {
			return new ChangeEdgeEventDescription(et, context);
		} else if (context == null && type != null) {
			return new ChangeEdgeEventDescription(et,
					this.getAttributedElement(type));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \"" + type + "\"");
		}
	}

	private EventDescription finishChangeAttributeEventDescription(
			String context, EventTime et, String type) throws ECAIOException {
		match("<");
		String name = this.nextToken();
		match(">");
		match(")");

		if (context != null && type == null) {
			return new ChangeAttributeEventDescription(et, context, name);
		} else if (context == null && type != null) {
			return new ChangeAttributeEventDescription(et,
					this.getAttributedElement(type), name);
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
					this.getAttributedElement(type));
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
					this.getAttributedElement(type));
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
			throw new ECAIOException(
					"EventTime expected. Possible are \"before\" and \"after\". Found: \""
							+ next + "\"");
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
		String next = this.nextToken();
		if (isMatching(next, "do")) {
			return null;
		} else if (isMatching(next, "with")) {
			match("<");
			String condexpr = this.nextToken();
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
	private Action parseAction() throws ECAIOException {
		match("<");
		String print = this.nextToken();
		match(">");
		return new PrintAction(print);
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
		String token = this.nextToken();
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
		return out.toString();
	}

	private final void skipWs() throws IOException {
		while (isWs(la)) {
			la = inStream.read();
		}
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

	// Exception
	public class ECAIOException extends Exception {
		private static final long serialVersionUID = 4569564712278582919L;

		public ECAIOException() {
		}

		public ECAIOException(String msg) {
			super(msg);
		}

		public ECAIOException(Throwable t) {
			super(t);
		}

		public ECAIOException(String msg, Throwable t) {
			super(msg, t);
		}
	}

}
