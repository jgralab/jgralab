package de.uni_koblenz.jgralab.eca;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	public static void main(String[] args) throws GraphIOException {
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

	private static final int BUFFER_SIZE = 65536;

	private Schema schema;

	private List<ECARule> rules;

	private BufferedInputStream inStream;

	private ECAIO(BufferedInputStream in) {
		this.inStream = in;
		this.rules = new ArrayList<ECARule>();
	}

	public static List<ECARule> loadECArules(Schema schema, String filename) {

		try {
			FileInputStream fileStream = new FileInputStream(filename);
			BufferedInputStream inputStream = new BufferedInputStream(
					fileStream,
					BUFFER_SIZE);
			ECAIO ecaLoader = new ECAIO(inputStream);
			ecaLoader.schema = schema;
			try {
				ecaLoader.load();
				return ecaLoader.rules;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void load() throws IOException {

		this.la = inStream.read();
		// parse Rules until the Stream is finished
		while (la != -1) {
			parseRule();
			skipWs();
		}
	}
	
	private void parseRule() {
		matchRule();

		EventDescription ed = parseEventDescription();
		Condition cond = parseCondition();
		Action action = parseAction();

		if (cond == null) {
			this.rules.add(new ECARule(ed, action));
		} else {
			this.rules.add(new ECARule(ed, cond, action));
		}
		

	}

	private EventDescription parseEventDescription() {

		EventTime et = null;

		String next = this.nextToken();
		String context = null;
		if (next.equals("<")) {
			context = matchContext();
			next = this.nextToken();
		}
		et = this.getEventTime(next);

		String eventdestype = this.nextToken();
		System.out.println(eventdestype);
		EventDescription ed;
		String type = matchType();

		if (eventdestype.equals("createVertex")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new CreateVertexEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new CreateVertexEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}
		} else if (eventdestype.equals("createEdge")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new CreateEdgeEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new CreateEdgeEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}
		} else if (eventdestype.equals("updatedAttributeValue")) {
			if (type != null) {
				staticMatch(",");
			}
			String name = this.matchAttributeName();
			staticMatch(")");
			if (context != null && type == null) {
				ed = new ChangeAttributeEventDescription(et, context, name);
			} else if (context == null && type != null) {
				ed = new ChangeAttributeEventDescription(et,
						this.getAttributedElement(type), name);
			} else {
				throw new RuntimeException();
			}
		} else if (eventdestype.equals("updatedStartVertex")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new ChangeEdgeEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new ChangeEdgeEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}

		} else if (eventdestype.equals("updatedEndVertex")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new ChangeEdgeEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new ChangeEdgeEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}
		} else if (eventdestype.equals("deleteVertex")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new DeleteVertexEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new DeleteVertexEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}
		} else if (eventdestype.equals("deleteEdge")) {
			staticMatch(")");
			if (context != null && type == null) {
				ed = new DeleteEdgeEventDescription(et, context);
			} else if (context == null && type != null) {
				ed = new DeleteEdgeEventDescription(et,
						this.getAttributedElement(type));
			} else {
				throw new RuntimeException();
			}
		} else {
			throw new RuntimeException("");
		}
		return ed;
	}

	private EventTime getEventTime(String next) {
		EventTime et;
		if (next.equals("after")) {
			et = EventTime.AFTER;
		} else if (next.equals("before")) {
			et = EventTime.BEFORE;
		} else {
			throw new RuntimeException("");
		}
		return et;
	}

	private Condition parseCondition() {
		String next = this.nextToken();
		if (isMatching(next, "do")) {
			return null;
		} else if (isMatching(next, "with")) {
			staticMatch("<");
			String condexpr = this.nextToken();
			staticMatch(">");
			staticMatch("do");
			return new Condition(condexpr);
		}
		return null;
	}

	private Action parseAction() {


		staticMatch("<");
		String print = this.nextToken();
		staticMatch(">");
		return new PrintAction(print);
	}

	private void matchRule() {
		staticMatch("Rule");
		staticMatch(":=");
	}

	private String matchContext() {
		String context = this.nextToken();
		staticMatch(">");
		staticMatch(":");
		return context;
	}

	private String matchAttributeName() {
		staticMatch("<");
		String name = this.nextToken();
		staticMatch(">");
		return name;
	}

	private String matchType() {
		staticMatch("(");
		String next = this.nextToken();
		if (isMatching(next, "<")) {
			return matchElementType();
		} else {
			return null;
		}
	}

	private String matchElementType() {
		String typename = this.nextToken();
		staticMatch(">");
		return typename;
	}

	private boolean isMatching(String one, String two) {
		if (one.equals(two)) {
			return true;
		} else {
			return false;
		}
	}
	

	private void staticMatch(String toMatch) {
		String token = this.nextToken();
		System.out.println("Match: " + token);
		if (!token.equals(toMatch)) {
			throw new RuntimeException("parsing error");
		}

	}

	int la;

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

	private Class<? extends AttributedElement> getAttributedElement(String name) {
		Class<? extends AttributedElement> aecl;
		AttributedElementClass aeclo = schema.getAttributedElementClass(name);
		System.out.println(aeclo);
		System.out.println(aeclo.getM1Class());
		aecl = aeclo.getM1Class();
		return aecl;
	}

}
