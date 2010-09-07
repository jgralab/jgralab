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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 */
public class AttributeImpl implements Attribute, Comparable<Attribute> {

	/**
	 * the name of the attribute
	 */
	private final String name;

	/**
	 * the domain of the attribute
	 */
	private final Domain domain;

	/**
	 * the owning AttributedElementClass of the atribute
	 */
	private final AttributedElementClass aec;

	/**
	 * defines a total order of all attributes
	 */
	private final String sortKey;

	private String defaultValueAsString;

	private Object defaultValue;

	private Object defaultTransactionValue;

	private boolean defaultTransactionValueComputed;

	private boolean defaultValueComputed;

	/**
	 * builds a new attribute
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param domain
	 *            the domain of the attribute
	 * @param aec
	 *            the {@link AttributedElementClass} owning the
	 *            {@link Attribute}
	 * @param defaultValue
	 *            a String in TG value format denoting the default value of this
	 *            Attribute, or null if no default value shall be specified.
	 */
	public AttributeImpl(String name, Domain domain,
			AttributedElementClass aec, String defaultValue) {
		this.name = name;
		this.domain = domain;
		this.aec = aec;
		this.sortKey = name + ":" + domain.getQualifiedName();
		setDefaultValueAsString(defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "attribute " + sortKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Attribute#getDomain()
	 */
	public Domain getDomain() {
		return domain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Attribute#getName()
	 */
	public String getName() {
		return name;
	}

	public AttributedElementClass getAttributedElementClass() {
		return aec;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AttributeImpl)) {
			return false;
		}
		return sortKey.equals(((AttributeImpl) o).getSortKey());
	}

	@Override
	public int hashCode() {
		return sortKey.hashCode();
	}

	public int compareTo(Attribute o) {
		return sortKey.compareTo(o.getSortKey());
	}

	public String getSortKey() {
		return sortKey;
	}

	@Override
	public String getDefaultValueAsString() {
		return defaultValueAsString;
	}

	@Override
	public void setDefaultValueAsString(String defaultValue)
			throws SchemaException {
		if (defaultValueAsString != null) {
			throw new SchemaException(
					"Cannot assign a new default value to Attribute " + name
							+ " of " + aec.getQualifiedName() + ".");
		}
		defaultValueAsString = defaultValue;

	}

	@Override
	public void setDefaultTransactionValue(AttributedElement element)
			throws GraphIOException {
		if (defaultValueAsString != null) {
			if (!defaultTransactionValueComputed) {
				element
						.readAttributeValueFromString(name,
								defaultValueAsString);
				if (!domain.isComposite()) {
					defaultTransactionValue = element.getAttribute(name);
					defaultTransactionValueComputed = true;
				}
			} else {
				element.setAttribute(name, defaultTransactionValue);
			}
		}
	}

	@Override
	public void setDefaultValue(AttributedElement element)
			throws GraphIOException {
		if (!defaultValueComputed) {
			if (defaultValueAsString != null) {
				element
						.readAttributeValueFromString(name,
								defaultValueAsString);
			}
			defaultValue = element.getAttribute(name);
			defaultValueComputed = true;
		} else {
			Object cloneOfDefaultValue = null;

			if (defaultValue instanceof JGraLabCloneable) {
				cloneOfDefaultValue = ((JGraLabCloneable) defaultValue).clone();
			} else {
				cloneOfDefaultValue = defaultValue;
			}

			element.setAttribute(name, cloneOfDefaultValue);
		}
	}
}
