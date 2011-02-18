package de.uni_koblenz.jgralab;

public class Closing extends ListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new Closing();
		}
		return instance;
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case ACK:
			TimeWait.Instance().activate();
			return;
		default:
			break;
		}
	}
}
