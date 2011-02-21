/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;

public class TGTree extends JFrame {

	private static final long serialVersionUID = 7575497288342049993L;

	private Graph graph;
	private JTree tree;
	JScrollPane scrollPane;

	public TGTree(Graph g) {
		super("TGTree <" + g.getId() + ">");
		this.graph = g;

		JMenuBar menuBar = new JMenuBar();
		JLabel idLabel = new JLabel("Select by id: ");
		menuBar.add(idLabel);
		final JTextField idField = new JTextField();
		menuBar.add(idField);
		idField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = idField.getText();
				char type = txt.charAt(0);
				int id = Integer.parseInt(txt.substring(1));
				if (type == 'v') {
					setTreeViewRoot(graph.getVertex(id));
				} else if (type == 'e') {
					setTreeViewRoot(graph.getEdge(id));
				}
			}
		});
		setJMenuBar(menuBar);

		Container cp = getContentPane();
		tree = new JTree(new TGraphTreeModel(new VertexTreeNode(
				graph.getFirstVertex(), null)));
		tree.setCellRenderer(new GraphElementCellRenderer());
		tree.addMouseListener(new TreeViewMouseAdapter());
		tree.addKeyListener(new TreeViewKeyAdapter());
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		cp.add(scrollPane);

		ToolTipManager.sharedInstance().registerComponent(tree);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(800, 800);
		pack();
	}

	public void setTreeViewRoot(GraphElement ge) {
		GraphElementTreeNode tn = null;
		if (ge instanceof Edge) {
			tn = new EdgeTreeNode((Edge) ge, null);
		} else if (ge instanceof Vertex) {
			tn = new VertexTreeNode((Vertex) ge, null);
		} else {
			throw new RuntimeException(ge + " is neither Vertex nor Edge.");
		}
		tree.setModel(new TGraphTreeModel(tn));
	}

	public void copySelectionToClipboard(GraphElementTreeNode getn) {
		String selection = getn.getClipboardText();
		System.out.println(selection + " ==> Clipboard");
		StringSelection data = new StringSelection(selection);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(data, null);
	}

	public static void main(String[] args) throws GraphIOException {
		if (args.length != 1) {
			System.err.println("Usage: TGTree <graphfile>");
			System.exit(1);
		}
		Graph g = GraphIO.loadSchemaAndGraphFromFile(args[0],
				CodeGeneratorConfiguration.MINIMAL,
				new ConsoleProgressFunction());
		TGTree tgtree = new TGTree(g);
		tgtree.setVisible(true);
	}

	private class TreeViewKeyAdapter extends KeyAdapter {

		@Override
		public void keyTyped(KeyEvent e) {
			final GraphElementTreeNode getn = (GraphElementTreeNode) tree
					.getLastSelectedPathComponent();
			if (getn == null) {
				return;
			}
			// Somehowe getKeyCode() returns 0 on my system, but enter is 10...
			if ((e.getKeyCode() == KeyEvent.VK_ENTER)
					|| (e.getKeyChar() == '\n')) {
				setTreeViewRoot(getn.get());
			} else if (e.getKeyChar() == 'c') {
				copySelectionToClipboard(getn);
			}
		}

	}

	private class TreeViewMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				handlePopupTrigger(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				handlePopupTrigger(e);
			}
		}

		private void handlePopupTrigger(MouseEvent e) {
			final GraphElementTreeNode getn = (GraphElementTreeNode) tree
					.getLastSelectedPathComponent();
			if (getn == null) {
				return;
			}
			JPopupMenu contextMenu = new JPopupMenu("Context Menu");
			contextMenu.add(new AbstractAction("Set Root") {

				private static final long serialVersionUID = 6789881997870852275L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setTreeViewRoot(getn.get());
				}
			});
			contextMenu.add(new AbstractAction("Copy to Clipboard") {

				private static final long serialVersionUID = 2860962360240219247L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					copySelectionToClipboard(getn);
				}
			});
			contextMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
