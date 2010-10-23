/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;

/**
 * This class implements the Level2 logging component of the GreqlEvaluator. It
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
 * EvaluationLogger logger = new Level2Logger(loggerDirectory, dataGraph, LoggingType.SCHEMA);<br>
 * ...<br>
 * logger.logResultSize(&quot;foo&quot;, 33);<br>
 * ...<br>
 * logger.store();<br>
 * </code>
 * 
 * Any additional store() will return false and nothing will be written anymore.
 * 
 * @author ist@uni-koblenz.de
 * @author ist@uni-koblenz.de
 */
public class Level2Logger extends Level2LoggingBase implements EvaluationLogger {

	private static HashMap<String, Semaphore> logFileLockMap;

	static {
		logFileLockMap = new HashMap<String, Semaphore>();
	}

	/**
	 * creates a new {@link Level2Logger}
	 */
	private Level2Logger() {
		inputSize = new HashMap<String, ArrayLogEntry>();
		resultSize = new HashMap<String, SimpleLogEntry>();
		selectivity = new HashMap<String, SimpleLogEntry>();
	}

	/**
	 * Creates a new {@link Level2Logger}
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
	public Level2Logger(File logDirectory, Graph dataGraph,
			LoggingType loggingType) throws InterruptedException {
		this();
		loggerDirectory = logDirectory;
		if (dataGraph != null) {
			schemaName = dataGraph.getSchema().getQualifiedName();
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
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#
	 * logSelectivity(java.lang.String, boolean)
	 */
	@Override
	public void logSelectivity(String name, boolean wasSelected) {
		SimpleLogEntry entry = selectivity.get(name);
		if (entry == null) {
			entry = new SimpleLogEntry(name);
			selectivity.put(entry.getName(), entry);
		}
		if (wasSelected) {
			entry.logSum(1);
		} else {
			entry.logSum(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#
	 * logResultSize(java.lang.String, long)
	 */
	@Override
	public void logResultSize(String name, long size) {
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
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#logInputSize
	 * (java.lang.String, java.util.ArrayList)
	 */
	@Override
	public void logInputSize(String name, ArrayList<Long> size) {
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
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#store()
	 */
	@Override
	public boolean store() throws IOException {
		if (!loggerDirectory.exists() && !loggerDirectory.mkdir()) {
			throw new IOException("Couldn't create directory "
					+ loggerDirectory + ".");
		}

		boolean result = store(getLogFile());

		if (result) {
			// release the lock for the used logfile
			logFileLockMap.get(getLogfileName()).release();
		}

		return result;
	}

	/**
	 * stores the log to the given file
	 */
	public boolean store(File file) throws IOException {
		Element rootElement = new Element("level2log");
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

		BufferedWriter bw = null;
		try {
			Document doc = new Document(rootElement);
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			out.output(doc, bw);
			bw.flush();
		} finally {
			try {
				bw.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#
	 * getLogfileName()
	 */
	@Override
	public String getLogfileName() {
		return getLogFile().getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger#load()
	 */
	@Override
	public boolean load() {
		return load(getLogFile());
	}
}
