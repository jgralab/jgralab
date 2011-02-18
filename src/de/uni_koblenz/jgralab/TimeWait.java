package de.uni_koblenz.jgralab;

public class TimeWait extends State {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new TimeWait();
		}
		return instance;
	}

	public void timeWait() throws TimeoutException {
		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
		}
		throw new TimeoutException();
	}

	@Override
	protected void run() {
		try {
			timeWait();
		} catch (TimeoutException e) {
			Closed.Instance().activate();
		}
	}
}
