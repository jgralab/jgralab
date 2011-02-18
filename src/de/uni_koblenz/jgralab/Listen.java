package de.uni_koblenz.jgralab;

public class Listen extends SSSSSListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new Listen();
		}
		return instance;
	}

	public void close() {
		Closed.Instance().activate();
	}

	public void send() {
		send(Flag.SYN);
		SynSent.Instance().activate();
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case SYN:
			send(Flag.SYN_ACK);
			SynReceived.Instance().activate();
			return;
		default:
			break;
		}
	}

}
