package de.uni_koblenz.jgralab.gretl.templategraphparser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pcollections.ArrayPMap;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.gretl.template.CreateEdge;
import de.uni_koblenz.jgralab.gretl.template.CreateVertex;
import de.uni_koblenz.jgralab.gretl.template.TemplateGraph;
import de.uni_koblenz.jgralab.gretl.template.TemplateSchema;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class TemplateGraphParser {

	private List<Token> tokens;
	private TemplateGraph graph;
	private Map<String, CreateVertex> ident2VertexMap = new HashMap<String, CreateVertex>();

	public TemplateGraphParser(List<Token> toks) {
		tokens = toks;
	}

	private TemplateGraph parse() {
		graph = TemplateSchema.instance().createTemplateGraph();

		while (!tokens.isEmpty()) {
			matchVertexOrSubgraph();
		}

		return graph;
	}

	private void matchVertexOrSubgraph() {
		CreateVertex v = matchVertex();
		while (!tryMatch(TokenType.COMMA) && !tokens.isEmpty()) {
			v = matchEdgeAndNextVertex(v);
		}
		if (tryMatch(TokenType.COMMA)) {
			match(TokenType.COMMA);
		}
	}

	private CreateVertex matchEdgeAndNextVertex(CreateVertex srcOrDst) {
		// match the edge stuff
		TokenType arrow = null;
		String ecName = null;
		boolean typeIsQuery = false;
		String arch = null;
		PMap<String, String> attrs = null;
		boolean copyAttributes = false;

		if (tryMatch(TokenType.L_ARROW)) {
			arrow = match(TokenType.L_ARROW).type;
		} else {
			arrow = match(TokenType.R_ARROW).type;
		}

		match(TokenType.L_CURLY);

		if (tryMatch(TokenType.IDENT)) {
			ecName = matchComplexToken(TokenType.IDENT).value;
		} else if (tryMatch(TokenType.HASH)) {
			// The type given as greql expression
			match(TokenType.HASH);
			typeIsQuery = true;
			ecName = matchComplexToken(TokenType.STRING).value;
		}

		// The archetype is optional...
		if (tryMatch(TokenType.STRING)) {
			arch = matchComplexToken(TokenType.STRING).value;
		}

		if (tryMatch(TokenType.PIPE)) {
			match(TokenType.PIPE);
			attrs = matchAttributes();
			// read ... for copying attributes
			if (tryMatch(TokenType.TRIPLE_DOT)) {
				match(TokenType.TRIPLE_DOT);
				copyAttributes = true;
			}
		}
		match(TokenType.R_CURLY);

		// match the next vertex
		CreateVertex srcOrDst2 = matchVertex();

		// Create the appropriate edge
		CreateEdge ce = null;
		if (arrow == TokenType.R_ARROW) {
			ce = graph.createCreateEdge(srcOrDst, srcOrDst2);
		} else {
			ce = graph.createCreateEdge(srcOrDst2, srcOrDst);
		}

		ce.set_typeName(ecName);
		ce.set_typeNameIsQuery(typeIsQuery);
		ce.set_archetype(arch);
		ce.set_attributes(attrs);
		ce.set_copyAttributeValues(copyAttributes);

		return srcOrDst2;
	}

	private CreateVertex matchVertex() {
		// ident(Type 'archQuery' | attr1 = 'greql', ...)
		// where ident, Type, archQuery, and the attribute part are optional.
		// Furthermore, the type may be also given as greql expression.
		// (,'greql-resulting-into-string').
		CreateVertex vertex = null;
		String vertexIdent = null;
		if (tryMatch(TokenType.IDENT)) {
			vertexIdent = matchComplexToken(TokenType.IDENT).value;
			vertex = ident2VertexMap.get(vertexIdent);
		}
		if (vertex == null) {
			match(TokenType.L_PAREN);
			vertex = graph.createCreateVertex();
			if (tryMatch(TokenType.IDENT)) {
				// The type name given as identifier
				String vcName = matchComplexToken(TokenType.IDENT).value;
				vertex.set_typeNameIsQuery(false);
				vertex.set_typeName(vcName);
			} else if (tryMatch(TokenType.HASH)) {
				// The type given as greql expression
				match(TokenType.HASH);
				String vcNameQuery = matchComplexToken(TokenType.STRING).value;
				vertex.set_typeNameIsQuery(true);
				vertex.set_typeName(vcNameQuery);
			}

			if (tryMatch(TokenType.STRING)) {
				vertex.set_archetype(matchComplexToken(TokenType.STRING).value);
			}

			if (tryMatch(TokenType.PIPE)) {
				// read attributes
				match(TokenType.PIPE);
				vertex.set_attributes(matchAttributes());
				// read ... for copying attributes
				if (tryMatch(TokenType.TRIPLE_DOT)) {
					match(TokenType.TRIPLE_DOT);
					vertex.set_copyAttributeValues(true);
				}
			}
			match(TokenType.R_PAREN);
			if (vertexIdent != null) {
				ident2VertexMap.put(vertexIdent, vertex);
			}
		}
		return vertex;
	}

	private PMap<String, String> matchAttributes() {
		PMap<String, String> attrs = ArrayPMap.empty();
		do {
			if (tryMatch(TokenType.TRIPLE_DOT)) {
				return attrs;
			}
			String attrName = matchComplexToken(TokenType.IDENT).value;
			match(TokenType.ASSIGN);
			String val = matchComplexToken(TokenType.STRING).value;
			attrs = attrs.plus(attrName, val);
		} while (tryMatch(TokenType.COMMA) && (match(TokenType.COMMA) != null));
		return attrs;
	}

	private boolean tryMatch(TokenType tt) {
		return !tokens.isEmpty() && (tokens.get(0).type == tt);
	}

	private ComplexToken matchComplexToken(TokenType tt) {
		return (ComplexToken) match(tt);
	}

	private Token match(TokenType tt) {
		if (tokens.isEmpty()) {
			throw new TemplateGraphParserException("Expected " + tt
					+ " but no Tokens were left over.");
		}
		Token t = tokens.get(0);
		if (t.type == tt) {
			return tokens.remove(0);
		}
		throw new TemplateGraphParserException("Expected " + tt + " but got "
				+ t + ".");
	}

	public static TemplateGraph parse(String txt) {
		// System.out.println("Parsing '" + txt + "'");
		TemplateGraphParser p = new TemplateGraphParser(
				TemplateGraphLexer.scan(txt));
		return p.parse();
	}

	public static void main(String[] args) throws GraphIOException, IOException {
		String[] txts = {
				"v1(a.A 'tup($[1], \"aArch\")' | a1 = '\"A\"', a2='$[2] + 1') "
						+ "-->{a.E 'tup($[1], $[2])' | e1 = '$[17]'} v1",

				"v1(a.A 'tup($[1], \"aArch\")' | a1 = '\"A\"', a2='$[2] + 1') "
						+ "-->{a.E 'tup($[1], $[2])' | e1 = '$[17]'} v1"
						+ "<--{a.E2 '$[3]'} v3(a.B '$[4]'), "
						+ "v4('$[6]') <--{a.F '1'| e3='17'} v1",

				"('$' | name = '$.name')" };
		for (int i = 0; i < txts.length; i++) {
			System.out.println("Trying txts[" + i + "]...");
			Graph idg = TemplateGraphParser.parse(txts[i]);
			Tg2Dot.convertGraph(idg, "/tmp/g" + i + ".dot", false);
		}
		System.out.println("Fini.");
	}
}
