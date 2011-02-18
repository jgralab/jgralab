package de.uni_koblenz.jgralab;

public abstract class State {
	public static boolean DEBUG = false;
	private static State activeState = null;

	protected boolean isActive() {
		return activeState == this;
	}

	final protected void activate() {
		synchronized (activeState) {
			activeState = this;
		}
	}

	public enum Flag {
		SYN, ACK, FIN, RST, SYN_ACK, FIN_ACK
	}

	final protected void send(Flag flag) {
		System.out.println(this.getClass().getSimpleName() + " sends "
				+ flag.toString());
	}

	protected void run() {
	}

}
