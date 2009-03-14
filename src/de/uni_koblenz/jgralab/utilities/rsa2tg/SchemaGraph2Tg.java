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
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

@WorkInProgress(responsibleDevelopers = "mmce")
public class SchemaGraph2Tg {

	private final static String SPACE = " ";
	private final static String SUBELEMENT = "\n\t";
	private final static String EMPTY = "";

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

	private SchemaGraph schemaGraph;
	private String outputFilename;
	private PrintWriter stream;
	private boolean hierarchical;

	/**
	 * Constructs an object, which will print out the specified
	 * {@link SchemaGraph} to a TG file with the given output filename. If the
	 * boolean <code>hierarchical</code> flag will be on true to force to print
	 * out a hierarchical schema. This means all qualified names will be simple
	 * names.
	 * 
	 * Note: run() have to be executed to get a TG file.
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
	}

	/**
	 * Constructs an object, which will print out the specified
	 * {@link SchemaGraph} to a TG file with the given output filename. The TG
	 * output will be hierarchical ordered. This means all qualified names will
	 * be simple names.
	 * 
	 * Note: run() have to be executed to get a TG file.
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
	 * Prints the specified {@link SchemaGraph} to a location according to the
	 * given outputFilename via a {@link PrintWriter}.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		assert (outputFilename != null || !outputFilename.equals(EMPTY)) : "No filename specified!";
		assert (schemaGraph != null) : "No SchemaGraph specified!";
		stream = new PrintWriter(outputFilename);

		// This line is for debugging and developing purposes only.
		// stream = new PrintWriter(System.out);

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
	 * corresponding to a prefix "print" and the name of the EBNF rule.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   TGSchema ::= "Schema" SchemaName ";"
	 * 		GraphClassDefinition
	 * 		{ 
	 * 		  (
	 * 			PackageDeclaration
	 * 			| DomainDefinition 
	 * 			| VertexClassDefinition
	 * 			| EdgeClassDefinition
	 * 			| AggregationClassDefinition
	 * 			| CompositionClassDefinition
	 * 		  ) ";"
	 * 		}
	 * 
	 *   SchemaName ::= PackagePrefix SchemaClassName
	 * 
	 *   PackagePrefix ::= (PackageName "." )+
	 * 
	 *   SchemaClassName ::= ClassNameString
	 * 
	 *   PackageName ::= PackageNameString
	 * </code>
	 * 
	 * @param schemaGraph
	 *            {@link SchemaGraph}, which should be transformed to TG string.
	 */
	private void printTGSchema(SchemaGraph schemaGraph) {
		println(SCHEMA, SPACE, schemaGraph.getFirstSchema().getPackagePrefix(),
				POINT, schemaGraph.getFirstSchema().getName(), DELIMITER);

		printGraphClassDefinition(schemaGraph.getFirstGraphClass());

		printPackageDeclaration((Package) schemaGraph
				.getFirstContainsDefaultPackageInGraph().getOmega());

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
		EdgeClass edge = schemaGraph.getFirstEdgeClass();

		while (edge != null) {
			printEdgeClassDefinition(edge);
			edge = edge.getNextEdgeClass();
		}
	}

	/**
	 * Transforms a {@link GraphClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>Attributes</code> and
	 * <code>Constraint</code> are encapsulated in methods corresponding to a
	 * prefix "print" and the name of the EBNF rule.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   GraphClassDefinition ::= "GraphClass" GraphClassName [Attributes] { Constraint }
	 * 
	 *   GraphClassName ::= ClassNameString
	 * </code>
	 * 
	 * @param graph
	 *            {@link GraphClass}, which should be transformed to TG string.
	 */
	private void printGraphClassDefinition(
			de.uni_koblenz.jgralab.grumlschema.structure.GraphClass graph) {

		assert (graph != null) : "Object of type GraphClass is null!";
		println(GRAPH_CLASS, SPACE, graph.getQualifiedName());

		// Prints all outgoing edges of type HasAttribute are interesting
		printAttributes(graph.getFirstHasAttribute(EdgeDirection.OUT));

		// Only outgoing edges of type HasConstraint are interesting
		printConstraints(graph.getFirstHasConstraint(EdgeDirection.OUT));
	}

	/**
	 * Transforms a {@link Package} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   PackageDeclaration ::= "Package" [ {PackageName "."} PackageName ]
	 *   
	 *   PackageName ::= PackageNameString
	 * </code>
	 * 
	 * @param tgPackage
	 *            {@link Package}, which should be transformed to TG string.
	 */
	private void printPackageDeclaration(Package tgPackage) {

		if (tgPackage == null) {
			return;
		}

		if (hierarchical) {
			println(SPACE);
		}

		println(PACKAGE, SPACE, tgPackage.getQualifiedName(), DELIMITER);

		if (hierarchical) {
			Iterator<ContainsDomain> itDomain = tgPackage
					.getContainsDomainIncidences(EdgeDirection.OUT).iterator();
			while (itDomain.hasNext()) {
				printDomainDefinition(itDomain.next());
			}

			// First only VertexClass should be printed!
			GraphElementClass graphElement;
			Iterator<ContainsGraphElementClass> itGraphElement = tgPackage
					.getContainsGraphElementClassIncidences(EdgeDirection.OUT)
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
					EdgeDirection.OUT).iterator();
			while (itGraphElement.hasNext()) {
				graphElement = (GraphElementClass) itGraphElement.next()
						.getOmega();
				if (graphElement instanceof EdgeClass) {
					printEdgeClassDefinition((EdgeClass) graphElement);
				}
			}
		}

		// All Domain, VertexClass and EdgeClass objects were printed. Now alle
		// Subpackages needs to be printed.
		ContainsSubPackage subPackage = tgPackage
				.getFirstContainsSubPackage(EdgeDirection.OUT);

		while (subPackage != null) {
			printPackageDeclaration((Package) subPackage.getOmega());
			subPackage = subPackage
					.getNextContainsSubPackage(EdgeDirection.OUT);
		}
	}

	/**
	 * Transforms a {@link VertexClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>SuperClasses</code>,
	 * <code>Attributes</code> and <code>Constraint</code> are encapsulated in
	 * methods corresponding to a prefix "print" and the name of the EBNF rule.
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

		printAttributes(vertexClass.getFirstHasAttribute(EdgeDirection.OUT));

		printConstraints(vertexClass.getFirstHasConstraint(EdgeDirection.OUT));

		println(DELIMITER);
	}

	/**
	 * Transforms a {@link EdgeClass}, {@link AggregationClass} or
	 * {@link CompositionClass} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules <code>SuperClasses</code>,
	 * <code>Multiplicity</code>, <code>Role</code>, <code>Attributes</code> and
	 * <code>Constraint</code> are encapsulated in methods corresponding to a
	 * prefix "print" and the name of the EBNF rule.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   EdgeClassDefinition ::= ["abstract"] "EdgeClass" EdgeClassName [SuperClasses] 
	 *     "from" VertexClassName Multiplicity [Role] "to" VertexClassName Multiplicity [Role] 
	 *     [Attributes] { Constraint }
	 *     
	 *   AggregationClassDefinition ::= ["abstract"] "AggregationClass" AggregationClassName [SuperClasses]
	 *     "from" VertexClassName Multiplicity [Role] "to" VertexClassName Multiplicity [Role]
	 *     [Attributes] "aggregate" ( "from" | "to" ) { Constraint }
	 *     
	 *   CompositionClassDefinition ::= ["abstract"] "CompositionClass" CompositionClassName [SuperClasses]
	 *     "from" VertexClassName Multiplicity [Role] "to" VertexClassName Multiplicity [Role] 
	 *     [Attributes] "aggregate" ( "from" | "to" ) { Constraint }
	 *   
	 *   VertexClassName ::= QualifiedClassName
	 *   
	 *   EdgeClassName ::= QualifiedClassName
	 *   
	 *   AggregationClassName ::= QualifiedClassName
	 *   
	 *   CompositionClassName ::= QualifiedClassName
	 * </code>
	 * 
	 * Note: The EBNF rules EdgeClassDefinition, AggregationClassDefinition and
	 * CompositionClassDefinition are much the same. That is the reason, why
	 * they were merged.
	 * 
	 * @param edge
	 *            {@link EdgeClass}, which will be transformed to a TG string.
	 */
	private void printEdgeClassDefinition(EdgeClass edge) {
		if (edge.isIsAbstract()) {
			print(ABSTRACT, SPACE);
		}

		print(getEdgeClassIdentifier(edge), SPACE, getName(edge), SPACE);
		printSuperClasses(edge);

		VertexClass vertex;

		vertex = (VertexClass) edge.getFirstFrom().getOmega();
		assert (vertex != null) : "Object of type VertexClass is null";
		print(SUBELEMENT, FROM, SPACE, vertex.getQualifiedName());
		printMultiplicity(edge.getFirstFrom());
		printRole(edge.getFirstFrom());

		vertex = (VertexClass) edge.getFirstTo().getOmega();
		assert (vertex != null) : "Object of type VertexClass is null";
		print(SUBELEMENT, TO, SPACE, vertex.getQualifiedName());
		printMultiplicity(edge.getFirstTo());
		printRole(edge.getFirstTo());

		printAttributes(edge.getFirstHasAttribute(EdgeDirection.OUT));

		if (edge instanceof AggregationClass
				|| edge instanceof CompositionClass) {
			AggregationClass aggregation = (AggregationClass) edge;
			print(SUBELEMENT, AGGREGATE, SPACE,
					aggregation.isAggregateFrom() ? FROM : TO);
		}

		printConstraints(edge.getFirstHasConstraint(EdgeDirection.OUT));

		println(DELIMITER);
	}

	/**
	 * Transforms a {@link To} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * All EBNF rule, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Role ::= "role" RoleName [ Redefinitions ]
	 *   
	 *   RoleName ::= IdentifierString
	 *   
	 *   Redefinitions ::= "redefines" RoleName { "," RoleName }
	   * </code>
	 * 
	 * @param to
	 *            {@link To} edge, which will be transformed to a TG string.
	 */
	private void printRole(To to) {
		assert (to != null) : "To edge shouldn't be null";
		printRole(to.getRoleName(), to.getRedefinedRoles());
	}

	/**
	 * Transforms a {@link From} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * All EBNF rule, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Role ::= "role" RoleName [ Redefinitions ]
	 *   
	 *   RoleName ::= IdentifierString
	 *   
	 *   Redefinitions ::= "redefines" RoleName { "," RoleName }
	   * </code>
	 * 
	 * @param from
	 *            {@link From} edge, which will be transformed to a TG string.
	 */
	private void printRole(From from) {
		assert (from != null) : "From edge shouldn't be null";
		printRole(from.getRoleName(), from.getRedefinedRoles());
	}

	/**
	 * Transforms a role {@link String} and a {@link Set} of redefined role in
	 * {@link String} format to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * All EBNF rule, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   Role ::= "role" RoleName [ Redefinitions ]
	 *   
	 *   RoleName ::= IdentifierString
	 *   
	 *   Redefinitions ::= "redefines" RoleName { "," RoleName }
	   * </code>
	 * 
	 * @param role
	 *            {@link To} edge, which will be transformed to a TG string.
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
	 * Transforms a {@link To} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <code>
	 *   Multiplicity ::= "(" ( NaturalNumber | "0" ) "," ( NaturalNumber | "*" ) ")"
	 * </code>
	 * 
	 * @param to
	 *            {@link To} edge, which will be transformed to TG string.
	 */
	private void printMultiplicity(To to) {
		assert (to != null) : "To edge shouldn't be null!";
		printMultiplicity(to.getMin(), to.getMax());
	}

	/**
	 * Transforms a {@link From} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <code>
	 *   Multiplicity ::= "(" ( NaturalNumber | "0" ) "," ( NaturalNumber | "*" ) ")"
	 * </code>
	 * 
	 * @param from
	 *            {@link From} edge, which will be transformed to TG string.
	 */
	private void printMultiplicity(From from) {
		assert (from != null) : "To edge shouldn't be null!";
		printMultiplicity(from.getMin(), from.getMax());
	}

	/**
	 * Transforms a two int values from and till to a TG Multiplicity string,
	 * which is written to a {@link PrintWriter} object stored in the member
	 * variable <code>stream</code>.
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <code>
	 *   Multiplicity ::= "(" ( NaturalNumber | "0" ) "," ( NaturalNumber | "*" ) ")"
	 * </code>
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
			containsDomain = containsDomain
					.getNextContainsDomain(EdgeDirection.OUT);
		}
	}

	/**
	 * Transforms a {@link Domain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>. The transformation rules
	 * <code>RecordDefinition</code> and <code>EnumDefinition</code> are
	 * encapsulated in methods corresponding to a prefix "print" and the name of
	 * the EBNF rule.
	 * 
	 * Only {@link RecordDomain} objects or {@link EnumDomain} objects are
	 * transformed. All other {@link Domain} objects are predefined.
	 * 
	 * The EBNF rule, used in this method, is enlisted below:
	 * 
	 * <code>
	 *   DomainDefinition ::= RecordDefinition | EnumDefinition
	 * </code>
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
	 * <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   RecordDefinition ::= "RecordDomain" DomainName "(" RecordComponent { "," RecordComponent } ")"
	 *   
	 *   DomainName ::= QualifiedClassName
	 *   
	 *   RecordComponent ::= IdentifierString ":" Domain
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString;
	 *   
	 *   Qualifier ::= "." | { PackageName "." }
	 *   
	 *   PackageName ::= PackageNameString
	 * </code>
	 * 
	 * @param domain
	 *            {@link RecordDomain}, which should be transformed to TG
	 *            string.
	 */
	private Domain printRecordDomain(RecordDomain domain) {
		// Gets the first outgoing HasRecordDomainComponent edge
		HasRecordDomainComponent hasComponent = domain
				.getFirstHasRecordDomainComponent(EdgeDirection.OUT);

		// A RecordDomain object must have at least one HasRecordDomainComponent
		// edge.
		assert (hasComponent != null) : "HasRecordDomainComponent is null of Domain "
				+ domain.getQualifiedName();
		Domain d = (Domain) hasComponent.getOmega();

		// Formated output of the EBNF rule "RecordDefinition" without the
		// possible repetition.
		print(RECORD_DOMAIN, SPACE, domain.getQualifiedName(), SUBELEMENT,
				ROUND_BRACKET_OPENED, hasComponent.getName(), COLON, SPACE, d
						.getQualifiedName());

		// Next outgoing edge
		hasComponent = hasComponent
				.getNextHasRecordDomainComponent(EdgeDirection.OUT);

		// Loop over all remaining Compnents
		while (hasComponent != null) {
			d = (Domain) hasComponent.getOmega();
			// Formated output of the EBNF rule "RecordDefinition" with only the
			// possible repetition.
			print(COMMA, SPACE, hasComponent.getName(), COLON, SPACE, d
					.getQualifiedName());
			// Next outgoing edge
			hasComponent = hasComponent
					.getNextHasRecordDomainComponent(EdgeDirection.OUT);
		}
		println(ROUND_BRACKET_CLOSED, DELIMITER);
		return domain;
	}

	/**
	 * Transforms a {@link EnumDomain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
	 * 
	 * All EBNF rules, used in this method, are enlisted below:
	 * 
	 * <code>
	 *   EnumDefinition ::= "EnumDomain" DomainName "(" EnumComponent { "," EnumComponent } ")"
	 *   
	 *   EnumComponent ::= String
	 *   
	 *   DomainName ::= QualifiedClassName
	 *   
	 *   QualifiedClassName :: = [ Qualifier ] ClassNameString;
	 *   
	 *   Qualifier ::= "." | { PackageName "." }
	 *   
	 *   PackageName ::= PackageNameString
	 * </code>
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
		print(ENUM_DOMAIN, SPACE, domain.getQualifiedName(), SUBELEMENT,
				ROUND_BRACKET_OPENED, it.next());

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
	 * member variable <code>stream</code>.
	 * 
	 * Note: There are no loops for specialization allowed.
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
	 * 
	 * @param vertex
	 *            {@link VertexClass} of which all superclasses should be
	 *            transformed to TG string.
	 */
	private void printSuperClasses(VertexClass vertex) {
		// Get the first outgoing edge "SpecializesVertexClass"
		assert (vertex != null) : "Object of type VertexClass is null";
		printSuperClasses(vertex
				.getFirstSpecializesVertexClass(EdgeDirection.OUT));
	}

	/**
	 * Transforms all superclass objects of a {@link EdgeClass} to a TG string,
	 * which is written to a {@link PrintWriter} object stored in the member
	 * variable <code>stream</code>.
	 * 
	 * Note: There are no loops for specialization allowed.
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
	 * 
	 * @param edge
	 *            {@link EdgeClass} of which all superclasses should be
	 *            transformed to TG string.
	 */
	private void printSuperClasses(EdgeClass edge) {
		// Get the first outgoing edge "SpecializesVertexClass"
		assert (edge != null) : "Object of type EdgeClass is null";
		printSuperClasses(edge.getFirstSpecializesEdgeClass(EdgeDirection.OUT));
	}

	/**
	 * Transforms all superclasses of a {@link GraphElementClass} to a TG
	 * string, which is written to a {@link PrintWriter} object stored in the
	 * member variable <code>stream</code>.
	 * 
	 * Note: There are no loops for specialization allowed.
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
			print(SPACE, COLON, SPACE, getName(superClass));

			// Get the next outgoing edge "SpecializesVertexClass"
			superClassEdge = superClassEdge.getNextEdgeOfClass(edgeClass,
					EdgeDirection.OUT);
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
					EdgeDirection.OUT);
		}
	}

	/**
	 * Transforms all {@link Attribute} objects of a {@link VertexClass} to a TG
	 * string, which is written to a {@link PrintWriter} object stored in the
	 * member variable <code>stream</code>. The transformation rule
	 * <code>Domain</code> is encapsulated in methods corresponding to the name
	 * of the EBNF rule.
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
			printDomain((Domain) attribute.getFirstHasDomain(EdgeDirection.OUT)
					.getOmega());
			// Gets the next edge to look at
			hasAttribute = hasAttribute.getNextHasAttribute(EdgeDirection.OUT);
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
			printDomain((Domain) attribute.getFirstHasDomain(EdgeDirection.OUT)
					.getOmega());
			// Gets the next edge to look at
			hasAttribute = hasAttribute.getNextHasAttribute(EdgeDirection.OUT);
		}
		// Closes this expression with a "}" character
		print(SPACE, CURLY_BRACKET_CLOSED);
	}

	/**
	 * Transforms a {@link Domain} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
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
	 * 
	 * @param domain
	 *            {@link Domain}, which should be transformed to TG string.
	 */
	private void printDomain(Domain domain) {
		assert (domain != null) : "Object of type Domain is null!";
		print(domain.getQualifiedName());
	}

	/**
	 * Transforms all {@link Constraint} objects referenced by
	 * {@link HasConstraint} edge to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
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
	 * 
	 * @param constraint
	 *            {@link Constraint}, which should be transformed to TG string.
	 */
	private void printConstraints(HasConstraint hasConstraint) {

		while (hasConstraint != null) {
			printConstraint((Constraint) hasConstraint.getOmega());
			hasConstraint = hasConstraint
					.getNextHasConstraint(EdgeDirection.OUT);
		}
	}

	/**
	 * Transforms a {@link Constraint} to a TG string, which is written to a
	 * {@link PrintWriter} object stored in the member variable
	 * <code>stream</code>.
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
	 * Returns the correct class name for a specified {@link EdgeClass}.
	 * 
	 * Possible return values are:
	 * 
	 * "EdgeClass" for an EdgeClass or subclasses "AggregationClass" for an
	 * AggregationClass or subclasses "CompositionClass" for an CompositionClass
	 * or subclasses
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
		return (hierarchical) ? qualifiedNameToSimpleName(element
				.getQualifiedName()) : element.getQualifiedName();
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
	 * <code>stream</code>.
	 * 
	 * Instead of writing <code>  
	 *   stream.print(SCHEMA);
	 *   stream.print(SPACE);
	 *   stream.print(s.getPackageName());
	 *   stream.print(POINT);
	 *   stream.print(s.getSimpleName());
	 *   stream.print(DELIMITER);
	 * </code>
	 * 
	 * it is possible to simply write <code>  
	 *   print(SCHEMA, SPACE, "node", POINT, "Nothing", DELIMITER);
	 * </code>
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
	 * <code>stream</code>.
	 * 
	 * Instead of writing <code>  
	 *   stream.print(SCHEMA);
	 *   stream.print(SPACE);
	 *   stream.print(s.getPackageName());
	 *   stream.print(POINT);
	 *   stream.print(s.getSimpleName());
	 *   stream.print(DELIMITER);
	 * </code>
	 * 
	 * it is possible to simply write <code>  
	 *   print(SCHEMA, SPACE, "node", POINT, "Nothing", DELIMITER);
	 * </code>
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
