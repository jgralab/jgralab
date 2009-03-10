/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.Package;

@WorkInProgress(responsibleDevelopers = "mmce")
public class SchemaGraph2Tg {
	
	private final static String SCHEMA = "Schema";
	private final static String DELIMITER = ";\n";
	private final static String OFTYPE = ":";
	private final static String SPACE = " ";
	private final static String BRACEOPENED = "{";
	private final static String BRACECLOSED = "}";
	private final static String EDGEDBRACEOPENED = "[";
	private final static String EDGEDBRACECLOSED = "]";
	private final static String POINT = ".";
	private final static String COMMA = ",";
	private final static String PACKAGE = "Package";
	private final static String ABSTRACT = "abstract";
	private final static String VERTEXCLASS = "VertexClass";
	private final static String SUBELEMENT = "\n\t";
	private final static String GRAPHCLASS = "GraphClass";
	private final static String QUOTE = "\"";
	
	private SchemaGraph schemaGraph;
	private String outputFilename;
	private StringBuffer stream;
	
	public SchemaGraph2Tg(SchemaGraph sg, String outputFilename) {
		stream = new StringBuffer();
		schemaGraph = sg;
		this.outputFilename = outputFilename;
	}

	public void run() throws IOException {
		
		ruleTGSchema(schemaGraph);
		System.out.println(stream.toString());
	}
	
	/**
	 * Appends all given String object in order to the member variable <code>stream</code>. This method should reduce the overhead of appending
	 * multiple String objects to the member variable <code>stream</code>.
	 * 
	 * Instead of writing 
	 * <code>  
	 *   stream.append(SCHEMA);
	 *   stream.append(SPACE);
	 *   stream.append(s.getPackageName());
	 *   stream.append(POINT);
	 *   stream.append(s.getSimpleName());
	 *   stream.append(DELIMITER);
	 * </code>  
	 *   
	 * it is possible to simply write
	 * <code>  
	 *   append(SCHEMA, SPACE, s.getPackageName(), POINT, s.getSimpleName(), DELIMITER);
	 * </code>
	 *   
	 * @param strings Variable parameter list with all string, which should be added to the member variable <code>stream</code>.
	 */
	private void append(String ... strings)
	{
		for(int i = 0; i < strings.length; i++)
		{
			stream.append(strings[i]);
		}
	}
	
	/**
	 * Transforms a TG SchemaGraph to a TG string, which is written to a StringBuffer object stored in the 
	 * member variable <code>stream</code>. The transformation rules <code>PackageDeclaration</code>, 
	 * <code>DomainDefinition</code>, <code>VertexClassDefinition</code>, <code>EdgeClassDefinition</code>,
	 * <code>AggregationClassDefinition</code> and <code>CompositionClassDefinition</code> are encapsulated in
	 * method corresponding to the name of the EBNF rule. 
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 * TGSchema ::= "Schema" SchemaName ";"
	 * 		GraphClassDefinition
	 * 		{ 
	 * 		  (
	 * 			PackageDeclaration
	 *			| DomainDefinition 
	 * 			| VertexClassDefinition
	 * 			| EdgeClassDefinition
	 * 			| AggregationClassDefinition
	 * 			| CompositionClassDefinition
	 * 		  ) ";"
	 * 		}
	 * 
	 * SchemaName ::= PackagePrefix SchemaClassName
	 * 
	 * PackagePrefix ::= (PackageName "." )+
	 * 
	 * SchemaClassName ::= ClassNameString
	 * 
	 * PackageName ::= PackageNameString
	 * </code>
	 * @param schema TG Schema, which should be transformed to TG string.
	 */
	private void ruleTGSchema(SchemaGraph schemaGraph)
	{	
		append(SCHEMA, SPACE, QUOTE, schemaGraph.getFirstSchema().getPackagePrefix(), 
				POINT, schemaGraph.getFirstSchema().getName(), QUOTE, DELIMITER);
		
		ruleGraphClass(schemaGraph.getFirstGraphClass());
		
//		for(de.uni_koblenz.jgralab.schema.Package p : schema.getPackages().values())
//		{
//			rulePackageDeclaration(p);
//			// this DELIMITER (";") could also be added in the appropriated method, but it is added in this method
//			// corresponding to the EBNF rule.
//			append(DELIMITER);
//		}
//		
//		for(de.uni_koblenz.jgralab.schema.Package p : schema.getPackages().values())
//		{
//			for(VertexClass v : p.getVertexClasses().values())
//			{
//				ruleVertexClassDefinition(v);
//				// this DELIMITER (";") could also be added in the appropriated method, but it is added in this method
//				// corresponding to the EBNF rule.
//				append(DELIMITER);
//			}
//		}

	}

	/**
	 * Transforms a TG GraphClass to a TG string, which is written to a StringBuffer object stored in the 
	 * member variable <code>stream</code>. The transformation rules <code>Attributes</code> and  
	 * <code>Constraint</code> are encapsulated in method corresponding to the name of the EBNF rule. 
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   GraphClassDefinition ::= "GraphClass" GraphClassName [Attributes] { Constraint }
	 * 
	 *   GraphClassName ::= ClassNameString
	 * </code>
	 * @param graph TG GraphClass, which should be transformed to TG string.
	 */
	private void ruleGraphClass(de.uni_koblenz.jgralab.grumlschema.structure.GraphClass graph) {
		append(GRAPHCLASS, SPACE, QUOTE, graph.getQualifiedName(), QUOTE);
		
//		ruleAttributes(graph.getFirstHasAttribute());
//		
//		for(Constraint c : graph.getConstraints())
//		{
//			ruleConstraint(c);
//		}		
	}

	/**
	 * Transforms a TG Package to a TG string, which is written to a StringBuffer object stored in the 
	 * member variable <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   PackageDeclaration ::= "Package" [ {PackageName "."} PackageName ]
	 *   
	 *   PackageName ::= PackageNameString
	 * </code>
	 * @param tgPackage TG Package, which should be transformed to TG string.
	 */
	private void rulePackageDeclaration(Package tgPackage) {
		append(PACKAGE, SPACE, tgPackage.getQualifiedName());
	}

	/**
	 * Transforms a TG VertexClass to a TG string, which is written to a StringBuffer object stored in the 
	 * member variable <code>stream</code>. The transformation rules <code>SuperClasses</code>, 
	 * <code>Attributes</code> and <code>Constraint</code> are encapsulated in method corresponding to the 
	 * name of the EBNF rule.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   VertexClassDefinition ::= ["abstract"] "VertexClass" VertexClassName
	 *   	[SuperClasses] [Attributes] { Constraint }
	 *   
	 *   VertexClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString;
	 *   
	 *   Qualifier ::= "." | { PackageName "." }
	 *   
	 *   PackageName ::= PackageNameString   
	 * </code>
	 * @param vertexClass TG VertexClass, which should be transformed to TG string.
	 */
	private void ruleVertexClassDefinition(VertexClass vertexClass) {
		if(vertexClass.isAbstract())
		{
			append(ABSTRACT, SPACE);
		}
		append(VERTEXCLASS, SPACE, vertexClass.getQualifiedName());
		
		ruleSuperClasses(vertexClass);
		
		ruleAttributes(vertexClass.getAttributeList());
		
		for(Constraint c : vertexClass.getConstraints())
		{
			ruleConstraint(c);
		}
	}

	/**
	 * Transforms all superclasses of a TG VertexClass to a TG string, which is written to a 
	 * StringBuffer object stored in the member variable <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   SuperClasses ::= ":" SuperClassName { "," SuperClassName }
	 *   
	 *   SuperClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString;
	 *    
	 *   Qualifier ::= "." | { PackageName "." }
	 *   
	 *   PackageName ::= PackageNameString
	 * </code>
	 * @param vertexClass TG vertexClass of which all superclasses should be transformed to TG string.
	 */
	private void ruleSuperClasses(VertexClass vertexClass) {
		Iterator<AttributedElementClass> it = vertexClass.getAllSuperClasses().iterator();
		
		if(it.hasNext())
		{
			append(SUBELEMENT, OFTYPE, SPACE, it.next().getQualifiedName());
		}
		
		while(it.hasNext())
		{
			append(COMMA, SPACE, it.next().getQualifiedName());
		}
	}
	
	/**
	 * Transforms all attributes of a TG VertexClass to a TG string, which is written to a 
	 * StringBuffer object stored in the member variable <code>stream</code>. The transformation 
	 * rule <code>Domain</code> is encapsulated in method corresponding to the name of the EBNF rule.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Attributes ::= "{" Attribute { "," Attribute } "}"
	 * 
	 *   Attribute ::= AttributeName ":" Domain
	 * 
	 *   AttributeName ::= IdentifierString
	 * </code>
	 * @param vertexClass TG VertexClass, which should be transformed to TG string.
	 */
	private void ruleAttributes(SortedSet<Attribute> attribute) {
		
		if(attribute.size() == 0)
		{
			return;
		}
		
		Iterator<Attribute> it = attribute.iterator();
		Attribute a;
		
		append(SUBELEMENT, BRACEOPENED);
		
		if(it.hasNext())
		{
			a = it.next();
			append(SPACE, a.getName(), SPACE, OFTYPE, SPACE);
			ruleDomain(a);
		}
		
		while(it.hasNext())
		{
			a = it.next();
			append(SPACE, COMMA, SPACE, a.getName(), SPACE, OFTYPE, SPACE);
			ruleDomain(a);
		}
		
		append(SPACE, BRACECLOSED);
	}
	
	/**
	 * Transforms a TG Domain of a Attribute to a TG string, which is written to a StringBuffer object
	 * stored in the member variable <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Domain ::= DomainName | "Boolean" | "Integer" | "Long" | "Double" |
	 *   	"String" | ( ("List" | "Set") "<" Domain ">" ) | ("Map" "<" Domain ","
	 *   	Domain ">")
	 *   
	 *   DomainName ::= QualifiedClassName
	 * </code>
	 * @param attribute TG Attribute, of which the TG Domain should be transformed to TG string.
	 */
	private void ruleDomain(Attribute attribute) {
		ruleDomain(attribute.getDomain());
	}
	
	/**
	 * Transforms a TG Domain to a TG string, which is written to a StringBuffer object
	 * stored in the member variable <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Domain ::= DomainName | "Boolean" | "Integer" | "Long" | "Double" |
	 *   	"String" | ( ("List" | "Set") "<" Domain ">" ) | ("Map" "<" Domain ","
	 *   	Domain ">")
	 *   
	 *   DomainName ::= QualifiedClassName
	 * </code>
	 * @param domain TG Domain, which should be transformed to TG string.
	 */
	private void ruleDomain(Domain domain) {
		append(domain.getQualifiedName());
	}

	/**
	 * Transforms a TG Constraint to a TG string, which is written to a StringBuffer 
	 * object stored in the member variable <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Constraint ::= "[" Message PredicateQuery [ OffendingElementsQuery ] "]"
	 *   
	 *   Message ::= String
	 *   
	 *   PredicateQuery ::= GReQLString
	 *   
	 *   OffendingElementsQuery ::= GReQLString
	 * </code>
	 * @param constraint TG Constraint, which should be transformed to TG string.
	 */
	private void ruleConstraint(Constraint constraint) {
		
		assert(constraint.getMessage() != null) 
			: "Message of a TG Constraint is null";
		assert(constraint.getPredicate() != null) 
			: "PredicateQuery of a TG Constraint is null";
		
		append(SUBELEMENT, EDGEDBRACEOPENED, 
				QUOTE, constraint.getMessage(), QUOTE, SPACE, 
				QUOTE, constraint.getPredicate(), QUOTE, SPACE,
				QUOTE, constraint.getOffendingElementsQuery(), QUOTE, SPACE,
				EDGEDBRACECLOSED);
	}
}
