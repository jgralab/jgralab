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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.Vector;

/**
 * a list of <code>CodeBlocks</code> <code>CodeBlock</code>s may be added and
 * removed Code may be created out of the saved <code>CodeBlock</code>s
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class CodeList extends CodeBlock {
	/**
	 * contains the <code>CodeBlock</code>s building <code>this</code>
	 */
	Vector<CodeBlock> blocks;

	/**
	 * creates an empty <code>CodeList</code>
	 */
	public CodeList() {
		this(null);
	}

	/**
	 * creates a <code>CodeList</code> with an empty vector of CodeBlocks and an
	 * empty CodeBlock the content of <code>parent</code> does not affect the
	 * created CodeList
	 * 
	 * @param parent
	 *            a <code>CodeList</code> to which the new, empty CodeBlock will
	 *            be added
	 */
	public CodeList(CodeList parent) {
		super(parent);
		blocks = new Vector<CodeBlock>();
	}

	/**
	 * removes <code>block</code> from <code>this</code> and sets
	 * <code>block</code>´s parent to null
	 * 
	 * @param block
	 *            if it is part of the <code>blocks</code> stored in
	 *            <code>this</code>, <code>block</code> will be removed, may not
	 *            be null, because the null.setParent does not work
	 */
	public void remove(CodeBlock block) {
		blocks.remove(block);
		block.setParent(null);
	}

	/**
	 * adds <code>block</code> to <code>this</code>, saves zero as the indent
	 * 
	 * @param block
	 *            will be added to <code>this</code>, must not be
	 *            <code>this</code>
	 */
	public void add(CodeBlock block) {
		add(block, 0);
	}

	/**
	 * adds <code>block</code> to <code>this</code> <code>this</code> will be
	 * removed from <code>block</code>´s parent and set as its new parent
	 * 
	 * @param block
	 *            will be added to <code>this</code>, must not be
	 *            <code>this</code>
	 * @param addIndent
	 *            the indent belonging to <code>block</code>
	 */
	public void add(CodeBlock block, int addIndent) {
		if (block == null) {
			return;
		}
		block.additionalIndent = addIndent;
		if (block.getParent() != null) {
			block.getParent().remove(block);
		}
		blocks.add(block);
		block.setParent(this);
	}

	/**
	 * adds <code>block</code> to <code>this</code> with -1 as the indent, this
	 * will result in no tabulators in front of <code>block</code>´s content
	 * when creating the code
	 * 
	 * @param block
	 *            will be added to <code>this</code>, must not be
	 *            <code>this</code>
	 */
	public void addNoIndent(CodeBlock block) {
		if (block != null) {
			add(block);
			block.additionalIndent = -1;
		}
	}

	/**
	 * calls for every saved block out of <code>blocks</code> its
	 * getCode()-function and puts the resulting Strings together
	 * 
	 * @param indentLevel
	 *            defines how much tabulators are used in front of every line,
	 *            if it is negative no tabulators are used, if it is zero one
	 *            tabulator is used and so on
	 * @return the stored <code>blocks</code> as a String
	 */
	@Override
	public String getCode(int indentLevel) {
		StringBuilder buf = new StringBuilder();
		for (CodeBlock block : blocks) {
			buf.append(block.getCode(block.additionalIndent + indentLevel + 1));
		}
		return buf.toString();
	}

	/**
	 * empties <code>this</code>
	 */
	@Override
	public void clear() {
		blocks.clear();
	}

	/**
	 * @return the number of elements, which are in all the blocks of which
	 *         <code>this</code> consists
	 */
	@Override
	public int size() {
		int result = 0;
		for (CodeBlock block : blocks) {
			result += block.size();
		}
		return result;
	}
}
