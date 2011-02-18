package de.uni_koblenz.jgralab;

public class FinWait1 extends SSListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new FinWait1();
		}
		return instance;
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case ACK:
			FinWait2.Instance().activate();
			return;
		case FIN:
			send(Flag.ACK);
			Closing.Instance().activate();
			return;
		case FIN_ACK:
			send(Flag.ACK);
			TimeWait.Instance().activate();
			return;
		default:
			break;
		}
	}
}
