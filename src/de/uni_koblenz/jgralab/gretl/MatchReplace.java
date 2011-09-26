package de.uni_koblenz.jgralab.gretl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeBase;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueMap;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;
import de.uni_koblenz.jgralab.gretl.template.CreateEdge;
import de.uni_koblenz.jgralab.gretl.template.CreateVertex;
import de.uni_koblenz.jgralab.gretl.template.TemplateGraph;
import de.uni_koblenz.jgralab.gretl.templategraphparser.TemplateGraphParser;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class MatchReplace extends InPlaceTransformation {

	private TemplateGraph replaceGraph;
	private String semanticExpression;
	private JValueSet matches;
	private Map<CreateVertex, Vertex> createVertices2Vertices = new HashMap<CreateVertex, Vertex>();
	private LinkedHashSet<Vertex> matchedVertices = new LinkedHashSet<Vertex>();
	private LinkedHashSet<Edge> matchedEdges = new LinkedHashSet<Edge>();
	private Set<GraphElement> preservables = new HashSet<GraphElement>();
	private boolean addGlobalMappings = false;

	/**
	 * for validating successive matches.
	 */
	private HashSet<GraphElement> allModifiedElements = new HashSet<GraphElement>();

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
			JValueSet matches) {
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
			matches = context.evaluateGReQLQuery(semanticExpression)
					.toJValueSet();
		}
		allModifiedElements.clear();

		int applicationCount = 0;
		for (JValue match : matches) {
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

	private void calculateMatchedElements(JValue jv) {
		if (jv.isAttributedElement()) {
			AttributedElement ae = jv.toAttributedElement();
			if (ae instanceof Vertex) {
				matchedVertices.add((Vertex) ae);
			} else if (ae instanceof Edge) {
				matchedEdges.add(((Edge) ae).getNormalEdge());
			}
		} else if (jv.isCollection()) {
			for (JValue j : jv.toCollection()) {
				calculateMatchedElements(j);
			}
		} else if (jv.isMap()) {
			JValueMap m = jv.toJValueMap();
			calculateMatchedElements(m.keySet());
			calculateMatchedElements(m.values());
		}
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
			JValue arch = null;
			if (ce.get_archetype() != null) {
				arch = context.evaluateGReQLQuery(ce.get_archetype());
			}
			if ((ce.get_typeName() == null) && (arch != null) && arch.isEdge()) {
				// Existing, preserved edge. However, maybe we might need to
				// update alpha and/or omega.
				EdgeBase e = (EdgeBase) arch.toEdge().getNormalEdge();
				preservables.add(e);
				allModifiedElements.add(e);
				// setAlpha/Omega are no-ops, if nothing is to be set. So that's
				// acually cheaper than checking first.
				e.setAlpha(startVertex);
				e.setOmega(endVertex);

				setAttributeValues(e, arch, ce.get_attributes(), ce
						.is_copyAttributeValues());
			} else {
				Edge newEdge = createEdge(ce, startVertex, endVertex);
				if (addGlobalMappings && (arch != null)) {
					JValueMap m = new JValueMap(1);
					m.put(arch, new JValueImpl(newEdge));
					new AddMappings(context, m).execute();
					if (arch.isEdge()) {
						Edge replacedEdge = arch.toEdge();
						allModifiedElements.add(replacedEdge);
					}
				}
				setAttributeValues(newEdge, arch, ce.get_attributes(), ce
						.is_copyAttributeValues());
			}
		}

	}

	private void createAndUpdateVertices() {
		for (CreateVertex cv : replaceGraph.getCreateVertexVertices()) {
			JValue arch = null;
			if (cv.get_archetype() != null) {
				arch = context.evaluateGReQLQuery(cv.get_archetype());
			}
			if ((cv.get_typeName() == null) && (arch != null)
					&& arch.isVertex()) {
				// Existing, preserved vertex
				Vertex v = arch.toVertex();
				createVertices2Vertices.put(cv, v);
				preservables.add(v);
				allModifiedElements.add(v);
				setAttributeValues(v, arch, cv.get_attributes(), cv
						.is_copyAttributeValues());
			} else {
				// A new Vertex has to be created
				Vertex newVertex = createVertex(cv);
				createVertices2Vertices.put(cv, newVertex);
				if (arch != null) {
					if (addGlobalMappings) {
						JValueMap m = new JValueMap(1);
						m.put(arch, new JValueImpl(newVertex));
						new AddMappings(context, m).execute();
					}
					if (arch.isVertex()) {
						Vertex replacedVertex = arch.toVertex();
						allModifiedElements.add(replacedVertex);
						relinkIncidences(replacedVertex, newVertex);
					}
				}
				setAttributeValues(newVertex, arch, cv.get_attributes(), cv
						.is_copyAttributeValues());
			}
		}
	}

	private Vertex createVertex(CreateVertex cv) {
		VertexClass vc = vc(CreateSubgraph.getVertexClassName(cv, context));
		Vertex nv = context.getTargetGraph().createVertex(vc.getM1Class());
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
			ec = getSingleEdgeClassBetween((VertexClass) startVertex
					.getAttributedElementClass(), (VertexClass) endVertex
					.getAttributedElementClass());
		}
		return context.getTargetGraph().createEdge(ec.getM1Class(),
				startVertex, endVertex);
	}

	private EdgeClass getSingleEdgeClassBetween(VertexClass from, VertexClass to) {
		List<EdgeClass> possibles = new LinkedList<EdgeClass>();
		for (EdgeClass ec : from.getConnectedEdgeClasses()) {
			if (ec.isAbstract()) {
				// Cannot be instantiated, so don't care
				continue;
			}
			VertexClass f = ec.getFrom().getVertexClass();
			VertexClass t = ec.getTo().getVertexClass();
			if (f.isSuperClassOfOrEquals(from) && t.isSuperClassOfOrEquals(to)) {
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

	private void setAttributeValues(GraphElement ge, JValue arch,
			Map<String, String> attrMap, boolean copy) {
		if (attrMap == null) {
			return;
		}
		if (copy && ((arch == null) || !arch.isAttributedElement())) {
			throw new GReTLException(context,
					"Should copy attribute values, but the archetype '" + arch
							+ "' is no AttributedElement.");
		}
		// Maybe copy matching attributes over
		if (copy) {
			AttributedElement ae = arch.toAttributedElement();
			AttributedElementClass aeClass = ae.getAttributedElementClass();
			AttributedElementClass geClass = ge.getAttributedElementClass();
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
			ge.setAttribute(e.getKey(), context
					.evaluateGReQLQuery(e.getValue()).toObject());
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
