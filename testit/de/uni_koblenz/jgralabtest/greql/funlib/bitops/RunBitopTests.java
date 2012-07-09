package de.uni_koblenz.jgralabtest.greql.funlib.bitops;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BitAndTest.class, BitNotTest.class, BitOrTest.class,
		BitShlTest.class, BitShrTest.class, BitUnsignedShrTest.class,
		BitXorTest.class })
public class RunBitopTests {

}
