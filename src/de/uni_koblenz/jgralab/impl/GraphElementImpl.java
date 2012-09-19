/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.TemporaryGraphElementException;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class GraphElementImpl<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		implements InternalGraphElement<SC, IC> {
	protected int id;

	protected GraphElementImpl(Graph graph) {
		assert graph != null;
		this.graph = (GraphBaseImpl) graph;
	}

	protected GraphBaseImpl graph;

	@Override
	public Graph getGraph() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getGraphClass()
	 */
	@Override
	public GraphClass getGraphClass() {
		return graph.getAttributedElementClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElement#getSchema()
	 */
	@Override
	public Schema getSchema() {
		return graph.getSchema();
	}

	@Override
	public void graphModified() {
		graph.graphModified();
	}

	/**
	 * Triggers ECA-rules before an Attribute is changed
	 * 
	 * @param name
	 *            of the changing Attribute
	 */
	public void ecaAttributeChanging(String name, Object oldValue,
			Object newValue) {
		if (!graph.isLoading() && (graph.hasECARuleManager())) {
			graph.getECARuleManager().fireBeforeChangeAttributeEvents(this,
					name, oldValue, newValue);
		}
	}

	/**
	 * Triggers ECA-rule after an Attribute is changed
	 * 
	 * @param name
	 *            of the changed Attribute
	 */
	public void ecaAttributeChanged(String name, Object oldValue,
			Object newValue) {
		if (!graph.isLoading() && (graph.hasECARuleManager())) {
			graph.getECARuleManager().fireAfterChangeAttributeEvents(this,
					name, oldValue, newValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.GraphElement#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		for (Attribute attr : getAttributedElementClass().getAttributeList()) {
			if (attr.getDefaultValueAsString() == null) {
				continue;
			}
			try {
				internalSetDefaultValue(attr);
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void internalSetDefaultValue(Attribute attr) throws GraphIOException {
		attr.setDefaultValue(this);
	}

	@Override
	public boolean isInstanceOf(SC cls) {
		// This is specific to all impl variants with code generation. Generic
		// needs to implement this with a schema lookup.
		return cls.getSchemaClass().isInstance(this);
	}

	@Override
	public boolean isTemporary() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IC bless() {
		return (IC) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IC bless(SC schemaClass) {
		if (this.getAttributedElementClass().equals(schemaClass)) {
			return (IC) this;
		} else {
			throw new TemporaryGraphElementException(
					"The graph element "
							+ this
							+ " is not a TemporaryElement and can not be blessed to "
							+ schemaClass + ".");
		}
	}
}
