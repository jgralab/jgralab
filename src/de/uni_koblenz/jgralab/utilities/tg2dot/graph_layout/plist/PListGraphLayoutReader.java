package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.plist;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.riediger.plist.PList;
import org.riediger.plist.PListDict;
import org.riediger.plist.PListException;

import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.AbstractGraphLayoutReader;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;

/**
 * A GraphLayoutReader reading in graph layout from PList-files.
 * 
 * 
 * @author ist@uni-koblenz.de
 */
public class PListGraphLayoutReader extends
		AbstractGraphLayoutReader {

	/**
	 * Constructs a PListGraphLayoutReader.
	 */
	public PListGraphLayoutReader(GreqlEvaluatorFacade evaluator) {
		super(evaluator);
	}

	@Override
	public void startProcessing(File file, GraphLayout layout)
			throws FileNotFoundException {
		startProcessing(file.getPath(), layout);
	}

	@Override
	public void startProcessing(String path, GraphLayout graphLayout)
			throws FileNotFoundException {
		this.graphLayout = graphLayout;
		try {
			PList plist = new PList(path);
			PListDict dict = plist.getDict();
			readOut(dict);
		} catch (PListException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads out every key-value-pairs from a PList and processes them as global
	 * variables or as definition.
	 * 
	 * @param dict
	 *            PList dictionary including all read data from the PLIst-file.
	 */
	private void readOut(PListDict dict) {
		for (Entry<String, Object> entry : dict.entrySet()) {
			String key = entry.getKey();

			if (isGlobalVariable(key)) {
				processGlobalVariable(key, dict.getString(key, "''"));
			} else {
				definitionStarted(key);
				processDefinitionAttributes(dict.getDict(key));
				definitionEnded();
			}
		}
	}

	/**
	 * Processes all key-value-pairs as attributes of a definition.
	 * 
	 * @param dict
	 *            Dictionary with all attributes of a single definition.
	 */
	private void processDefinitionAttributes(PListDict dict) {
		for (Entry<String, Object> attribute : dict.entrySet()) {
			processDefinitionAttribute(attribute.getKey(), attribute.getValue()
					.toString());
		}
	}
}
