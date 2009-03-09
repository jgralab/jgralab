package de.uni_koblenz.jgralab;

/**
 * Items marked by #WorkInProgress annotations are generally untested, subject
 * to change, unstable, and may have any kind of undesireable properties ;-)
 * 
 * You should NOT use WorkInProgress parts of JGraLab without exactly knowning
 * what you do!
 * 
 * @author ist@uni-koblenz.de
 */
public @interface WorkInProgress {
	String description() default "[not specified]";

	String responsibleDevelopers() default "[not assigned]";

	String expectedFinishingDate() default "[not specified]";
}
