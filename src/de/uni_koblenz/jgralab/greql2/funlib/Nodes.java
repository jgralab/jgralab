package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

public class Nodes extends Greql2Function {
	{
		JValueType[][] x = { { JValueType.PATHSYSTEM, JValueType.COLLECTION },
				{ JValueType.SLICE, JValueType.COLLECTION } };
		signatures = x;

		description = "Returns all inner nodes of the given pathsystem or slice as set.\n"
				+ "Inner nodes are all vertices besides leaves of the pathsystem or slice. That\n"
				+ "means, all vertices, that are neither the end nor the root of a path.";

		Category[] c = { Category.PATHS_AND_PATHSYSTEMS_AND_SLICES };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		switch (checkArguments(arguments)) {
		case 0:
			return arguments[0].toPathSystem().nodes();
		case 1:
			return arguments[0].toSlice().nodes();
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 5;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
