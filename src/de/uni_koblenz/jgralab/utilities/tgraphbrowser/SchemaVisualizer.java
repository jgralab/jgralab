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
package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tgraphbrowser.StateRepository.State;

/**
 * The SchemaVisualizer creates the representation of the inheritance hierarchy
 * of the EdgeClasses and the VertexClasses.
 */
public class SchemaVisualizer {

	/**
	 * Returns the JavaScript-code to create the representation of the
	 * inheritance hierarchy of the EdgeClasses and the VertexClasses.
	 * 
	 * @param code
	 * 
	 * @param state
	 */
	public void createSchemaRepresentation(StringBuilder code, State state) {
		createAttributedElementClassRepresentation(code, state, true);
		createAttributedElementClassRepresentation(code, state, false);
		createPackageRepresentation(code, state);
	}

	/**
	 * Creates the code for the representation of the packages.
	 * 
	 * @param code
	 * 
	 * @param state
	 */
	private void createPackageRepresentation(StringBuilder code, State state) {
		Package defaultPackage = state.getGraph().getSchema()
				.getDefaultPackage();
		code
				.append("var divPackage = document.getElementById(\"divPackage\");\n");
		code.append("var ulRootPackage = document.createElement(\"ul\");\n");
		code.append("ulRootPackage.id = \"ulRootPackage\";\n");
		code.append("divPackage.appendChild(ulRootPackage);\n");
		createEntriesForPackage(code, "ulRootPackage", defaultPackage, true,
				state.getGraph().getSchema()
						.getVertexClassesInTopologicalOrder(), state.getGraph()
						.getSchema().getEdgeClassesInTopologicalOrder());
	}

	/**
	 * Creates the representation of the package <code>currentPackage</code> and
	 * its containing packages.
	 * 
	 * @param code
	 * 
	 * @param parentUl
	 *            the JavaScript variable name of the parent ul
	 * @param currentPackage
	 *            the current package
	 * @param isDefaultPackage
	 *            true if it is the default package
	 * @param vertices
	 *            the vertices which are contained direct and indirect in this
	 *            package
	 * @param edges
	 *            the edges which are contained direct and indirect in this
	 *            package
	 */
	private void createEntriesForPackage(StringBuilder code, String parentUl,
			Package currentPackage, boolean isDefaultPackage,
			List<VertexClass> vertices, List<EdgeClass> edges) {
		String simpleName = isDefaultPackage ? "defaultPackage"
				: currentPackage.getSimpleName();
		String uniqueName = isDefaultPackage ? "defaultPackage"
				: replaceDollar(currentPackage.getUniqueName());
		String qualifiedName = isDefaultPackage ? "defaultPackage"
				: currentPackage.getQualifiedName();

		// deSelect all elements direct or indirect in this package
		String thenPartOfAdditionalCode = "";
		// deSelect only elements which are direct in this package
		String elsePartOfAdditionalCode = "";
		for (VertexClass vc : vertices) {
			if (vc.getQualifiedName().equals("Vertex")) {
				continue;
			}
			// if a supertype was already deselected, ignore this class
			Iterator<AttributedElementClass> iterator = vc
					.getDirectSuperClasses().iterator();
			boolean superClassIsAlreadyDeselected = false;
			while (iterator.hasNext() && !superClassIsAlreadyDeselected) {
				VertexClass next = (VertexClass) iterator.next();
				if (next.getQualifiedName().equals("Vertex")) {
					continue;
				}
				superClassIsAlreadyDeselected |= vertices.contains(next);
			}
			if (superClassIsAlreadyDeselected) {
				continue;
			}
			String uniqueNameOfVc = replaceDollar(vc.getUniqueName());
			String codeSnippet = "";
			// invert the checked value of the representation of this
			// VertexClass
			codeSnippet += "var checkbox" + uniqueNameOfVc
					+ " = document.getElementById(\"input" + uniqueNameOfVc
					+ "\");\n";
			codeSnippet += "checkbox" + uniqueNameOfVc + ".checked = !checkbox"
					+ uniqueNameOfVc + ".checked;\n";
			// deSelect current vertexClass
			codeSnippet += "deSelect(\"" + uniqueNameOfVc + "\",\"input"
					+ uniqueName + "\");\n";
			if (vc.getPackage() == currentPackage) {
				// this VertexClass is in a subpackage of the current package
				elsePartOfAdditionalCode += codeSnippet;
			}
			thenPartOfAdditionalCode += codeSnippet;
		}
		for (EdgeClass e : edges) {
			if (e.getQualifiedName().equals("Edge")
					|| e.getQualifiedName().equals("Aggregation")
					|| e.getQualifiedName().equals("Composition")) {
				continue;
			}
			// if a supertype was already deselected, ignore this class
			Iterator<AttributedElementClass> iterator = e
					.getDirectSuperClasses().iterator();
			boolean superClassIsAlreadyDeselected = false;
			while (iterator.hasNext() && !superClassIsAlreadyDeselected) {
				EdgeClass next = (EdgeClass) iterator.next();
				if (next.getQualifiedName().equals("Edge")
						|| next.getQualifiedName().equals("Aggregation")
						|| next.getQualifiedName().equals("Composition")) {
					continue;
				}
				superClassIsAlreadyDeselected |= edges.contains(next);
			}
			if (superClassIsAlreadyDeselected) {
				continue;
			}
			String uniqueNameOfE = replaceDollar(e.getUniqueName());
			String codeSnippet = "";
			// invert the checked value of the representation of this
			// VertexClass
			codeSnippet += "var checkbox" + uniqueNameOfE
					+ " = document.getElementById(\"input" + uniqueNameOfE
					+ "\");\n";
			codeSnippet += "checkbox" + uniqueNameOfE + ".checked = !checkbox"
					+ uniqueNameOfE + ".checked;\n";
			// deSelect current vertexClass
			codeSnippet += "deSelect(\"" + uniqueNameOfE + "\",\"input"
					+ uniqueName + "\");\n";
			if (e.getPackage() == currentPackage) {
				// this VertexClass is in a subpackage of the current package
				elsePartOfAdditionalCode += codeSnippet;
			}
			thenPartOfAdditionalCode += codeSnippet;
		}
		// deSelect the VertexClasses and EdgeClasses which are in this package
		String additionalCode = "if(document.getElementById(\"checkSelectAll\").checked){\n";
		additionalCode += thenPartOfAdditionalCode;
		additionalCode += "}else{\n";
		additionalCode += elsePartOfAdditionalCode;
		additionalCode += "}\n";
		createLi(code, parentUl, uniqueName, simpleName);
		createCheckBox(code, uniqueName, qualifiedName, additionalCode);
		createP(code, simpleName, uniqueName, qualifiedName, false, null);
		Map<String, Package> subPackages = currentPackage.getSubPackages();
		if (!subPackages.isEmpty()) {
			convertToTypeWithSubtypes(code, uniqueName);
			for (Package p : subPackages.values()) {
				// get vertices of this package
				ArrayList<VertexClass> verticesOfThisPackage = new ArrayList<VertexClass>();
				for (VertexClass vc : vertices) {
					if (vc.getQualifiedName().startsWith(p.getQualifiedName())) {
						verticesOfThisPackage.add(vc);
					}
				}
				// get edges of this package
				ArrayList<EdgeClass> edgesOfThisPackage = new ArrayList<EdgeClass>();
				for (EdgeClass e : edges) {
					if (e.getQualifiedName().startsWith(p.getQualifiedName())) {
						edgesOfThisPackage.add(e);
					}
				}
				createEntriesForPackage(code, "ul" + uniqueName, p, false,
						verticesOfThisPackage, edgesOfThisPackage);
			}
		}
	}

	/**
	 * Returns a String in which all occurrences of "$" in
	 * <code>uniqueName</code> are replaced by "".
	 * 
	 * @param uniqueName
	 * @return
	 */
	private String replaceDollar(String uniqueName) {
		return Pattern.compile(Matcher.quoteReplacement("$")).matcher(
				uniqueName).replaceAll(Matcher.quoteReplacement(""));
	}

	/**
	 * If <code>createForVertex</code> == true the representation for the
	 * VertexClasses is created. Otherwise for the EdgeClasses
	 * 
	 * @param code
	 * 
	 * @param state
	 * @param createForVertex
	 * @return the needed JavaScript commands
	 */
	private void createAttributedElementClassRepresentation(StringBuilder code,
			State state, boolean createForVertex) {
		assert state != null : "state is null";
		assert state.getGraph() != null : "graph is null";
		assert state.getGraph().getSchema() != null : "schema is null";
		// list of the AttributedElementClasses in topological order
		List<? extends GraphElementClass> classes = createForVertex ? state
				.getGraph().getSchema().getVertexClassesInTopologicalOrder()
				: state.getGraph().getSchema()
						.getEdgeClassesInTopologicalOrder();
		String var = createForVertex ? "Vertex" : "Edge";
		createRootUl(code, var);
		// unsetAEClasses saves the classes which have more than one superclass
		ArrayList<AttributedElementClass> unsetAEClasses = new ArrayList<AttributedElementClass>();
		// unsetSuperClasses are the superclasses which still need the
		// representation of the class from unsetAEClasses at the same index
		ArrayList<Iterator<AttributedElementClass>> unsetSuperClasses = new ArrayList<Iterator<AttributedElementClass>>();
		// iterate all classes
		for (AttributedElementClass aeclass : classes) {
			if (aeclass.getQualifiedName().equals("Vertex")
					|| aeclass.getQualifiedName().equals("Edge")
					|| aeclass.getQualifiedName().equals("Aggregation")
					|| aeclass.getQualifiedName().equals("Composition")) {
				// ignore the base classes
				continue;
			}
			// mark all Classes as selected
			if (createForVertex) {
				state.selectedVertexClasses.put((VertexClass) aeclass, true);
			} else {
				state.selectedEdgeClasses.put((EdgeClass) aeclass, true);
			}
			// create the first entry for this class
			Set<AttributedElementClass> superClasses = aeclass
					.getDirectSuperClasses();
			String ulName = "";
			Iterator<AttributedElementClass> superClassIter = superClasses
					.iterator();
			AttributedElementClass superClass = superClassIter.next();
			// check if firstClass is a base class
			while ((superClass.getQualifiedName().equals("Vertex")
					|| superClass.getQualifiedName().equals("Edge")
					|| superClass.getQualifiedName().equals("Aggregation") || superClass
					.getQualifiedName().equals("Composition"))
					&& superClassIter.hasNext()) {
				// find first superclass whicht is not a base class
				superClass = superClassIter.next();
			}
			if (superClass.getQualifiedName().equals("Vertex")
					|| superClass.getQualifiedName().equals("Edge")
					|| superClass.getQualifiedName().equals("Aggregation")
					|| superClass.getQualifiedName().equals("Composition")) {
				// This class is a rootClass in the representation
				ulName = "Root" + var;
			} else {
				// this class is not a rootClass in the representation
				ulName = replaceDollar(superClass.getUniqueName());
				if (superClassIter.hasNext()) {
					// this class has several superclasses
					// put further superclasses in unsetSuperClasses
					unsetAEClasses.add(aeclass);
					unsetSuperClasses.add(superClassIter);
				}
			}
			createLi(code, "ul" + ulName,
					replaceDollar(aeclass.getUniqueName()), aeclass
							.getSimpleName());
			createCheckBox(code, replaceDollar(aeclass.getUniqueName()),
					aeclass.getQualifiedName(), "");
			createP(code, aeclass.getSimpleName(), replaceDollar(aeclass
					.getUniqueName()), aeclass.getQualifiedName(), aeclass
					.isAbstract(), aeclass.getAttributeList());
			// if the class has subclasses create a link and a sub-ul
			if (aeclass.getDirectSubClasses().size() > 0) {
				convertToTypeWithSubtypes(code, replaceDollar(aeclass
						.getUniqueName()));
			}
		}
		/*
		 * Now the elements which have several superclasses have to be put under
		 * the other superclasses. It is executed now, because this elements are
		 * created once and their subtrees are set. The elements are in the
		 * ArrayList in the topological order. They are iterated backwards
		 * because on this way the deepest elements can be put first.
		 */
		if (!unsetSuperClasses.isEmpty()) {
			for (int z = unsetAEClasses.size() - 1; z >= 0; z--) {
				AttributedElementClass aeclass = unsetAEClasses.get(z);
				int i = 0;
				while (unsetSuperClasses.get(z).hasNext()) {
					AttributedElementClass cls = unsetSuperClasses.get(z)
							.next();
					String supCls = replaceDollar(cls.getUniqueName());
					if (state.getGraph().getSchema().getAttributedElementClass(
							"Aggregation") != cls
							&& state.getGraph().getSchema()
									.getAttributedElementClass("Composition") != cls) {
						aeclass.getM1Class();
						// copy aeEntry.getKey() and all subclasses to new
						// position
						cloneType(code, aeclass, i, supCls);
						adaptClone(code, aeclass, i);
					}
					// increase counter
					i++;
				}
			}
		}
	}

	/**
	 * Clones <code>"li"+aeclass.getUniqueName()</code> and its subtrees. It is
	 * inserted under <code>"ul"+supCls</code> at the lexicografically correct
	 * position. <b>Created variables:</b><br>
	 * <code>"li"+aeclass.getUniqueName()+"_"+i</code>: the clone of
	 * <code>"li"+aeclass.getUniqueName()</code><br>
	 * 
	 * @param code
	 * 
	 * @param aeclass
	 *            the class which representation is cloned
	 * @param i
	 *            the number of the copy
	 * @param supCls
	 *            the superclass, under which the clone of the current li has to
	 *            be put
	 * @return the needed JavaScript commands
	 */
	private void cloneType(StringBuilder code, AttributedElementClass aeclass,
			int i, String supCls) {
		String uniqueName = replaceDollar(aeclass.getUniqueName());
		code.append("var li").append(uniqueName).append("_").append(i).append(
				" = li").append(uniqueName).append(".cloneNode(true);\n");
		code.append("insertSorted(li").append(uniqueName).append("_").append(i)
				.append(", '").append(aeclass.getSimpleName()).append("', ul")
				.append(supCls).append(", 0, (ul").append(supCls).append(
						".childNodes && ul").append(supCls).append(
						".childNodes.length>0)?ul").append(supCls).append(
						".childNodes.length-1:0);\n");
	}

	/**
	 * Adds to all ids and names <code>":"+i</code>. It adapts the
	 * onclick-events and the hrefs to the new ids. The checkboxes are set
	 * checked again, because the Internet Explorer doesn't clone these values.
	 * 
	 * @param code
	 * 
	 * @param aeclass
	 *            the class which cloned representation is adapted
	 * @param i
	 *            the current number of this clone
	 * @return the needed JavaScript commands
	 */
	private void adaptClone(StringBuilder code, AttributedElementClass aeclass,
			int i) {
		String uniqueName = replaceDollar(aeclass.getUniqueName());
		// function which finds the next unused "_i"
		code.append("var findFreeNumber = function(prefix){\n");
		code.append("var sufcnt=0;\n");
		code.append("while(document.getElementById(prefix+\":\"+sufcnt)){\n");
		code.append("sufcnt++;\n");
		code.append("}\n;");
		code.append("return sufcnt;\n");
		code.append("}\n;");
		// add _i to the root li-id
		code.append("var baseName = li").append(uniqueName).append("_").append(
				i).append(".id.substr(2);\n");
		code.append("li").append(uniqueName).append("_").append(i).append(
				".id = \"li\"+baseName+\":\"").append(
				" + findFreeNumber(\"li\"+baseName);\n");
		// add to all child ul-ids "_i"
		code.append("var li").append(uniqueName).append("_").append(i).append(
				"ulChildren = li").append(uniqueName).append("_").append(i)
				.append(".getElementsByTagName(\"ul\");\n");
		code.append("for (var i=0; i<li").append(uniqueName).append("_")
				.append(i).append("ulChildren.length;i++){\n");
		code.append("var baseName = li").append(uniqueName).append("_").append(
				i).append(
				"ulChildren[i].parentNode.childNodes[1].name.substr(5);\n");
		code.append("li").append(uniqueName).append("_").append(i).append(
				"ulChildren[i].id = \"ul\"+baseName+\":\"").append(
				" + findFreeNumber(\"ul\"+baseName);\n");
		code.append("}\n");
		// add to all child li-ids "_i"
		code.append("var li").append(uniqueName).append("_").append(i).append(
				"liChildren = li").append(uniqueName).append("_").append(i)
				.append(".getElementsByTagName(\"li\");\n");
		code.append("for (var i=0; i<li").append(uniqueName).append("_")
				.append(i).append("liChildren.length;i++){\n");
		code.append("var baseName = li").append(uniqueName).append("_").append(
				i).append("liChildren[i].childNodes[1].name.substr(5);\n");
		code.append("li").append(uniqueName).append("_").append(i).append(
				"liChildren[i].id = \"li\"+baseName+\":\" ").append(
				"+ findFreeNumber(\"li\"+baseName);\n");
		code.append("}\n");
		// add to all child a-ids and -hrefs "_i"
		code.append("var li").append(uniqueName).append("_").append(i).append(
				"aChildren = li").append(uniqueName).append("_").append(i)
				.append(".getElementsByTagName(\"a\");\n");
		code.append("for (var i=0; i<li").append(uniqueName).append("_")
				.append(i).append("aChildren.length;i++){\n");
		code.append("var baseName = li").append(uniqueName).append("_").append(
				i).append(
				"aChildren[i].parentNode.childNodes[1].name.substr(5);\n");
		code
				.append("var baseName = baseName+\":\"+findFreeNumber(\"a\"+baseName);\n");
		code.append("li").append(uniqueName).append("_").append(i).append(
				"aChildren[i].id = \"a\"+baseName;\n");
		code
				.append("li")
				.append(uniqueName)
				.append("_")
				.append(i)
				.append("aChildren[i].href =")
				.append(
						" \"javascript:expand('ul\"+baseName+\"','a\"+baseName+\"');\";\n");
		code.append("}\n");
		// add to all child input-ids "_i"
		// and set all input-onclick
		code.append("var li").append(uniqueName).append("_").append(i).append(
				"inputChildren = li").append(uniqueName).append("_").append(i)
				.append(".getElementsByTagName(\"input\");\n");
		code.append("for (var i=0; i<li").append(uniqueName).append("_")
				.append(i).append("inputChildren.length;i++){\n");
		code.append("var baseName = li").append(uniqueName).append("_").append(
				i).append("inputChildren[i].name.substr(5);\n");
		code.append("li").append(uniqueName).append("_").append(i).append(
				"inputChildren[i].id = \"input\"+baseName+\":\"").append(
				" + findFreeNumber(\"input\"+baseName);\n");
		code
				.append("li")
				.append(uniqueName)
				.append("_")
				.append(i)
				.append("inputChildren[i].onclick = function(){")
				.append(
						"deSelect(this.value.substr(this.value.lastIndexOf('.')+1),this.id);\n")
				.append("submitDeselectedTypes();\n" + "};\n");
		// IE doesn't clone the checked-value
		code.append("li").append(uniqueName).append("_").append(i).append(
				"inputChildren[i].checked = true;\n");
		code.append("}\n");
	}

	/**
	 * Creates the rootUl which is put under <code>"div"+var</code>.<br>
	 * <b>Created variables:</b><br>
	 * <code>"div"+var</code>: the parent div<br>
	 * <code>"ulRoot"+var</code>: the root ul<br>
	 * <b>The created Tag has the form:</b><br>
	 * &lt;ul id="ulRoot<code>var</code>"&gt;&lt;/ul&gt;
	 * 
	 * @param code
	 * 
	 * @param var
	 *            equals "Vertex", if we create the representation of the
	 *            VertexClasses. It equals "Edge" otherwise.
	 */
	private void createRootUl(StringBuilder code, String var) {
		code.append("var div").append(var).append(
				"Class = document.getElementById(\"div").append(var).append(
				"Class\");\n");
		code.append("var ulRoot").append(var).append(
				" = document.createElement(\"ul\");\n");
		code.append("ulRoot").append(var).append(".id = \"ulRoot").append(var)
				.append("\";\n");
		code.append("div").append(var).append("Class.appendChild(ulRoot")
				.append(var).append(");\n");
	}

	/**
	 * Deletes the four whitespaces in parent li. Adds an onclickEvent to the
	 * parent checkbox. Creates an a which is put under
	 * <code>"li"+uniqueName</code> before <code>"input"+uniqueName</code> .<br>
	 * <b>Created variables:</b><br>
	 * <code>"a"+uniqueName</code>: the created a-tag<br>
	 * <code>"ul"+uniqueName</code>: the created ul-tag<br>
	 * <code>"img"+uniqueName</code>: the created img-tag<br>
	 * <b>The created Tag has the form:</b><br>
	 * &lt;a id="a<code>uniqueName</code>" href="javascript:expand('ul
	 * <code>uniqueName</code>','a<code>uniqueName</code>');"&gt;&lt;img
	 * src="plus.png" alt="+" /&gt;&lt;/a&gt;
	 * 
	 * @param code
	 * 
	 * @param uniqueName
	 */
	private void convertToTypeWithSubtypes(StringBuilder code, String uniqueName) {
		// create a
		code.append("var a").append(uniqueName).append(
				" = document.createElement(\"a\");\n");
		code.append("a").append(uniqueName).append(".id = \"a").append(
				uniqueName).append("\";\n");
		code.append("a").append(uniqueName).append(
				".href = \"javascript:expand('ul").append(uniqueName).append(
				"','a").append(uniqueName).append("');\";\n");
		// create plus image
		code.append("var img").append(uniqueName).append(
				" = document.createElement(\"img\");\n");
		code.append("img").append(uniqueName).append(".src = \"plus.png\";\n");
		code.append("img").append(uniqueName).append(".alt = \"+\";\n");
		code.append("a").append(uniqueName).append(".appendChild(img").append(
				uniqueName).append(");\n");
		// create ul
		code.append("var ul").append(uniqueName).append(
				" = document.createElement(\"ul\");\n");
		code.append("ul").append(uniqueName).append(".id = \"ul").append(
				uniqueName).append("\";\n");
		code.append("li").append(uniqueName).append(".appendChild(ul").append(
				uniqueName).append(");\n");
		// put a at the right place and deletewhitespaces
		code.append("li").append(uniqueName).append(".replaceChild(a").append(
				uniqueName).append(",ws").append(uniqueName).append(");\n");
	}

	/**
	 * Creates a p which is put under <code>"li"+uniqueName</code>. If it is an
	 * abstract class the shown text is put in i-tags.<br>
	 * <b>Created variables:</b><br>
	 * <code>"p"+uniqueName</code>: the created p-tag<br>
	 * <b>The created Tag has the form:</b><br>
	 * &lt;p title="<code>qualifiedName</code>+attributes"&gt;
	 * <code>simpleName</code> &lt;/p&gt;
	 * 
	 * @param code
	 * 
	 * @param simpleName
	 * @param uniqueName
	 * @param qualifiedName
	 * @param isAbstract
	 * @param attributes
	 *            it is null iff p is creates for an package
	 */
	private void createP(StringBuilder code, String simpleName,
			String uniqueName, String qualifiedName, boolean isAbstract,
			SortedSet<Attribute> attributes) {
		StringBuilder title = new StringBuilder(qualifiedName);
		if (attributes != null) {
			if (!attributes.isEmpty()) {
				title.append(":");
			}
			for (Attribute attr : attributes) {
				title.append("    ")
						.append(attr.getDomain().getQualifiedName())
						.append(" ").append(attr.getName()).append(";");
			}
		}
		code.append("var p").append(uniqueName).append(
				" = document.createElement(\"p\");\n");
		code.append("p").append(uniqueName).append(".title = \"").append(title)
				.append("\";\n");
		code.append("p").append(uniqueName).append(".innerHTML = \"").append(
				isAbstract ? "<i>" : "").append(simpleName).append(
				isAbstract ? "</i>" : "").append("\";\n");
		code.append("li").append(uniqueName).append(".appendChild(p").append(
				uniqueName).append(");\n");
	}

	/**
	 * Creates a checkbox which is put under <code>"li"+uniqueName</code>.<br>
	 * <b>Created variables:</b><br>
	 * <code>"input"+uniqueName</code>: the created input-tag<br>
	 * <b>The created Tag has the form:</b><br>
	 * &lt;input id="input<code>uniqueName</code>
	 * " type="checkbox" checked="checked" name="input<code>uniqueName</code>
	 * " value="<code>qualifiedName</code>" onclick="<code>additionalCode</code>
	 * ;deSelect(' <code>uniqueName</code>',this.id);" /&gt;
	 * 
	 * @param code
	 * 
	 * @param uniqueName
	 * @param qualifiedName
	 * @param additionalCode
	 */
	private void createCheckBox(StringBuilder code, String uniqueName,
			String qualifiedName, String additionalCode) {
		code.append("var input").append(uniqueName).append(";\n");
		code
				.append("if(navigator.appName == \"Microsoft Internet Explorer\"){\n");
		// IE7 does not set the checkboxes to checked. This is the workaround.
		code
				.append("input")
				.append(uniqueName)
				.append(
						" = document.createElement('<input checked=\"checked\" />');\n");
		code.append("} else {\n");
		code.append("input").append(uniqueName).append(
				" = document.createElement(\"input\");\n");
		code.append("}\n");
		code.append("input").append(uniqueName).append(".checked = true;\n");
		code.append("input").append(uniqueName).append(".id = \"input").append(
				uniqueName).append("\";\n");
		code.append("input").append(uniqueName).append(
				".type = \"checkbox\";\n");
		code.append("input").append(uniqueName).append(".name = \"input")
				.append(uniqueName).append("\";\n");
		code.append("input").append(uniqueName).append(".value = \"").append(
				qualifiedName).append("\";\n");
		code.append("li").append(uniqueName).append(".appendChild(input")
				.append(uniqueName).append(");\n");
		// create onclick-event for checkbox
		code.append("input").append(uniqueName).append(
				".onclick = function(){\n").append(additionalCode).append(
				"deSelect('").append(uniqueName).append("',this.id);\n")
				.append("submitDeselectedTypes();\n").append("};\n");
	}

	/**
	 * Creates an li which is put under <code>parentUl</code>.<br>
	 * <b>Created variables:</b><br>
	 * <code>"li"+uniqueName</code>: the created li-tag<br>
	 * <code>"ws"+uniqueName</code>: the whitspaces in the tag<br>
	 * <b>The created Tag has the form:</b><br>
	 * &lt;li id="li+<code>uniqueName</code> 
	 * "&gt;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&lt;/li&gt;
	 * 
	 * @param code
	 * 
	 * @param parentUl
	 *            the name of the variable of the parent ul
	 * @param uniqueName
	 */
	private void createLi(StringBuilder code, String parentUl,
			String uniqueName, String simpleName) {
		code.append("var li").append(uniqueName).append(
				" = document.createElement(\"li\");\n");
		code.append("li").append(uniqueName).append(".id = \"li").append(
				uniqueName).append("\";\n");
		code.append("insertSorted(li").append(uniqueName).append(", '").append(
				simpleName).append("', ").append(parentUl).append(", 0, (")
				.append(parentUl).append(".childNodes && ").append(parentUl)
				.append(".childNodes.length>0)?").append(parentUl).append(
						".childNodes.length-1:0);\n");
		// create whitespace image
		code.append("var ws").append(uniqueName).append(
				" = document.createElement(\"span\");\n");
		code.append("ws").append(uniqueName).append(
				".style.border = \"none\";\n");
		code.append("ws").append(uniqueName).append(
				".style.paddingRight = \"1em\";\n");
		code.append("ws").append(uniqueName).append(
				".style.marginRight = \"0.2em\";\n");
		code.append("ws").append(uniqueName).append(
				".style.visibility = \"hidden\";\n");
		code.append("li").append(uniqueName).append(".appendChild(ws").append(
				uniqueName).append(");\n");
	}

}
