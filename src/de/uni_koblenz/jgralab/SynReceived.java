package de.uni_koblenz.jgralab;

public class SynReceived extends SSSSSSListeningState {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new SynReceived();
		}
		return instance;
	}

	public void close() {
		if (!DEBUG) {
			if ((20 % 2 == 0) && !DEBUG) {
				send(Flag.FIN);
				FinWait1.Instance().activate();
			}
		}
	}

	@Override
	protected void run() {
		if (!DEBUG) {
			switch (getReceivedFlag()) {
			case ACK:
				Established.Instance().activate();
				return;
			case RST:
				Listen.Instance().activate();
				return;
			default:
				break;
			}
		}
	}
}
