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
package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class QueryEditorPanel extends JPanel {
	private static final long serialVersionUID = -5113284469152114552L;

	private GreqlGui gui;
	private JTextArea queryArea;
	private UndoManager undoManager;
	private File queryFile;
	private boolean modified;

	public QueryEditorPanel(GreqlGui parent) throws IOException {
		this(parent, null);
	}

	public QueryEditorPanel(GreqlGui app, File f) throws IOException {
		gui = app;
		queryFile = null;

		queryArea = new JTextArea(15, 50);
		queryArea.setEditable(true);
		queryArea.setFont(gui.getQueryFont());
		queryArea.setToolTipText(MessageFormat.format(
				gui.getMessage("GreqlGui.QueryArea.ToolTip"),//$NON-NLS-1$
				gui.getEvaluateQueryShortcut()));
		undoManager = new UndoManager();
		undoManager.setLimit(10000);
		Document doc = queryArea.getDocument();

		if (f == null) {
			newFile();
		} else {
			loadFromFile(f);
		}

		doc.addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undoManager.addEdit(evt.getEdit());
				setModified(true);
				gui.updateActions();
			}
		});

		JScrollPane queryScrollPane = new JScrollPane(queryArea);
		setLayout(new BorderLayout());
		add(queryScrollPane, BorderLayout.CENTER);
	}

	public void setSelection(int offset, int length) {
		if (offset < 0) {
			return;
		}
		int start = Math.max(0,
				Math.min(offset, queryArea.getDocument().getLength()));
		int end = Math.max(start,
				Math.min(start + length, queryArea.getDocument().getLength()));
		queryArea.setSelectionStart(start);
		queryArea.setSelectionEnd(end);
	}

	public boolean canUndo() {
		return undoManager.canUndo();
	}

	public boolean canRedo() {
		return undoManager.canRedo();
	}

	public void undo() {
		undoManager.undo();
		setModified(true);
	}

	public void redo() {
		undoManager.redo();
		setModified(true);
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public boolean isModified() {
		return modified;
	}

	public void cut() {
		queryArea.cut();
	}

	public void copy() {
		queryArea.copy();
	}

	public void paste() {
		queryArea.paste();
	}

	public String getText() {
		return queryArea.getText();
	}

	public void removeJavaQuotes() {
		String text = queryArea.getText().trim();
		text = text.replaceAll("\"\\s*\\+\\s*\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\"", "\uffff"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\uffff", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		queryArea.setText(text);
	}

	public void insertJavaQuotes() {
		String text = queryArea.getText();
		String[] lines = text.split("\n"); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			line = line.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			line = line.replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
			line = line.replace("\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("\"").append(line); //$NON-NLS-1$
			if (i < lines.length - 1) {
				sb.append("\\n\" +\n"); //$NON-NLS-1$ 
			} else {
				sb.append("\""); //$NON-NLS-1$
			}
		}
		text = sb.toString();
		queryArea.setText(text);
		setSelection(0, queryArea.getDocument().getLength());
		queryArea.copy();
	}

	@Override
	public void requestFocus() {
		queryArea.requestFocus();
	}

	public void setQuery(File queryFile, String queryText) {
		this.queryFile = queryFile;
		queryArea.setText(queryText);
		undoManager.discardAllEdits();
		setModified(false);
	}

	public File getQueryFile() {
		return queryFile;
	}

	public void setQueryFile(File queryFile) {
		this.queryFile = queryFile;
	}

	public void loadFromFile(File f) throws IOException {
		BufferedReader rdr = new BufferedReader(new FileReader(f));
		StringBuffer sb = new StringBuffer();
		for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
			sb.append(line).append("\n"); //$NON-NLS-1$
		}
		rdr.close();
		setQuery(f, sb.toString());
	}

	public void saveToFile(File f) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		pw.print(queryArea.getText());
		pw.close();
		queryFile = f;
		setModified(false);
	}

	private void newFile() {
		setQuery(null, gui.getMessage("GreqlGui.NewQuery.Text")); //$NON-NLS-1$
		setSelection(0, queryArea.getDocument().getLength());
	}

	public String getFileName() {
		return queryFile == null ? gui.getMessage("GreqlGui.NewQuery.Title") //$NON-NLS-1$
				: queryFile.getName();
	}

	public void setQueryFont(Font queryFont) {
		queryArea.setFont(queryFont);
	}
}
