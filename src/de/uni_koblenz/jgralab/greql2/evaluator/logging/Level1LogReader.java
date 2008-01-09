/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is the counterpart of {@link Level1Logger}. The logger is
 * responsible for creating logs (in a thread-safe way), whereas this log reader
 * reads the logs and provides informations about the average input and result
 * sizes of {@link Greql2Vertex}s and the average selectivity.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class Level1LogReader extends Level1LoggingBase implements
		EvaluationLogReader {

	private Level1LogReader() {
		inputSize = new HashMap<String, ArrayLogEntry>();
		resultSize = new HashMap<String, SimpleLogEntry>();
		selectivity = new HashMap<String, SimpleLogEntry>();
	}

	/**
	 * Creates a new {@link Level1LogReader}.
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level1Logger}
	 *            stored the logfiles
	 * @param dataGraph
	 *            a datagraph (used to get the {@link Schema} and the graphid)
	 * @param loggingType
	 *            determines the used logfile. See {@link LoggingType} for more
	 *            informations. If this is parameter is
	 *            {@link LoggingType#GENERIC}, then the parameter dataGraph may
	 *            be null.
	 */
	public Level1LogReader(File logDirectory, Graph dataGraph,
			LoggingType loggingType) {
		this();
		loggerDirectory = logDirectory;
		if (dataGraph != null) {
			schemaName = dataGraph.getSchema().getName();
			dataGraphId = dataGraph.getId();
		}
		this.loggingType = loggingType;
		if (load()) {
			System.out.println("Level1LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			System.err.println("Level1LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/**
	 * Creates a new {@link Level1LogReader} that uses the same values for
	 * logDirectory, schemaName, graphId and loggingType as the given
	 * {@link Level1Logger}.
	 * 
	 * @param logger
	 *            the {@link Level1Logger} which's values to use
	 */
	public Level1LogReader(Level1Logger logger) {
		this();
		loggerDirectory = logger.loggerDirectory;
		schemaName = logger.schemaName;
		dataGraphId = logger.dataGraphId;
		loggingType = logger.loggingType;

		if (load()) {
			System.out.println("Level1LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			System.err.println("Level1LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/**
	 * Creates a new {@link Level1LogReader} which can read logfiles for
	 * {@link LoggingType#GENERIC} and {@link LoggingType#SCHEMA}, but no
	 * datagraph specific logfiles.
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level1Logger}
	 *            stored the logfiles
	 * @param schema
	 *            the schema
	 * @param loggingType
	 *            determines the used logfile. See {@link LoggingType} for more
	 *            informations. If this is parameter is
	 *            {@link LoggingType#GENERIC}, then the parameter schema may be
	 *            null.
	 */
	public Level1LogReader(File logDirectory, Schema schema,
			LoggingType loggingType) {
		this();
		loggerDirectory = logDirectory;
		if (schema != null) {
			schemaName = schema.getName();
		}
		this.loggingType = loggingType;

		if (load()) {
			System.out.println("Level1LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			System.err.println("Level1LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/**
	 * A shorthand for
	 * <code>new Level1LogReader(logDir, null, LoggingType.GENERIC);</code>
	 * 
	 * @param logDirectory
	 *            the directory where the corresponding {@link Level1Logger}
	 *            stored the logfiles
	 */
	public Level1LogReader(File logDirectory) {
		this();
		loggerDirectory = logDirectory;
		this.loggingType = LoggingType.GENERIC;

		if (load()) {
			System.out.println("Level1LogReader successfully loaded "
					+ getLogFile().getPath());
		} else {
			System.err.println("Level1LogReader couldn't load "
					+ getLogFile().getPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#getAvgSelectivity(java.lang.String)
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
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#getAvgResultSize(java.lang.String)
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
	 * @see de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader#getAvgInputSize(java.lang.String)
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
