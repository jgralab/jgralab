/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pcollections.Empty;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.gretl.template.CreateEdge;
import de.uni_koblenz.jgralab.gretl.template.CreateVertex;
import de.uni_koblenz.jgralab.gretl.template.TemplateGraph;
import de.uni_koblenz.jgralab.gretl.templategraphparser.TemplateGraphParser;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class MatchReplace extends InPlaceTransformation {

	private TemplateGraph replaceGraph;
	private String semanticExpression;
	private PSet<Object> matches;
	private Map<CreateVertex, Vertex> createVertices2Vertices = new HashMap<>();
	private LinkedHashSet<Vertex> matchedVertices = new LinkedHashSet<>();
	private LinkedHashSet<Edge> matchedEdges = new LinkedHashSet<>();
	private Set<GraphElement<?, ?>> preservables = new HashSet<>();
	private boolean addGlobalMappings = false;

	/**
	 * for validating successive matches.
	 */
	private HashSet<GraphElement<?, ?>> allModifiedElements = new HashSet<>();

	/**
	 * Creates a new {@link MatchReplace} transformation.
	 * 
	 * @param context
	 *            the {@link Context}
	 * @param replaceGraph
	 *            the replacement graph
	 * @param semanticExpression
	 *            the semantic expression in the form of a set of tuples. The
	 *            tuples may contain vertices, edges, and other values. Any
	 *            vertex or edge contained in the tuple which is not used as
	 *            archetype in replaceGraph will be deleted.
	 */
	public MatchReplace(Context context, TemplateGraph replaceGraph,
			String semanticExpression) {
		super(context);
		this.replaceGraph = replaceGraph;
		this.semanticExpression = semanticExpression;
	}

	/**
	 * Just like
	 * {@link MatchReplace#MatchReplace(Context, TemplateGraph, String)}, but
	 * also add global mappings to the arch/img functions if addGlobalMappings
	 * is true. This has to be done with caution, because if you delete an edge
	 * or vertex for which some mapping exists (or which is contained as part of
	 * a value in some mapping) later, then your mappings are skrewed up!
	 * 
	 * 
	 * @param context
	 * @param replaceGraph
	 * @param addGlobalMappings
	 * @param semanticExpression
	 */
	public MatchReplace(Context context, TemplateGraph replaceGraph,
			boolean addGlobalMappings, String semanticExpression) {
		super(context);
		this.replaceGraph = replaceGraph;
		this.semanticExpression = semanticExpression;
		this.addGlobalMappings = addGlobalMappings;
	}

	public MatchReplace(Context context, String replaceGraphText,
			String semanticExpression) {
		this(context, TemplateGraphParser.parse(replaceGraphText),
				semanticExpression);
	}

	public MatchReplace(Context context, TemplateGraph replaceGraph,
			PSet<Object> matches) {
		super(context);
		this.replaceGraph = replaceGraph;
		this.matches = matches;
	}

	public static MatchReplace parseAndCreate(ExecuteTransformation et) {
		TemplateGraph replaceGraph = TemplateGraphParser.parse(et
				.match(TokenTypes.DOMAIN_SPECIFIC).value);
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new MatchReplace(et.context, replaceGraph, semExp);
	}

	@Override
	protected Integer transform() {
		if (context.getPhase() == TransformationPhase.SCHEMA) {
			throw new GReTLException("SCHEMA phase in InPlaceTransformatio?!?");
		}

		if (matches == null) {
			matches = context.evaluateGReQLQuery(semanticExpression);
		}
		allModifiedElements.clear();

		int applicationCount = 0;
		for (Object match : matches) {
			context.setGReQLVariable("$", match);

			matchedVertices.clear();
			matchedEdges.clear();
			preservables.clear();
			createVertices2Vertices.clear();

			calculateMatchedElements(match);
			if (isValidMatch()) {
				applicationCount++;
				// System.out.println("Match: " + match);
				apply();
			}
		}

		// be side-effect free!
		matches = null;

		return applicationCount;
	}

	private void apply() {
		createAndUpdateVertices();
		createAndUpdateEdges();
		deleteNonPreservables();
	}

	@SuppressWarnings("unchecked")
	private void calculateMatchedElements(Object matchedElem) {
		if (matchedElem instanceof Vertex) {
			matchedVertices.add((Vertex) matchedElem);
		} else if (matchedElem instanceof Edge) {
			matchedEdges.add(((Edge) matchedElem).getNormalEdge());
		} else if (matchedElem instanceof Collection) {
			for (Object j : (Collection<Object>) matchedElem) {
				calculateMatchedElements(j);
			}
		} else if (matchedElem instanceof Record) {
			for (Object o : ((Record) matchedElem).toPMap().values()) {
				calculateMatchedElements(o);
			}
		} else if (matchedElem instanceof Map) {
			Map<Object, Object> m = (Map<Object, Object>) matchedElem;
			calculateMatchedElements(m.keySet());
			calculateMatchedElements(m.values());
		}
		// Anything else can be ignored.
	}

	private void deleteNonPreservables() {
		// Delete the unmatched vertices
		for (Vertex v : matchedVertices) {
			if (v.isValid() && !preservables.contains(v)) {
				verifyNoIncidentalDeletion(v);
				v.delete();
			}
		}

		// Delete the unmatched edges
		for (Edge e : matchedEdges) {
			if (e.isValid() && !preservables.contains(e)) {
				e.delete();
			}
		}
	}

	private void verifyNoIncidentalDeletion(Vertex v) {
		for (Edge e : v.incidences()) {
			if (preservables.contains(e.getNormalEdge())
					|| !matchedEdges.contains(e.getNormalEdge())) {
				throw new GReTLException(context,
						"Prevervable or non-matched edge " + e + " from "
								+ e.getAlpha() + " to " + e.getOmega()
								+ " would be deleted by deleting " + v
								+ "\ndeleted vertex = " + v
								+ ",\nmatchedVertices = " + matchedVertices
								+ ",\nmatchedEdges = " + matchedEdges
								+ ",\npreservables = " + preservables
								+ ",\nquery = " + semanticExpression);
			}
		}
	}

	private void createAndUpdateEdges() {
		for (CreateEdge ce : replaceGraph.getCreateEdgeEdges()) {
			Vertex startVertex = createVertices2Vertices.get(ce.getAlpha());
			Vertex endVertex = createVertices2Vertices.get(ce.getOmega());
			Object arch = null;
			if (ce.get_archetype() != null) {
				arch = context.evaluateGReQLQuery(ce.get_archetype());
			}
			if ((ce.get_typeName() == null) && (arch != null)
					&& (arch instanceof Edge)) {
				// Existing, preserved edge. However, maybe we might need to
				// update alpha and/or omega.
				Edge e = ((Edge) arch).getNormalEdge();
				preservables.add(e);
				allModifiedElements.add(e);
				// setAlpha/Omega are no-ops, if nothing is to be set. So that's
				// actually cheaper than checking first.
				e.setAlpha(startVertex);
				e.setOmega(endVertex);

				setAttributeValues(e, arch, ce.get_attributes(),
						ce.is_copyAttributeValues());
			} else {
				Edge newEdge = createEdge(ce, startVertex, endVertex);
				if (addGlobalMappings && (arch != null)) {
					PMap<Object, AttributedElement<?, ?>> m = Empty
							.orderedMap();
					m = m.plus(arch, newEdge);
					new AddMappings(context, m).execute();
					if (arch instanceof Edge) {
						allModifiedElements.add((Edge) arch);
					}
				}
				setAttributeValues(newEdge, arch, ce.get_attributes(),
						ce.is_copyAttributeValues());
			}
		}

	}

	private void createAndUpdateVertices() {
		for (CreateVertex cv : replaceGraph.getCreateVertexVertices()) {
			Object arch = null;
			if (cv.get_archetype() != null) {
				arch = context.evaluateGReQLQuery(cv.get_archetype());
			}
			if ((cv.get_typeName() == null) && (arch != null)
					&& (arch instanceof Vertex)) {
				// Existing, preserved vertex
				Vertex v = (Vertex) arch;
				createVertices2Vertices.put(cv, v);
				preservables.add(v);
				allModifiedElements.add(v);
				setAttributeValues(v, arch, cv.get_attributes(),
						cv.is_copyAttributeValues());
			} else {
				// A new Vertex has to be created
				Vertex newVertex = createVertex(cv);
				createVertices2Vertices.put(cv, newVertex);
				if (arch != null) {
					if (addGlobalMappings) {
						PMap<Object, AttributedElement<?, ?>> m = Empty
								.orderedMap();
						m = m.plus(arch, newVertex);
						new AddMappings(context, m).execute();
					}
					if (arch instanceof Vertex) {
						Vertex replacedVertex = (Vertex) arch;
						allModifiedElements.add(replacedVertex);
						relinkIncidences(replacedVertex, newVertex);
					}
				}
				setAttributeValues(newVertex, arch, cv.get_attributes(),
						cv.is_copyAttributeValues());
			}
		}
	}

	private Vertex createVertex(CreateVertex cv) {
		VertexClass vc = vc(CreateSubgraph.getVertexClassName(cv, context));
		Vertex nv = context.getTargetGraph().createVertex(vc);
		return nv;
	}

	private Edge createEdge(CreateEdge e, Vertex startVertex, Vertex endVertex) {
		EdgeClass ec = null;
		if (e.get_typeName() != null) {
			String type = e.get_typeName();
			if (e.is_typeNameIsQuery()) {
				// Type given as greql expression resulting in a string
				ec = ec(context.evaluateGReQLQuery(type).toString());
			} else {
				ec = ec(e.get_typeName());
			}
		} else {
			ec = getSingleEdgeClassBetween(
					startVertex.getAttributedElementClass(),
					endVertex.getAttributedElementClass());
		}
		return context.getTargetGraph().createEdge(ec, startVertex, endVertex);
	}

	private EdgeClass getSingleEdgeClassBetween(VertexClass from, VertexClass to) {
		List<EdgeClass> possibles = new LinkedList<>();
		for (EdgeClass ec : from.getConnectedEdgeClasses()) {
			if (ec.isAbstract()) {
				// Cannot be instantiated, so don't care
				continue;
			}
			VertexClass f = ec.getFrom().getVertexClass();
			VertexClass t = ec.getTo().getVertexClass();
			if ((f.equals(from) || f.isSuperClassOf(from))
					&& (t.equals(to) || t.isSuperClassOf(to))) {
				possibles.add(ec);
			}
		}
		if (possibles.size() != 1) {
			throw new GReTLException(context,
					"Cannot find exactly one EdgeClass between "
							+ from.getQualifiedName() + " and "
							+ to.getQualifiedName()
							+ ", but there were these possibilities: "
							+ possibles + ".");
		}
		return possibles.get(0);
	}

	private void setAttributeValues(GraphElement<?, ?> ge, Object arch,
			Map<String, String> attrMap, boolean copy) {
		if (attrMap == null) {
			return;
		}
		if (copy && ((arch == null) || !(arch instanceof AttributedElement))) {
			throw new GReTLException(context,
					"Should copy attribute values, but the archetype '" + arch
							+ "' is no AttributedElement.");
		}
		// Maybe copy matching attributes over
		if (copy) {
			AttributedElement<?, ?> ae = (AttributedElement<?, ?>) arch;
			AttributedElementClass<?, ?> aeClass = ae
					.getAttributedElementClass();
			GraphElementClass<?, ?> geClass = ge.getAttributedElementClass();
			for (Attribute attr : geClass.getAttributeList()) {
				String attrName = attr.getName();
				if (attrMap.containsKey(attrName)) {
					continue;
				}
				if (aeClass.containsAttribute(attrName)
						&& (aeClass.getAttribute(attrName).getDomain() == attr
								.getDomain())) {
					ge.setAttribute(attrName, ae.getAttribute(attrName));
				}
			}
		}
		for (Entry<String, String> e : attrMap.entrySet()) {
			ge.setAttribute(e.getKey(),
					context.evaluateGReQLQuery(e.getValue()));
		}
	}

	private boolean isValidMatch() {
		for (Vertex v : matchedVertices) {
			if (allModifiedElements.contains(v)) {
				return false;
			}
		}
		for (Edge e : matchedEdges) {
			if (allModifiedElements.contains(e)) {
				return false;
			}
		}
		return true;
	}

}
