package de.uni_koblenz.jgralab.eca;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
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

public class ECAIO {

	/*
	 * EBNF:
	 * 
	 * Rule := [<Context>:] "after"|"before" <Event> ["with" <Condition>] "do"
	 * <Action>
	 * 
	 * Context := (string with GReQL expression that evaluates to a set of graph
	 * elements)
	 * 
	 * Event := createdVertex(<Type>) | createdEdge(<Type>) |
	 * updatedAttributeValue(<Type>,<Attribute>) | updatedStartVertex(<Type>) |
	 * updatedEndVertex(<Type>) | updatedStartOrEndVertex(<Type>) |
	 * deletedVertex(<Type>) | deletedEdge(<Type>)
	 * 
	 * Condition := (string with boolean GReQL expression)
	 * 
	 * Action := "print" <String> | (name of user defined action) | (name of
	 * GReTL Transformation class)
	 * 
	 * Type := (string with qualified name of monitoring GraphElement)
	 * 
	 * Attribute := (string, representing name of Attribute)
	 */

	// #########################################################################
	// ++++++++ public static Methods - behavior to the outside ++++++++++++++++
	// #########################################################################

	/**
	 * Loads ECARules from a given file using a Schema to get Vertex- and
	 * EdgeClasses
	 * 
	 * @param schema
	 *            Schema to load Vertex- and EdgeClasses
	 * @param filename
	 *            location of the ".eca" file
	 * @return list of loaded ECA rules
	 * @throws ECAIOException
	 */
	public static List<ECARule> loadECArules(Schema schema, String filename)
			throws ECAIOException {

		BufferedInputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(
					new FileInputStream(filename), BUFFER_SIZE);

			ECAIO ecaLoader = new ECAIO();
			ecaLoader.inStream = inputStream;
			ecaLoader.schema = schema;
			ecaLoader.load();

			return ecaLoader.rules;

		} catch (IOException e) {
			throw new ECAIOException("Error while reading file " + filename);
		} finally {
			close(inputStream);
		}
	}

	/**
	 * Save ECA rules to file
	 * 
	 * @param schema
	 *            Schema corresponding to the rules
	 * @param filename
	 *            file to save rules
	 * @param rules
	 *            list of ECA rules
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
	 */
	private ECAIO() {
		rules = new ArrayList<ECARule>();
	}

	// #########################################################################
	// ++++++++ Saving ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	/**
	 * Save all given ECARules
	 */
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
		// Save EventDescription
		saveEventDescriptionToStream(rule.getEventDescription());
		// Save Condition if there is one
		if (rule.getCondition() != null) {
			saveConditionToStream(rule.getCondition());
		}
		// Save Action
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
		String eventDescriptionString = "";
		// context?
		if (ev.getContext().equals(EventDescription.Context.EXPRESSION)) {
			eventDescriptionString += ev.getContextExpression();
			eventDescriptionString += " : ";
		}
		// 'before' or 'after'
		if (ev.getTime().equals(EventTime.AFTER)) {
			eventDescriptionString += "after ";
		} else {
			eventDescriptionString += "before ";
		}

		// adding Type, e.g. createdVertex(VertexClass1)
		eventDescriptionString += getEventDescriptionType(ev);

		// write the EventDescription to the output stream
		writeToStream(eventDescriptionString);
	}

	/**
	 * Determines the String representation of an EventDescription type
	 * 
	 * @param ev
	 *            the EventDescription
	 * @return the type of an EventDescription as String, e.g.
	 *         createdVertex(VertexClass1)
	 */
	private String getEventDescriptionType(EventDescription ev) {
		if (ev instanceof CreateVertexEventDescription) {
			return "createdVertex(" + getEventElementTypeString(ev) + ") ";
		} else if (ev instanceof DeleteVertexEventDescription) {
			return "deletedVertex(" + getEventElementTypeString(ev) + ") ";
		} else if (ev instanceof ChangeAttributeEventDescription) {
			return "updatedAttributeValue("
					+ getEventElementTypeString(ev)
					+ ", "
					+ ((ChangeAttributeEventDescription) ev)
							.getConcernedAttribute() + ")";
		} else if (ev instanceof ChangeEdgeEventDescription) {
			if (((ChangeEdgeEventDescription) ev).getEdgeEnd().equals(
					EdgeEnd.ALPHA)) {
				return "updatedStartVertex(" + getEventElementTypeString(ev)
						+ ") ";
			} else if (((ChangeEdgeEventDescription) ev).getEdgeEnd().equals(
					EdgeEnd.OMEGA)) {
				return "updatedEndVertex(" + getEventElementTypeString(ev)
						+ ") ";
			} else {
				return "updatedStartOrEndVertex("
						+ getEventElementTypeString(ev) + ") ";
			}
		} else if (ev instanceof CreateEdgeEventDescription) {
			return "createdEdge(" + getEventElementTypeString(ev) + ") ";
		} else {
			return "deletedEdge(" + getEventElementTypeString(ev) + ") ";
		}

	}

	/**
	 * Determines the type of the Vertex or Edge that a given EventDescription
	 * monitors
	 * 
	 * @param ev
	 *            EventDescription to get the type
	 * @return the type of the monitored Edge or Vertex as string or "" if the
	 *         elements are filtered by context
	 */
	private String getEventElementTypeString(EventDescription ev) {
		String nameOfGraphElementClass = "";
		if (ev.getContext().equals(EventDescription.Context.TYPE)) {
			nameOfGraphElementClass += ev.getType().getName()
					.replace(schema.getPackagePrefix() + ".", "");
		}
		return nameOfGraphElementClass;
	}

	/**
	 * Save Condition as String to output stream
	 * 
	 * @param cond
	 * @throws ECAIOException
	 */
	private void saveConditionToStream(Condition cond) throws ECAIOException {
		writeToStream("with \"" + cond.getConditionExpression() + "\" ");
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
			actionstring += "print \"";
			actionstring += ((PrintAction) act).getMessage();
			actionstring += "\"";
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
	 * Internal load method
	 * 
	 * @throws ECAIOException
	 */
	private void load() throws ECAIOException {
		la = 0;
		// parse ECARules until end of stream
		while (la != -1) {
			parseRule();
			skipWs();
		}
	}

	// ######################################################################

	/**
	 * Match with parsing EventDescription, Condition and Action
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
	 * Parses the EventDescription
	 * 
	 * @return the parsed EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription parseEventDescription() throws ECAIOException {

		// Check whether a context is given
		String context = null;
		String currentToken = nextToken();
		String lookAheadToken = nextToken();
		if (lookAheadToken.equals(":")) {
			context = currentToken;
			currentToken = nextToken();
			lookAheadToken = nextToken();
		}

		// Get the EventTime
		EventTime et = getEventTime(currentToken);

		// Get the Type of the EventDescription
		String eventdestype = lookAheadToken;

		// Get the Type of the AttributedElement if there is one
		String type = null;
		match("(");
		if (context == null) {
			type = nextToken();
		}

		// Create an EventDescription depending on the Type

		// -- ChangeAttributeEventDescription

		if (eventdestype.equals("updatedAttributeValue")) {
			match(",");
			return finishChangeAttributeEventDescription(context, et, type);
		} else {
			match(")");
			// -- CreateVertexEventDescription
			if (eventdestype.equals("createdVertex")) {
				return finishCreateVertexEvent(context, et, type);
			}
			// -- CreateEdgeEventDescription
			else if (eventdestype.equals("createdEdge")) {
				return finishCreateEdgeEventDescription(context, et, type);
			}
			// -- ChangeEdgeEventDescription
			else if (eventdestype.equals("updatedStartVertex")) {
				return finishChangeEdgeEventDescription(context, et, type,
						EdgeEnd.ALPHA);
			}
			// -- ChangeEdgeEventDescription
			else if (eventdestype.equals("updatedEndVertex")) {
				return finishChangeEdgeEventDescription(context, et, type,
						EdgeEnd.OMEGA);
			}
			// -- ChangeEdgeDescription
			else if (eventdestype.equals("updatedStartOrEndVertex")) {
				return finishChangeEdgeEventDescription(context, et,
						type, EdgeEnd.ANY);
			}
			// -- DeleteVertexEventDescription
			else if (eventdestype.equals("deletedVertex")) {
				return finishDeleteVertexEventDescription(context, et, type);
			}
			// -- DeleteEdgeEventDescription
			else if (eventdestype.equals("deletedEdge")) {
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
								+ "\"updatedStartOrEndVertex\""
								+ "\"changeAttributeValue");
			}
		}
	}

	/**
	 * Determines the EventTime
	 * 
	 * @param eventTimeString
	 *            EventTime as String "before" or "after"
	 * @return the resulting EventTime
	 * @throws ECAIOException
	 */
	private EventTime getEventTime(String eventTimeString)
			throws ECAIOException {
		EventTime eventTime;
		if (eventTimeString.equals("after")) {
			eventTime = EventTime.AFTER;
		} else if (eventTimeString.equals("before")) {
			eventTime = EventTime.BEFORE;
		} else {
			throw new ECAIOException(
					"EventTime expected. Possible are \"before\" and \"after\". Found: \""
							+ eventTimeString + "\" ");
		}
		return eventTime;
	}

	/**
	 * Create a DeleteEdgeEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' or 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishDeleteEdgeEventDescription(String context,
			EventTime eventTime, String qualNameOfGraphElementToMonitor)
			throws ECAIOException {
		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new DeleteEdgeEventDescription(eventTime, context);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new DeleteEdgeEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and GraphElementType: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
	}

	/**
	 * Create a DeleteVertexEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' or 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishDeleteVertexEventDescription(String context,
			EventTime eventTime, String qualNameOfGraphElementToMonitor)
			throws ECAIOException {
		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new DeleteVertexEventDescription(eventTime, context);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new DeleteVertexEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
	}

	/**
	 * Create a ChangeEdgeEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' of 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @param edgeEnd
	 *            ALPHA, OMEGA or BOTH
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishChangeEdgeEventDescription(String context,
			EventTime eventTime, String qualNameOfGraphElementToMonitor,
			EdgeEnd edgeEnd) throws ECAIOException {
		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new ChangeEdgeEventDescription(eventTime, context);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new ChangeEdgeEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor),
					edgeEnd);
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
	}

	/**
	 * Create a ChangeAttributeEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' or 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishChangeAttributeEventDescription(
			String context, EventTime eventTime,
			String qualNameOfGraphElementToMonitor) throws ECAIOException {
		String name = nextToken();
		match(")");

		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new ChangeAttributeEventDescription(eventTime, context, name);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new ChangeAttributeEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor), name);
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
	}

	/**
	 * Create a CreateEdgeEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' or 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishCreateEdgeEventDescription(String context,
			EventTime eventTime, String qualNameOfGraphElementToMonitor)
			throws ECAIOException {
		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new CreateEdgeEventDescription(eventTime, context);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new CreateEdgeEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
	}

	/**
	 * Create a CreateVertexEventDescription with the given parameters
	 * 
	 * @param context
	 *            Context of the EventDescription or null if there is none
	 * @param eventTime
	 *            'before' or 'after'
	 * @param qualNameOfGraphElementToMonitor
	 *            qualified name of the GraphElement, the EventDescription
	 *            monitors
	 * @return the created EventDescription
	 * @throws ECAIOException
	 */
	private EventDescription finishCreateVertexEvent(String context,
			EventTime eventTime, String qualNameOfGraphElementToMonitor)
			throws ECAIOException {
		if (context != null && qualNameOfGraphElementToMonitor == null) {
			return new CreateVertexEventDescription(eventTime, context);
		} else if (context == null && qualNameOfGraphElementToMonitor != null) {
			return new CreateVertexEventDescription(eventTime,
					getAttributedElement(qualNameOfGraphElementToMonitor));
		} else {
			throw new ECAIOException(
					"It's necessary to give a context OR a type. Its an XOR. Found: context: \""
							+ context + "\" and type: \""
							+ qualNameOfGraphElementToMonitor + "\"");
		}
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

	// ######################################################################

	/**
	 * Parses the condition
	 * 
	 * @return the condition or null if there is no
	 * @throws ECAIOException
	 */
	private Condition parseCondition() throws ECAIOException {
		String currentToken = nextToken();
		if (isMatching(currentToken, "do")) {
			return null;
		} else if (isMatching(currentToken, "with")) {
			String condexpr = nextToken();
			match("do");
			return new Condition(condexpr);
		} else {
			throw new ECAIOException(
					"Parsing Error. Expected \"do\" or \"with\". Found: \""
							+ currentToken + "\"");
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
		String currentToken = nextToken();
		if (isMatching("print", currentToken)) {
			String message = nextToken();
			return new PrintAction(message);
		} else {
			try {
				Class<?> actionclass = Class.forName(currentToken);
				if (actionclass.getSuperclass().equals(Transformation.class)) {
					return new GretlTransformAction(
							(Class<? extends Transformation<Graph>>) actionclass);
				} else {
					return (Action) actionclass.newInstance();
				}

			} catch (ClassNotFoundException e) {
				throw new ECAIOException("Specified Action " + currentToken
						+ " not found.");
			} catch (InstantiationException e) {
				throw new ECAIOException("Error while instanciating Action "
						+ currentToken);
			} catch (IllegalAccessException e) {
				throw new ECAIOException("Error while instanciating Action "
						+ currentToken);
			}
		}

	}

	// #########################################################################
	// ++++++++ Help-Methods for parsing +++++++++++++++++++++++++++++++++++++++
	// #########################################################################

	/**
	 * Returns whether the two Strings equals each other
	 */
	private boolean isMatching(String one, String two) {
		if (one.equals(two)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Matches a given String to the next token from the stream, throws an
	 * Exception if they doesn't equal
	 * 
	 * @param expected
	 * @throws ECAIOException
	 */
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
	private String nextToken() throws ECAIOException {
		StringBuilder out = new StringBuilder();

		try {
			skipWs();
			if (la == '"') {
				readUtfString(out);
			} else if (isUnitSymbol(la)) {
				out.append((char) la);
				la = inStream.read();
			} else {
				if (la != -1) {
					do {
						out.append((char) la);
						la = inStream.read();
					} while (!isWs(la) && !isUnitSymbol(la) && (la != -1));
				}
			}
		} catch (IOException e) {
			throw new ECAIOException(
					"Error while reading next token from stream.");
		}

		return myTrim0(out.toString());
	}

	/**
	 * Read the input stream as long as there are only whitespaces
	 * 
	 * @throws ECAIOException
	 */
	private final void skipWs() throws ECAIOException {
		while (isWs(la) || la == 0) {
			try {
				la = inStream.read();
			} catch (IOException e) {
				throw new ECAIOException("Error while reading from stream.");
			}
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

	/**
	 * @return whether a given char is whitespace
	 */
	private boolean isWs(int c) {
		return (c == ' ') || (c == '\n') || (c == '\t') || (c == '\r');
	}

	/**
	 * @return whether a given char is a unit symbol
	 */
	private boolean isUnitSymbol(int c) {
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
			if (la == 0) {
				la = inStream.read();
				continue;
			}
			if ((la < 32) || (la > 127)) {
				throw new RuntimeException("invalid character '" + (char) la
						+ "'");
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
