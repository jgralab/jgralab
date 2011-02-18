package de.uni_koblenz.jgralab;

public abstract class SListeningState extends ListeningState {

	private void iShallBeIgnored() {
		Listen.Instance().activate();
	}
}
