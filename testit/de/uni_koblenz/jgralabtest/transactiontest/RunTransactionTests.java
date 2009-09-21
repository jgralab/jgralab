package de.uni_koblenz.jgralabtest.transactiontest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralab.impl.trans.TransactionImplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( { TransactionImplTest.class, SavepointImplTest.class,
		ConflictDetectionTest.class, AttributedElementIterableTest.class })
public class RunTransactionTests {

}
