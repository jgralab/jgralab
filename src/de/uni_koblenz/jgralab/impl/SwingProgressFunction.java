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
package de.uni_koblenz.jgralab.impl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import de.uni_koblenz.jgralab.ProgressFunction;

/**
 * A ProgressFunction to display a graphical progress bar in a Swing GUI. A
 * JFrame is automatically created on initialisation, and also automatically
 * disposed after finishing.
 * 
 * @author riediger
 * 
 */
public class SwingProgressFunction implements ProgressFunction, ActionListener {
	private JFrame wnd;
	private JProgressBar pb;
	private String title;
	private String label;
	private String itemName;
	private long totalElements;
	private long updateInterval;
	private BoundedRangeModel brm;
	private JLabel lbl;
	private long startTime;
	private Timer timer;
	private NumberFormat elementFormatter;
	private NumberFormat timeFormatter;

	/**
	 * Creates a ProgressFunction to display progress in a Swing GUI.
	 * 
	 * @param title
	 *            title for the JFrame of the progress display
	 * @param label
	 *            text to be displayed above the progress bar
	 */
	public SwingProgressFunction(String title, String label) {
		this(title, label, "elements");
	}

	public SwingProgressFunction(String title, String label, String itemName) {
		this.title = title;
		this.label = label;
		this.itemName = itemName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#finished()
	 */
	@Override
	public void finished() {
		invoke(new Runnable() {
			public void run() {
				brm.setValue(brm.getMaximum());
				setTimeText();
				timer = new Timer(1000, SwingProgressFunction.this);
				timer.start();
			};
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#getUpdateInterval()
	 */
	@Override
	public long getUpdateInterval() {
		return updateInterval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#init(long)
	 */
	@Override
	public void init(long totalElements) {
		final long realTotalElements = (totalElements < 0) ? Long.MAX_VALUE
				: totalElements;
		invoke(new Runnable() {

			@Override
			public void run() {
				SwingProgressFunction.this.totalElements = realTotalElements;
				elementFormatter = NumberFormat
						.getInstance(Locale.getDefault());
				timeFormatter = NumberFormat.getInstance(Locale.getDefault());
				timeFormatter.setMinimumFractionDigits(1);
				timeFormatter.setMaximumFractionDigits(1);

				wnd = new JFrame(title);
				wnd.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				wnd.setResizable(false);
				// wnd.setLayout(new BorderLayout());
				// wnd.setMinimumSize(new Dimension(200, 100));

				pb = new JProgressBar();
				brm = new DefaultBoundedRangeModel();
				pb.setModel(brm);
				updateInterval = brm.getMaximum() > realTotalElements ? 1
						: realTotalElements / brm.getMaximum();

				JPanel pnl = new JPanel();
				pnl.setLayout(new BorderLayout(8, 8));
				pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				wnd.getContentPane().add(pnl);

				lbl = new JLabel("#,###,### " + itemName + ", ###.#s",
						JLabel.CENTER);

				pnl.add(new JLabel(label, JLabel.CENTER), BorderLayout.NORTH);
				pnl.add(pb, BorderLayout.CENTER);
				pnl.add(lbl, BorderLayout.SOUTH);
				pnl.add(new JPanel(), BorderLayout.WEST);
				pnl.add(new JPanel(), BorderLayout.EAST);
				startTime = System.currentTimeMillis();
				wnd.pack();
				setTimeText();
				wnd.setVisible(true);
			}
		});
	}

	Runnable timeTextUpdater = new Runnable() {
		public void run() {
			lbl.setText(elementFormatter.format(totalElements)
					+ " "
					+ itemName
					+ ", "
					+ timeFormatter.format((System.currentTimeMillis() - startTime) / 1000.0)
					+ "s");
		}
	};

	private void setTimeText() {
		invoke(timeTextUpdater);
	}

	private void invoke(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}

	Runnable progressUpdater = new Runnable() {
		@Override
		public void run() {
			if (brm.getValue() < brm.getMaximum()) {
				brm.setValue(brm.getValue() + 1);
				setTimeText();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#progress(long)
	 */
	@Override
	public void progress(long processedElements) {
		invoke(progressUpdater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		timer.stop();
		wnd.dispose();
	}
}
