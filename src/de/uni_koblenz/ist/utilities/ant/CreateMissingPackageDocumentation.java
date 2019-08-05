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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CreateMissingPackageDocumentation extends Task {
    private File srcDir;

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    @Override
    public void execute() {
        if (srcDir == null) {
            throw new BuildException("No source Folder specified.");
        }
        if (!(srcDir.exists() && srcDir.isDirectory())) {
            throw new BuildException("No valid source Folder specified.");
        }
        try {
            processDirectory(srcDir);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void processDirectory(File dir) throws IOException {
        File documentation = new File(dir.getAbsoluteFile() + File.separator + "package-info.java");
        File[] content = dir.listFiles();
        if (content == null) {
            return;
        }
        boolean containsFiles = false;
        for (File current : content) {
            if (current.isDirectory()) {
                processDirectory(current);
            }
            if (!containsFiles && current.isFile()) {
                containsFiles = true;
            }
        }
        if (containsFiles && !documentation.exists()) {
            createDocumentation(dir, documentation);
        }
    }

    private void createDocumentation(File dir, File documentation) throws IOException {
        String packageString = computePackage(dir);
        if (packageString.contains("-")) {
            return;
        }
        System.out.println("Creating " + documentation.getName() + " for package " + packageString);
        PrintWriter out = new PrintWriter(documentation);
        out.println("/**");
        out.println(" * TODO [documentation] write documentation for this package.");
        out.println(" */");
        out.println();
        out.print("package ");
        out.print(packageString);
        out.println(";");
        out.flush();
        out.close();
    }

    private String computePackage(File dir) {
        if (srcDir == null) {
            throw new IllegalStateException("srcDir was not set");
        }
        String filesep = Pattern.quote(File.separator);
        String[] prefix = srcDir.getAbsolutePath()
                .split(filesep);
        String[] currentPath = dir.getAbsolutePath()
                .split(filesep);
        String packageString = null;
        StringBuilder builder = new StringBuilder();
        for (int i = prefix.length; i < currentPath.length; i++) {
            if (i > prefix.length) {
                builder.append(".");
            }
            builder.append(currentPath[i]);
        }
        packageString = builder.toString();
        return packageString;
    }

    public static void main(String[] args) {
        CreateMissingPackageDocumentation o = new CreateMissingPackageDocumentation();
        o.setSrcDir(new File("../jgralab/src"));
        o.execute();
        System.out.println("Fini.");
    }

}
