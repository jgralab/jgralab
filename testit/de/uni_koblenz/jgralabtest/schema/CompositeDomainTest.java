package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;

public abstract class CompositeDomainTest extends DomainTest {

	protected CompositeDomain domain3;
	protected CompositeDomain domain4;
	protected Set<CompositeDomain> expectedCompositeDomains1 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains2 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains3 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains4 = new HashSet<CompositeDomain>();
	protected Set<Domain> expectedDomains1 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains2 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains3 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains4 = new HashSet<Domain>();

	@Override
	public void init() {
		super.init();
		isComposite = true;
	}

	@Test
	public void testGetAllComponentCompositeDomains() {
		// tests if a set with all CompositeDomains is returned
		Set<CompositeDomain> set1 = ((CompositeDomain) domain1)
				.getAllComponentCompositeDomains();
		Set<CompositeDomain> set2 = ((CompositeDomain) domain2)
				.getAllComponentCompositeDomains();
		Set<CompositeDomain> set3 = domain3.getAllComponentCompositeDomains();
		Set<CompositeDomain> set4 = domain4.getAllComponentCompositeDomains();

		assertEquals(expectedCompositeDomains1, set1);
		assertEquals(expectedCompositeDomains2, set2);
		assertEquals(expectedCompositeDomains3, set3);
		assertEquals(expectedCompositeDomains4, set4);
	}

	@Test
	public void testGetAllComponentDomains() {
		// tests if a set with the domains of all components is returned
		Set<Domain> set1 = ((CompositeDomain) domain1).getAllComponentDomains();
		Set<Domain> set2 = ((CompositeDomain) domain2).getAllComponentDomains();
		Set<Domain> set3 = domain3.getAllComponentDomains();
		Set<Domain> set4 = domain4.getAllComponentDomains();

		assertEquals(expectedDomains1, set1);
		assertEquals(expectedDomains2, set2);
		assertEquals(expectedDomains3, set3);
		assertEquals(expectedDomains4, set4);
	}
}
