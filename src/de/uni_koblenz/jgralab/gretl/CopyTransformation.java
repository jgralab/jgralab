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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This Transformation implements a generic copy transformation, that is: The
 * target schema equals the source schema (or, in case of many source graphs, it
 * is the union of all source schemas), and the graph element instances in the
 * target graph is the union of the source graph's graph element instances, too.
 * It has to be noted, that the id's won't be retained. <br/>
 * <br/>
 *
 * In case of more than one source graph, the qualified names of their schema
 * elements must be disjoint. As an exception to this rule, if there a multiple
 * source graphs conforming to the same schema, then the schema element creation
 * will only be performed once, but all vertices and edges from all graphs will
 * be copied to the target graph.<br/>
 * <br/>
 *
 * To exclude elements simply provide a regular expression {@link Pattern} to
 * the <code>excludePattern</code> constructor parameter. There's also an
 * <code>includePattern</code> parameter, which overrides the exclude pattern.
 * If the exclude pattern is omitted, only the elements matching the include
 * pattern are copied.
 *
 * To exclude/include attributes use a syntax like this:
 * <code>"bar\\.Foo\\.(a|c)"</code>. This would omit the attributes
 * <code>a</code> and <code>c</code> of the element <code>bar.Foo</code>.<br/>
 * <br/>
 *
 * One usecase for this transformation is <b>schema evolution</b>, thas is, you
 * want to create a schema update transformation. Therefore, you want to copy
 * all unchanged parts (schema elements and their instances) and exclude the
 * parts that have changed for which you want to provide custom operation calls. <br/>
 * <br/>
 *
 * To do so, derive your own transformation from {@link CopyTransformation}, and
 * put the custom vertex class creation operations in a method overriding
 * {@link #transformVertexClasses()} and the edge creation operations in a
 * method overriding {@link #transformEdgeClasses()}.
 *
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class CopyTransformation extends Transformation<Graph> {
	protected Pattern excludePattern, includePattern;
	private HashSet<String> vcsCreated = new HashSet<String>();
	private HashSet<String> ecsCreated = new HashSet<String>();
	private HashSet<String> attrsCreated = new HashSet<String>();

	private static Logger log = JGraLab.getLogger(CopyTransformation.class
			.getPackage().getName());

	public CopyTransformation(Context context, Pattern excludePattern,
			Pattern includePattern) {
		super(context);
		this.excludePattern = excludePattern;
		this.includePattern = includePattern;
	}

	public CopyTransformation(Context context) {
		this(context, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.uni_koblenz.jgralab.gretl.Transformation#transform()
	 */
	@Override
	protected final Graph transform() {
		createVertexClasses();
		transformVertexClasses();
		createEdgeClasses();
		transformEdgeClasses();
		createAttributes();

		return context.getTargetGraph();
	}

	/**
	 * Override this method to create additional vertex classes (and their
	 * attributes)
	 */
	protected void transformVertexClasses() {
	}

	/**
	 * Override this method to create additional edge classes (and their
	 * attributes)
	 */
	protected void transformEdgeClasses() {
	}

	@After
	protected void clearNameCaches() {
		vcsCreated.clear();
		ecsCreated.clear();
		attrsCreated.clear();
	}

	private void createAttributes() {
		for (Entry<String, Graph> e : context.getSourceGraphs().entrySet()) {
			// If there a multiple source graphs conforming to the same schema,
			// then skip additional schema creations.
			String schemaQName = e.getValue().getSchema().getQualifiedName();
			if ((context.getPhase() == TransformationPhase.SCHEMA)
					&& attrsCreated.contains(schemaQName)) {
				continue;
			}
			attrsCreated.add(schemaQName);

			// first create GraphClass attributes
			GraphClass gc = e.getValue().getSchema().getGraphClass();
			createAttributes(gc, e);

			// then create the attributes of vertex and edge classes
			for (GraphElementClass<?, ?> aec : gc.getGraphElementClasses()) {
				if (isExcluded(aec.getQualifiedName())) {
					continue;
				}
				createAttributes(aec, e);
			}
		}
	}

	private boolean isExcluded(String qName) {
		// no patterns set ==> nothing is excluded
		if ((excludePattern == null) && (includePattern == null)) {
			return false;
		}

		// no excludes set ==> only includes are copied
		if (excludePattern == null) {
			return !includePattern.matcher(qName).matches();
		}

		// no includes set ==> excludes are excluded
		if (includePattern == null) {
			return excludePattern.matcher(qName).matches();
		}

		// both are set ==> includes override excludes
		return excludePattern.matcher(qName).matches()
				&& !includePattern.matcher(qName).matches();
	}

	private void createAttributes(AttributedElementClass<?, ?> oldAEC,
			Entry<String, Graph> e) {
		for (Attribute oldAttr : oldAEC.getOwnAttributeList()) {
			if (isExcluded(oldAEC.getQualifiedName() + "." + oldAttr.getName())) {
				log.finer("Skipping attribute '" + oldAEC.getQualifiedName()
						+ "#" + oldAttr.getName() + "' because it is excluded.");
				continue;
			}

			AttributedElementClass<?, ?> newAec = aec(oldAEC.getQualifiedName());

			if (newAec == null) {
				throw new GReTLException(context, "Cannot create attribute '"
						+ oldAttr.getName() + "' when copying '"
						+ oldAEC.getQualifiedName() + "'.");
			}

			Domain domain = new CopyDomain(context, oldAttr.getDomain())
					.execute();

			String query = null;
			if (oldAEC instanceof GraphClass) {
				query = "#"
						+ e.getKey()
						+ "# map(getGraph() -> "
						+ getAttributeReportString("getGraph()",
								oldAttr.getName(), domain) + ")";
			} else {
				String varName = "x";
				query = "#"
						+ e.getKey()
						+ "# from "
						+ varName
						+ " : keySet("
						+ Context.toGReTLVarNotation(oldAEC.getQualifiedName(),
								Context.GReTLVariableType.IMG)
						+ ") reportMap "
						+ varName
						+ " -> "
						+ getAttributeReportString(varName, oldAttr.getName(),
								domain) + " end";
			}
			new CreateAttribute(context, new AttributeSpec(newAec,
					oldAttr.getName(), domain,
					oldAttr.getDefaultValueAsString()), query).execute();
			log.info("Copied Attribute '" + oldAEC.getQualifiedName() + "#"
					+ oldAttr.getName() + "'.");
		}
	}

	/**
	 * Creates an attribute report string usable in GReQL queries delivered to
	 * {@link Transformation}'s <code>createAttribute()</code>, i.e. it returns
	 * a string like <code>get(enum_my$Enum, toString(varName.attrName))</code>
	 * for enum domain attributes,
	 * <code>recordInstance(record_my$Record, rec(a : varName.a, b : varName.b))</code>
	 * , and simply <code>varName.attrName</code> for simple domains (that is,
	 * domains that can be assigned between graphs).
	 *
	 * @param varName
	 *            the name of the attributed element variable in the query
	 * @param attrName
	 *            name of the {@link Attribute}
	 * @param domain
	 *            domain of the attribute
	 * @return see description...
	 *
	 *         // TODO: this won't do the trick for complex attributes that
	 *         contain records or enums, like Set&lt;MyRecord&gt;...
	 */
	public static String getAttributeReportString(String varName,
			String attrName, Domain domain) {
		// TODO: this won't do the trick for complex attributes that
		// contain records or enums, like Set<MyRecord>...
		StringBuilder sb = new StringBuilder();
		if (domain instanceof RecordDomain) {
			RecordDomain d = (RecordDomain) domain;
			sb.append("rec(");
			boolean first = true;
			for (RecordComponent c : d.getComponents()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(c.getName());
				sb.append(" : ");
				sb.append(varName);
				sb.append(".");
				sb.append(c.getName());
			}
			sb.append(")");
		} else {
			sb.append(varName);
			sb.append(".");
			sb.append(attrName);
		}

		return sb.toString();
	}

	private void createEdgeClasses() {
		for (Entry<String, Graph> e : context.getSourceGraphs().entrySet()) {

			// If there a multiple source graphs conforming to the same schema,
			// then skip additional schema creations.
			String schemaQName = e.getValue().getSchema().getQualifiedName();
			if ((context.getPhase() == TransformationPhase.SCHEMA)
					&& ecsCreated.contains(schemaQName)) {
				continue;
			}
			ecsCreated.add(schemaQName);

			for (EdgeClass oldEC : e.getValue().getGraphClass()
					.getEdgeClasses()) {
				// Skip excluded elements
				if (isExcluded(oldEC.getQualifiedName())) {
					log.finer("CopyTransformation: Skipped rule for EdgeClass "
							+ oldEC.getQualifiedName()
							+ ", because it is excluded.");
					continue;
				}
				IncidenceClass oldFromIC = oldEC.getFrom(), oldToIC = oldEC
						.getTo();

				// Create the edge class in the target schema
				EdgeClass newEC = null;
				String newECQName = oldEC.getQualifiedName();
				VertexClass newECFrom = context
						.getTargetSchema()
						.getGraphClass()
						.getVertexClass(
								oldFromIC.getVertexClass().getQualifiedName());
				VertexClass newECTo = context
						.getTargetSchema()
						.getGraphClass()
						.getVertexClass(
								oldToIC.getVertexClass().getQualifiedName());

				if (((newECFrom == null) && isExcluded(oldFromIC
						.getVertexClass().getQualifiedName()))
						|| ((newECTo == null) && isExcluded(oldToIC
								.getVertexClass().getQualifiedName()))) {
					log.finer("Skipping edge class '"
							+ newECQName
							+ "' because its from or to vertex class is excluded.");
					continue;
				} else if (((newECFrom == null) || (newECTo == null))) {
					throw new GReTLException(context,
							"Couldn't get from or to vertex class of '"
									+ newECQName
									+ "' although they are not excluded.");
				}

				if (oldEC.isAbstract()) {
					// ec is an abstract edge class
					newEC = new CreateAbstractEdgeClass(context, newECQName,
							new IncidenceClassSpec(newECFrom, oldFromIC),
							new IncidenceClassSpec(newECTo, oldToIC)).execute();

				} else {
					// ec is a concrete edge class
					String archetypeTripleQuery = "#" + e.getKey()
							+ "# from e : E{" + oldEC.getQualifiedName()
							+ "!} "
							+ "reportSet e, startVertex(e), endVertex(e) end";
					newEC = new CreateEdgeClass(context, newECQName,
							new IncidenceClassSpec(newECFrom, oldFromIC),
							new IncidenceClassSpec(newECTo, oldToIC),
							archetypeTripleQuery).execute();

				}

				assert newEC != null : "The newly created EdgeClass '"
						+ newECQName + "' is null!";

				// Add generalizations
				for (EdgeClass superEC : oldEC.getDirectSuperClasses()) {
					if (superEC.isDefaultGraphElementClass()
							|| isExcluded(superEC.getQualifiedName())) {
						continue;
					}
					EdgeClass supEC = superEC;
					EdgeClass newSuperEC = ec(supEC.getQualifiedName());
					new AddSuperClass(context, newEC, newSuperEC).execute();
				}
				// Handle role redefinitions
				for (String fromRole : oldFromIC.getRedefinedRoles()) {
					try {
						new RedefineFromRole(context, newEC, fromRole)
								.execute();
					} catch (GReTLException ex) {
						ex.printStackTrace();
					}
				}

				for (String toRole : oldToIC.getRedefinedRoles()) {
					try {
						new RedefineToRole(context, newEC, toRole).execute();
					} catch (GReTLException ex) {
						ex.printStackTrace();
					}
				}
				log.info("Copied EdgeClass '" + oldEC.getQualifiedName() + "'.");
			}
		}
	}

	private void createVertexClasses() {
		for (Entry<String, Graph> e : context.getSourceGraphs().entrySet()) {

			// If there a multiple source graphs conforming to the same schema,
			// then skip additional schema creations.
			String schemaQName = e.getValue().getSchema().getQualifiedName();
			if ((context.getPhase() == TransformationPhase.SCHEMA)
					&& vcsCreated.contains(schemaQName)) {
				continue;
			}
			vcsCreated.add(schemaQName);

			for (VertexClass oldVC : e.getValue().getSchema().getGraphClass()
					.getVertexClasses()) {
				// Skip excluded elements
				if (isExcluded(oldVC.getQualifiedName())) {
					log.finer("CopyTransformation: Skipped rule for VertexClass "
							+ oldVC.getQualifiedName()
							+ ", because it is excluded.");
					continue;
				}

				// Create the vertex class in the target schema
				VertexClass newVC = null;
				if (oldVC.isAbstract()) {
					newVC = new CreateAbstractVertexClass(context,
							oldVC.getQualifiedName()).execute();
				} else {
					newVC = new CreateVertexClass(context,
							oldVC.getQualifiedName(), "#" + e.getKey() + "# V{"
									+ oldVC.getQualifiedName() + "!}")
							.execute();
				}

				// Add generalizations
				for (VertexClass superVC : oldVC.getDirectSuperClasses()) {
					if (superVC.isDefaultGraphElementClass()
							|| isExcluded(superVC.getQualifiedName())) {
						continue;
					}
					VertexClass superClass = (VertexClass) context
							.getTargetSchema().getAttributedElementClass(
									superVC.getQualifiedName());
					new AddSuperClass(context, newVC, superClass).execute();
				}
				log.info("Copied VertexClass '" + oldVC.getQualifiedName()
						+ "'.");
			}
		}
	}
}
