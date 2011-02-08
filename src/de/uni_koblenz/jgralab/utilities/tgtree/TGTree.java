package de.uni_koblenz.jgralab.utilities.tgtree;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;

public class TGTree extends JFrame {

	abstract private class GraphElementTreeNode implements TreeNode {
		protected ArrayList<GraphElementTreeNode> incs;
		protected GraphElementTreeNode parent;

		protected abstract void init();

		protected abstract GraphElement get();

		protected GraphElementTreeNode(GraphElementTreeNode parent) {
			this.parent = parent;
		}

		@Override
		public Enumeration<GraphElementTreeNode> children() {
			init();
			return Collections.enumeration(incs);
		}

		@Override
		public boolean getAllowsChildren() {
			// TODO Auto-generated method stub
			init();
			return false;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			init();
			return incs.get(childIndex);
		}

		@Override
		public int getChildCount() {
			init();
			return incs.size();
		}

		@Override
		public int getIndex(TreeNode node) {
			init();
			return incs.indexOf(node);
		}

		@Override
		public TreeNode getParent() {
			return parent;
		}

		@Override
		public boolean isLeaf() {
			init();
			return incs.isEmpty();
		}

		public String getToolTipText() {
			if (get().getAttributedElementClass().getAttributeList().isEmpty()) {
				return "<no attrs>";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			for (Attribute attr : get().getAttributedElementClass()
					.getAttributeList()) {
				sb.append(attr.getName());
				sb.append(" = ");
				sb.append(get().getAttribute(attr.getName()));
				sb.append("<br>");
			}
			sb.append("</html>");
			return sb.toString();
		}
	}

	private class VertexTreeNode extends GraphElementTreeNode {
		private Vertex v;

		public VertexTreeNode(Vertex v, EdgeTreeNode parent) {
			super(parent);
			this.v = v;
		}

		@Override
		protected void init() {
			if (incs != null) {
				return;
			}
			incs = new ArrayList<GraphElementTreeNode>();
			for (Edge e : v.incidences()) {
				incs.add(new EdgeTreeNode(e, this));
			}
		}

		@Override
		public String toString() {
			return v.toString();
		}

		@Override
		protected GraphElement get() {
			return v;
		}
	}

	private class EdgeTreeNode extends GraphElementTreeNode {

		private Edge e;

		public EdgeTreeNode(Edge e, VertexTreeNode parent) {
			super(parent);
			this.e = e;
		}

		@Override
		public String toString() {
			String arrow = null;
			if (e.isNormal()) {
				if (e.getThatSemantics() != AggregationKind.NONE) {
					arrow = "<>--> ";
				} else if (e.getThisSemantics() != AggregationKind.NONE) {
					arrow = "--><> ";
				} else {
					arrow = "--> ";
				}
			} else {
				if (e.getThatSemantics() != AggregationKind.NONE) {
					arrow = "<><-- ";
				} else if (e.getThisSemantics() != AggregationKind.NONE) {
					arrow = "<--<> ";
				} else {
					arrow = "<-- ";
				}
			}
			return arrow + e.toString();
		}

		@Override
		protected void init() {
			incs = new ArrayList<GraphElementTreeNode>();
			incs.add(new VertexTreeNode(e.getThat(), this));
		}

		@Override
		protected GraphElement get() {
			return e;
		}

	}

	private class TGraphTreeModel extends DefaultTreeModel {
		public TGraphTreeModel(TreeNode root) {
			super(root);
		}
	}

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

	private class GraphElementCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = -1698523886275339684L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			GraphElementTreeNode getn = (GraphElementTreeNode) value;
			setToolTipText(getn.getToolTipText());

			if (getn instanceof EdgeTreeNode) {
				setIcon(null);
			}
			
			return this;
		}
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
