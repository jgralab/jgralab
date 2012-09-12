package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import de.uni_koblenz.ist.utilities.gui.DrawingPanel;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.utilities.gui.xdot.XDotPanel;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizLayouter;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizProgram;

@SuppressWarnings("serial")
public class GraphViewer extends JDialog {

	private GreqlGui gui;
	private Graph graph;
	private String queryText;
	private InputStream xdotInputStream;
	private XDotPanel xdPanel;
	private JScrollPane graphScrollPane;

	public GraphViewer(GreqlGui gui, Graph graph, String queryText) {
		super(gui, gui.getMessage("GraphViewer.Title")); //$NON-NLS-1$
		this.gui = gui;
		this.queryText = queryText;
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.graph = graph;
		Tg2Dot t2d = new Tg2Dot();
		t2d.setGraph(graph);
		t2d.setReversedEdges(true);
		t2d.setPrintIncidenceNumbers(true);
		try {
			xdotInputStream = t2d.convertToGraphVizStream(new GraphVizProgram()
					.layouter(GraphVizLayouter.DOT).outputFormat(
							GraphVizOutputFormat.XDOT));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		add(createContent(), BorderLayout.CENTER);
		pack();
	}

	protected Component createContent() {
		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		pnl.setLayout(new BorderLayout());

		try {
			xdPanel = new XDotPanel(graph, xdotInputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		JSlider zoomSlider = new JSlider(xdPanel.getZoomLevelModel());
		zoomSlider
				.setMajorTickSpacing((DrawingPanel.ZOOM_MAX - DrawingPanel.ZOOM_MIN) / 10);
		zoomSlider.setPaintTicks(true);

		JButton btn = new JButton(gui.getMessage("GraphViewer.ZoomToFit")); //$NON-NLS-1$
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xdPanel.zoomToFit(graphScrollPane.getSize());
			}
		});

		JPanel controlPanel = new JPanel();
		controlPanel.add(btn);
		controlPanel.add(zoomSlider);
		pnl.add(controlPanel, BorderLayout.NORTH);

		xdPanel.setAutoscrolls(true);
		graphScrollPane = new JScrollPane(xdPanel);
		graphScrollPane.setPreferredSize(new Dimension(800, 600));
		graphScrollPane.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		JTextArea queryArea = new JTextArea();
		queryArea.setEditable(false);
		queryArea.setLineWrap(true);
		queryArea.setWrapStyleWord(true);
		queryArea.setText(queryText);
		JScrollPane queryTextScrollpane = new JScrollPane(queryArea);
		queryTextScrollpane.setPreferredSize(new Dimension(100, 100));

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				graphScrollPane, queryTextScrollpane);
		split.setContinuousLayout(true);
		pnl.add(split, BorderLayout.CENTER);
		return pnl;
	}
}
