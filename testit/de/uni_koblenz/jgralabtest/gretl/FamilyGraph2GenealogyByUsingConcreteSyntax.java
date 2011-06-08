package de.uni_koblenz.jgralabtest.gretl;

import java.io.File;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.ExecuteTransformation;
import de.uni_koblenz.jgralab.gretl.Transformation;

public class FamilyGraph2GenealogyByUsingConcreteSyntax extends
		Transformation<Graph> {

	public FamilyGraph2GenealogyByUsingConcreteSyntax(Context context) {
		super(context);
	}

	@Override
	protected Graph transform() {
		File transformFile = new File(getClass().getResource(
				"transforms/Families2GenealogyIndirect.gretl").getFile());
		return new ExecuteTransformation(context, transformFile).execute();
	}
}
