package de.uni_koblenz.jgralab;

public class SynSent extends ListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new SynSent();
		}
		return instance;
	}

	public void close() {
		Closed.Instance().activate();
	}

	@Override
	protected void run() {
		switch (getReceivedFlag()) {
		case SYN:
			send(Flag.SYN_ACK);
			SynReceived.Instance().activate();
			return;
		case SYN_ACK:
			send(Flag.ACK);
			Established.Instance().activate();
			return;
		default:
			break;
		}
	}
}
