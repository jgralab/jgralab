/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.codegenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * A ClassFileAbstraction holds Java bytecode for M1 classes compiled in-memory .
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ClassFileAbstraction extends SimpleJavaFileObject {
	private byte[] bytecode;

	/**
	 * Creates a new {@code ClassFileAbstraction} for the class given by
	 * {@code name}.
	 * 
	 * @param name
	 *            the name of the class
	 */
	public ClassFileAbstraction(String name) {
		super(URI.create("string:///" + name.replace('.', '/')
				+ Kind.CLASS.extension), Kind.CLASS);
	}

	public byte[] getBytecode() {
		return bytecode;
	}

	@Override
	public ByteArrayOutputStream openOutputStream() {
		return new ByteArrayOutputStream() {
			@Override
			public void close() {
				bytecode = this.toByteArray();
			}
		};
	}

	@Override
	public ByteArrayInputStream openInputStream() {
		return new ByteArrayInputStream(bytecode);
	}
}
