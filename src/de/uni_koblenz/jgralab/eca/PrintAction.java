package de.uni_koblenz.jgralab.eca;

public class PrintAction extends Action {

	/**
	 * Message to print on console
	 */
	private String message;
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++
	
	/**
	 * Creates a PrintAction with the given message
	 * 
	 * @param message
	 *            the message to print
	 */
	public PrintAction(String message){
		this.message = message;
	}
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Executes the action, in this case print the message on console
	 */
	@Override
	public void doAction(){
		System.out.println(this.message);
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return Message, this action prints on Console
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message to print on Console
	 * 
	 * @param message
	 *            to print
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
