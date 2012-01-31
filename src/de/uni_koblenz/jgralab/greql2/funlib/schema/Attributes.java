package de.uni_koblenz.jgralab.greql2.funlib.schema;

import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class Attributes extends Function {

	public Attributes() {
		super(
				"Returns the attribute names and domains of the specified element or schema class "
						+ "in terms of a vector containing one map per attribute with the keys name and domain.",
				2, 1, 1.0, Category.SCHEMA_ACCESS);
	}

	public PVector<PMap<String, String>> evaluate(
			AttributedElementClass<?, ?> cls) {
		PVector<PMap<String, String>> result = JGraLab.vector();
		for (Attribute a : cls.getAttributeList()) {
			PMap<String, String> entry = JGraLab.map();
			entry = entry.plus("name", a.getName()).plus("domain",
					a.getDomain().getQualifiedName());
			result = result.plus(entry);
		}
		return result;
	}

	public PVector<PMap<String, String>> evaluate(AttributedElement<?, ?> el) {
		return evaluate(el.getAttributedElementClass());
	}
}
