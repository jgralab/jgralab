package de.uni_koblenz.jgralab.greql2.funlib.schema;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPVector;
import org.pcollections.PMap;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class Attributes extends Function {

	public Attributes() {
		super(
				"Determines the attribute names and domains of the specified element or schema class.",
				2, 1, 1.0, Category.SCHEMA_ACCESS);
	}

	public PVector<PMap<String, String>> evaluate(AttributedElementClass cls) {
		PVector<PMap<String, String>> result = ArrayPVector.empty();
		for (Attribute a : cls.getAttributeList()) {
			PMap<String, String> entry = ArrayPMap.empty();
			entry = entry.plus("name", a.getName()).plus("domain",
					a.getDomain().getQualifiedName());
			result = result.plus(entry);
		}
		return result;
	}

	public PVector<PMap<String, String>> evaluate(AttributedElement el) {
		return evaluate(el.getAttributedElementClass());
	}
}
