package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TemporaryDefinitionStruct;

public class EmptyGraphLayoutReader implements TemporaryGraphLayoutReader {

	private List<TemporaryDefinitionStruct> noDefinitions;
	private Map<String, String> noGlobalVariables;

	{
		noDefinitions = new ArrayList<TemporaryDefinitionStruct>(0);
		noGlobalVariables = new HashMap<String, String>(0);
	}

	@Override
	public List<TemporaryDefinitionStruct> getDefinitionList() {
		return noDefinitions;
	}

	@Override
	public Map<String, String> getGlobalVariables() {
		return noGlobalVariables;
	}

	@Override
	public void startProcessing(String path) throws FileNotFoundException {
	}

	@Override
	public void startProcessing(File file) throws FileNotFoundException {
	}

}
