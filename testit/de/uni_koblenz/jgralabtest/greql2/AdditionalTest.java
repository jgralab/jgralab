package de.uni_koblenz.jgralabtest.greql2;

import org.junit.Test;

public class AdditionalTest extends GenericTests {

	@Test
	public void testRecordAccess() throws Exception {
		evalTestQuery("rec (menue1:'bratwurst', menue2:'currywurst', menue3:'steak', menue4:'kaenguruhfleisch', menue5:'spiessbraten') store as x");

		assertQueryEquals("using x: x.menue4", "kaenguruhfleisch");
		assertQueryEquals("using x: x.menue1", "bratwurst");
		assertQueryEquals("using x: x.menue5", "spiessbraten");
		assertQueryEquals("using x: x.menue3", "steak");

		assertQueryEqualsNull("using x: x.menue0");
		assertQueryEqualsNull("using x: x.menue6");
	}
	

}
