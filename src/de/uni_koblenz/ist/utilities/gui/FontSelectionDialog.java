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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FontSelectionDialog extends JDialog {
	/**
	 * Displays a modal dialog to select a Font. If an <code>oldFont</code> is
	 * passed, this will be preselected. The flag <code>monospacedOnly</code>
	 * can be used to restrict the selection to monospaced fonts (omitting
	 * proportional fonts). Setting <code>monospacedOnly</code> to
	 * <code>true</code> can be slow, since there is no efficient way to
	 * determine, whether a font is monospaced. Instead, all available fonts are
	 * tested.
	 * 
	 * @param parent
	 *            parent frame
	 * @param oldFont
	 *            a Font to be preselected
	 * @param monospacedOnly
	 *            when set to <code>true</code>, only monospaced fonts are
	 *            selectable
	 * @return the selected Font, or null if the dialog is cancelled
	 */
	public static Font selectFont(SwingApplication parent, String title,
			Font oldFont, boolean monospacedOnly) {
		FontSelectionDialog d = new FontSelectionDialog(parent, title, oldFont,
				monospacedOnly);
		d.setVisible(true);
		return d.getSelectedFont();
	}

	private static final long serialVersionUID = 8838643052145525312L;

	@SuppressWarnings("rawtypes")
	private final JList familyList;
	private final JLabel previewLabel;
	@SuppressWarnings("rawtypes")
	private final JList styleList;

	private String family;
	private int style;
	private Font selectedFont;
	@SuppressWarnings("rawtypes")
	private final JList sizeList;
	private final BoundedRangeModel fontSize;
	private final JLabel fontNameLabel;
	private final JTextField sizeField;
	private SwingApplication app;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private FontSelectionDialog(SwingApplication parent, String title,
			Font oldFont, boolean monospacedOnly) {
		super(parent, title != null ? title : parent
				.getMessage("FontSelectionDialog.Title")); //$NON-NLS-1$
		app = parent;
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				selectedFont = null;
			}
		});

		String[] availableFamilies = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		Arrays.sort(availableFamilies);

		if (monospacedOnly) {
			ArrayList<String> l = new ArrayList<String>();
			FontRenderContext frc = new FontRenderContext(
					AffineTransform.getTranslateInstance(0, 0), false, false);
			for (String fam : availableFamilies) {
				Font f = new Font(fam, Font.PLAIN, 10);
				Rectangle2D sbi = f.getStringBounds("i", frc); //$NON-NLS-1$
				Rectangle2D sbm = f.getStringBounds("m", frc); //$NON-NLS-1$
				if (sbi.getWidth() == sbm.getWidth()) {
					l.add(fam);
				}
			}
			availableFamilies = new String[l.size()];
			{
				int i = 0;
				for (String fam : l) {
					availableFamilies[i++] = fam;
				}
			}
		}
		Font f = new Font("SansSerif", Font.PLAIN, 11); //$NON-NLS-1$
		familyList = new JList(availableFamilies);
		familyList.setFont(f);
		familyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		familyList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = familyList.getSelectedIndex();
				if (i >= 0) {
					setFamily((String) familyList.getModel().getElementAt(i));
				}
			}

		});
		JPanel familyPanel = new JPanel();
		familyPanel.setLayout(new BorderLayout(0, 4));
		JLabel lbl = new JLabel(
				app.getMessage("FontSelectionDialog.FamilyLabel"), JLabel.LEFT); //$NON-NLS-1$
		lbl.setFont(f);
		familyPanel.add(lbl, BorderLayout.NORTH);
		JScrollPane scp = new JScrollPane(familyList);
		familyPanel.add(scp, BorderLayout.CENTER);

		String[] styles = { app.getMessage("FontSelectionDialog.PlainStyle"), //$NON-NLS-1$
				app.getMessage("FontSelectionDialog.BoldStyle"), //$NON-NLS-1$
				app.getMessage("FontSelectionDialog.ItalicStyle"), //$NON-NLS-1$
				app.getMessage("FontSelectionDialog.BoldItalicStyle") }; //$NON-NLS-1$
		styleList = new JList(styles);
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleList.setFont(f);
		styleList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = styleList.getSelectedIndex();
				if (i >= 0) {
					switch (i) {
					case 0:
						setStyle(Font.PLAIN);
						break;
					case 1:
						setStyle(Font.BOLD);
						break;
					case 2:
						setStyle(Font.ITALIC);
						break;
					case 3:
						setStyle(Font.BOLD + Font.ITALIC);
						break;
					}
				}
			}
		});

		JPanel stylePanel = new JPanel();
		stylePanel.setLayout(new BorderLayout(0, 4));
		lbl = new JLabel(
				app.getMessage("FontSelectionDialog.StyleLabel"), JLabel.LEFT); //$NON-NLS-1$
		lbl.setFont(f);
		stylePanel.add(lbl, BorderLayout.NORTH);
		scp = new JScrollPane(styleList);
		stylePanel.add(scp, BorderLayout.CENTER);

		fontSize = new DefaultBoundedRangeModel(12, 0, 9, 288);
		fontSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setSelectedFont();
				if (e.getSource() != sizeField) {
					sizeField.setText(Integer.toString(fontSize.getValue()));
				}
				if (e.getSource() != sizeList) {
					for (int i = 0; i < sizeList.getModel().getSize(); ++i) {
						if ((Integer) sizeList.getModel().getElementAt(i) == fontSize
								.getValue()) {
							sizeList.setSelectedIndex(i);
							sizeList.ensureIndexIsVisible(i);
							return;
						}
					}
					sizeList.clearSelection();
				}
			}
		});
		JSlider sld = new JSlider(fontSize);
		sld.setOrientation(JSlider.VERTICAL);

		final Integer[] sizes = { 9, 10, 11, 12, 13, 14, 18, 24, 36, 48, 64,
				72, 96, 144, 288 };
		sizeList = new JList(sizes);
		sizeList.setFont(f);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		sizeList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = sizeList.getSelectedIndex();
				if (i >= 0) {
					int newSize = (Integer) sizeList.getModel().getElementAt(i);
					fontSize.setValue(newSize);
				}
			}

		});

		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new BorderLayout(0, 0));
		lbl = new JLabel(
				app.getMessage("FontSelectionDialog.SizeLabel"), JLabel.LEFT); //$NON-NLS-1$
		lbl.setFont(f);
		sizePanel.add(lbl, BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		sizeField = new JTextField("13"); //$NON-NLS-1$
		sizeField.setHorizontalAlignment(JTextField.CENTER);
		sizeField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					Integer.parseInt(sizeField.getText().trim());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});
		p.add(sizeField, BorderLayout.NORTH);
		sizeField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sizeField.getInputVerifier().verify(sizeField)) {
					fontSize.setValue(Integer.parseInt(sizeField.getText()
							.trim()));
				}
			}
		});

		scp = new JScrollPane(sizeList);
		p.add(scp, BorderLayout.WEST);
		p.add(sld, BorderLayout.EAST);
		sizePanel.add(p, BorderLayout.CENTER);

		JPanel previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout(4, 4));
		previewPanel.setBackground(Color.WHITE);

		previewLabel = new JLabel(
				app.getMessage("FontSelectionDialog.SampleText"), JLabel.CENTER); //$NON-NLS-1$
		previewLabel.setPreferredSize(new Dimension(320, 60));

		fontNameLabel = new JLabel(
				app.getMessage("FontSelectionDialog.FontNameLabel"), JLabel.CENTER); //$NON-NLS-1$
		fontNameLabel.setForeground(Color.GRAY);
		fontNameLabel.setFont(f);
		previewPanel.setBorder(BorderFactory.createEtchedBorder());
		previewPanel.add(fontNameLabel, BorderLayout.SOUTH);
		previewPanel.add(previewLabel, BorderLayout.CENTER);

		p = new JPanel();
		p.setLayout(new BorderLayout(4, 0));
		p.add(familyPanel, BorderLayout.CENTER);

		JPanel p5 = new JPanel();
		p5.setLayout(new BorderLayout(4, 0));
		p5.add(stylePanel, BorderLayout.CENTER);
		p5.add(sizePanel, BorderLayout.EAST);
		p.add(p5, BorderLayout.EAST);

		JPanel p6 = new JPanel();
		JButton okButton = new JButton(
				app.getMessage("FontSelectionDialog.OkButtonText")); //$NON-NLS-1$
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}

		});
		JButton cancelButton = new JButton(
				app.getMessage("FontSelectionDialog.CancelButtonText")); //$NON-NLS-1$
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedFont = null;
				setVisible(false);
				dispose();
			}
		});
		p6.add(okButton);
		p6.add(cancelButton);
		p.add(p6, BorderLayout.SOUTH);

		p.setPreferredSize(new Dimension(previewLabel.getPreferredSize().width,
				180));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(4, 4));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		contentPanel.add(previewPanel, BorderLayout.CENTER);
		contentPanel.add(p, BorderLayout.SOUTH);
		getContentPane().add(contentPanel);

		if (oldFont == null) {
			oldFont = new Font(monospacedOnly ? "Monospaced" : "SansSerif", //$NON-NLS-1$ //$NON-NLS-2$
					Font.PLAIN, 13);
		}

		family = oldFont.getFamily();
		for (int i = 0; i < availableFamilies.length; ++i) {
			if (family.equals(availableFamilies[i])) {
				familyList.setSelectedIndex(i);
				familyList.ensureIndexIsVisible(i);
				break;
			}
		}

		style = oldFont.getStyle();
		switch (style) {
		case Font.PLAIN:
			styleList.setSelectedIndex(0);
			break;
		case Font.BOLD:
			styleList.setSelectedIndex(1);
			break;
		case Font.ITALIC:
			styleList.setSelectedIndex(2);
			break;
		case Font.BOLD + Font.ITALIC:
			styleList.setSelectedIndex(3);
			break;
		}
		fontSize.setValue(oldFont.getSize());
		pack();
	}

	private void setFamily(String newFamily) {
		if (!family.equals(newFamily)) {
			family = newFamily;
			setSelectedFont();
		}
	}

	private void setStyle(int newStyle) {
		if (style != newStyle) {
			style = newStyle;
			setSelectedFont();
		}
	}

	private String getStyleAsString() {
		switch (style) {
		case Font.PLAIN:
			return app.getMessage("FontSelectionDialog.PlainSampleText"); //$NON-NLS-1$
		case Font.BOLD:
			return app.getMessage("FontSelectionDialog.BoldSampleText"); //$NON-NLS-1$
		case Font.ITALIC:
			return app.getMessage("FontSelectionDialog.ItalicSampleText"); //$NON-NLS-1$
		case Font.BOLD + Font.ITALIC:
			return app.getMessage("FontSelectionDialog.BoldItalicSampleText"); //$NON-NLS-1$
		default:
			return app.getMessage("FontSelectionDialog.UnknownExampleText"); //$NON-NLS-1$
		}
	}

	public static String getFontName(SwingApplication app, Font font) {
		String style = (font.getStyle() == Font.PLAIN) ? app
				.getMessage("FontSelectionDialog.PlainStyle") : font //$NON-NLS-1$
				.getStyle() == Font.BOLD ? app
				.getMessage("FontSelectionDialog.BoldStyle") //$NON-NLS-1$
				: font.getStyle() == Font.ITALIC ? app
						.getMessage("FontSelectionDialog.ItalicStyle") : //$NON-NLS-1$
						app.getMessage("FontSelectionDialog.BoldItalicStyle"); //$NON-NLS-1$
		return font.getFamily() + "-" + style + "-" + font.getSize(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getInternalFontName(Font font) {
		String style = (font.getStyle() == Font.PLAIN) ? "plain" : font //$NON-NLS-1$
				.getStyle() == Font.BOLD ? "bold" //$NON-NLS-1$
				: font.getStyle() == Font.ITALIC ? "italic" : "bolditalic"; //$NON-NLS-1$ //$NON-NLS-2$
		return font.getFamily() + "-" + style + "-" + font.getSize(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private int getFontSize() {
		return fontSize.getValue();
	}

	private void setSelectedFont() {
		selectedFont = new Font(family, style, fontSize.getValue());
		previewLabel.setFont(selectedFont);
		fontNameLabel.setText(MessageFormat.format(
				app.getMessage("FontSelectionDialog.FontNameDisplay"), //$NON-NLS-1$
				selectedFont.getName(), getStyleAsString(), getFontSize()));
	}

	private Font getSelectedFont() {
		return selectedFont;
	}

}
