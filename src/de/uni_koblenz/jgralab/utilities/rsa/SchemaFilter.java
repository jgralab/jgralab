/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.rsa;

import java.io.PrintStream;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;

/**
 * This class handles the filtering of schemas. It is used for example in
 * SchemaGraph2XSD.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class SchemaFilter {

	private String[] patterns;
	private PrintStream debugOutputStream;
	private SchemaGraph schemaGraph;
	private BooleanGraphMarker includes;
	private boolean autoExclude;

	public BooleanGraphMarker processPatterns() {
		includes = new BooleanGraphMarker(schemaGraph);
		if (patterns != null) {
			// always include the GraphClass
			includes.mark(schemaGraph);
			// accept everything by default
			Pattern matchesAll = Pattern.compile(".*");

			if (patterns.length <= 0 || patterns[0].trim().startsWith("-")) {
				includeOrExcludeAllGraphElements(true, matchesAll);
			}

			Pattern validPattern = Pattern.compile("^[+\\-]");
			for (String currentRawPattern : patterns) {
				Pattern currentPattern = Pattern.compile(validPattern
						.split(currentRawPattern)[1]);
				if (currentRawPattern.trim().startsWith("+")) {
					includeOrExcludeAllGraphElements(true, currentPattern);
				} else if (currentRawPattern.trim().startsWith("-")) {
					includeOrExcludeAllGraphElements(false, currentPattern);
				}
			}

			if (autoExclude) {
				explicitlyExcludeImplicitlyExcludedClasses();
			}
			excludeUnnecessaryAbstractVertexClasses();
			// TODO what about abstract EdgeClasses without included
			// specializations?
			excludeUnecessaryEdgeClasses();

			includeAllNecessaryDomains();

			if (debugOutputStream != null) {
				writeDebugInformation();
			}

		} else {
			// if no patterns are set include everything
			for (AttributedElementClass currentAttributedElement : schemaGraph
					.getAttributedElementClassVertices()) {
				includes.mark(currentAttributedElement);
			}
			for (EnumDomain currentEnumDomain : schemaGraph
					.getEnumDomainVertices()) {
				includes.mark(currentEnumDomain);
			}
			for (RecordDomain currentRecordDomain : schemaGraph
					.getRecordDomainVertices()) {
				includes.mark(currentRecordDomain);
			}
		}
		return includes;
	}

	/**
	 * Handles the writing of the debug information.
	 */
	private void writeDebugInformation() {
		debugOutputStream.println("[VertexClasses]");
		for (VertexClass current : schemaGraph.getVertexClassVertices()) {
			writeElementDebugInformation(current);
		}
		debugOutputStream.println();
		debugOutputStream.println("[EdgeClasses]");
		for (EdgeClass current : schemaGraph.getEdgeClassVertices()) {
			writeElementDebugInformation(current);
		}
		debugOutputStream.println();
		debugOutputStream.println("[Domains]");
		for (EnumDomain current : schemaGraph.getEnumDomainVertices()) {
			writeDomainDebugInformation(current);
		}
		for (RecordDomain current : schemaGraph.getRecordDomainVertices()) {
			writeDomainDebugInformation(current);
		}
		debugOutputStream.flush();
		debugOutputStream.close();
	}

	/**
	 * Writes the debug information of the given domain.
	 * 
	 * @param d
	 *            the domain to write the information of.
	 */
	private void writeDomainDebugInformation(Domain d) {
		writeIncludeOrExcludeInformation(d);
		debugOutputStream.println(d.get_qualifiedName());
	}

	/**
	 * Writes debug information of the given GraphElementClass.
	 * 
	 * @param gec
	 *            the GraphElementClass to write the information of.
	 */
	private void writeElementDebugInformation(GraphElementClass gec) {
		writeIncludeOrExcludeInformation(gec);
		debugOutputStream.println(gec.get_qualifiedName());
	}

	/**
	 * Writes "IN: " or "OUT :" at the beginning of each line in the debug file.
	 * 
	 * @param ae
	 *            the element to decide whether to write "IN: " or "OUT: "
	 */
	private void writeIncludeOrExcludeInformation(AttributedElement ae) {
		if (includes.isMarked(ae)) {
			debugOutputStream.print("IN: ");
		} else {
			debugOutputStream.print("EX: ");
		}
	}

	/**
	 * Excludes all subclasses of excluded superclasses for EdgeClasses and
	 * VertexClasses.
	 */
	private void explicitlyExcludeImplicitlyExcludedClasses() {
		BooleanGraphMarker processed = new BooleanGraphMarker(schemaGraph);
		for (VertexClass currentVertexClass : schemaGraph
				.getVertexClassVertices()) {
			if (!processed.isMarked(currentVertexClass)
					&& !includes.isMarked(currentVertexClass)) {
				excludeGraphElementClass(processed, currentVertexClass);
			}
		}
		for (EdgeClass currentEdgeClass : schemaGraph.getEdgeClassVertices()) {
			if (!processed.isMarked(currentEdgeClass)
					&& !includes.isMarked(currentEdgeClass)) {
				excludeGraphElementClass(processed, currentEdgeClass);
			}
		}
	}

	/**
	 * Excludes the given GraphElementClass and all its subclasses.
	 * 
	 * @param processed
	 *            marks already processed Elements.
	 * @param currentGraphElementClass
	 *            the GraphElementClass to exclude.
	 */
	private void excludeGraphElementClass(BooleanGraphMarker processed,
			VertexClass currentGraphElementClass) {
		processed.mark(currentGraphElementClass);
		includes.removeMark(currentGraphElementClass);
		for (SpecializesVertexClass current : currentGraphElementClass
				.getSpecializesVertexClassIncidences(EdgeDirection.IN)) {
			VertexClass superclass = (VertexClass) current.getThat();
			excludeGraphElementClass(processed, superclass);
		}
	}

	/**
	 * Excludes the given GraphElementClass and all its subclasses.
	 * 
	 * @param processed
	 *            marks already processed Elements.
	 * @param currentGraphElementClass
	 *            the GraphElementClass to exclude.
	 */
	private void excludeGraphElementClass(BooleanGraphMarker processed,
			EdgeClass currentGraphElementClass) {
		processed.mark(currentGraphElementClass);
		includes.removeMark(currentGraphElementClass);
		for (SpecializesEdgeClass current : currentGraphElementClass
				.getSpecializesEdgeClassIncidences(EdgeDirection.IN)) {
			EdgeClass superclass = (EdgeClass) current.getThat();
			excludeGraphElementClass(processed, superclass);
		}
	}

	/**
	 * Includes all necessary domains according to the included
	 * GraphElementClasses.
	 */
	private void includeAllNecessaryDomains() {
		for (AttributedElementClass currentAttributedElementClass : schemaGraph
				.getAttributedElementClassVertices()) {
			if (includes.isMarked(currentAttributedElementClass)) {
				for (HasAttribute currentAttributeLink : currentAttributedElementClass
						.getHasAttributeIncidences()) {
					Domain currentDomain = (Domain) ((Attribute) currentAttributeLink
							.getThat()).getFirstHasDomain().getThat();
					includeDomain(currentDomain);
				}
			}
		}
	}

	/**
	 * Includes the given domain.
	 * 
	 * @param d
	 *            the domain to include.
	 */
	private void includeDomain(Domain d) {
		if (d instanceof EnumDomain) {
			includeDomain((EnumDomain) d);
		} else if (d instanceof RecordDomain) {
			includeDomain((RecordDomain) d);
		} else if (d instanceof CollectionDomain) {
			includeDomain((CollectionDomain) d);
		} else if (d instanceof MapDomain) {
			includeDomain((MapDomain) d);
		}
	}

	/**
	 * Includes the given MapDomain.
	 * 
	 * @param md
	 *            the MapDomain to include.
	 */
	private void includeDomain(MapDomain md) {
		includeDomain((Domain) md.getFirstHasKeyDomain().getThat());
		includeDomain((Domain) md.getFirstHasValueDomain().getThat());
	}

	/**
	 * Includes the given CollectionDomain (Set or List).
	 * 
	 * @param cd
	 *            the CollectionDomain to include.
	 */
	private void includeDomain(CollectionDomain cd) {
		includeDomain((Domain) cd.getFirstHasBaseDomain().getThat());
	}

	/**
	 * Includes the given EnumDomain.
	 * 
	 * @param ed
	 *            the EnumDomain to include.
	 */
	private void includeDomain(EnumDomain ed) {
		includes.mark(ed);
	}

	/**
	 * Includes the given RecordDomain.
	 * 
	 * @param rd
	 *            the RecordDomain to exclude.
	 */
	private void includeDomain(RecordDomain rd) {
		includes.mark(rd);
		// recursively include all RecordDomainComponentDomains
		for (HasRecordDomainComponent currentRecordDomainComponentEdgeClass : rd
				.getHasRecordDomainComponentIncidences()) {
			includeDomain((Domain) currentRecordDomainComponentEdgeClass
					.getThat());
		}
	}

	/**
	 * Excludes all EdgeClasses that have an excluded to or from VertexClass.
	 */
	private void excludeUnecessaryEdgeClasses() {
		for (EdgeClass currentEdgeClass : schemaGraph.getEdgeClassVertices()) {
			if (includes.isMarked(currentEdgeClass)) {
				// only look at included EdgeClasses
				IncidenceClass fromIC = (IncidenceClass) currentEdgeClass
						.getFirstComesFrom().getOmega();
				VertexClass fromVC = (VertexClass) fromIC.getFirstEndsAt()
						.getOmega();
				IncidenceClass toIC = (IncidenceClass) currentEdgeClass
						.getFirstGoesTo().getOmega();
				VertexClass toVC = (VertexClass) toIC.getFirstEndsAt()
						.getOmega();
				if (!includes.isMarked(fromVC) || !includes.isMarked(toVC)) {
					// exclude all EdgeClasses whose to or from VertexClasses
					// are already excluded
					includes.removeMark(currentEdgeClass);
				}
			}
		}
	}

	/**
	 * Excludes all VertexClasses that have only excluded subclasses.
	 */
	private void excludeUnnecessaryAbstractVertexClasses() {
		BooleanGraphMarker processed = new BooleanGraphMarker(schemaGraph);
		for (VertexClass currentVertexClass : schemaGraph
				.getVertexClassVertices()) {
			if (currentVertexClass.is_abstract()) {
				// only process abstract VertexClasses
				if (isVertexClassExcluded(processed, currentVertexClass)) {
					includes.removeMark(currentVertexClass);
				}
			}
		}
	}

	/**
	 * Checks if a VertexClass should be excluded.
	 * 
	 * @param processed
	 *            marks already processed elements.
	 * @param currentVertexClass
	 *            the VertexClass to check.
	 * @return true if the given VertexClass should be excluded
	 */
	private boolean isVertexClassExcluded(BooleanGraphMarker processed,
			VertexClass currentVertexClass) {
		if (processed.isMarked(currentVertexClass)
				|| !currentVertexClass.is_abstract()) {
			return !includes.isMarked(currentVertexClass);
		}
		processed.mark(currentVertexClass);
		if (!includes.isMarked(currentVertexClass)) {
			// abstract and already excluded
			return false;
		}
		for (SpecializesVertexClass current : currentVertexClass
				.getSpecializesVertexClassIncidences(EdgeDirection.IN)) {
			if (!isVertexClassExcluded(processed,
					(VertexClass) current.getThat())) {
				// at least one subclass is not excluded
				return false;
			}
		}
		// all subclasses are excluded or is abstract leaf (which should not
		// occur)
		return true;
	}

	/**
	 * Matches the given pattern and either includes or excludes all matching
	 * GraphElements.
	 * 
	 * @param include
	 *            if true, this method includes, if false, it excludes.
	 * @param currentPattern
	 *            the pattern to match.
	 */
	private void includeOrExcludeAllGraphElements(boolean include,
			Pattern currentPattern) {
		for (GraphElementClass gec : schemaGraph.getGraphElementClassVertices()) {
			includeOrExcludeIfMatches(include, gec, currentPattern);
		}
	}

	/**
	 * Includes or excludes the given GraphElementClass according to the given
	 * pattern.
	 * 
	 * @param include
	 *            flag to decide whether to include or exclude the given
	 *            GraphElementClass if it matches the given pattern.
	 * @param gec
	 *            the GraphElementClass to include or exclude.
	 * @param currentPattern
	 *            the pattern to match.
	 */
	private void includeOrExcludeIfMatches(boolean include,
			GraphElementClass gec, Pattern currentPattern) {
		if (currentPattern.matcher(gec.get_qualifiedName()).matches()) {
			if (include) {
				includes.mark(gec);
			} else {
				includes.removeMark(gec);
			}
		}
	}

	public SchemaFilter(SchemaGraph schemaGraph, String[] patterns,
			PrintStream debugOutput, boolean autoExclude) {
		super();
		this.patterns = patterns;
		debugOutputStream = debugOutput;
		this.schemaGraph = schemaGraph;
		this.autoExclude = autoExclude;
	}

	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	public void setDebugOutputStream(PrintStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}

	public void setSchemaGraph(SchemaGraph schemaGraph) {
		this.schemaGraph = schemaGraph;
	}

	public void setAutoExclude(boolean autoExclude) {
		this.autoExclude = autoExclude;
	}

}
