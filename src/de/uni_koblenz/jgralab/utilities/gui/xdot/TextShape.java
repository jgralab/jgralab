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
package de.uni_koblenz.jgralab.utilities.gui.xdot;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class TextShape extends Rectangle2D.Double {
	private static final long serialVersionUID = -582551581120693871L;

	private Font font;
	private String text;
	private int justify;
	private boolean justified;

	TextShape(double x, double y, int j, double w, String t, Font f) {
		// super(j == -1 ? x : j == 0 ? x - w / 2 : x - w, y, w, f.getSize2D());
		super(x, y, w, f.getSize2D());
		justify = j;
		font = f;
		text = t;
	}

	public Font getFont() {
		return font;
	}

	public String getText() {
		return text;
	}

	public void justify(Graphics2D g2) {
		if (justified) {
			return;
		}
		if (justify >= 0) {
			Rectangle2D bounds = font.getStringBounds(text,
					g2.getFontRenderContext());
			if (justify == 0) {
				x -= bounds.getWidth() / 2;
			} else {
				x -= bounds.getWidth();
			}
			width = bounds.getWidth();
		}
		justified = true;
	}
}
