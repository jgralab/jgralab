/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.utilities.tgschema2java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SchemaJarGenerator {

	private String path;

	private String packageName;

	private String jarFileName;

	private int storeMethod; // 0 = uncompressed, 1= compressed

	/**
	 * Creates a new SchemaJarGenerator.
	 * 
	 * @param pathToFiles
	 *            the path to the directory where the files that should be part
	 *            of the jar are located excluding the package name
	 * @param packageName
	 *            the name of the package the fiels are located in
	 * @param jarFileName
	 *            the name of the jar-file to create
	 */
	public SchemaJarGenerator(String pathToFiles, String packageName,
			String jarFileName) {
		this(pathToFiles, packageName, jarFileName, false);
	}

	/**
	 * Creates a new SchemaJarGenerator.
	 * 
	 * @param pathToFiles
	 *            the path to the directory where the files that should be part
	 *            of the jar are located excluding the package name
	 * @param packageName
	 *            the name of the package the fiels are located in
	 * @param jarFileName
	 *            the name of the jar-file to create
	 * @param compress
	 *            toggles wether the contents of thew jar should be compressed
	 *            or not
	 */
	public SchemaJarGenerator(String pathToFiles, String packageName,
			String jarFileName, boolean compress) {
		path = pathToFiles;
		this.packageName = packageName;
		this.jarFileName = jarFileName;
		if (compress) {
			storeMethod = ZipEntry.STORED;
		} else {
			storeMethod = ZipEntry.DEFLATED;
		}
	}

	public void createJar() throws Exception {
		ZipOutputStream zipStream = null;
		try {
			System.out.println("Jar file name is: " + jarFileName);
			zipStream = new ZipOutputStream(new FileOutputStream(path + "/"
					+ jarFileName));
			zipStream.setMethod(storeMethod);
			String ifacePackageDir = packageName.replaceAll("\\.", "/");
			File interfaceDir = new File(path + "/" + ifacePackageDir);
			putDirInJar(zipStream, interfaceDir.getAbsolutePath(),
					ifacePackageDir + "/");
			putDirInJar(zipStream, interfaceDir.getAbsolutePath() + "/impl",
					ifacePackageDir + "/impl/");
			zipStream.closeEntry();
		} finally {
			zipStream.close();
		}
	}

	private void putDirInJar(ZipOutputStream zipStream, String dirPath,
			String pathInJar) throws IOException {
		ZipEntry interfaceDirEntry = new ZipEntry(pathInJar);
		zipStream.putNextEntry(interfaceDirEntry);
		File[] interfaces = new File(dirPath).listFiles();
		for (File currentInterface : interfaces) {
			// don't write hidden files like they are created from svn
			if (currentInterface.getName().startsWith(".")) {
				continue;
			}
			// don't write directories
			if (currentInterface.isDirectory()) {

			} else {
				ZipEntry entry = new ZipEntry(pathInJar + "/"
						+ currentInterface.getName());
				zipStream.putNextEntry(entry);
				FileInputStream in = null;
				try {
					in = new FileInputStream(currentInterface);
					int len = 0;
					byte[] buf = new byte[4096];
					while ((len = in.read(buf)) > 0) {
						zipStream.write(buf, 0, len);
					}
				} finally {
					in.close();
				}
			}
		}
		zipStream.closeEntry();
	}

}
