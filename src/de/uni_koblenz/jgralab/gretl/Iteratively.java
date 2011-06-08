package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

public class Iteratively extends InPlaceTransformation {

	private CountingTransformation[] transforms;

	public Iteratively(Context context,
			CountingTransformation... transformations) {
		super(context);
		this.transforms = transformations;
	}

	public static Iteratively parseAndCreate(ExecuteTransformation et) {
		List<CountingTransformation> ts = new LinkedList<CountingTransformation>();
		while (et.tryMatchTransformation()) {
			CountingTransformation t = (CountingTransformation) et
					.matchTransformation();
			ts.add(t);
		}
		return new Iteratively(et.context,
				ts.toArray(new CountingTransformation[ts.size()]));
	}

	@Override
	protected Integer transform() {
		int iterations = 0;
		boolean iterate = true;
		while (iterate) {
			int cnt = 0;
			for (CountingTransformation t : transforms) {
				// System.out.println(t.getClass().getSimpleName() +
				// ", iteration " + iterations);
				cnt += t.execute();
			}
			if (cnt == 0) {
				iterate = false;
			} else {
				iterations++;
			}
		}

		return iterations;
	}

}
