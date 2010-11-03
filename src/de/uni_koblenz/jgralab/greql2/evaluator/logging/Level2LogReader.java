/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
/**
 *
 */
package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * This class is the counterpart of {@link Level2Logger}. The logger is
 * responsible for creating logs (in a thread-safe way), whereas this log reader
 * reads the logs and provides informations about the average input and result
 * sizes of {@link Greql2Vertex}s and the average selectivity.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Level2LogReader extends Level2LoggingBase implements
		EvaluationLogReader {

	private static Logger logger = JGraLab.getLogger(Level2LogReader.class
			.getPackage().getName());

	private Level2LogReader() {
		inputSize = new HashMap<String, ArrayLogEntry>();
		resultSize = new HashMap<String, SimpleLogEntry>();
		selectivity = new HashMap<String, SimpleLogEntry>();
	}

	/**
	 * Creates a new {@link Level2LogReader} that uses the same values for
	 * logDirectory, schemaName, graphId and loggingType as the given
	 * {@link Level2Logger}.
	 * 
	 * @param l2logger
	 *            the {@link Level2Logger} which's values to use
	 */
	public Level2LogReader(Level2Logger l2logger) {
		this();
		loggerDirectory = l2logger.loggerDirectory;
		schemaName = l2logger.schemaName;
		dataGraphId = l2logger.dataGraphId;
		loggingType = l2logger.loggingType;

		if (load()) {
			logger.info("Level2LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			logger.warning("Level2LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/**
	 * Creates a new {@link Level2LogReader} which can read logfiles for
	 * {@link LoggingType#GENERIC} and {@link LoggingType#SCHEMA}, but no
	 * datagraph specific logfiles.
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level2Logger}
	 *            stored the logfiles
	 * @param schema
	 *            the schema
	 * @param loggingType
	 *            determines the used logfile. See {@link LoggingType} for more
	 *            informations. If this is parameter is
	 *            {@link LoggingType#GENERIC}, then the parameter schema may be
	 *            null.
	 */
	public Level2LogReader(File logDirectory, Schema schema,
			LoggingType loggingType) {
		this();
		loggerDirectory = logDirectory;
		if (schema != null) {
			schemaName = schema.getQualifiedName();
		}
		this.loggingType = loggingType;

		if (load()) {
			logger.info("Level2LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			logger.warning("Level2LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/**
	 * Creates a new Level2LogReader for evaluating a query on the given graph,
	 * which uses the best possible logging type depending on the log files in
	 * logDirectory.
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level2Logger}
	 *            stored the logfiles
	 */
	public Level2LogReader(File logDirectory, Graph g) {
		this();

		dataGraphId = g.getId();
		schemaName = g.getSchema().getQualifiedName();

		loggerDirectory = logDirectory;
		File graphLogFile = new File(loggerDirectory + File.separator
				+ schemaName + "-" + dataGraphId + ".log");
		File schemaLogFile = new File(loggerDirectory + File.separator
				+ schemaName + ".log");
		if (graphLogFile.exists()) {
			loggingType = LoggingType.GRAPH;
		} else if (schemaLogFile.exists()) {
			loggingType = LoggingType.SCHEMA;
		} else {
			loggingType = LoggingType.GENERIC;
		}

		if (load()) {
			logger.info("Level2LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			logger.warning("Level2LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#
	 * getAvgSelectivity(java.lang.String)
	 */
	@Override
	public double getAvgSelectivity(String name) {
		SimpleLogEntry entry = selectivity.get(name);
		if (entry != null) {
			return entry.getAverageValue();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#
	 * getAvgResultSize(java.lang.String)
	 */
	@Override
	public double getAvgResultSize(String name) {
		SimpleLogEntry entry = resultSize.get(name);
		if (entry != null) {
			return entry.getAverageValue();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#
	 * getAvgInputSize(java.lang.String)
	 */
	@Override
	public ArrayList<Double> getAvgInputSize(String name) {
		ArrayLogEntry entry = inputSize.get(name);
		if (entry != null) {
			return entry.getAverageValue();
		} else {
			return null;
		}
	}

	/**
	 * loads the log from the default file
	 */
	@Override
	public boolean load() {
		return load(getLogFile());
	}

}
