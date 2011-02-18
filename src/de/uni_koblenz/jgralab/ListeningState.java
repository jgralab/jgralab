package de.uni_koblenz.jgralab;

public abstract class ListeningState extends State {
	final protected Flag getReceivedFlag() {
		// return Flag.values()[(int) Math.round(Math.random()
		// * Flag.values().length)];
		return Math.random() < 0.5 ? Flag.ACK : Flag.FIN;
	}
}
