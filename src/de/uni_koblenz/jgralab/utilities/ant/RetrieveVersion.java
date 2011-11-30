package de.uni_koblenz.jgralab.utilities.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.tools.ant.Task;

public class RetrieveVersion extends Task {
	public final String VERSION_FILENAME = "version.properties";
	private static final String MAJOR = "major";
	private static final String MINOR = "minor";
	private static final String MICRO = "micro";
	private static final String CODENAME = "codename";

	private final Properties prop = new Properties();
	protected String major;
	protected String minor;
	protected String micro;
	protected String codename;

	@Override
	public void execute() {
		readProperties();
		getProject().setNewProperty("version",
				major + "." + minor + "." + micro);
		getProject().setNewProperty("version.major", major);
		getProject().setNewProperty("version.minor", minor);
		getProject().setNewProperty("version.micro", micro);
		getProject().setNewProperty("version.codename", codename);
	}

	protected void readProperties() {
		File propertyFile = new File(getProject().getBaseDir(),
				VERSION_FILENAME);
		try {
			prop.load(new FileInputStream(propertyFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		major = prop.getProperty(MAJOR, "0");
		minor = prop.getProperty(MINOR, "0");
		micro = prop.getProperty(MICRO, "0");
		codename = prop.getProperty(CODENAME, "?");
	}

	protected void saveProperties() {
		File propertyFile = new File(getProject().getBaseDir(),
				VERSION_FILENAME);
		prop.put(MAJOR, major);
		prop.put(MINOR, minor);
		prop.put(MICRO, micro);
		prop.put(CODENAME, codename);
		try {
			prop.store(new FileOutputStream(propertyFile),
					"JGraLab version information");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void writePomFile() {
		try {
			// write a new POM file
			PrintWriter w = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream("pom.xml"), "UTF-8"));
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<project>");
			w.println("\t<modelVersion>4.0.0</modelVersion>");
			w.println("\t<groupId>de.uni-koblenz.ist</groupId>");
			w.println("\t<artifactId>jgralab</artifactId>");
			w.println("\t<version>" + major + "." + minor + "." + micro + "-"
					+ codename + "</version>");
			w.println("\t<name>JGraLab</name>");
			w.println("\t<description>");
			w.println("\t\tA high-performance TGraph library.");
			w.println("\t\tSee http://jgralab.uni-koblenz.de");
			w.println("\t</description>");
			w.println("\t<repositories>");
			w.println("\t\t<repository>");
			w.println("\t\t\t<id>clojars</id>");
			w.println("\t\t\t<url>http://clojars.org/repo/</url>");
			w.println("\t\t</repository>");
			w.println("\t</repositories>");
			w.println("</project>");
			w.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
