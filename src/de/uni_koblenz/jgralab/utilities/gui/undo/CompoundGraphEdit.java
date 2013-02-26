package de.uni_koblenz.jgralab.utilities.gui.undo;

import javax.swing.undo.CompoundEdit;

public class CompoundGraphEdit extends CompoundEdit {
	private static final long serialVersionUID = 1061803791604107837L;
	private String name;

	public CompoundGraphEdit(String name) {
		this.name = name;
	}

	@Override
	public String getPresentationName() {
		return name;
	}

	@Override
	public String getUndoPresentationName() {
		// TODO Auto-generated method stub
		return "Undo " + name;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo " + name;
	}
}
