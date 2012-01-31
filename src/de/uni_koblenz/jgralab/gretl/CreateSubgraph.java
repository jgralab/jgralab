package de.uni_koblenz.jgralab.gretl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pcollections.Empty;
import org.pcollections.PMap;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.gretl.Context.GReTLVariableType;
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
 * In the template graph, edges may omit the archetype, but only if they have no
 * attributes. Vertices must have the archetype set!
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class CreateSubgraph extends Transformation<Void> {

	private TemplateGraph templateGraph = null;
	private Object match;
	private String matchText;
	private Set<GraphElement<?, ?>> createdElements = new HashSet<GraphElement<?, ?>>();

	public CreateSubgraph(Context c, String subgraphCreationText, Object match) {
		super(c);
		templateGraph = TemplateGraphParser.parse(subgraphCreationText);
		this.match = match;
	}

	public CreateSubgraph(Context c, String subgraphCreationText,
			String greqlText) {
		super(c);
		templateGraph = TemplateGraphParser.parse(subgraphCreationText);
		matchText = greqlText;
	}

	public CreateSubgraph(Context c, TemplateGraph g, String greqlText) {
		super(c);
		templateGraph = g;
		matchText = greqlText;
	}

	public static CreateSubgraph parseAndCreate(ExecuteTransformation et) {
		TemplateGraph graph = TemplateGraphParser.parse(et
				.match(TokenTypes.DOMAIN_SPECIFIC).value);
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new CreateSubgraph(et.context, graph, semExp);
	}

	private String getUniqueArchetype() {
		StringBuilder sb = new StringBuilder();
		sb.append("'");
		sb.append(context.getUniqueString().toString());
		sb.append("'");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Void transform() {
		if (context.getPhase() == TransformationPhase.SCHEMA) {
			return null;
		}

		if (context.getTargetSchema() == null) {
			throw new GReTLException(context,
					CreateSubgraph.class.getSimpleName()
							+ " needs an existing target schema!");
		}

		for (CreateVertex cv : templateGraph.getCreateVertexVertices()) {
			if (cv.get_archetype() == null) {
				throw new GReTLException(context,
						"All vertices in the template graph need to have an archetype!");
			}
		}

		if (match == null) {
			match = context.evaluateGReQLQuery(matchText);
		}

		PSet<Object> matchCollection = null;
		if (match instanceof Collection) {
			matchCollection = (PSet<Object>) match;
		} else {
			matchCollection = Empty.set();
			matchCollection = matchCollection.plus(match);
		}

		applyVertexCreations(matchCollection);
		applyEdgeCreations(matchCollection);
		applyAttributeSetting(matchCollection);

		return null;
	}

	/**
	 * Create vertices as specified by the subgraph creation graph.
	 * 
	 * @param matchCollection
	 */
	private void applyVertexCreations(PSet<Object> matchCollection) {
		for (CreateVertex v : templateGraph.getCreateVertexVertices()) {
			String qName = getVertexClassName(v, context);
			VertexClass vc = vc(qName);

			if (vc == null) {
				throw new GReTLException(context, "There's no vertex class '"
						+ qName + "' in the target schema.");
			}
			PSet<Object> archSet = Empty.set();
			for (Object jv : matchCollection) {
				context.setGReQLVariable("$", jv);
				Object arch = context.evaluateGReQLQuery(v.get_archetype());
				if (context.getImg(vc).containsKey(arch)) {
					log.finer("There's already an image for '"
							+ arch
							+ "' in "
							+ Context.toGReTLVarNotation(qName,
									GReTLVariableType.IMG)
							+ ", so we use that instead.");
				} else {
					archSet = archSet.plus(arch);
				}
			}
			log.finer("Instantiating " + archSet.size() + " '"
					+ vc.getQualifiedName() + "' vertices.");
			createdElements.addAll(new CreateVertices(context, vc, archSet)
					.execute());
		}
	}

	/**
	 * @param v
	 *            a create vertex
	 * @return the value of v's typeName attribute, or if that's a greql
	 *         expression its result (as string), or if typeName is null the
	 *         correct type determined by accessing the schema. In the latter
	 *         case, the determined type name will be set for that vertex, too.
	 */
	public static String getVertexClassName(CreateVertex v, Context context) {
		if (v.get_typeName() != null) {
			String type = v.get_typeName();
			if (v.is_typeNameIsQuery()) {
				// Type given as greql expression resulting in a string
				return context.evaluateGReQLQuery(type).toString();
			} else {
				return v.get_typeName();
			}
		}

		VertexClass mostSpecialVClass = context.getTargetSchema()
				.getDefaultVertexClass();
		for (CreateEdge e : v.getCreateEdgeIncidences()) {
			String eType = e.get_typeName();
			EdgeClass eClass = (EdgeClass) context.getTargetSchema()
					.getAttributedElementClass(eType);
			VertexClass vClass = (e.isNormal()) ? eClass.getFrom()
					.getVertexClass() : eClass.getTo().getVertexClass();
			if (vClass.isSubClassOf(mostSpecialVClass)) {
				mostSpecialVClass = vClass;
			}
		}

		if (mostSpecialVClass == context.getTargetSchema()
				.getDefaultVertexClass()) {
			throw new GReTLException(context,
					"Couldn't determine typeName for " + v + ".");
		}

		v.set_typeName(mostSpecialVClass.getQualifiedName());

		return v.get_typeName();
	}

	private void applyEdgeCreations(PSet<Object> matchCollection) {
		for (CreateEdge e : templateGraph.getCreateEdgeEdges()) {
			String qName = e.get_typeName();
			EdgeClass ec = null;
			if (e.is_typeNameIsQuery()) {
				// The type name is given as greql query
				ec(context.evaluateGReQLQuery(qName).toString());
			} else {
				ec = ec(qName);
			}

			if (ec == null) {
				throw new GReTLException(context, "There's no edge class '"
						+ qName + "' in the target schema.");
			}

			PSet<Tuple> archTripleSet = Empty.set();
			for (Object jv : matchCollection) {
				context.setGReQLVariable("$", jv);
				Tuple triple = Tuple.empty();
				Object arch = context.evaluateGReQLQuery(getArch(e
						.get_archetype()));
				triple = triple.plus(arch);

				triple = triple.plus(context.evaluateGReQLQuery((e.getAlpha())
						.get_archetype()));
				triple = triple.plus(context.evaluateGReQLQuery((e.getOmega())
						.get_archetype()));
				archTripleSet = archTripleSet.plus(triple);
			}
			log.finer("Instantiating " + archTripleSet.size() + " '"
					+ ec.getQualifiedName() + "' edges.");
			createdElements.addAll(new CreateEdges(context, ec, archTripleSet)
					.execute());
		}
	}

	private String getArch(String archStr) {
		if (archStr == null) {
			return getUniqueArchetype();
		}
		return archStr;
	}

	private void applyAttributeSetting(PSet<Object> matchCollection) {
		List<Iterable<? extends GraphElement<?, ?>>> iterables = new LinkedList<Iterable<? extends GraphElement<?, ?>>>();
		iterables.add(templateGraph.getCreateVertexVertices());
		iterables.add(templateGraph.getCreateEdgeEdges());

		for (Iterable<? extends GraphElement<?, ?>> it : iterables) {
			for (GraphElement<?, ?> ge : it) {
				AttributedElementClass<?, ?> aec;
				Map<String, String> attrs;
				String qName;
				String archetype;
				if (ge instanceof CreateVertex) {
					CreateVertex cv = (CreateVertex) ge;
					qName = getVertexClassName(cv, context);
					aec = aec(qName);
					archetype = cv.get_archetype();
					attrs = cv.get_attributes();
				} else {
					CreateEdge ce = (CreateEdge) ge;
					qName = ce.get_typeName();
					aec = aec(qName);
					archetype = ce.get_archetype();
					attrs = ce.get_attributes();
				}

				if (aec == null) {
					throw new GReTLException(context,
							"There's no attributed element class '" + qName
									+ "' in the target schema.");
				}

				if (attrs == null) {
					// No attributes are given for that element.
					continue;
				}

				for (Entry<String, String> e : attrs.entrySet()) {
					Attribute attr = aec.getAttribute(e.getKey());

					if (attr == null) {
						throw new GReTLException(context,
								"There's no attribute '" + e.getKey()
										+ "' defined for '"
										+ aec.getQualifiedName()
										+ "' in the target schema.");
					}

					PMap<Object, Object> archMap = Empty.orderedMap();
					for (Object jv : matchCollection) {
						context.setGReQLVariable("$", jv);
						archMap = archMap.plus(
								context.evaluateGReQLQuery(archetype),
								context.evaluateGReQLQuery(e.getValue()));
					}
					new SetAttributes(context, attr, archMap).execute();
				}
			}
		}
	}
}
