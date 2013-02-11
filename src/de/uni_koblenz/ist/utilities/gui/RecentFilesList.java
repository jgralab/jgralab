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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public abstract class RecentFilesList extends StringListPreferences {
	private int maxEntries;
	private JMenu menu;
	private int initialItemCount;

	public RecentFilesList(Preferences prefs, String key, int maxEntries,
			JMenu menu) {
		super(prefs, key);
		this.maxEntries = maxEntries;
		this.menu = menu;
		initialItemCount = menu.getItemCount();
		load(maxEntries);
		updateMenu();
	}

	public void rememberFile(File f) {
		if (f == null) {
			return;
		}
		try {
			String name = f.getCanonicalPath();
			int i = entries.indexOf(name);
			if (i == 0) {
				return;
			}
			if (i > 0) {
				entries.remove(i);
			}
			while (entries.size() >= maxEntries) {
				entries.remove(size() - 1);
			}
			entries.add(0, name);
			updateMenu();
			save();
		} catch (IOException e) {
		}
	}

	private void updateMenu() {
		while (menu.getItemCount() > initialItemCount) {
			menu.remove(menu.getItem(0));
		}
		for (int n = 0; n < entries.size(); ++n) {
			menu.add(new RecentMenuItem(entries.get(n)), n);
		}
	}

	private class RecentMenuItem extends JMenuItem {
		private static final long serialVersionUID = 6317371073043983226L;
		String filename;

		public RecentMenuItem(String n) {
			super(n);
			filename = n;
			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					openRecentFile(new File(filename));
				}
			});
		}
	}

	public abstract void openRecentFile(File file);
}
