package de.uni_koblenz.jgralab;

public class LastAck extends SSSSListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new LastAck();
		}
		return instance;
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case ACK:
			Closed.Instance().activate();
			return;
		default:
			break;
		}
	}
}
