/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public class UnifiedToolbarPanel extends JPanel {
	private static final long serialVersionUID = 1825519489284594986L;

	public static final Color OS_X_UNIFIED_TOOLBAR_FOCUSED_BOTTOM_COLOR = new Color(
			64, 64, 64);

	public static final Color OS_X_UNIFIED_TOOLBAR_UNFOCUSED_BORDER_COLOR = new Color(
			135, 135, 135);

	public UnifiedToolbarPanel() {
		// make the component transparent
		setOpaque(false);
		// create an empty border around the panel
		// note the border below is created using JGoodies Forms
		setBorder(BorderFactory.createEmptyBorder(3, 3, 1, 3));
	}

	@Override
	public Border getBorder() {
		Window window = SwingUtilities.getWindowAncestor(this);
		return window != null && window.isFocused() ? BorderFactory
				.createMatteBorder(0, 0, 1, 0,
						OS_X_UNIFIED_TOOLBAR_FOCUSED_BOTTOM_COLOR)
				: BorderFactory.createMatteBorder(0, 0, 1, 0,
						OS_X_UNIFIED_TOOLBAR_UNFOCUSED_BORDER_COLOR);
	}
}