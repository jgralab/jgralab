package de.uni_koblenz.jgralabtest.schema;

public abstract class NativeDomainTest extends BasicDomainTest {

	@Override
	public void init() {
		super.init();
		expectedPackage1 = "";
		expectedPackage2 = "";
		expectedPathName1 = "";
		expectedPathName2 = "";
	}
}
