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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public abstract class SwingApplication extends JFrame {
	public static boolean RUNS_ON_WINDOWS = System.getProperty("os.name") //$NON-NLS-1$
			.toLowerCase().startsWith("windows"); //$NON-NLS-1$
	public static boolean RUNS_ON_LINUX = System.getProperty("os.name") //$NON-NLS-1$
			.toLowerCase().startsWith("linux"); //$NON-NLS-1$
	public static boolean RUNS_ON_MAC_OS_X = System.getProperty("os.name") //$NON-NLS-1$
			.toLowerCase().startsWith("mac os x"); //$NON-NLS-1$

	protected JMenu fileMenu;
	protected JMenu recentFilesMenu;
	protected Action fileClearRecentFilesList;
	protected Action fileNewAction;
	protected Action fileOpenAction;
	protected Action fileSaveAction;
	protected Action fileSaveAsAction;
	protected Action fileCloseAction;
	protected Action filePrintAction;
	protected Action fileExitAction;

	protected JMenu editMenu;
	protected Action editUndoAction;
	protected Action editRedoAction;
	protected Action editCopyAction;
	protected Action editCutAction;
	protected Action editPasteAction;

	protected Action editPreferencesAction;

	protected JMenu helpMenu;
	protected Action helpAboutAction;

	private boolean modified;
	private JMenuBar menuBar;
	protected JPanel toolBar;
	private JPanel contentPanel;
	private StatusBar statusBar;
	protected int menuEventMask;

	private ResourceBundle messages;

	public SwingApplication(ResourceBundle messages) {
		super(messages.getString("Application.mainwindow.title")); //$NON-NLS-1$
		this.messages = messages;
	}

	public String getMessage(String key) {
		try {
			return messages.getString(key);
		} catch (MissingResourceException e) {
			System.err.println(key);
			return '!' + key + '!';
		}
	}

	public String getMessage(String key, String defaultValue) {
		try {
			return messages.getString(key);
		} catch (MissingResourceException e) {
			System.err.println(key);
			return defaultValue;
		}
	}

	public void initializeApplication() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// UIManager.setLookAndFeel(UIManager
			// .getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (RUNS_ON_MAC_OS_X) {
			System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		menuEventMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		createActions();
		setJMenuBar(createMenuBar());
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		toolBar = createToolBar();
		contentPanel.add(toolBar, BorderLayout.NORTH);
		statusBar = createStatusBar();
		contentPanel.add(statusBar, BorderLayout.SOUTH);
		Component content = createContent();
		contentPanel.add(content, BorderLayout.CENTER);

		getContentPane().add(contentPanel);
		updateActions();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				fileExit();
			}
		});
		pack();
	}

	protected JPanel createToolBar() {
		JPanel result = RUNS_ON_MAC_OS_X ? new UnifiedToolbarPanel()
				: new JPanel();
		return result;
	}

	public int getMenuEventMask() {
		return menuEventMask;
	}

	protected void createActions() {
		fileClearRecentFilesList = new AbstractAction(getMessage(
				"Application.Action.File.ClearRecentFiles", "Clear list")) { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public void actionPerformed(ActionEvent e) {
				fileClearRecentFiles();
			}
		};

		fileNewAction = new AbstractAction(getMessage(
				"Application.Action.File.New", "New")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_N, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileNew();
			}
		};

		fileOpenAction = new AbstractAction(getMessage(
				"Application.Action.File.Open", "Open ...")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_O, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileOpen();
			}
		};

		fileSaveAction = new AbstractAction(getMessage(
				"Application.Action.File.Save", "Save")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_S, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileSave();
			}

		};

		fileSaveAsAction = new AbstractAction(getMessage(
				"Application.Action.File.SaveAs", "Save as ...")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_S,
								KeyEvent.SHIFT_DOWN_MASK | menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileSaveAs();
			}
		};

		fileCloseAction = new AbstractAction(getMessage(
				"Application.Action.File.Close", "Close ...")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_W, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileClose();
			}
		};

		filePrintAction = new AbstractAction(getMessage(
				"Application.Action.File.Print", "Print ...")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_P, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				filePrint();
			}
		};

		fileExitAction = new AbstractAction(getMessage(
				"Application.Action.File.Exit", "Exit")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_Q, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				fileExit();
			}
		};

		editUndoAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Undo", "Undo")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editUndo();
			}
		};

		editRedoAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Redo", "Redo")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_Z,
								KeyEvent.SHIFT_DOWN_MASK | menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editRedo();
			}
		};

		editCutAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Cut", "Cut")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_X, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editCut();
			}
		};

		editCopyAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Copy", "Copy")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_C, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editCopy();
			}
		};

		editPasteAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Paste", "Paste")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_V, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editPaste();
			}
		};

		editPreferencesAction = new AbstractAction(getMessage(
				"Application.Action.Edit.Settings", "Settings")) { //$NON-NLS-1$ //$NON-NLS-2$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke
								.getKeyStroke(KeyEvent.VK_COMMA, menuEventMask));
				setEnabled(true);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				editPreferences();
			}
		};

		helpAboutAction = new AbstractAction(MessageFormat.format(
				getMessage("Application.Action.Help.About", "About {0} ..."), //$NON-NLS-1$ //$NON-NLS-2$
				getTitle())) {
			@Override
			public void actionPerformed(ActionEvent e) {
				helpAbout();
			}
		};
	}

	protected JMenuBar createMenuBar() {
		recentFilesMenu = new JMenu(getMessage(
				"Application.Menu.File.RecentFiles", "Recent files")); //$NON-NLS-1$ //$NON-NLS-2$
		recentFilesMenu.addSeparator();
		recentFilesMenu.add(fileClearRecentFilesList);
		fileMenu = new JMenu(
				getMessage(
						RUNS_ON_MAC_OS_X ? "Application.Menu.File.MacOS" : "Application.Menu.File", //$NON-NLS-1$ //$NON-NLS-2$ 
						"File"));
		fileMenu.add(fileNewAction);
		fileMenu.add(fileOpenAction);
		fileMenu.add(recentFilesMenu);
		fileMenu.addSeparator();
		fileMenu.add(fileCloseAction);
		fileMenu.add(fileSaveAction);
		fileMenu.add(fileSaveAsAction);
		fileMenu.addSeparator();
		fileMenu.add(filePrintAction);
		if (RUNS_ON_MAC_OS_X) {
			try {
				OSXAdapter.setPreferencesHandler(this,
						SwingApplication.class.getDeclaredMethod(
								"editPreferences", new Class<?>[] {})); //$NON-NLS-1$
				OSXAdapter.setQuitHandler(this, SwingApplication.class
						.getDeclaredMethod("fileExit", new Class<?>[] {})); //$NON-NLS-1$
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			fileMenu.addSeparator();
			fileMenu.add(fileExitAction);
		}

		editMenu = new JMenu(getMessage("Application.Menu.Edit", "Edit")); //$NON-NLS-1$ //$NON-NLS-2$
		editMenu.add(editUndoAction);
		editMenu.add(editRedoAction);
		editMenu.addSeparator();
		editMenu.add(editCutAction);
		editMenu.add(editCopyAction);
		editMenu.add(editPasteAction);
		if (!RUNS_ON_MAC_OS_X) {
			editMenu.addSeparator();
			editMenu.add(editPreferencesAction);
		}

		helpMenu = new JMenu(getMessage("Application.Menu.Help", "Help")); //$NON-NLS-1$ //$NON-NLS-2$
		// System.out.println(helpMenu);
		if (RUNS_ON_MAC_OS_X) {
			try {
				OSXAdapter.setAboutHandler(this, SwingApplication.class
						.getDeclaredMethod("helpAbout", new Class<?>[] {})); //$NON-NLS-1$
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			helpMenu.add(helpAboutAction);
		}

		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		// helpMenu.putClientProperty(JComponent., value)
		return menuBar;
	}

	protected ImageIcon getApplicationIcon() {
		return null;
	}

	public String getApplicationName() {
		return getMessage("Application.name"); //$NON-NLS-1$
	}

	protected StatusBar createStatusBar() {
		return new StatusBar(this);
	}

	protected void updateActions() {

	}

	protected void fileClearRecentFiles() {

	}

	protected void fileNew() {

	}

	protected void fileOpen() {

	}

	public void fileOpenRecent(RecentFilesList recentFilesList, String filename) {

	}

	protected void fileSave() {

	}

	protected boolean fileSaveAs() {
		return false;
	}

	protected void fileClose() {

	}

	protected void filePrint() {

	}

	protected void editUndo() {

	}

	protected void editRedo() {

	}

	protected void editCut() {

	}

	protected void editCopy() {

	}

	protected void editPaste() {

	}

	protected void editPreferences() {

	}

	protected boolean fileExit() {
		if (confirmExit()) {
			System.exit(0);
		}
		return false;
	}

	protected void helpAbout() {
	}

	protected abstract Component createContent();

	protected boolean confirmExit() {
		return false;
	}

	protected boolean confirmClose() {
		return false;
	}

	public abstract String getVersion();

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		if (this.modified == modified) {
			return;
		}
		if (RUNS_ON_MAC_OS_X) {
			getRootPane()
					.putClientProperty("Window.documentModified", modified); //$NON-NLS-1$
		}
		this.modified = modified;
	}

	public static void invokeAndWait(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public static void invokeLater(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	protected String getKeyStrokeAsString(KeyStroke ks) {
		String result = ""; //$NON-NLS-1$
		if ((ks.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
			result += (RUNS_ON_MAC_OS_X ? "\u21e7" : getMessage( //$NON-NLS-1$
					"Application.Key.Shift", "Shift")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ((ks.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
			result += (RUNS_ON_MAC_OS_X ? "\u2303" //$NON-NLS-1$
					: (result.length() > 0 ? "+" : "") //$NON-NLS-1$ //$NON-NLS-2$
							+ getMessage("Application.Key.Ctrl", "Ctrl")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ((ks.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
			result += (RUNS_ON_MAC_OS_X ? "\u2325" //$NON-NLS-1$
					: (result.length() > 0 ? "+" : "") //$NON-NLS-1$ //$NON-NLS-2$
							+ getMessage("Application.Key.Alt", "Alt")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ((ks.getModifiers() & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
			result += (result.length() > 0 ? "+" : "") //$NON-NLS-1$ //$NON-NLS-2$
					+ getMessage("Application.Key.AltGr", "AltGr"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ((ks.getModifiers() & InputEvent.META_DOWN_MASK) != 0) {
			result += (RUNS_ON_MAC_OS_X ? "\u2318" //$NON-NLS-1$
					: (result.length() > 0 ? "+" : "") //$NON-NLS-1$ //$NON-NLS-2$
							+ getMessage("Application.Key.Meta", "Meta")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (RUNS_ON_MAC_OS_X) {
			result += (char) ks.getKeyCode();
		} else {
			result += (result.length() > 0 ? "+" : "") + //$NON-NLS-1$ //$NON-NLS-2$ 
					(char) ks.getKeyCode();
		}
		return result;
	}

	public class FileDialog {
		private File lastDir;
		private String appName;

		public FileDialog(String appName) {
			this.appName = appName;
		}

		public void setDirectory(File dir) {
			if (dir.exists() && dir.isDirectory() && dir.canRead()) {
				lastDir = dir;
			}
		}

		public File showFileOpenDialog(JFrame parent, final String title,
				final String extension, final String documentName) {
			File selectedFile = null;
			if (RUNS_ON_MAC_OS_X) {
				java.awt.FileDialog fd = new java.awt.FileDialog(parent, title,
						java.awt.FileDialog.LOAD);
				if (lastDir != null) {
					fd.setDirectory(lastDir.getAbsolutePath());
				}
				fd.setFilenameFilter(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						File f = new File(dir, name);
						return f.canRead() && name.endsWith(extension);
					}
				});
				if (RUNS_ON_MAC_OS_X) {
					menuBar.setEnabled(false);
				}
				fd.setVisible(true);
				if (RUNS_ON_MAC_OS_X) {
					menuBar.setEnabled(true);
				}
				if (fd.getFile() != null) {
					String name = fd.getDirectory() + fd.getFile();
					selectedFile = new File(name);
				}
			} else {
				JFileChooser jfc = new JFileChooser(lastDir);
				jfc.setDialogTitle(title);
				jfc.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return documentName;
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.canRead() && f.isFile()
								&& f.getAbsolutePath().endsWith(extension);
					}
				});
				if (jfc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					File f = jfc.getSelectedFile();
					if (f.isFile()) {
						selectedFile = f;
					}
				}
				lastDir = jfc.getCurrentDirectory();
			}
			return selectedFile;
		}

		public File showFileSaveAsDialog(JFrame parent, final String title,
				final String extension, File oldFile) {
			java.awt.FileDialog fd = new java.awt.FileDialog(parent, title,
					java.awt.FileDialog.SAVE);
			fd.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					File f = new File(dir, name);
					return f.canRead() && f.canWrite()
							&& name.endsWith(extension);
				}
			});
			fd.setModal(true);
			if (RUNS_ON_MAC_OS_X) {
				menuBar.setEnabled(false);
				SwingApplication.this.validate();
			}
			fd.setVisible(true);
			if (RUNS_ON_MAC_OS_X) {
				menuBar.setEnabled(true);
				SwingApplication.this.validate();
			}
			if (fd.getFile() != null) {
				String name = fd.getDirectory() + fd.getFile();
				if (!name.endsWith(extension)) {
					name += extension;
				}
				File f = new File(name);
				System.out.println(f);
				if (f.exists()) {
					if (JOptionPane.showConfirmDialog(parent, MessageFormat
							.format(SwingApplication.this.getMessage(
									"Application.FileDialog.Overwrite", //$NON-NLS-1$
									"File {0} exists. Overwrite?"), f //$NON-NLS-1$
									.getName()), appName,
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						return f;
					}
				} else if (!f.exists() && f.getParentFile().canWrite()) {
					return f;
				}
			}
			return null;
		}
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			updateActions();
		}
	}
}
