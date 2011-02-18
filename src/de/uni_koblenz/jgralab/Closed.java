package de.uni_koblenz.jgralab;
public class Closed extends State {
	private static State instance;

	public static State Instance() {
		if (instance == null) {
			instance = new Closed();
		}
		return instance;
	}

	public void listen() {
		Listen.Instance().activate();
	}

	public void connect() {
		send(Flag.SYN);
		SynSent.Instance().activate();
	}
}
