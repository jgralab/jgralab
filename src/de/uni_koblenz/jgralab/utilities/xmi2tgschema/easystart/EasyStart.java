package de.uni_koblenz.jgralab.utilities.xmi2tgschema.easystart;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * <b><code>EasyStart</code></b> simplifies the process of initialising the <code>XMI to TG file conversion</code>.<br />
 * <br />Normally the manual process of launching the conversion, by entering all needed arguments in a command shell, is very time consuming.
 * <br /><b><code>EasyStart</code></b> therefore accomplishes the following tasks:
 * <ol>
 * 	<li>
 * 		For starters, needed information regarding the <b><code>XMI2TG</code></b>-Conversion process are queried.
 * 		<br />The requested informations are:
 * 		<ul>
 * 			<li> The path to the <code>saxonX</code> (X stands for the version) Jar file; the masterpiece to the transformation process.</li>
 * 			<li> The path to the exported Enterprise Architect <code>diagram</code>, in <code>XML</code> format.</li>
 * 			<li> The path to the <code>XMI2TG.xsl</code> file, a stylesheet describing how exported diagrams in XML should be transformed.</li>
 * 			<li> The path to the output <code>TG</code> file, created during the XMI2TG conversion.</li>
 * 			<li> The name of the <code>diagram</code>.</li>
 * 			<li> The <code>Operating System</code>, for which to create a <code>shell script</code> for.</li>
 * 			<li> The path to where to save the output <code>shell script</code>. <i>(see point 2 for more information regarding created the shell script)</i></li>
 * 		</ul>
 * 	</li>
 * 	<li>
 * 		A shell script, containing the polled date, is written to disk.
 * 		<br />This shell script is intended to quickly and easily launch the XMI to TG file conversion.
 * 	</li><br /><br />
 * 	<li>
 * 		Finally the user is given the choice to immediately start the XMI2TG conversion.
 * 	</li>
 * </ol>
 * @author Grégory Catellani
 * @organisation Institute for Software Technology - University of Koblenz-Landau, Germany
 * @email ist@uni-koblenz.de
 * @version 1.0
 * 
 */
public class EasyStart{
	
	/*---- Class Variables ----*/
	
	private static EasyStart instance;	// Sole instance of this class | Created according to the Singleton Pattern
	
	
	/*---- Object Variables -----*/
	
	// Dialog options
	private Object[] noSelectionMadeDialogOptions = {"Redo this", "Restart program", "Quit"};
	private Object[] confirmationDialogOptions = {"Yes" , "No" , "Quit"};

	// Operating systems for which EasyStart can create shell scripts 
	private String[][] os = new String[][]{
			{"Linux", "Mac", "Windows"},	// The OS
			{"", "", "BAT File"},			// The according shell script filename
			{"", "", "bat"}					// The according shell script filename extension
	};
	
	private final int SAXONX_PATH = 0, XML_PATH = 1, XMI2TG_XSL_PATH = 2, TG_PATH = 3, DIAGRAM_NAME = 4, OS = 5, SHELL_SCRIPT_PATH = 6; // Indices of the different queries in the argument array
	private String[] arguments = new String[7]; // Array containing the needed information for the XMI2TG conversion
	
	
	/*---- Constructors ----*/
	
	/**
	 * Starts a new EasyStart instance.<br />
	 * This constructor is <b>private</b> because a class-object is to be requested via the <code>instance()</code> method.
	 */
	private EasyStart(){
		System.out.println("XMI2TG - EasyStart");
		System.out.println("------------------\n");
		
		gatherInformation();
		
		writeShellScriptFile();
		
		// Start XMI2TG conversion immediately, if desired
		if(JOptionPane.showOptionDialog(null, "Do you want to initiate the XMI2TG conversion immediately?", "Start conversion now?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) initiateXMI2TGConversion();
		
		System.out.println("\nGood Bye");
		System.out.println("--------");
		System.out.println("\nProgram written by Grégory Catellani");
		System.out.println("Institute for Software Technology - University of Koblenz-Landau, Germany");
	}
	
	
	/*---- Class Methods ----*/
	
	/**
	 * Returns the <b>unique instance</b> of this class according to the <b>Singleton</b> pattern.<br />
	 * A new instance is created upon the first access to this method.
	 */
	public static EasyStart instanceOf(){
		if(instance == null) instance = new EasyStart();
		return instance;
	}
	
	public static void main(String[] args) {
		instanceOf();
	}
	
	/*---- Object Methods ----*/
	
	/**
	 * Accomplishes the first of the three EasyStart tasks, by polling the user for vital information concerning the XMI2TG conversion.
	 */
	private void gatherInformation(){
		for(int i = 0; i < arguments.length; i++) arguments[i] = "";	// Initialise arguments
		
		/* Big loop, for polling all required information
		 * Upon user request the program can restart polling the information from the beginning.
		 * Thus allowing correction.
		 */
		start:
		while(true){
			if((arguments[SAXONX_PATH] = openPrompt("Please select the saxonX.jar file", "JAR File", "jar")) == null) continue;
			if((arguments[XML_PATH] = openPrompt("Please select the exported .xml scheme file", "XML File", "xml")) == null) continue;
			if((arguments[XMI2TG_XSL_PATH] = openPrompt("Please select the XMI2TGSchema.xsl file:", "XSL File", "xsl")) == null) continue;
			if((arguments[TG_PATH] = savePrompt("Select the destination of the TG file", "TG File", "tg")) == null) continue;
			if((arguments[DIAGRAM_NAME] = inputPrompt("Please enter the name of the schematics", "Prompt - Schematics name")) == null) continue;
			
			/* Poll for the operating system, to create a shell script file for
			 * For the moment only Microsoft Windows operating systems are supported.
			 * Support for GNU/Linux and MacOS will be implemented in a further release of EasyStart
			 */
			do{	//TODO Implement support for GNU/Linux and MacOS shell scripts
				if((arguments[OS] = inputPrompt("Please choose the operating system you want a shell script for", "Prompt - OS Selection", os[0], 2)) == null) continue start;
				else if(!arguments[OS].equals("Windows")) JOptionPane.showMessageDialog(null, "Sorry, but currently only Windows is supported!", "Warning - OS currently unsupported!", JOptionPane.ERROR_MESSAGE);
			}while(!arguments[OS].equals("Windows"));
			
			// Request the desired file path for the shell script. The shell script being in the appropriate format for the entered operating system
			{
				int selectedOS = 2;	// Dictate Windows as default, if something goes wrong
				for(int i = 0; i < os[0].length; i++) if(arguments[OS].equals(os[0][i])) selectedOS = i;	// Get the index of the correct file format informations
				if((arguments[SHELL_SCRIPT_PATH] = savePrompt("Select the destination of the shell script file", os[1][selectedOS], os[2][selectedOS])) == null) continue;
			}
			
			System.out.println("SaxonX.jar path: " + arguments[0]);
			System.out.println("XML Schematics path: " + arguments[1]);
			System.out.println("XMI2TGSchema.xsl path: " + arguments[2]);
			System.out.println("TG Output path: " + arguments[3]);
			System.out.println("Schematics name: " + arguments[4]);
			System.out.println("Selected OS: " + arguments[5]);
			System.out.println("Shell Script location: " + arguments[6] + "\n");
			
			break;
		}
	}
	
	/**
	 * Writes the <code>command shell input</code>, to the designated <code>shell script file</code>.
	 */
	private void writeShellScriptFile(){
		System.out.println("Writing Shell Script file...");
		
		try {
			// Replace backslash characters by double backslashes, for Java character escaping in Strings
			FileWriter shellScriptFile = new FileWriter(arguments[SHELL_SCRIPT_PATH].replace("\\", "\\\\"));
			
			// Write the call to execute the XMI2TG conversion, to the shell script file
			shellScriptFile.write("java -jar \"" + arguments[SAXONX_PATH] + "\" -s:\"" + arguments[XML_PATH] + "\" -xsl:\"" + arguments[XMI2TG_XSL_PATH] + "\" -o:\"" + arguments[TG_PATH] + "\" schemaName=" + arguments[DIAGRAM_NAME] + " tool=ea");
			
			shellScriptFile.close();
			System.out.println("Writing done!\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the <code>saxonX Jar file</code> and starts the XMI2TG conversion.
	 */
	private void initiateXMI2TGConversion(){
		System.out.println("Starting XMI2TGConversion...");
		
		// Replace backslash characters by double backslashes, for Java character escaping in Strings
		for(int i = 0; i < arguments.length; i++) arguments[i] = arguments[i].replace("\\", "\\\\");
		
		try {
			// Load the saxonX Jar File
			URLClassLoader urlLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + arguments[SAXONX_PATH] + "!/")});
			
			// Load the net.sf.saxon.Transform class and get acquire the main method
			Method main = Class.forName("net.sf.saxon.Transform", true, urlLoader).getDeclaredMethod("main", new String[]{}.getClass());
			
			// Invoke the main method, with the required arguments. Thus starting the XMI2TG conversion
			main.invoke(null, (Object) new String[]{"-s:" + arguments[XML_PATH], "-xsl:" + arguments[XMI2TG_XSL_PATH], "-o:" + arguments[TG_PATH], "schemaName=" + arguments[DIAGRAM_NAME], "tool=ea"});
			
			System.out.println("Conversion completed!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String openPrompt(String prompt, String fileExtensionDescriptor, String fileExtension){
		if(fileExtensionDescriptor == null || fileExtensionDescriptor.length() == 0 || fileExtension == null || fileExtension.length() == 0) throw new RuntimeException("For prompt: \"" + prompt + "\",\nCheck File Extension or File Extension Descriptor!");
		
		String temp = "";
		int i;
		JFileChooser fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(new FileNameExtensionFilter(fileExtensionDescriptor, fileExtension));
			fileChooser.setDialogTitle(prompt);
		
		while(true){
			if((i = fileChooser.showOpenDialog(null)) == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().getName().endsWith("." + fileExtension) && fileChooser.getSelectedFile().exists()){
				try {
					temp = fileChooser.getSelectedFile().getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
				int n = JOptionPane.showOptionDialog(null, "You chose: " + fileChooser.getSelectedFile().getName() + ". Is that ok?", "Prompt - Confirmation Dialog", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmationDialogOptions, confirmationDialogOptions[0]);
				if(n == 0) break;
				else if(n == 1) continue;
				else System.exit(0);
			} else if(i == JFileChooser.APPROVE_OPTION && !fileChooser.getSelectedFile().getName().endsWith("." + fileExtension)){
				JOptionPane.showMessageDialog(null, "Sorry, but the filename must end with " + fileExtension + "!", "Warning - Wrong name entered", JOptionPane.ERROR_MESSAGE);
				continue;
			} else if(i == JFileChooser.APPROVE_OPTION && !fileChooser.getSelectedFile().exists()){
				JOptionPane.showMessageDialog(null, "Sorry, but the entered file does not exist!", "Warning - File nonexistend", JOptionPane.ERROR_MESSAGE);
				continue;
			} else{
				int n = JOptionPane.showOptionDialog(null, "What do you want to do?", "Prompt - Make a choice", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, noSelectionMadeDialogOptions, noSelectionMadeDialogOptions[0]);
				if(n == 0) continue;
				else if(n == 1) return null;
				else System.exit(0);
			}
		}
		return temp;
	}
	
	private String savePrompt(String prompt, String fileExtensionDescriptor, String fileExtension){
		if(fileExtensionDescriptor == null || fileExtensionDescriptor.length() == 0 || fileExtension == null || fileExtension.length() == 0) throw new RuntimeException("For prompt: \"" + prompt + "\",\nCheck File Extension or File Extension Descriptor!");

		String temp = "";
		int i;
		JFileChooser fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(new FileNameExtensionFilter(fileExtensionDescriptor, fileExtension));
			fileChooser.setDialogTitle(prompt);
		
		while(true){
			if((i = fileChooser.showSaveDialog(null)) == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().getName().endsWith("." + fileExtension)){
				/*Check if the filename is empty!*/
				if(fileChooser.getSelectedFile().getName().equals("." + fileExtension)){
					JOptionPane.showMessageDialog(null, "The filename must not be empty!", "Invalid filename", JOptionPane.ERROR_MESSAGE);
					continue;
				}
				
				/*If the file already exists and the user DOES not want to override the file, he will be prompted for another file*/
				if(fileChooser.getSelectedFile().exists() && JOptionPane.showOptionDialog(null, "Do you want to override the existing file?", "Prompt - Really Override?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 1) continue;
				else{
					try {
						temp = fileChooser.getSelectedFile().getCanonicalPath();
					} catch (IOException e) {
						e.printStackTrace();
					}
					int n = JOptionPane.showOptionDialog(null, "You chose: " + fileChooser.getSelectedFile().getName() + ". Is that ok?", "Confirmation Dialog", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmationDialogOptions, confirmationDialogOptions[0]);
					if(n == 0) break;
					else if(n == 1) continue;
					else System.exit(0);
				}
			} else if(i == JFileChooser.APPROVE_OPTION && !fileChooser.getSelectedFile().getName().endsWith("." + fileExtension)){
				JOptionPane.showMessageDialog(null, "Sorry, but the filename must end with " + fileExtension + "!", "Warning - Wrong name entered", JOptionPane.ERROR_MESSAGE);
				continue;
			} else{
				int n = JOptionPane.showOptionDialog(null, "What do you want to do?", "Choose your destiny", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, noSelectionMadeDialogOptions, noSelectionMadeDialogOptions[0]);
				if(n == 0) continue;
				else if(n == 1) return null;
				else System.exit(0);
			}
		}
		
		return temp;
	}
	
	private String inputPrompt(String msg, String caption){
		return inputPrompt(msg, caption, null, -1);
	}
	
	private String inputPrompt(String msg, String caption, Object[] options, int defaultOptionValue){
		String temp = "";
		Object defaultOption = (options != null && defaultOptionValue >= 0 && defaultOptionValue < options.length) ? options[defaultOptionValue] : null;
		
		while(true){
			temp = (String)JOptionPane.showInputDialog(null, msg, caption, JOptionPane.PLAIN_MESSAGE, null, options, defaultOption);
			if(temp == null || temp.length() == 0){
				int n = JOptionPane.showOptionDialog(null, "What do you want to do?", "Question Dialog", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, noSelectionMadeDialogOptions, noSelectionMadeDialogOptions[0]);
				if(n == 0) continue;
				else if(n == 1) return null;
				else System.exit(0);
			} else{
				int n = JOptionPane.showOptionDialog(null, "You entered: " + temp + ". Is that ok?", "Confirmation Dialog", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmationDialogOptions, confirmationDialogOptions[0]);
				if(n == 0) break;
				else if(n == 1) continue;
				else System.exit(0);
			}
		}
		return temp;
	}
}
