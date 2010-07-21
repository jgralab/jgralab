package de.uni_koblenz.jgralabtest.algolib;

public class Stopwatch {
	private long starttime;
	private long endtime;
	private int state;

	public Stopwatch() {
		state = 0;
	}

	public void start() {
		if (state != 0) {
			throw new IllegalStateException();
		}
		starttime = System.currentTimeMillis();
		state = 1;
	}

	public void stop() {
		if (state != 1) {
			throw new IllegalStateException();
		}
		endtime = System.currentTimeMillis();
		state = 2;
	}

	public long getDuration() {
		if (state != 2) {
			throw new IllegalStateException();
		}
		return endtime - starttime;
	}

	public void reset() {
		starttime = endtime = state = 0;
	}

	public String getDurationString() {
		return "Duration: " + getDuration() / 1000.0 + " sec";
	}
}
