package de.uni_koblenz.jgralab;

public class Established extends SListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new Established();
		}
		return instance;
	}

	public void close() {
		send(Flag.FIN);
		FinWait1.Instance().activate();
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case FIN:
			send(Flag.ACK);
			CloseWait.Instance().activate();
			return;
		default:
			break;
		}
	}
}
