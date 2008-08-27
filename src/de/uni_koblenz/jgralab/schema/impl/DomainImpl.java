/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class DomainImpl implements Domain, Comparable<Domain> {

	private QualifiedName qName;
	private Package pkg;
	private Schema schema;

	@Override
	public Schema getSchema() {
		return schema;
	}

	protected DomainImpl() {
	}

	protected DomainImpl(Schema schema, QualifiedName qn) {
		initialize(schema, qn);
	}

	protected void initialize(Schema schema, QualifiedName qn) {
		qName = qn;
		this.schema = schema;
		pkg = schema.getDefaultPackage();
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj)
				|| ((obj instanceof Domain) && getQName().equals(
						((Domain) obj).getQName()));
	}

	@Override
	public void setPackage(Package p) {
		pkg = p;
	}

	@Override
	public Package getPackage() {
		return pkg;
	}

	@Override
	public String getSimpleName() {
		return qName.getSimpleName();
	}

	@Override
	public String getQualifiedName() {
		return qName.getQualifiedName();
	}

	@Override
	public String getQualifiedName(Package pkg) {
		if (this.pkg == pkg) {
			return qName.getSimpleName();
		} else if (this.pkg.isDefaultPackage()) {
			return "." + qName.getSimpleName();
		} else {
			return qName.getQualifiedName();
		}
	}

	@Override
	public String getPackageName() {
		return qName.getPackageName();
	}

	@Override
	public String getDirectoryName() {
		return qName.getDirectoryName();
	}

	@Override
	public String getPathName() {
		return qName.getPathName();
	}

	@Override
	public QualifiedName getQName() {
		return qName;
	}

	@Override
	public String getUniqueName() {
		return qName.getUniqueName();
	}

	@Override
	public void setUniqueName(String uniqueName) {
		qName.setUniqueName(this, uniqueName);
	}

	@Override
	public int hashCode() {
		return qName.hashCode();
	}

	@Override
	public String toString() {
		return "domain " + qName.getQualifiedName();
	}

	public int compareTo(Domain o) {
		return qName.compareTo(o.getQName());
	}

	public String getName() {
		return qName.getQualifiedName();
	}

	@Override
	public String getVariableName() {
		throw new UnsupportedOperationException();
	}
}
