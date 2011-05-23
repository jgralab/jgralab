package de.uni_koblenz.jgralab.eca;

public class PrintAction implements Action {

	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	//++++++++++++++++++++++++++++++++++++++
	
	
	public PrintAction(String message){
		this.message = message;
	}
	
	public void doAction(){
		System.out.println(this.message);
	}
}
