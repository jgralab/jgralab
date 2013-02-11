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
package de.uni_koblenz.ist.utilities.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class EmphasizedLabel extends JLabel {
	private static final long serialVersionUID = 881191039458402594L;

	private boolean fUseEmphasisColor;

	public static final Color OS_X_EMPHASIZED_FONT_COLOR = new Color(255, 255,
			255, 110);
	public static final Color OS_X_EMPHASIZED_FOCUSED_FONT_COLOR = new Color(
			0x000000);
	public static final Color OS_X_EMPHASIZED_UNFOCUSED_FONT_COLOR = new Color(
			0x3f3f3f);

	public EmphasizedLabel(String text) {
		super(text);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height += 1;
		return d;
	}

	@Override
	public Color getForeground() {
		Color retVal;
		Window window = SwingUtilities.getWindowAncestor(this);
		boolean hasFoucs = window != null && window.isFocused();

		if (fUseEmphasisColor) {
			retVal = OS_X_EMPHASIZED_FONT_COLOR;
		} else if (hasFoucs) {
			retVal = OS_X_EMPHASIZED_FOCUSED_FONT_COLOR;
		} else {
			retVal = OS_X_EMPHASIZED_UNFOCUSED_FONT_COLOR;
		}

		return retVal;
	}

	@Override
	protected void paintComponent(Graphics g) {
		fUseEmphasisColor = true;
		g.translate(0, 1);
		super.paintComponent(g);
		g.translate(0, -1);
		fUseEmphasisColor = false;
		super.paintComponent(g);
	}
}