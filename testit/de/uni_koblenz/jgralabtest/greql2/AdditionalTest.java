package de.uni_koblenz.jgralabtest.greql2;

import org.junit.Test;

public class AdditionalTest extends GenericTest {

	@Test
	public void testRecordAccess() throws Exception {
		evalTestQuery("rec (menue1:'bratwurst', menue2:'currywurst', menue3:'steak', menue4:'kaenguruhfleisch', menue5:'spiessbraten') store as x");

		assertQueryEquals("using x: x.menue4", "kaenguruhfleisch");
		assertQueryEquals("using x: x.menue1", "bratwurst");
		assertQueryEquals("using x: x.menue5", "spiessbraten");
		assertQueryEquals("using x: x.menue3", "steak");

		assertQueryIsUndefined("using x: x.menue0");
		assertQueryIsUndefined("using x: x.menue6");
	}
	

}
