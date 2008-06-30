/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * This class is the counterpart of {@link Level2Logger}. The logger is
 * responsible for creating logs (in a thread-safe way), whereas this log reader
 * reads the logs and provides informations about the average input and result
 * sizes of {@link Greql2Vertex}s and the average selectivity.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class Level2LogReader extends Level2LoggingBase implements
		EvaluationLogReader {

	private static Logger logger = Logger.getLogger(Level2LogReader.class
			.getName());

	private Level2LogReader() {
		inputSize = new HashMap<String, ArrayLogEntry>();
		resultSize = new HashMap<String, SimpleLogEntry>();
		selectivity = new HashMap<String, SimpleLogEntry>();
	}

	/**
	 * Creates a new {@link Level2LogReader}.
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level2Logger}
	 *            stored the logfiles
	 * @param dataGraph
	 *            a datagraph (used to get the {@link Schema} and the graphid)
	 * @param loggingType
	 *            determines the used logfile. See {@link LoggingType} for more
	 *            informations. If this is parameter is
	 *            {@link LoggingType#GENERIC}, then the parameter dataGraph may
	 *            be null.
	 */
	public Level2LogReader(File logDirectory, Graph dataGraph,
			LoggingType loggingType) {
		this();
		loggerDirectory = logDirectory;
		if (dataGraph != null) {
			schemaName = dataGraph.getSchema().getQualifiedName();
			dataGraphId = dataGraph.getId();
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
			schemaName = schema.getSimpleName();
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
	 * A shorthand for
	 * <code>new Level2LogReader(logDir, null, LoggingType.GENERIC);</code>
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level2Logger}
	 *            stored the logfiles
	 */
	public Level2LogReader(File logDirectory) {
		this();
		loggerDirectory = logDirectory;
		this.loggingType = LoggingType.GENERIC;

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
