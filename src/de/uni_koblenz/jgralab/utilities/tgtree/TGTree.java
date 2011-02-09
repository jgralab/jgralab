package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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

	public TGTree(Graph g) {
		super("TGTree <" + g.getId() + ">");
		this.graph = g;

		Container cp = getContentPane();
		tree = new JTree(new TGraphTreeModel(new VertexTreeNode(
				graph.getFirstVertex(), null)));
		tree.setCellRenderer(new GraphElementCellRenderer());
		tree.addMouseListener(new TreeViewMouseAdapter());
		tree.addKeyListener(new TreeViewKeyAdapter());
		JScrollPane scrollPane = new JScrollPane();
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
			System.out.println("Key = " + e);
			final GraphElementTreeNode getn = (GraphElementTreeNode) tree
					.getLastSelectedPathComponent();
			if (getn == null) {
				return;
			}
			// Somehowe getKeyCode() returns 0 on my system, but enter is 10...
			if ((e.getKeyCode() == KeyEvent.VK_ENTER)
					|| (e.getKeyChar() == '\n')) {
				setTreeViewRoot(getn.get());
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
			contextMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
