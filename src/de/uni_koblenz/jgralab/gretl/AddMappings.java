package de.uni_koblenz.jgralab.gretl;

import java.util.Map.Entry;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * This transformation allows for adding mappings to the img/arch functions,
 * which are usually managed automatically by the transformation framework. The
 * transformation only expects a semantic expression resulting in a map from
 * arbitrary archetype to a graph elements. The mappings are added to the
 * arch/img functions for the respective graph element class.
 * 
 * This is mostly useful in in-place scenarios, where you can use this
 * transformation to add special mappings for elements the transformation does
 * not affect, but which should be referred to.
 * 
 * Example: Consider an in-place transformation, that only creates some new
 * edges. The {@link CreateEdges} transformation expects a set of 3-tuples
 * (newEdgeArchetype, startVertexArchetype, endVertexArchetype). However, the
 * already existing vertices that should be connected by the new edges don't
 * have an archetype, because they already existed in the source graph, which is
 * the target graph as well in this in-place scenario.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 */
public class AddMappings extends Transformation<Void> {

	private String semanticExpression;
	private PMap<Object, AttributedElement<?, ?>> archetypes;

	public AddMappings(Context c, String semanticExpression) {
		super(c);
		this.semanticExpression = semanticExpression;
	}

	public AddMappings(Context c,
			PMap<Object, AttributedElement<?, ?>> archetypeMap) {
		super(c);
		archetypes = archetypeMap;
	}

	public static AddMappings parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semanticExpression = et.matchSemanticExpression();
		return new AddMappings(et.context, semanticExpression);
	}

	@Override
	protected Void transform() {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return null;
		}

		if (archetypes == null) {
			archetypes = context.evaluateGReQLQuery(semanticExpression);
		}

		for (Entry<Object, AttributedElement<?, ?>> e : archetypes.entrySet()) {
			AttributedElementClass<?, ?> aec = e.getValue()
					.getAttributedElementClass();
			context.addMapping(aec, e.getKey(), e.getValue());
		}

		return null;
	}

}
