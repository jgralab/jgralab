package de.uni_koblenz.jgralab.utilities.ant;

public class BumpMicroVersion extends RetrieveVersion {
	@Override
	public void execute() {
		readProperties();
		micro = Integer.toString(Integer.parseInt(micro) + 1);
		saveProperties();
		writePomFile();
	}
}
