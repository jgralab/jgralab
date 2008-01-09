package de.uni_koblenz.jgralab.greql2.evaluator.logging;

/**
 * Used to specify how specific the {@link EvaluationLogger} logs. For GENERIC
 * all logging informations go into a file generic.log. For a SCHEMA it will be
 * logged to a file &lt;schemaName&gt;.log. For GRAPH it will be logged to a
 * file &lt;schemaName&gt;-&lt;graphId&gt;.log.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 */
public enum LoggingType {
	GENERIC, SCHEMA, GRAPH
};