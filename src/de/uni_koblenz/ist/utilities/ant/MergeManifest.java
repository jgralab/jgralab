/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.ist.utilities.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
            if (!targetMetaDir.exists()) {
                System.err.println("No " + metaDir + " directory. Skipping...");
                return;
            }
            File[] content = targetMetaDir.listFiles();
            if (content == null) {
                return;
            }
            List<File> manifests = new LinkedList<>();
            // find manifest files
            for (File currentContent : content) {
                if (currentContent.isDirectory()) {
                    String manifestFileName = currentContent.getAbsolutePath() + File.separator + metaDir
                            + File.separator + "MANIFEST.MF";
                    // System.out.println(manifestFileName);
                    File currentManifestFile = new File(manifestFileName);
                    if (currentManifestFile.exists()) {
                        manifests.add(currentManifestFile);
                    }
                }
            }
            int size = manifests.size();
            System.out.println("Found " + size + " manifest files from other jars.");
            if (size > 0) {
                StringWriter manifestString = new StringWriter();
                PrintWriter targetManifest = new PrintWriter(manifestString);
                BufferedReader reader = new BufferedReader(new FileReader(manifestFile));
                try {
                    for (String l = reader.readLine(); l != null; l = reader.readLine()) {
                        targetManifest.println(l);
                    }
                } finally {
                    reader.close();
                }

                // search for name part and copy the remainder of the file,
                // assuming that only name parts are following
                for (File currentManifest : manifests) {
                    reader = new BufferedReader(new FileReader(currentManifest));
                    try {
                        boolean copy = false;
                        for (String l = reader.readLine(); l != null; l = reader.readLine()) {
                            if (!copy && l.startsWith("Name: ")) {
                                System.out.println("Found name section in " + currentManifest.getAbsolutePath());
                                copy = true;
                                targetManifest.println();
                            }
                            if (copy) {
                                // System.out.println("Copying: " +
                                // currentLine);
                                targetManifest.println(l);
                            }

                        }
                    } finally {
                        reader.close();
                    }
                }
                targetManifest.close();
                PrintWriter out = new PrintWriter(manifestFile);
                out.println(manifestString.toString());
                out.close();
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
