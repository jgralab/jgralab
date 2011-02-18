package de.uni_koblenz.jgralab;

public class FinWait2 extends SSSListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new FinWait2();
		}
		return instance;
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case FIN:
			send(Flag.ACK);
			TimeWait.Instance().activate();
			return;
		default:
			break;
		}
	}
}
