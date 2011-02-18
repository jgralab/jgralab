package de.uni_koblenz.jgralab;
public class CloseWait extends State {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new CloseWait();
		}
		return instance;
	}

	public void close() {
		send(Flag.FIN);
		LastAck.Instance().activate();
	}
}
