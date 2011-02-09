package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
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
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		cp.add(scrollPane);

		ToolTipManager.sharedInstance().registerComponent(tree);

		setSize(800, 800);
		pack();
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

}
