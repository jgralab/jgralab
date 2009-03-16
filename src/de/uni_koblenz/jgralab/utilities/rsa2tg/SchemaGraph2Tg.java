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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.CompositionClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;

@WorkInProgress(responsibleDevelopers = "mmce")
public class SchemaGraph2Tg {

	private final static String SPACE = " ";
	private final static String EMPTY = "";
	private final static String DEFAULT_SUBELEMENT = "\n\t";

	private final static String STAR = "*";
	private final static String POINT = ".";
	private final static String COMMA = ",";
	private final static String DELIMITER = ";";
	private final static String COLON = ":";
	private final static String CURLY_BRACKET_OPENED = "{";
	private final static String CURLY_BRACKET_CLOSED = "}";
	private final static String SQUARE_BRACKET_OPENED = "[";
	private final static String SQUARE_BRACKET_CLOSED = "]";
	private final static String ROUND_BRACKET_OPENED = "(";
	private final static String ROUND_BRACKET_CLOSED = ")";

	private final static String FROM = "from";
	private final static String TO = "to";
	private final static String AGGREGATE = "aggregate";
	private final static String ROLE = "role";
	private final static String REDEFINES = "redefines";

	private final static String SCHEMA = "Schema";
	private final static String PACKAGE = "Package";
	private final static String ABSTRACT = "abstract";
	private final static String VERTEX_CLASS = "VertexClass";
	private final static String GRAPH_CLASS = "GraphClass";
	private final static String RECORD_DOMAIN = "RecordDomain";
	private final static String ENUM_DOMAIN = "EnumDomain";
	private final static String EDGE_CLASS = "EdgeClass";
	private final static String AGGREGATION_CLASS = "AggregationClass";
	private final static String COMPOSITION_CLASS = "CompositionClass";

	private final static EdgeDirection OUTGOING = EdgeDirection.OUT;

	private SchemaGraph schemaGraph;
	private String outputFilename;
	private String packageName;
	private PrintWriter stream;
	private boolean hierarchical;

	private String SUBELEMENT;

	/**
	 * Constructs an object, which will print out the specified
	 * {@link SchemaGraph} to a TG file with the given output filename. If the
	 * boolean <code>hierarchical</code> flag will be on true to force to print
	 * out a hierarchical schema. This means all qualified names will be simple
	 * names.<br>
	 * <br>
	 * 
	 * <strong>Note:</strong> run() have to be executed to get a TG file.
	 * 
	 * @param sg
	 *            {@link SchemaGraph}, which will be written to a TG file.
	 * @param outputFilename
	 *            {@link String} of the Location of the TG file. Note: The file
	 *            will be created or overwritten!
	 * @param hierachie
	 *            Determinants if the TG output will be hierarchical ordered and
	 *            with simple names for all elements or not.
	 */
	public SchemaGraph2Tg(SchemaGraph sg, String outputFilename,
			boolean hierachie) {
		schemaGraph = sg;
		this.outputFilename = outputFilename;
		this.hierarchical = hierachie;
		SUBELEMENT = DEFAULT_SUBELEMENT;
	}

	/**
	 * Constructs an object, which will print out the specified
	 * {@link SchemaGraph} to a TG file with the given output filename. The TG
	 * output will be hierarchical ordered. This means all qualified names will
	 * be simple names.<br>
	 * <br>
	 * 
	 * <strong>Note:</strong> run() have to be executed to get a TG file.
	 * 
	 * @param sg
	 *            {@link SchemaGraph}, which will be written to a TG file.
	 * @param outputFilename
	 *            {@link String} of the Location of the TG file. Note: The file
	 *            will be overwritten!
	 */
	public SchemaGraph2Tg(SchemaGraph sg, String outputFilename) {
		this(sg, outputFilename, true);
	}
	
	/**
	 * Gives the possibility to either format the output or not. 
	 * @param isFormated
	 * 		<code>true</code> will format the output. 
	 */
	public void setIsFormatted(boolean isFormatted)
	{
		SUBELEMENT = (isFormatted) ? DEFAULT_SUBELEMENT : SPACE;
	}
	
	/**
	 * Returns a boolean value indicating if the output is formated.
	 * @return
	 * 		<code>true</code>, if output is formated else <code>false</code>.
	 */
	public boolean isFormatted()
	{
		return SUBELEMENT == DEFAULT_SUBELEMENT;
	}

	/**
	 * Prints the specified {@link SchemaGraph} to a location according to the
	 * given outputFilename via a {@link PrintWriter}.<br>
	 * <br>
	 * Output is formated. For unformatted output
	 * <pre>
	 * setIsFormated(true);
	 * </pre> 
	 * have to be executed!
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		assert (outputFilename != null || !outputFilename.equals(EMPTY)) : "No filename specified!";
		assert (schemaGraph != null) : "No SchemaGraph specified!";
		stream = new PrintWriter(outputFilename);

		// This line is for debugging and developing purposes only.
//		 stream = new PrintWriter(System.out);

		printTGSchema(schemaGraph);

		stream.flush();
		stream.close();
		stream = null;
	}

	/**
	 * Transforms a {@link SchemaGraph} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules
	 * <code>PackageDeclaration</code>, <code>DomainDefinition</code>,
	 * <code>VertexClassDefinition</code>, <code>EdgeClassDefinition</code>,
	 * <code>AggregationClassDefinition</code> and
	 * <code>CompositionClassDefinition</code> are encapsulated in methods
	 * corresponding to a prefix "print" and the name of the EBNF rule.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   TGSchema ::= &quot;Schema&quot; SchemaName &quot;;&quot;
	 * 		GraphClassDefinition
	 * 		{
	 * 		  (
	 * 			PackageDeclaration
	 * 			| DomainDefinition
	 * 			| VertexClassDefinition
	 * 			| EdgeClassDefinition
	 * 			| AggregationClassDefinition
	 * 			| CompositionClassDefinition
	 * 		  ) &quot;;&quot;
	 * 		}
	 * 
	 *   SchemaName ::= PackagePrefix SchemaClassName
	 *   
	 *   PackagePrefix ::= (PackageName &quot;.&quot; )+
	 *   
	 *   SchemaClassName ::= ClassNameString
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param schemaGraph
	 *            {@link SchemaGraph}, which should be transformed to TG string.
	 */
	private void printTGSchema(SchemaGraph schemaGraph) {
		Schema schema = schemaGraph.getFirstSchema();
		println(SCHEMA, SPACE, schema.getPackagePrefix(),
				POINT, schema.getName(), DELIMITER);

		Edge edge;
		
		edge = schema.getFirstDefinesGraphClass(OUTGOING);
		assert(edge != null) : "No GraphClass defined!";
		printGraphClassDefinition((GraphClass)edge.getOmega());

		edge = schema.getFirstContainsDefaultPackage(OUTGOING);
		assert(edge != null) : "No Package defined!";
		printPackageDeclaration((Package) edge.getOmega());

		// If the print out should be hierarchical, no more printing beyond this
		// point
		if (hierarchical) {
			return;
		}

		Domain domain = schemaGraph.getFirstDomain();
		while (domain != null) {
			printDomainDefinition(domain);
			domain = domain.getNextDomain();
		}

		VertexClass vertex = schemaGraph.getFirstVertexClass();

		while (vertex != null) {
			printVertexClassDefinition(vertex);
			vertex = vertex.getNextVertexClass();
		}

		// At this point it seems, that only EdgeClass objects are printed, but
		// AggregationClass and CompositionClass are derived from EdgeClass.
		// This means all the types will be printed by the method
		// "printEdgeClassDefinition"
		EdgeClass edgeClass = schemaGraph.getFirstEdgeClass();

		while (edgeClass != null) {
			printEdgeClassDefinition(edgeClass);
			edgeClass = edgeClass.getNextEdgeClass();
		}
	}

	/**
	 * Transforms a {@link GraphClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>Attributes</code> and
	 * <code>Constraint</code> are encapsulated in methods corresponding to a
	 * prefix "print" and the name of the EBNF rule.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre><code>
	 *   GraphClassDefinition ::= "GraphClass" GraphClassName [Attributes] { Constraint }
	 *   
	 *   GraphClassName ::= ClassNameString
	 * </code>
	 * 
	 * <pre>
	 * 
	 * &#064;param graph
	 *            {@link GraphClass}
	 * , which should be transformed to TG string.
	 */
	private void printGraphClassDefinition(
			de.uni_koblenz.jgralab.grumlschema.structure.GraphClass graph) {

		assert (graph != null) : "Object of type GraphClass is null!";
		println(GRAPH_CLASS, SPACE, graph.getQualifiedName(), DELIMITER);

		// Prints all outgoing edges of type HasAttribute are interesting
		printAttributes(graph.getFirstHasAttribute(OUTGOING));

		// Only outgoing edges of type HasConstraint are interesting
		printConstraints(graph.getFirstHasConstraint(OUTGOING));
	}

	/**
	 * Transforms a {@link Package} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   PackageDeclaration ::= &quot;Package&quot; [ {PackageName &quot;.&quot;} PackageName ]
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param tgPackage
	 *            {@link Package}, which should be transformed to TG string.
	 */
	private void printPackageDeclaration(Package tgPackage) {

		if (tgPackage == null) {
			return;
		}

		if (hierarchical) {
			packageName = tgPackage.getQualifiedName();
			println(SPACE);
		}

		println(PACKAGE, SPACE, tgPackage.getQualifiedName(), DELIMITER);

		if (hierarchical) {
			Iterator<ContainsDomain> itDomain = tgPackage
					.getContainsDomainIncidences(OUTGOING).iterator();
			while (itDomain.hasNext()) {
				printDomainDefinition(itDomain.next());
			}

			// First only VertexClass should be printed!
			GraphElementClass graphElement;
			Iterator<ContainsGraphElementClass> itGraphElement = tgPackage
					.getContainsGraphElementClassIncidences(OUTGOING)
					.iterator();
			while (itGraphElement.hasNext()) {
				graphElement = (GraphElementClass) itGraphElement.next()
						.getOmega();
				if (graphElement instanceof VertexClass) {
					printVertexClassDefinition((VertexClass) graphElement);
				}
			}

			// Now all sorts of EdgeClass objects are printed
			itGraphElement = tgPackage.getContainsGraphElementClassIncidences(
					OUTGOING).iterator();
			while (itGraphElement.hasNext()) {
				graphElement = (GraphElementClass) itGraphElement.next()
						.getOmega();
				if (graphElement instanceof EdgeClass) {
					printEdgeClassDefinition((EdgeClass) graphElement);
				}
			}
		}

		// All Domain, VertexClass and EdgeClass objects were printed. Now alle
		// Sub packages needs to be printed.
		ContainsSubPackage subPackage = tgPackage
				.getFirstContainsSubPackage(OUTGOING);

		while (subPackage != null) {
			printPackageDeclaration((Package) subPackage.getOmega());
			subPackage = subPackage.getNextContainsSubPackage(OUTGOING);
		}
	}

	/**
	 * Transforms a {@link VertexClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>SuperClasses</code>,
	 * <code>Attributes</code> and <code>Constraint</code> are encapsulated in
	 * methods corresponding to a prefix "print" and the name of the EBNF rule.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   VertexClassDefinition ::= [&quot;abstract&quot;] &quot;VertexClass&quot; VertexClassName
	 *   	[SuperClasses] [Attributes] { Constraint }
	 *   
	 *   VertexClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param vertexClass
	 *            {@link VertexClass}, which should be transformed to TG string.
	 */
	private void printVertexClassDefinition(VertexClass vertexClass) {
		if (vertexClass.isIsAbstract()) {
			print(ABSTRACT, SPACE);
		}

		print(VERTEX_CLASS, SPACE, getName(vertexClass));

		printSuperClasses(vertexClass);

		printAttributes(vertexClass.getFirstHasAttribute(OUTGOING));

		printConstraints(vertexClass.getFirstHasConstraint(OUTGOING));

		println(DELIMITER);
	}

	/**
	 * Transforms a {@link EdgeClass}, {@link AggregationClass} or
	 * {@link CompositionClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>SuperClasses</code>,
	 * <code>Multiplicity</code>, <code>Role</code>, <code>Attributes</code> and
	 * <code>Constraint</code> are encapsulated in methods corresponding to a
	 * prefix "print" and the name of the EBNF rule.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   EdgeClassDefinition ::= [&quot;abstract&quot;] &quot;EdgeClass&quot; EdgeClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] { Constraint }
	 *     
	 *   AggregationClassDefinition ::= [&quot;abstract&quot;] &quot;AggregationClass&quot; AggregationClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] &quot;aggregate&quot; ( &quot;from&quot; | &quot;to&quot; ) { Constraint }
	 *     
	 *   CompositionClassDefinition ::= [&quot;abstract&quot;] &quot;CompositionClass&quot; CompositionClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] &quot;aggregate&quot; ( &quot;from&quot; | &quot;to&quot; ) { Constraint }
	 *     
	 *   VertexClassName ::= QualifiedClassName
	 *   
	 *   EdgeClassName ::= QualifiedClassName
	 *   
	 *   AggregationClassName ::= QualifiedClassName
	 *   
	 *   CompositionClassName ::= QualifiedClassName
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * <strong>Note:</strong> The EBNF rules EdgeClassDefinition,
	 * AggregationClassDefinition and CompositionClassDefinition are much the
	 * same. That is the reason, why they were merged.
	 * 
	 * @param edge
	 *            {@link EdgeClass}, which will be transformed to a TG string.
	 */
	private void printEdgeClassDefinition(EdgeClass edge) {

		assert (edge != null) : "There is no EdgeClass object! \"edge == null\"";

		if (edge.isIsAbstract()) {
			print(ABSTRACT, SPACE);
		}

		assert (!edge.getQualifiedName().equals(EMPTY)) : "This EdgeClass object has not name!";

		print(getEdgeClassIdentifier(edge), SPACE, getName(edge));
		printSuperClasses(edge);

		printFromEdge(edge.getFirstFrom(OUTGOING));
		printToEdge(edge.getFirstTo(OUTGOING));

		printAttributes(edge.getFirstHasAttribute(OUTGOING));

		if (edge instanceof AggregationClass
				|| edge instanceof CompositionClass) {
			AggregationClass aggregation = (AggregationClass) edge;
			print(SUBELEMENT, AGGREGATE, SPACE,
					aggregation.isAggregateFrom() ? FROM : TO);
		}

		printConstraints(edge.getFirstHasConstraint(OUTGOING));

		println(DELIMITER);
	}

	/**
	 * Transforms a {@link From}object to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>Multiplicity</code>,
	 * <code>Role</code>, are encapsulated in methods corresponding to a prefix
	 * "print" and the name of the EBNF rule.
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *     (&quot;from&quot; | &quot;to&quot;) VertexClassName Multiplicity [Role]
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * This is a part of a EBNF rule which is not explicitly defined! The
	 * original rule is written below. As you can see, the first time this
	 * method is called "from" has to be chosen to get a valid EBNF syntax. To
	 * achieve this, an instance of {@link From} have to be the parameter
	 * <code>aggregation</code>.
	 * 
	 * <pre>
	 *   EdgeClassDefinition ::= [&quot;abstract&quot;] &quot;EdgeClass&quot; EdgeClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] { Constraint }
	 * </pre>
	 * 
	 * @param aggreation
	 *            A {@link From} object, which will be transformed into a TG
	 *            string.
	 */
	private void printFromEdge(From from) {

		assert (from != null) : "Object of type Aggregation (To / From) is null!";
		// Getting referenced VertexClass
		VertexClass vertex = (VertexClass) from.getOmega();
		assert (vertex != null) : "There is no VertexClass object! \"vertex == null\"";
		assert (!vertex.getQualifiedName().equals(EMPTY)) : "This VertexClass object has no name!";

		printFromOrToEdge(true, vertex.getQualifiedName(), from.getMin(), from.getMax(), from.getRoleName(), from.getRedefinedRoles());
	}
	
	/**
	 * Transforms a {@link To} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>Multiplicity</code>,
	 * <code>Role</code>, are encapsulated in methods corresponding to a prefix
	 * "print" and the name of the EBNF rule.
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *     (&quot;from&quot; | &quot;to&quot;) VertexClassName Multiplicity [Role]
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * This is a part of a EBNF rule which is not explicitly defined! The
	 * original rule is written below. As you can see, the first time this
	 * method is called "from" has to be chosen to get a valid EBNF syntax. To
	 * achieve this, an instance of {@link From} have to be the parameter
	 * <code>aggregation</code>.
	 * 
	 * <pre>
	 *   EdgeClassDefinition ::= [&quot;abstract&quot;] &quot;EdgeClass&quot; EdgeClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] { Constraint }
	 * </pre>
	 * 
	 * @param aggreation
	 *            A To object, which will be transformed into a TG
	 *            string.
	 */
	private void printToEdge(To to) {

		assert (to != null) : "Object of type Aggregation (To / From) is null!";
		assert (to instanceof To || to instanceof From) : "Object in variable aggregation have to be of type To or From";
		// Getting referenced VertexClass
		VertexClass vertex = (VertexClass) to.getOmega();
		assert (vertex != null) : "There is no VertexClass object! \"vertex == null\"";
		assert (!vertex.getQualifiedName().equals(EMPTY)) : "This VertexClass object has no name!";

		printFromOrToEdge(false, vertex.getQualifiedName(), to.getMin(), to.getMax(), to.getRoleName(), to.getRedefinedRoles());	
	}
	
	/**
	 * Transforms a {@link To} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>Multiplicity</code>,
	 * <code>Role</code>, are encapsulated in methods corresponding to a prefix
	 * "print" and the name of the EBNF rule.
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *     (&quot;from&quot; | &quot;to&quot;) VertexClassName Multiplicity [Role]
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * This is a part of a EBNF rule which is not explicitly defined! The
	 * original rule is written below. As you can see, the first time this
	 * method is called "from" has to be chosen to get a valid EBNF syntax. To
	 * achieve this, an instance of {@link From} have to be the parameter
	 * <code>aggregation</code>.
	 * 
	 * <pre>
	 *   EdgeClassDefinition ::= [&quot;abstract&quot;] &quot;EdgeClass&quot; EdgeClassName [SuperClasses]
	 *     &quot;from&quot; VertexClassName Multiplicity [Role] &quot;to&quot; VertexClassName Multiplicity [Role]
	 *     [Attributes] { Constraint }
	 * </pre>
	 * 
	 * @param aggreation
	 *            A To object, which will be transformed into a TG
	 *            string.
	 */
	private void printFromOrToEdge(boolean from, String vertexName, int min, int max, String roleName, Set<String> redefinedRoles) {

		assert (vertexName != null || roleName != null || redefinedRoles != null) : "Object of type Aggregation (To / From) is null!";

		print(SUBELEMENT, (from) ? FROM : TO, SPACE,
				getName(vertexName));
		
		printMultiplicity(min, max);
		printRole(roleName, redefinedRoles);		
	}

	/**
	 * Transforms a role {@link String} and a {@link Set} of redefined role in
	 * {@link String} format to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rule, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   Role ::= &quot;role&quot; RoleName [ Redefinitions ]
	 *   
	 *   RoleName ::= IdentifierString
	 *   
	 *   Redefinitions ::= &quot;redefines&quot; RoleName { &quot;,&quot; RoleName }
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param role
	 *            {@link String} object, which specifies the role name for.
	 * @param redefinedRoles
	 *            {@link Set} of {@link String} containing redefined roles.
	 */
	private void printRole(String role, Set<String> redefinedRoles) {

		if (role == null || role.equals(EMPTY)) {
			return;
		}

		print(SPACE, ROLE, SPACE, role);

		if (redefinedRoles == null) {
			return;
		}

		Iterator<String> it = redefinedRoles.iterator();
		if (it.hasNext()) {
			print(SPACE, REDEFINES, SPACE, it.next());
		}
		while (it.hasNext()) {
			print(COMMA, SPACE, it.next());
		}
	}

	/**
	 * Transforms a two int values from and till to a TG Multiplicity string,
	 * which is written to a {@link PrintWriter} object stored in the member
	 * variable <code>stream</code>.<br>
	 * <br>
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   Multiplicity ::= &quot;(&quot; ( NaturalNumber | &quot;0&quot; ) &quot;,&quot; ( NaturalNumber | &quot;*&quot; ) &quot;)&quot;
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param to
	 *            {@link To} edge, which will be transformed to TG string.
	 */
	private void printMultiplicity(int from, int till) {
		assert (from >= 0 && till >= 0) : "from / to must be a positive number plus null";

		String stringFrom = (from == Integer.MAX_VALUE) ? STAR : Integer
				.toString(from);
		String stringTill = (till == Integer.MAX_VALUE) ? STAR : Integer
				.toString(till);
		print(SPACE, ROUND_BRACKET_OPENED, stringFrom, COMMA, SPACE,
				stringTill, ROUND_BRACKET_CLOSED);
	}

	/**
	 * Prints all {@link Domain} objects incident to all outgoing
	 * {@link ContainsDomain} edges of a Package. This method iterates over all
	 * ContainsDomain edges and uses <code>printDomainDefinition(Domain)</Code>
	 * to get a formated output.
	 * 
	 * @param containsDomain
	 *            First {@link ContainsDomain} edge, which should be transformed
	 *            to a TG String.
	 */
	private void printDomainDefinition(ContainsDomain containsDomain) {
		while (containsDomain != null) {
			printDomainDefinition((Domain) containsDomain.getOmega());
			containsDomain = containsDomain.getNextContainsDomain(OUTGOING);
		}
	}

	/**
	 * Transforms a {@link Domain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules
	 * <code>RecordDefinition</code> and <code>EnumDefinition</code> are
	 * encapsulated in methods corresponding to a prefix "print" and the name of
	 * the EBNF rule.<br>
	 * <br>
	 * 
	 * Only {@link RecordDomain} objects or {@link EnumDomain} objects are
	 * transformed. All other {@link Domain} objects are predefined.<br>
	 * <br>
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   DomainDefinition ::= RecordDefinition | EnumDefinition
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param domain
	 *            {@link Domain}, which should be transformed to TG string.
	 */
	private void printDomainDefinition(Domain domain) {

		// As mentioned above, only instances of RecordDomain and EnumDomain
		// needs to be printed.

		if (domain instanceof RecordDomain) {
			printRecordDomain((RecordDomain) domain);
		}

		if (domain instanceof EnumDomain) {
			printEnumDomain((EnumDomain) domain);
		}
	}

	/**
	 * Transforms a {@link RecordDomain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   RecordDefinition ::= &quot;RecordDomain&quot; DomainName &quot;(&quot; RecordComponent { &quot;,&quot; RecordComponent } &quot;)&quot;
	 *   
	 *   DomainName ::= QualifiedClassName
	 *   
	 *   RecordComponent ::= IdentifierString &quot;:&quot; Domain
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param domain
	 *            {@link RecordDomain}, which should be transformed to TG
	 *            string.
	 */
	private Domain printRecordDomain(RecordDomain domain) {
		// Gets the first outgoing HasRecordDomainComponent edge
		HasRecordDomainComponent hasComponent = domain
				.getFirstHasRecordDomainComponent(OUTGOING);

		// A RecordDomain object must have at least one HasRecordDomainComponent
		// edge.
		assert (hasComponent != null) : "HasRecordDomainComponent is null of Domain "
				+ domain.getQualifiedName();
		Domain d = (Domain) hasComponent.getOmega();

		// Formated output of the EBNF rule "RecordDefinition" without the
		// possible repetition.
		print(RECORD_DOMAIN, SPACE, getName(domain), SUBELEMENT,
				ROUND_BRACKET_OPENED, hasComponent.getName(), COLON, SPACE, getName(d));

		// Next outgoing edge
		hasComponent = hasComponent.getNextHasRecordDomainComponent(OUTGOING);

		// Loop over all remaining Components
		while (hasComponent != null) {
			d = (Domain) hasComponent.getOmega();
			// Formated output of the EBNF rule "RecordDefinition" with only the
			// possible repetition.
			print(COMMA, SPACE, hasComponent.getName(), COLON, SPACE, getName(d));
			// Next outgoing edge
			hasComponent = hasComponent
					.getNextHasRecordDomainComponent(OUTGOING);
		}
		println(ROUND_BRACKET_CLOSED, DELIMITER);
		return domain;
	}

	/**
	 * Transforms a {@link EnumDomain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   EnumDefinition ::= &quot;EnumDomain&quot; DomainName &quot;(&quot; EnumComponent { &quot;,&quot; EnumComponent } &quot;)&quot;
	 *   
	 *   EnumComponent ::= String
	 *   
	 *   DomainName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param domain
	 *            {@link EnumDomain}, which should be transformed to TG string.
	 */
	private void printEnumDomain(EnumDomain domain) {

		List<String> list = domain.getEnumConstants();
		// The EnumConstants list cannot be null!
		assert (list != null) : "Enum Constants list is a NullPointer!";

		Iterator<String> it = list.iterator();
		// There have to be at least one element in the list
		assert (it.hasNext()) : "Enum Constants list is to small!";

		// Formated output of the EBNF rule "EnumDefinition" without the
		// repetition.
		print(ENUM_DOMAIN, SPACE, getName(domain), SUBELEMENT, ROUND_BRACKET_OPENED, it.next());

		// Loop over all other constants
		while (it.hasNext()) {
			// Formated output of the EBNF rule "EnumDefinition" with only the
			// repetition
			print(COMMA, SPACE, it.next());
		}
		println(ROUND_BRACKET_CLOSED, DELIMITER);
	}

	/**
	 * Transforms all superclass objects of a {@link VertexClass} to a TG
	 * string, which is written to a {@link PrintWriter} object stored in the
	 * member variable <code>stream</code>.<br>
	 * <br>
	 * 
	 * Note: There are no loops for specialization allowed.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   SuperClasses ::= &quot;:&quot; SuperClassName { &quot;,&quot; SuperClassName }
	 *   
	 *   SuperClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param vertex
	 *            {@link VertexClass} of which all superclasses should be
	 *            transformed to TG string.
	 */
	private void printSuperClasses(VertexClass vertex) {
		// Get the first outgoing edge "SpecializesVertexClass"
		assert (vertex != null) : "Object of type VertexClass is null";
		printSuperClasses(vertex.getFirstSpecializesVertexClass(OUTGOING));
	}

	/**
	 * Transforms all superclass objects of a {@link EdgeClass} to a TG string,
	 * which is written to a {@link PrintWriter} object stored in the member
	 * variable <code>stream</code>.<br>
	 * <br>
	 * 
	 * <strong>Note:</strong> There are no loops for specialization allowed.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   SuperClasses ::= &quot;:&quot; SuperClassName { &quot;,&quot; SuperClassName }
	 *   
	 *   SuperClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param edge
	 *            {@link EdgeClass} of which all superclasses should be
	 *            transformed to TG string.
	 */
	private void printSuperClasses(EdgeClass edge) {
		// Get the first outgoing edge "SpecializesVertexClass"
		assert (edge != null) : "Object of type EdgeClass is null";
		printSuperClasses(edge.getFirstSpecializesEdgeClass(OUTGOING));
	}

	/**
	 * Transforms all superclasses of a {@link GraphElementClass} to a TG
	 * string, which is written to a {@link PrintWriter} object stored in the
	 * member variable <code>stream</code>.<br>
	 * <br>
	 * 
	 * <strong>Note:</strong> There are no loops for specialization allowed.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   SuperClasses ::= &quot;:&quot; SuperClassName { &quot;,&quot; SuperClassName }
	 *   
	 *   SuperClassName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString
	 *   
	 *   Qualifier ::= &quot;.&quot; | { PackageName &quot;.&quot; }
	 *   
	 *   PackageName ::= PackageNameString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param superClassEdge
	 *            First {@link SpecializesEdgeClass} or
	 *            {@link SpecializesVertexClass} edge, which should be
	 *            transformed to TG string. All following edges will also be
	 *            transformed.
	 */
	private void printSuperClasses(Edge superClassEdge) {

		Class<? extends Edge> edgeClass = null;

		GraphElementClass superClass;

		if (superClassEdge != null) {
			edgeClass = superClassEdge.getClass();
			// Gets the referenced super class at the end of superClassEdge.
			superClass = (GraphElementClass) superClassEdge.getOmega();
			assert (superClass != null) : "Object of type GraphElementClass is null";
			// Output conform to the first part of the EBNF rule "SuperClass"
			// without a possible repetition.
			print(COLON, SPACE, getName(superClass));

			// Get the next outgoing edge "SpecializesVertexClass"
			superClassEdge = superClassEdge.getNextEdgeOfClass(edgeClass,
					OUTGOING);
		}

		while (superClassEdge != null) {
			// Gets the referenced super class at the end of superClassEdge.
			superClass = (GraphElementClass) superClassEdge.getOmega();
			assert (superClass != null) : "Object of type GraphElementClass is null";
			// Output conform to the second part of the EBNF rule "SuperClass"
			// (the loop).

			print(COMMA, SPACE, getName(superClass));
			// Get the next outgoing edge "SpecializesVertexClass"
			superClassEdge = superClassEdge.getNextEdgeOfClass(edgeClass,
					OUTGOING);
		}
	}

	/**
	 * Transforms all {@link Attribute} objects of a {@link VertexClass} to a TG
	 * string, which is written to a {@link PrintWriter} object stored in the
	 * member variable <code>stream</code>. The transformation rule
	 * <code>Domain</code> is encapsulated in methods corresponding to the name
	 * of the EBNF rule.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 *<pre>
	 * &lt;code&gt;
	 *   Attributes ::= &quot;{&quot; Attribute { &quot;,&quot; Attribute } &quot;}&quot;
	 *   
	 *   Attribute ::= AttributeName &quot;:&quot; Domain
	 *   
	 *   AttributeName ::= IdentifierString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param hasAttribute
	 *            {@link HasAttribute}, which should be transformed to TG
	 *            string.
	 */
	private void printAttributes(HasAttribute hasAttribute) {

		Attribute attribute;

		if (hasAttribute != null) {
			// Gets the referenced Attribute at the end of this hasAttribute
			// edge
			attribute = (Attribute) hasAttribute.getOmega();
			assert (attribute != null) : "Object of type Attribute is null";
			print(SUBELEMENT, CURLY_BRACKET_OPENED, SPACE, attribute.getName(),
					COLON, SPACE);
			// Prints the Domain
			printDomain((Domain) attribute.getFirstHasDomain(OUTGOING)
					.getOmega());
			// Gets the next edge to look at
			hasAttribute = hasAttribute.getNextHasAttribute(OUTGOING);
		} else {
			// This case is important, because at the end of this method is a
			// print, which shouldn't be executed!
			return;
		}

		while (hasAttribute != null) {
			// Gets the referenced Attribute at the end of this hasAttribute
			// edge
			attribute = (Attribute) hasAttribute.getOmega();
			assert (attribute != null) : "Object of type Attribute is null";
			print(COMMA, SPACE, attribute.getName(), COLON, SPACE);
			// Prints the Domain
			printDomain((Domain) attribute.getFirstHasDomain(OUTGOING)
					.getOmega());
			// Gets the next edge to look at
			hasAttribute = hasAttribute.getNextHasAttribute(OUTGOING);
		}
		// Closes this expression with a "}" character
		print(SPACE, CURLY_BRACKET_CLOSED);
	}

	/**
	 * Transforms a {@link Domain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   Domain ::= DomainName | &quot;Boolean&quot; | &quot;Integer&quot; | &quot;Long&quot; | &quot;Double&quot; |
	 *   	&quot;String&quot; | ( (&quot;List&quot; | &quot;Set&quot;) &quot;&lt;&quot; Domain &quot;&gt;&quot; ) | (&quot;Map&quot; &quot;&lt;&quot; Domain &quot;,&quot;
	 *   	Domain &quot;&gt;&quot;)
	 *   
	 *   DomainName ::= QualifiedClassName
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param domain
	 *            {@link Domain}, which should be transformed to TG string.
	 */
	private void printDomain(Domain domain) {
		assert (domain != null) : "Object of type Domain is null!";
		print(getName(domain));
	}

	/**
	 * Transforms all {@link Constraint} objects referenced by
	 * {@link HasConstraint} edge to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   Constraint ::= &quot;[&quot; Message PredicateQuery [ OffendingElementsQuery ] &quot;]&quot;
	 *   
	 *   Message ::= String
	 *   
	 *   PredicateQuery ::= GReQLString
	 *   
	 *   OffendingElementsQuery ::= GReQLString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param constraint
	 *            {@link Constraint}, which should be transformed to TG string.
	 */
	private void printConstraints(HasConstraint hasConstraint) {

		while (hasConstraint != null) {
			printConstraint((Constraint) hasConstraint.getOmega());
			hasConstraint = hasConstraint.getNextHasConstraint(OUTGOING);
		}
	}

	/**
	 * Transforms a {@link Constraint} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * All EBNF rules, used in this method, are enlisted below: *
	 * 
	 * <pre>
	 * &lt;code&gt;
	 *   Constraint ::= &quot;[&quot; Message PredicateQuery [ OffendingElementsQuery ] &quot;]&quot;
	 *   
	 *   Message ::= String
	 *   
	 *   PredicateQuery ::= GReQLString
	 *   
	 *   OffendingElementsQuery ::= GReQLString
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param constraint
	 *            {@link Constraint}, which should be transformed to TG string.
	 */
	private void printConstraint(Constraint constraint) {

		assert (constraint.getMessage() != null) : "Message of a TG Constraint is null";
		assert (constraint.getPredicateQuery() != null) : "PredicateQuery of a TG Constraint is null";

		// GraphIO.toUtfString(String) transforms a given String to the
		// appropriated format
		print(SUBELEMENT, SQUARE_BRACKET_OPENED, GraphIO.toUtfString(constraint
				.getMessage()), SUBELEMENT, SPACE, GraphIO
				.toUtfString(constraint.getPredicateQuery()), SUBELEMENT,
				SPACE, GraphIO.toUtfString(constraint
						.getOffendingElementsQuery()), SQUARE_BRACKET_CLOSED);
	}

	/**
	 * Returns the correct class name for a specified {@link EdgeClass}.<br>
	 * <br>
	 * 
	 * Possible return values are:<br>
	 * <br>
	 * 
	 * "EdgeClass" for an EdgeClass or subclasses "AggregationClass" for an
	 * AggregationClass or subclasses "CompositionClass" for an CompositionClass
	 * or subclasses<br>
	 * <br>
	 * 
	 * Subclasses means subclasses of the three possible types. Normally
	 * {@link EdgeClass} is specialized by {@link AggregationClass} and
	 * {@link AggregationClass} is specialized by {@link CompositionClass}. This
	 * means the class name of the deepest class will be chosen as return value.
	 * 
	 * @param edge
	 *            {@link EdgeClass}, of which the class name should be returned.
	 * @return The correct class name of the specified {@link EdgeClass}.
	 */
	private String getEdgeClassIdentifier(EdgeClass edge) {

		if (edge instanceof CompositionClass) {
			return COMPOSITION_CLASS;
		}

		if (edge instanceof AggregationClass) {
			return AGGREGATION_CLASS;
		}

		return EDGE_CLASS;
	}

	/**
	 * Returns the qualified name of an {@link AttributedElementClass}, if the
	 * member variable <code>hierarchical</code> is <code>false</code> and the
	 * simple name if it's true.
	 * 
	 * @param element
	 *            {@link AttributedElementClass} of which the name is retrieved.
	 * @return The name of the specified {@link AttributedElementClass} object.
	 */
	private String getName(AttributedElementClass element) {
		assert (element != null) : "Object of type AttributedElementClass is null!";
		return getName(element.getQualifiedName());
	}
	
	/**
	 * Returns the qualified name of an {@link AttributedElement}, if the
	 * member variable <code>hierarchical</code> is <code>false</code> and the
	 * simple name if it's true.
	 * 
	 * @param element
	 *            {@link AttributedElement} of which the name is retrieved.
	 * @return The name of the specified {@link AttributedElement} object.
	 */
	private String getName(Domain element) {
		assert (element != null) : "Object of type AttributedElementClass is null!";
		return getName(element.getQualifiedName());
	}
	
	/**
	 * Returns the qualified name of an {@link AttributedElement}, if the
	 * member variable <code>hierarchical</code> is <code>false</code> and the
	 * simple name if it's true.
	 * 
	 * @param element
	 *            {@link AttributedElement} of which the name is retrieved.
	 * @return The name of the specified {@link AttributedElement} object.
	 */
	private String getName(String name) {
		assert (name != null) : "Object of type String is null!";
		if(!hierarchical)
		{
			return name;
		}
		QualifiedName qname = new QualifiedName(name);

		if(qname.getPackageName().equals(packageName))
		{
			return qname.getSimpleName();
		}
		
		return (qname.getPackageName().equals(EMPTY)) ? POINT + qname.getQualifiedName() : qname.getQualifiedName();
	}

	/**
	 * Retrieves the simple name {@link String} of a qualified name
	 * {@link String}.
	 * 
	 * @param qualifiedName
	 *            Qualified name {@link String} of which the simple name String
	 *            will be retrieved.
	 * @return Simple name {@link String}.
	 */
	public static String qualifiedNameToSimpleName(String qualifiedName) {
		assert (qualifiedName != null) : "Object of type String is null";
		int p = qualifiedName.lastIndexOf(".");
		if (qualifiedName.startsWith("List<")
				|| qualifiedName.startsWith("Set<")
				|| qualifiedName.startsWith("Map<") || p < 0) {
			return qualifiedName;
		} else {
			return qualifiedName.substring(p + 1);
		}
	}

	/**
	 * Appends all given {@link String} object in order to the member variable
	 * <code>stream</code>. This method reduces the overhead of appending
	 * multiple {@link String} objects to the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * Instead of writing
	 * 
	 * <pre>
	 * &lt;code&gt;  
	 *   stream.print(SCHEMA);
	 *   stream.print(SPACE);
	 *   stream.print(s.getPackageName());
	 *   stream.print(POINT);
	 *   stream.print(s.getSimpleName());
	 *   stream.print(DELIMITER);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * it is possible to simply write
	 * 
	 * <pre>
	 * &lt;code&gt;  
	 *   print(SCHEMA, SPACE, &quot;node&quot;, POINT, &quot;Nothing&quot;, DELIMITER);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param strings
	 *            Variable parameter list with all {@link String} objects, which
	 *            should be added to the member variable <code>stream</code>.
	 */
	private void print(String... strings) {
		assert (strings != null) : "Variable parameter list is empty!";

		for (int i = 0; i < strings.length; i++) {
			stream.print(strings[i]);
		}
	}

	/**
	 * Appends all given {@link String} object in order to the member variable
	 * <code>stream</code> and adds a new line. This method reduces the overhead
	 * of appending multiple {@link String} objects to the member variable
	 * <code>stream</code>.<br>
	 * <br>
	 * 
	 * Instead of writing
	 * 
	 * <pre>
	 * &lt;code&gt;  
	 *   stream.print(SCHEMA);
	 *   stream.print(SPACE);
	 *   stream.print(s.getPackageName());
	 *   stream.print(POINT);
	 *   stream.print(s.getSimpleName());
	 *   stream.print(DELIMITER);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * it is possible to simply write
	 * 
	 * <pre>
	 * &lt;code&gt;  
	 *   print(SCHEMA, SPACE, &quot;node&quot;, POINT, &quot;Nothing&quot;, DELIMITER);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param strings
	 *            Variable parameter list with all {@link String} objects, which
	 *            should be added to the member variable <code>stream</code>.
	 */
	private void println(String... strings) {
		assert (strings != null) : "Variable parameter list is empty!";

		for (int i = 0; i < strings.length - 1; i++) {
			stream.print(strings[i]);
		}
		stream.println(strings[strings.length - 1]);
	}
}
