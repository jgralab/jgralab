/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader.plist;

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
