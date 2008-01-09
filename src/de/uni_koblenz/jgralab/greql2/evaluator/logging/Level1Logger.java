/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import org.jdom.*;
import org.jdom.output.*;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * This class implements the Level1 logging component of the GreqlEvaluator. It
 * logs average values for selectivity, input and result size of the vertextypes
 * in the GReQL Syntaxgraph.
 * 
 * It is thread-safe, meaning that many {@link GreqlEvaluator}s can evaluate
 * different queries in parallel with logging enabled. If more than one logger
 * need to read and store to a logfile, the second logger will be blocked until
 * the first one stores the logfile. To make this work, be sure that a logger
 * lifetime looks like this.<br>
 * <br>
 * 
 * <code>
 * EvaluationLogger logger = new Level1Logger(loggerDirectory, dataGraph, LoggingType.SCHEMA);<br>
 * ...<br>
 * logger.logResultSize(&quot;foo&quot;, 33);<br>
 * ...<br>
 * logger.store();<br>
 * </code>
 * 
 * Any additional store() will return false and nothing will be written anymore.
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * @author Tassilo Horn <heimdall@uni-koblenz.de>, 2007, Diploma Thesis
 */
public class Level1Logger extends Level1LoggingBase implements EvaluationLogger {

	private static HashMap<String, Semaphore> logFileLockMap;

	static {
		logFileLockMap = new HashMap<String, Semaphore>();
	}

	/**
	 * Indicates if this {@link Level1Logger} already has called its store()
	 * method.
	 */
	private boolean hasStored = false;

	/**
	 * creates a new level1logger
	 */
	private Level1Logger() {
		inputSize = new HashMap<String, ArrayLogEntry>();
		resultSize = new HashMap<String, SimpleLogEntry>();
		selectivity = new HashMap<String, SimpleLogEntry>();
	}

	/**
	 * Creates a new Level1Logger
	 * 
	 * @param logDirectory
	 *            the directory where the log should be stored
	 * @param dataGraph
	 *            the {@link Graph} upon which's evaluation the logging should
	 *            be done.
	 * @param loggingType
	 *            see {@link LoggingType}. If {@link LoggingType#GENERIC} is
	 *            used here, the given dataGraph may be null.
	 */
	public Level1Logger(File logDirectory, Graph dataGraph,
			LoggingType loggingType) throws InterruptedException {
		this();
		loggerDirectory = logDirectory;
		if (dataGraph != null) {
			schemaName = dataGraph.getSchema().getName();
			dataGraphId = dataGraph.getId();
		}
		this.loggingType = loggingType;

		// acquire a lock for the used logfile. Only one EvaluationLogger may
		// run on one logfile at each time.
		String logFile = getLogfileName();
		if (logFileLockMap.containsKey(logFile)) {
			logFileLockMap.get(logFile).acquire();
		} else {
			logFileLockMap.put(logFile, new Semaphore(1));
			logFileLockMap.get(logFile).acquire();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#logSelectivity(java.lang.String,
	 *      boolean)
	 */
	@Override
	public void logSelectivity(String name, boolean wasSelected) {
		SimpleLogEntry entry = selectivity.get(name);
		if (entry == null) {
			entry = new SimpleLogEntry(name);
			selectivity.put(entry.getName(), entry);
		}
		if (wasSelected)
			entry.logSum(1);
		else
			entry.logSum(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#logResultSize(java.lang.String,
	 *      int)
	 */
	@Override
	public void logResultSize(String name, int size) {
		SimpleLogEntry entry = resultSize.get(name);
		if (entry == null) {
			entry = new SimpleLogEntry(name);
			resultSize.put(entry.getName(), entry);
		}
		entry.logSum(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#logInputSize(java.lang.String,
	 *      java.util.ArrayList)
	 */
	@Override
	public void logInputSize(String name, ArrayList<Integer> size) {
		ArrayLogEntry entry = inputSize.get(name);
		if (entry == null) {
			entry = new ArrayLogEntry(name);
			inputSize.put(entry.getName(), entry);
		}
		entry.logSum(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#store()
	 */
	@Override
	public boolean store() throws IOException {
		if (hasStored) {
			// This logger already has stored its values
			return false;
		}

		if (!loggerDirectory.exists()) {
			loggerDirectory.mkdir();
		}

		boolean result = store(getLogFile());

		if (result) {
			// release the lock for the used logfile
			logFileLockMap.get(getLogfileName()).release();
			hasStored = true;
		}

		return result;
	}

	/**
	 * stores the log to the given file
	 */
	public boolean store(File file) throws IOException {
		Element rootElement = new Element("level1log");
		Element resultElement = new Element("results");
		Iterator<SimpleLogEntry> iter = resultSize.values().iterator();
		while (iter.hasNext()) {
			LogEntry entry = iter.next();
			resultElement.addContent(entry.toJDOMEntry());
		}
		rootElement.addContent(resultElement);
		Element inputElement = new Element("input");
		Iterator<ArrayLogEntry> iter2 = inputSize.values().iterator();
		while (iter2.hasNext()) {
			LogEntry entry = iter2.next();
			inputElement.addContent(entry.toJDOMEntry());
		}
		rootElement.addContent(inputElement);
		Element selectivityElement = new Element("selectivity");
		Iterator<SimpleLogEntry> iter3 = selectivity.values().iterator();
		while (iter3.hasNext()) {
			LogEntry entry = iter3.next();
			selectivityElement.addContent(entry.toJDOMEntry());
		}
		rootElement.addContent(selectivityElement);
		Document doc = new Document(rootElement);
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		out.output(doc, bw);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#getLogfileName()
	 */
	@Override
	public String getLogfileName() {
		return getLogFile().getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#load()
	 */
	@Override
	public boolean load() {
		return load(getLogFile());
	}
}
