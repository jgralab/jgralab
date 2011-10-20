package de.uni_koblenz.jgralab.greql2.funlib.schema;

import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class HasComponent extends Function {

	public HasComponent() {
		super("Determines whether the record $r$ has a component $name$.",
				Category.SCHEMA_ACCESS);
	}

	public Boolean evaluate(Record r, String name) {
		return r.getComponentNames().contains(name);
	}
}
