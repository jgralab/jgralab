package de.uni_koblenz.jgralabtest.trans;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralab.impl.trans.TransactionImplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( { SavepointImplTest.class,
		ConflictDetectionTest.class, AttributedElementIterableTest.class,
		TransactionImplTest.class, NullValueTest.class, UndoTest.class })
public class RunTransactionTests {

}
