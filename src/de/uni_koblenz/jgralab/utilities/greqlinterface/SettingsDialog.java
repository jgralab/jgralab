package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uni_koblenz.ist.utilities.gui.FontSelectionDialog;

public class SettingsDialog extends JDialog implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = -5656234050436317561L;

	private GreqlGui gui;
	private JButton cancelButton;
	private JButton okButton;

	private Font queryFont;
	private JTextField queryFontLabel;
	private JButton queryFontButton;

	private Font resultFont;
	private JTextField resultFontLabel;
	private JButton resultFontButton;

	private JList greqlFunctionList;
	private DefaultListModel greqlFunctionModel;
	private Action removeAction;
	private Action addAction;
	private JTextField functionNameField;

	@SuppressWarnings("serial")
	public SettingsDialog(GreqlGui app, Font qf, Font rf,
			List<String> greqlFunctions) {
		super(app, app.getApplicationName()
				+ app.getMessage("SettingsDialog.Title"), true); //$NON-NLS-1$
		gui = app;
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout(8, 8));
		pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		getContentPane().add(pnl);

		queryFont = qf;
		resultFont = rf;

		JPanel fontSettingsPanel = new JPanel();
		fontSettingsPanel.setLayout(new GridBagLayout());
		fontSettingsPanel.setBorder(BorderFactory.createTitledBorder(gui
				.getMessage("SettingsDialog.FontTitle")));//$NON-NLS-1$
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;

		c.gridy = 0;
		c.gridx = 0;
		JLabel lbl = new JLabel(
				gui.getMessage("SettingsDialog.QueryFontLabel"), JLabel.RIGHT); //$NON-NLS-1$
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		fontSettingsPanel.add(lbl, c);
		++c.gridx;
		queryFontLabel = new JTextField(FontSelectionDialog.getFontName(gui,
				queryFont), 30);
		queryFontLabel.setEditable(false);
		fontSettingsPanel.add(queryFontLabel, c);
		queryFontButton = new JButton(
				gui.getMessage("SettingsDialog.QueryFontButtonText")); //$NON-NLS-1$
		++c.gridx;
		fontSettingsPanel.add(queryFontButton, c);
		queryFontButton.addActionListener(this);

		c.gridy = 1;
		c.gridx = 0;
		lbl = new JLabel(gui.getMessage("SettingsDialog.ResultFontLabel"), //$NON-NLS-1$
				JLabel.RIGHT);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		fontSettingsPanel.add(lbl, c);
		++c.gridx;
		resultFontLabel = new JTextField(FontSelectionDialog.getFontName(gui,
				resultFont), 30);
		resultFontLabel.setEditable(false);
		fontSettingsPanel.add(resultFontLabel, c);
		resultFontButton = new JButton(
				gui.getMessage("SettingsDialog.ResultFontButtonText")); //$NON-NLS-1$
		++c.gridx;
		fontSettingsPanel.add(resultFontButton, c);
		resultFontButton.addActionListener(this);

		JPanel funcListPanel = new JPanel();
		funcListPanel.setLayout(new GridBagLayout());
		funcListPanel.setBorder(BorderFactory.createTitledBorder(gui
				.getMessage("SettingsDialog.FunctionsTitle"))); //$NON-NLS-1$

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridwidth = 4;
		c.weightx = c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		greqlFunctionModel = new DefaultListModel();
		for (String s : greqlFunctions) {
			greqlFunctionModel.addElement(s);
		}
		greqlFunctionList = new JList(greqlFunctionModel);
		greqlFunctionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		greqlFunctionList.setToolTipText(gui
				.getMessage("SettingsDialog.FunctionListToolTip")); //$NON-NLS-1$
		JScrollPane scp = new JScrollPane(greqlFunctionList);
		funcListPanel.add(scp, c);

		c.gridy = 1;
		c.gridx = 0;
		c.fill = 0;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		removeAction = new AbstractAction(
				gui.getMessage("SettingsDialog.RemoveFunctionAction")) { //$NON-NLS-1$
			{
				setEnabled(false);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = greqlFunctionList.getSelectedIndex();
				functionNameField.setText((String) greqlFunctionModel
						.getElementAt(index));
				functionNameField.requestFocusInWindow();
				greqlFunctionModel.remove(index);
				int size = greqlFunctionModel.getSize();

				if (size == 0) { // Nobody's left, disable firing.
					removeAction.setEnabled(false);
				} else { // Select an index.
					if (index == greqlFunctionModel.getSize()) {
						// removed item in last position
						index--;
					}
					greqlFunctionList.setSelectedIndex(index);
					greqlFunctionList.ensureIndexIsVisible(index);
				}
			}
		};
		funcListPanel.add(new JButton(removeAction), c);
		++c.gridx;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		functionNameField = new JTextField();
		functionNameField.setToolTipText(gui
				.getMessage("SettingsDialog.FunctionNameToolTip")); //$NON-NLS-1$
		funcListPanel.add(functionNameField, c);
		c.weightx = 0;
		++c.gridx;
		addAction = new AbstractAction(
				gui.getMessage("SettingsDialog.AddFunctionAction")) { //$NON-NLS-1$

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String name = functionNameField.getText().trim();

				// User didn't type in a unique name...
				if (name.equals("") || greqlFunctionModel.contains(name)) { //$NON-NLS-1$
					Toolkit.getDefaultToolkit().beep();
					functionNameField.requestFocusInWindow();
					functionNameField.selectAll();
					return;
				}

				// ordered insert
				int index = 0;
				while (index < greqlFunctionModel.size()
						&& name.compareTo((String) greqlFunctionModel
								.getElementAt(index)) >= 0) {
					++index;
				}
				greqlFunctionModel.insertElementAt(name, index);
				// If we just wanted to add to the end, we'd do this:
				// listModel.addElement(employeeName.getText());

				// Reset the text field.
				functionNameField.requestFocusInWindow();
				functionNameField.setText("");

				// Select the new item and make it visible.
				greqlFunctionList.setSelectedIndex(index);
				greqlFunctionList.ensureIndexIsVisible(index);
			}
		};
		funcListPanel.add(new JButton(addAction), c);
		greqlFunctionList.addListSelectionListener(this);
		++c.gridy;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		lbl = new JLabel(gui.getMessage("SettingsDialog.FunctionInfoText"), //$NON-NLS-1$
				JLabel.CENTER);
		funcListPanel.add(lbl, c);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		cancelButton = new JButton(
				gui.getMessage("SettingsDialog.CancelButtonText")); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		okButton = new JButton(gui.getMessage("SettingsDialog.OkButtonText")); //$NON-NLS-1$
		okButton.addActionListener(this);
		okButton.setDefaultCapable(true);
		buttonPanel.add(okButton);

		pnl.add(fontSettingsPanel, BorderLayout.NORTH);
		pnl.add(funcListPanel, BorderLayout.CENTER);
		pnl.add(buttonPanel, BorderLayout.SOUTH);
		pack();
		okButton.requestFocus();

		setLocationRelativeTo(gui);
		setVisible(true);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if (greqlFunctionList.getSelectedIndex() == -1) {
				// No selection, disable fire button.
				removeAction.setEnabled(false);
			} else {
				// Selection, enable the fire button.
				removeAction.setEnabled(true);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			dispose();
		}
		if ((e.getSource() == okButton) && isOk()) {
			gui.saveSettings(this);
			dispose();
		}
		if (e.getSource() == resultFontButton) {
			Font newFont = FontSelectionDialog.selectFont(gui,
					gui.getMessage("SettingsDialog.ResultFontTitle"), //$NON-NLS-1$
					resultFont, false);
			if (newFont != null) {
				resultFont = newFont;
				resultFontLabel.setText(FontSelectionDialog.getFontName(gui,
						resultFont));
			}
		}
		if (e.getSource() == queryFontButton) {
			Font newFont = FontSelectionDialog
					.selectFont(
							gui,
							gui.getMessage("SettingsDialog.QueryFontTitle"), queryFont, false); //$NON-NLS-1$
			if (newFont != null) {
				queryFont = newFont;
				queryFontLabel.setText(FontSelectionDialog.getFontName(gui,
						queryFont));
			}
		}
	}

	public Font getQueryFont() {
		return queryFont;
	}

	public Font getResultFont() {
		return resultFont;
	}

	public List<String> getGreqlFunctionList() {
		ArrayList<String> result = new ArrayList<String>();
		Enumeration<?> e = greqlFunctionModel.elements();
		while (e.hasMoreElements()) {
			result.add((String) e.nextElement());
		}
		return result;
	}

	private boolean isOk() {
		return true;
	}

}
