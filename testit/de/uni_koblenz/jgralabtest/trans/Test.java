package de.uni_koblenz.jgralabtest.trans;

import de.uni_koblenz.jgralab.JGraLabSet;
import de.uni_koblenz.jgralab.trans.InvalidSavepointException;
import de.uni_koblenz.jgralab.trans.Savepoint;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MotorwayMapSchema schema = MotorwayMapSchema.instance();
		MotorwayMap motorwayMap = schema
				.createMotorwayMapWithTransactionSupport();
		motorwayMap.newTransaction();
		motorwayMap.createCity();
		JGraLabSet<String> set = motorwayMap.createSet();
		set.add("t1");
		Savepoint sp = motorwayMap.defineSavepoint();
		set.add("t2");
		System.out.println(set.size());
		try {
			motorwayMap.restoreSavepoint(sp);
		} catch (InvalidSavepointException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(set.size());
	}

}
