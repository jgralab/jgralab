package de.uni_koblenz.jgralab.utilities.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class MergeManifest extends Task {
	private String metaDir;
	private String dest;
	private File manifestFile;

	public MergeManifest() {
		metaDir = "META-INF";
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public void setManifest(File manifestFile) {
		this.manifestFile = manifestFile;
	}

	@Override
	public void execute() {
		try {
			File targetMetaDir = new File(dest + File.separator + metaDir);
			File[] content = targetMetaDir.listFiles();
			List<File> manifests = new LinkedList<File>();
			// find manifest files
			for (File currentContent : content) {
				if (currentContent.isDirectory()) {
					String manifestFileName = currentContent.getAbsolutePath()
							+ File.separator + metaDir + File.separator
							+ "MANIFEST.MF";
					// System.out.println(manifestFileName);
					File currentManifestFile = new File(manifestFileName);
					if (currentManifestFile.exists()) {
						manifests.add(currentManifestFile);
					}
				}
			}
			int size = manifests.size();
			System.out.println("Found " + size
					+ " manifest files from other jars.");
			if (size > 0) {
				StringWriter manifestString = new StringWriter();
				PrintWriter targetManifest = new PrintWriter(manifestString);
				BufferedReader reader = new BufferedReader(new FileReader(
						manifestFile));

				while (true) {
					String currentLine = reader.readLine();
					if (currentLine != null) {
						targetManifest.println(currentLine);
					} else {
						reader.close();
						break;
					}
				}

				// search for name part and copy the remainder of the file,
				// assuming that only name parts are following
				for (File currentManifest : manifests) {
					reader = new BufferedReader(new FileReader(currentManifest));
					boolean copy = false;
					while (true) {
						String currentLine = reader.readLine();
						if (currentLine != null) {
							if (!copy && currentLine.startsWith("Name: ")) {
								System.out.println("Found name section in "
										+ currentManifest.getAbsolutePath());
								copy = true;
								targetManifest.println();
							}
							if (copy) {
								// System.out.println("Copying: " +
								// currentLine);
								targetManifest.println(currentLine);
							}
						} else {
							reader.close();
							break;
						}
					}
				}

				PrintWriter out = new PrintWriter(manifestFile);
				targetManifest.close();
				out.println(manifestString.toString());
				out.close();

			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
}
