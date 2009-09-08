/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Holds the data that both {@link Level2LogReader} and {@link Level2Logger}
 * share plus some internal methods both of them use.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Level2LoggingBase {
	/**
	 * The directory where this logger saves it's logfiles.
	 */
	protected File loggerDirectory;

	/**
	 * The name of the datagraphs schema, if the {@link LoggingType} is
	 * {@link LoggingType#SCHEMA} or {@link LoggingType#GRAPH}.
	 */
	protected String schemaName;

	/**
	 * The ID of the datagraph, if the {@link LoggingType} is
	 * {@link LoggingType#GRAPH}.
	 */
	protected String dataGraphId;

	/**
	 * See {@link LoggingType}
	 */
	protected LoggingType loggingType;

	/**
	 * This HashMap stores the average selectivity of the VertexTypes. It maps
	 * from VertexTypeName to average selectivity
	 */
	protected HashMap<String, SimpleLogEntry> selectivity;

	/**
	 * This HashMap stores the average resultsize of the various VertexTypes in
	 * the GReQL syntaxgraph. It maps from VertexTypeName to LogEntry
	 */
	protected HashMap<String, SimpleLogEntry> resultSize;

	/**
	 * This HashMap stores the average
	 */
	protected HashMap<String, ArrayLogEntry> inputSize;

	protected File getLogFile() {
		String filename = "";
		switch (loggingType) {
		case GENERIC:
			filename = "generic";
			break;
		case SCHEMA:
			filename = schemaName;
			break;
		case GRAPH:
			filename = schemaName + "-" + dataGraphId;
			break;
		}
		filename += ".log";
		File file = new File(loggerDirectory, filename);
		return file;
	}

	/**
	 * loads the log from the given file
	 */
	@SuppressWarnings("unchecked")
	protected boolean load(File file) {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(file);
		} catch (Exception ex) {
			return false;
		}
		Element rootElement = doc.getRootElement();
		Element resultElem = rootElement.getChild("results");

		if (resultElem == null) {
			// The logfile contains only the root element
			return true;
		}

		/*
		 * the use of generic List<Element> and Iterator<Element> will result in
		 * compiler warning because JDom doesn't support Java generics
		 */
		List<Element> childList = resultElem.getChildren();
		Iterator<Element> iter = childList.iterator();
		while (iter.hasNext()) {
			SimpleLogEntry logEntry = new SimpleLogEntry(iter.next());
			resultSize.put(logEntry.getName(), logEntry);
		}
		Element inputElem = rootElement.getChild("input");
		childList = inputElem.getChildren();
		iter = childList.iterator();
		while (iter.hasNext()) {
			ArrayLogEntry logEntry = new ArrayLogEntry((Element) iter.next());
			inputSize.put(logEntry.getName(), logEntry);
		}
		Element selectivityElem = rootElement.getChild("selectivity");
		childList = selectivityElem.getChildren();
		iter = childList.iterator();
		while (iter.hasNext()) {
			SimpleLogEntry logEntry = new SimpleLogEntry((Element) iter.next());
			selectivity.put(logEntry.getName(), logEntry);
		}
		return true;
	}
}
