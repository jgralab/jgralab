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
package de.uni_koblenz.jgralab.gretl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.gretl.parser.GReTLLexer;
import de.uni_koblenz.jgralab.gretl.parser.GReTLParsingException;
import de.uni_koblenz.jgralab.gretl.parser.Token;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class ExecuteTransformation extends Transformation<Graph> {

	static String FACTORY_METHOD_NAME = "parseAndCreate";

	private static HashMap<String, Method> knownTransformations = new HashMap<String, Method>();
	private static Logger logger = JGraLab
			.getLogger(ExecuteTransformation.class.getPackage().getName());

	public static void registerTransformation(
			Class<? extends Transformation<?>> tClass) {
		String className = tClass.getSimpleName();
		String createMethodName = ExecuteTransformation.FACTORY_METHOD_NAME;
		Method createMethod;
		try {
			createMethod = tClass.getMethod(createMethodName,
					ExecuteTransformation.class);
		} catch (SecurityException e) {
			throw new GReTLException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new GReTLException("The transformation class " + className
					+ " has no " + createMethodName
					+ "(ExecuteTransformation) method.", e);
		}

		knownTransformations.put(className, createMethod);
		logger.finer("Registered transformation " + className + ".");
	}

	static {
		registerTransformation(AddMappings.class);
		registerTransformation(AddSourceGraph.class);
		registerTransformation(AddSubClass.class);
		registerTransformation(AddSubClasses.class);
		registerTransformation(AddSuperClass.class);
		registerTransformation(AddSuperClasses.class);
		registerTransformation(Assert.class);
		registerTransformation(ExecuteTransformation.class);
		registerTransformation(CopyDomain.class);
		registerTransformation(CopyEdgeClass.class);
		registerTransformation(CopyVertexClass.class);
		registerTransformation(CreateAbstractEdgeClass.class);
		registerTransformation(CreateAbstractVertexClass.class);
		registerTransformation(CreateAttribute.class);
		registerTransformation(CreateAttributes.class);
		registerTransformation(CreateEdgeClass.class);
		registerTransformation(CreateEdges.class);
		registerTransformation(CreateEnumDomain.class);
		registerTransformation(CreateListDomain.class);
		registerTransformation(CreateMapDomain.class);
		registerTransformation(CreateVertexClassDisjoint.class);
		registerTransformation(CreateRecordDomain.class);
		registerTransformation(CreateSetDomain.class);
		registerTransformation(CreateSubgraph.class);
		registerTransformation(CreateVertexClass.class);
		registerTransformation(CreateVertices.class);
		registerTransformation(Delete.class);
		registerTransformation(Iteratively.class);
		registerTransformation(MatchReplace.class);
		registerTransformation(MergeVertices.class);
		registerTransformation(NTimes.class);
		registerTransformation(PrintGraph.class);
		registerTransformation(RedefineFromRole.class);
		registerTransformation(RedefineFromRoles.class);
		registerTransformation(RedefineToRole.class);
		registerTransformation(RedefineToRoles.class);
		registerTransformation(SetAttributes.class);
		registerTransformation(SetMultipleAttributes.class);
		registerTransformation(SysErr.class);
		registerTransformation(SysOut.class);
	}

	private File file = null;
	private String name = null;
	private List<Token> tokens;
	private int current;

	public ExecuteTransformation(Context c, File file) {
		super(c);
		this.file = file;
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = r.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new GReTLParsingException(context,
					"Error while reading transformation...", e);
		}
		tokens = GReTLLexer.scan(sb.toString());

		// Match the name directly, so that it is available directly after
		// instantiation.
		current = 0;
		match(TokenTypes.TRANSFORMATION);
		setName(match(TokenTypes.IDENT).value);
		match(TokenTypes.SEMICOLON);
	}

	public static ExecuteTransformation parseAndCreate(
			final ExecuteTransformation et) {
		File f = new File(et.match(TokenTypes.STRING).value);
		if (!f.isAbsolute()) {
			// File is specified relatively to the currently executed gretl
			// file.
			f = new File(et.file.getParent() + File.separator + f);
		}
		System.out.println(f.getAbsolutePath());
		return new ExecuteTransformation(et.context, f);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected Graph transform() {
		switch (context.getPhase()) {
		case SCHEMA:
		case GRAPH:
			interpretFile();
			break;
		default:
			throw new GReTLException(context, "Unknown TransformationPhase "
					+ context.getPhase() + "!");
		}

		return context.getTargetGraph();
	}

	private void interpretFile() {
		current = 3;
		while (!tryMatch(TokenTypes.EOF)) {
			if (tryMatch(TokenTypes.GREQL_IMPORT)) {
				match(TokenTypes.GREQL_IMPORT);
				addGReQLImport(match(TokenTypes.IDENT).value);
				match(TokenTypes.SEMICOLON);
				continue;
			}
			matchAndExecute();
		}
		match(TokenTypes.EOF);
	}

	private void matchAndExecute() {
		if (tryMatchTransformationCall()) {
			// This is a transformation op.
			Transformation<?> t = matchTransformation();
			t.execute();
			return;
		} else if (tryMatchHelperDefinition()) {
			Transformation<?> t = matchHelperDefinition();
			t.execute();
			return;
		} else if (tryMatchVariableAssignment()) {
			Transformation<?> t = matchVariableAssignment();
			t.execute();
			return;
		}
		throw new GReTLParsingException(context, "Don't know how to parse "
				+ tokens.get(current));
	}

	private Transformation<?> matchHelperDefinition() {
		final String helperName = match(TokenTypes.IDENT).value;
		match(TokenTypes.PAREN_OPEN);
		match(TokenTypes.PAREN_CLOSE);
		match(TokenTypes.DEFINES);
		final String query = match(TokenTypes.GREQL).value;
		match(TokenTypes.SEMICOLON);
		return new Transformation<Void>(context) {
			@Override
			protected Void transform() {
				setGReQLHelper(helperName, query);
				return null;
			}
		};
	}

	private Transformation<?> matchVariableAssignment() {
		final String varName = match(TokenTypes.IDENT).value;
		match(TokenTypes.DEFINES);
		final String query = match(TokenTypes.GREQL).value;
		match(TokenTypes.SEMICOLON);
		return new Transformation<Void>(context) {
			@Override
			protected Void transform() {
				setGReQLVariable(varName, query);
				return null;
			}
		};
	}

	public boolean tryMatchTransformationCall() {
		Token t = lookAhead(0);
		return (t.type == TokenTypes.IDENT)
				&& knownTransformations.containsKey(t.value);
	}

	private boolean tryMatchTransformationDefinition() {
		Token id = lookAhead(0);
		Token assign = lookAhead(1);
		Token trans = lookAhead(2);
		return (id.type == TokenTypes.IDENT)
				&& (assign.type == TokenTypes.ASSIGN)
				&& (trans.type == TokenTypes.IDENT)
				&& knownTransformations.containsKey(trans.value);
	}

	private boolean tryMatchHelperDefinition() {
		return (lookAhead(3) != null)
				&& (lookAhead(0).type == TokenTypes.IDENT)
				&& (lookAhead(1).type == TokenTypes.PAREN_OPEN)
				&& (lookAhead(2).type == TokenTypes.PAREN_CLOSE)
				&& (lookAhead(3).type == TokenTypes.DEFINES);
	}

	private boolean tryMatchVariableAssignment() {
		return (lookAhead(0).type == TokenTypes.IDENT)
				&& (lookAhead(1).type == TokenTypes.DEFINES);
	}

	public Transformation<?> matchTransformation() {
		String transformName = match(TokenTypes.IDENT).value;
		Method createMethod = knownTransformations.get(transformName);
		if (createMethod == null) {
			throw new GReTLParsingException(context,
					"Unknown transformation class '" + transformName + "'.");
		}

		try {
			Transformation<?> t = (Transformation<?>) createMethod.invoke(null,
					this);
			match(TokenTypes.SEMICOLON);
			return t;
		} catch (Exception e) {
			throw new GReTLParsingException(context, "Could not match "
					+ transformName + " at position " + lookAhead(0).start, e);
		}
	}

	public Domain matchDomain() {
		StringBuilder sb = new StringBuilder(match(TokenTypes.IDENT).value);
		while (getParenBalance(sb.toString(), '<', '>') != 0) {
			if (tryMatch(TokenTypes.COMMA)) {
				sb.append(match(TokenTypes.COMMA).value);
				sb.append(" ");
			}
			sb.append(match(TokenTypes.IDENT).value);
		}
		return domain(sb.toString());
	}

	public String matchGraphAlias() {
		String alias = match(TokenTypes.IDENT).value;
		if (alias.startsWith("#") && alias.endsWith("#")) {
			return alias.substring(1, alias.length() - 1);
		}
		throw new GReTLParsingException(context, "'" + alias
				+ "' is no valid graph alias.");
	}

	public boolean tryMatchGraphAlias() {
		Token cur = lookAhead(0);
		return ((cur != null) && (cur.type == TokenTypes.IDENT))
				&& (cur.value != null) && cur.value.startsWith("#")
				&& cur.value.endsWith("#");
	}

	private int getParenBalance(String s, char open, char close) {
		int b = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == open) {
				b++;
			} else if (c == close) {
				b--;
			}
		}
		return b;
	}

	public Attribute matchAttribute() {
		return attr(match(TokenTypes.IDENT).value);
	}

	public Attribute[] matchAttributeArray() {
		List<Attribute> lst = new LinkedList<Attribute>();
		while (tryMatch(TokenTypes.IDENT)) {
			lst.add(matchAttribute());
			if (tryMatch(TokenTypes.COMMA)) {
				match(TokenTypes.COMMA);
				if (!tryMatch(TokenTypes.IDENT)) {
					// Error out
					match(TokenTypes.IDENT);
				}
			}
		}
		return lst.toArray(new Attribute[lst.size()]);
	}

	public GraphElementClass<?, ?> matchGraphElementClass() {
		return gec(match(TokenTypes.IDENT).value);
	}

	public VertexClass matchVertexClass() {
		return vc(match(TokenTypes.IDENT).value);
	}

	public EdgeClass matchEdgeClass() {
		return ec(match(TokenTypes.IDENT).value);
	}

	public VertexClass[] matchVertexClassArray() {
		LinkedList<VertexClass> lst = new LinkedList<VertexClass>();
		while (tryMatch(TokenTypes.IDENT)) {
			lst.add(vc(match(TokenTypes.IDENT).value));
		}
		return lst.toArray(new VertexClass[lst.size()]);
	}

	public EdgeClass[] matchEdgeClassArray() {
		LinkedList<EdgeClass> lst = new LinkedList<EdgeClass>();
		while (tryMatch(TokenTypes.IDENT)) {
			lst.add(ec(match(TokenTypes.IDENT).value));
		}
		return lst.toArray(new EdgeClass[lst.size()]);
	}

	public RecordComponent[] matchRecordComponentArray() {
		List<RecordComponent> l = new LinkedList<RecordComponent>();
		match(TokenTypes.PAREN_OPEN);
		while (tryMatch(TokenTypes.IDENT)) {
			String compName = match(TokenTypes.IDENT).value;
			match(TokenTypes.COLON);
			String domName = match(TokenTypes.IDENT).value;
			l.add(new RecordComponent(compName, domain(domName)));

			if (tryMatch(TokenTypes.COMMA)) {
				match(TokenTypes.COMMA);
				if (!tryMatch(TokenTypes.IDENT)) {
					// Error out
					match(TokenTypes.IDENT);
				}
			} else if (!tryMatch(TokenTypes.PAREN_CLOSE)) {
				// Error out...
				match(TokenTypes.PAREN_CLOSE);
			}
		}
		match(TokenTypes.PAREN_CLOSE);
		return l.toArray(new RecordComponent[l.size()]);
	}

	/**
	 * concrete syntax for string arrays: (val1, val2, ...)
	 *
	 * @return an array of identifiers
	 */
	public String[] matchIdentifierArray() {
		match(TokenTypes.PAREN_OPEN);
		List<String> strs = new LinkedList<String>();
		while (tryMatch(TokenTypes.IDENT)) {
			strs.add(match(TokenTypes.IDENT).value);
			if (tryMatch(TokenTypes.COMMA)) {
				match(TokenTypes.COMMA);
				if (!tryMatch(TokenTypes.IDENT)) {
					match(TokenTypes.IDENT);
				}
			} else if (!tryMatch(TokenTypes.PAREN_CLOSE)) {
				// Error out...
				match(TokenTypes.PAREN_CLOSE);
			}
		}
		match(TokenTypes.PAREN_CLOSE);
		return strs.toArray(new String[strs.size()]);
	}

	public IncidenceClassSpec matchIncidenceClassSpec() {
		if (tryMatch(TokenTypes.FROM)) {
			match();
		} else {
			match(TokenTypes.TO);
		}
		VertexClass vc = vc(match(TokenTypes.IDENT).value);
		int min = -1, max = -1;
		String role = "";
		AggregationKind kind = AggregationKind.NONE;

		if (tryMatch(TokenTypes.PAREN_OPEN)) {
			match();
			min = Integer.parseInt(match(TokenTypes.IDENT).value);
			match(TokenTypes.COMMA);
			String maxStr = match(TokenTypes.IDENT).value;
			if (maxStr.equals("*")) {
				max = Integer.MAX_VALUE;
			} else {
				max = Integer.parseInt(maxStr);
			}
			match(TokenTypes.PAREN_CLOSE);
		}

		if (tryMatch(TokenTypes.ROLE)) {
			match();
			role = match(TokenTypes.IDENT).value;
		}

		if (tryMatch(TokenTypes.AGGREGATION)) {
			match();
			kind = AggregationKind.valueOf(match(TokenTypes.IDENT).value
					.toUpperCase());
		}
		try {
			Constructor<?> c = IncidenceClassSpec.class.getConstructor(
					VertexClass.class, int.class, int.class, String.class,
					AggregationKind.class);
			return (IncidenceClassSpec) c.newInstance(vc, min, max, role, kind);
		} catch (Exception e) {
			throw new GReTLParsingException(context,
					"Exception while instantiating an IncidenceClassSpec.", e);
		}
	}

	public String matchQualifiedName() {
		return match(TokenTypes.IDENT).value;
	}

	public void matchTransformationArrow() {
		match(TokenTypes.TRANSFORM_ARROW);
	}

	public String matchSemanticExpression() {
		return match(TokenTypes.GREQL).value;
	}

	public AttributeSpec matchAttributeSpec() {
		String qName = match(TokenTypes.IDENT).value;
		match(TokenTypes.COLON);
		String domain = matchDomain().getQualifiedName();
		String defaultValue = null;
		if (tryMatch(TokenTypes.ASSIGN)) {
			match(TokenTypes.ASSIGN);
			defaultValue = match(TokenTypes.STRING).value;
		}

		try {
			Constructor<?> c = AttributeSpec.class.getConstructor(
					AttributedElementClass.class, String.class, Domain.class,
					String.class);
			int lastDot = qName.lastIndexOf('.');
			String className = qName.substring(0, lastDot);
			AttributedElementClass<?, ?> aec = aec(className);
			if (aec == null) {
				throw new GReTLParsingException(context,
						"There's no AttributedElementClass '" + className
								+ "'.");
			}
			String attrName = qName.substring(lastDot + 1);
			return (AttributeSpec) c.newInstance(aec, attrName, domain(domain),
					defaultValue);
		} catch (Exception e) {
			throw new GReTLParsingException(context,
					"Exception while instantiating an IncidenceClassSpec.", e);
		}
	}

	public AttributeSpec[] matchAttributeSpecArray() {
		List<AttributeSpec> l = new LinkedList<AttributeSpec>();
		boolean first = true;
		do {
			if (first) {
				first = false;
			} else {
				match(TokenTypes.COMMA);
			}
			l.add(matchAttributeSpec());
		} while (tryMatch(TokenTypes.COMMA));
		return l.toArray(new AttributeSpec[l.size()]);
	}

	public final Token match(TokenTypes type) {
		if (lookAhead(0).type == type) {
			return match();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = -5; i < 6; i++) {
			Token t = lookAhead(i);
			if (t == null) {
				continue;
			}
			if (i == 0) {
				sb.append("»");
				sb.append(t.value);
				sb.append("«");
			} else {
				sb.append(t.value);
			}
			sb.append(" ");
		}
		throw new GReTLParsingException(context, "Expected " + type
				+ " but got " + tokens.get(current).type + " while parsing: "
				+ sb.toString());
	}

	public final Token match() {
		return tokens.get(current++);

	}

	public final boolean tryMatch(TokenTypes type) {
		if (lookAhead(0).type == type) {
			return true;
		}
		return false;
	}

	public final Token lookAhead(int i) {
		int idx = current + i;
		if ((idx >= 0) && (idx < tokens.size())) {
			return tokens.get(idx);
		}
		return null;
	}

}
