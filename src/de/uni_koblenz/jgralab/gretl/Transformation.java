/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.gretl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;

/**
 * Abstract base class for all user-defined transformations. Simply derive from
 * this class and override the {@link #transform()} method. In there you have to
 * use the {@code create}-Methods defined here, to build up a new {@link Schema}
 * , and provide semantic expressions (GReQL queries on the source graph) to
 * specify the transformation on instance level.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public abstract class Transformation<T> {
	/**
	 * Use this annotation to annotate transformation methods that should be run
	 * <b>after</b> the transformation finished.
	 * 
	 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface After {

	}

	/**
	 * Use this annotation to annotate transformation methods that should be run
	 * <b>before</b> the transformation started.
	 * 
	 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface Before {

	}

	/**
	 * Run all methods annotated with <code>annotationClass</code> of this
	 * transformation including annotated methods in superclasses up to the base
	 * class {@link Transformation}.
	 * 
	 * @param annotationClass
	 */
	private final void invokeHooks(Class<? extends Annotation> annotationClass) {
		for (Class<?> cls = getClass(); Transformation.class
				.isAssignableFrom(cls); cls = cls.getSuperclass()) {
			for (Method method : cls.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotationClass)) {
					try {
						method.setAccessible(true);
						method.invoke(this);
					} catch (Exception e) {
						logger.severe("Couldn't run @"
								+ annotationClass.getSimpleName() + " method "
								+ method.getName() + " of "
								+ cls.getSimpleName() + ".");
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * DEBUGGING
	 */
	public static boolean DEBUG_EXECUTION = Boolean.parseBoolean(System
			.getProperty("debugGReTLExecution", "false"));
	public static boolean DEBUG_REVERSE_EDGES = false;
	private int executionStep = 1;

	protected Context context;
	protected static Logger logger = JGraLab.getLogger(Transformation.class);

	protected Transformation(Context context) {
		this.context = context;
	}

	protected Transformation() {
	}

	public final void setContext(Context c) {
		this.context = c;
	}

	/**
	 * Executes this transformation.
	 * 
	 * When it finishes, the target graph be accessed via the {@link Context}
	 * object.
	 */
	public final T execute() {
		long startTime = System.currentTimeMillis();

		if (context == null) {
			throw new GReTLException("No Context set for " + this);
		}

		// Transformation starts, call the before hook
		invokeHooks(Before.class);

		T result = null;

		if (context.outermost) {
			executionStep = 1;
			context.phase = TransformationPhase.SCHEMA;
			context.outermost = false;

			if (context.getTargetSchema() == null) {
				logger.info("Starting Schema creation phase...");
				context.createTargetSchema();
				transform();
			} else {
				logger.info("Target Schema exists. "
						+ "Skipping schema creation phase...");
			}
			context.ensureAllMappings();

			// If a target graph was set externally, use that.
			if (context.targetGraph == null) {
				logger.info("Creating a new target graph...");
				context.createTargetGraph();
			} else if (context.targetGraph.getSchema().getQualifiedName()
					.equals(context.targetSchema.getQualifiedName())) {
				logger.info("Using a preset target graph...");
			} else {
				// This can only happen, if a user first sets a target graph and
				// then a different target schema...
				throw new GReTLException(context,
						"Preset target graph has wrong schema '"
								+ context.targetGraph.getSchema()
										.getQualifiedName()
								+ "'. Expected was '"
								+ context.targetSchema.getQualifiedName()
								+ "'.");
			}

			// Start the GRAPH phase
			logger.info("SCHEMA Phase took "
					+ ((System.currentTimeMillis() - startTime) + "ms."));
			startTime = System.currentTimeMillis();

			context.phase = TransformationPhase.GRAPH;
			logger.info("Starting instance creation phase...");
			result = transform();

			if (DEBUG_EXECUTION) {
				// I'm the outermost, so check the mappings before finishing
				context.validateMappings();
			}

			logger.info("GRAPH Phase took "
					+ ((System.currentTimeMillis() - startTime) + "ms."));
		} else {
			// hey, I'm nested, so run the phase my parent is running.
			result = transform();

			// Debugging stuff...
			Graph tg = context.targetGraph;
			if (DEBUG_EXECUTION
					&& ((tg.getVCount() + tg.getECount()) < GReTLRunner.MAX_VISUALIZATION_SIZE)) {
				try {
					String name = getClass().getSimpleName();
					if (name.isEmpty()) {
						name = "$anonymous$";
					}
					Tg2Dot.convertGraph(context.getTargetGraph(), "__debug_"
							+ (executionStep++) + "_" + name + ".pdf",
							DEBUG_REVERSE_EDGES, GraphVizOutputFormat.PDF);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Transformation is done, call the after hook
		invokeHooks(After.class);

		return result;
	}

	/**
	 * In this method the individual transformation operation calls are
	 * specified. Concrete transformations must override this method.
	 * 
	 * @return
	 */
	protected abstract T transform();

	/**
	 * @return the {@link BooleanDomain} from the target-schema
	 */
	protected final BooleanDomain getBooleanDomain() {
		return context.targetSchema.getBooleanDomain();
	}

	/**
	 * @return the {@link IntDomain} from the target-schema
	 */
	protected final IntegerDomain getIntegerDomain() {
		return context.targetSchema.getIntegerDomain();
	}

	/**
	 * @return the {@link LongDomain} from the target-schema
	 */
	protected final LongDomain getLongDomain() {
		return context.targetSchema.getLongDomain();
	}

	/**
	 * @return the {@link StringDomain} from the target-schema
	 */
	protected final StringDomain getStringDomain() {
		return context.targetSchema.getStringDomain();
	}

	/**
	 * @return the {@link DoubleDomain} from the target-schema
	 */
	protected final DoubleDomain getDoubleDomain() {
		return context.targetSchema.getDoubleDomain();
	}

	protected final void setGReQLVariable(String name, Object val) {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return;
		}
		context.setGReQLVariable(name, val);
	}

	protected final void setGReQLVariable(String name, String greqlExpression) {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return;
		}
		context.setGReQLVariable(name, greqlExpression);
	}

	protected final void setGReQLHelper(String name, String greqlExpression) {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return;
		}
		context.setGReQLHelper(name, greqlExpression);
	}

	protected final void addGReQLImport(String qualifiedName) {
		if (context.getPhase() != TransformationPhase.GRAPH) {
			return;
		}
		context.addGReQLImport(qualifiedName);
	}

	/**
	 * @param qualifiedName
	 * @return the target schema {@link VertexClass} with the given qualified
	 *         name.
	 */
	protected final VertexClass vc(String qualifiedName) {
		VertexClass vc = context.targetSchema.getGraphClass().getVertexClass(
				qualifiedName);
		if (vc == null) {
			throw new GReTLException("There's no target VertexClass '"
					+ qualifiedName + "'.");
		}
		return vc;
	}

	/**
	 * @param qualifiedName
	 * @return the target schema {@link EdgeClass} with the given qualified
	 *         name.
	 */
	protected final EdgeClass ec(String qualifiedName) {
		EdgeClass ec = context.targetSchema.getGraphClass().getEdgeClass(
				qualifiedName);
		if (ec == null) {
			throw new GReTLException(context, "There's no target EdgeClass '"
					+ qualifiedName + "'.");
		}
		return ec;
	}

	/**
	 * @param qualifiedName
	 * @return the target schema {@link AttributedElementClass} with the given
	 *         qualified name.
	 */
	protected final AttributedElementClass<?, ?> aec(String qualifiedName) {
		AttributedElementClass<?, ?> aec = context.targetSchema
				.getAttributedElementClass(qualifiedName);
		if (aec == null) {
			throw new GReTLException(context,
					"There's no target AttributedElementClass '"
							+ qualifiedName + "'.");
		}
		return aec;
	}

	/**
	 * @param qualifiedName
	 * @return the target schema {@link GraphElementClass} with the given
	 *         qualified name.
	 */
	protected final GraphElementClass<?, ?> gec(String qualifiedName) {
		GraphElementClass<?, ?> gec = context.targetSchema.getGraphClass()
				.getGraphElementClass(qualifiedName);
		if (gec == null) {
			throw new GReTLException(context,
					"There's no target GraphElementClass '" + qualifiedName
							+ "'.");
		}
		return gec;
	}

	protected final Attribute attr(String qualifiedName) {
		int lastDot = qualifiedName.lastIndexOf('.');
		String className = qualifiedName.substring(0, lastDot);
		AttributedElementClass<?, ?> aec = context.getTargetSchema()
				.getAttributedElementClass(className);
		if (aec == null) {
			throw new GReTLException(context,
					"There's no target AttributedElementClass '" + className
							+ "'.");
		}
		String attrName = qualifiedName.substring(lastDot + 1);
		Attribute attr = aec.getAttribute(attrName);
		if (attr == null) {
			throw new GReTLException(context, "There's no target Attribute '"
					+ attrName + "' in AttributedElementClass '" + className
					+ "'.");
		}
		return attr;
	}

	protected Domain domain(String domain) {
		Domain d = context.getTargetSchema().getDomain(domain);
		if (d == null) {
			throw new GReTLException(context, "There's no target Domain '"
					+ domain + "'.");
		}
		return d;
	}
}
