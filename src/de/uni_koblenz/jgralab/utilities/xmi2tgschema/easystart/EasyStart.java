package de.uni_koblenz.jgralab.utilities.xmi2tgschema.easystart;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author Grégory Catellani
 * @version 1.0
 * 
 */
public class EasyStart{
	
	/*---- Class Variables ----*/
	
	private static EasyStart instance;
	
	/*---- Object Variables -----*/
		
	//private JFileChooser fileChooser = new JFileChooser();
	private Object[] noSelectionMadeDialogOptions = {"Redo this", "Restart program", "Quit"};
	private Object[] confirmationDialogOptions = {"Yes" , "No" , "Quit"};

	private String[][] os = new String[][]{
			{"Linux", "Mac", "Windows"},
			{"", "", "BAT File"},
			{"", "", "bat"}
	};
	private final int SAXONX_PATH = 0, XML_PATH = 1, XMI2TG_XSL_PATH = 2, TG_PATH = 3, SCHEMATICS_NAME = 4, OS = 5, SHELL_SCRIPT_PATH = 6;
	private String[] arguments = new String[7];
	
	/*---- Constructors ----*/
	
	{
		for(int i = 0; i < arguments.length; i++) arguments[i] = "";
	}
	
	private EasyStart(){
		System.out.println("XMI2TG - EasyStart");
		System.out.println("------------------\n");
		
		gatherInformation();
		
		writeShellScriptFile();
		
		if(JOptionPane.showOptionDialog(null, "Do you want to initiate the XMI2TG conversion immediately?", "Start conversion now?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == 0) initiateXMI2TGConversion();
		
		System.out.println("\nGood Bye");
		System.out.println("--------");
		System.out.println("\nProgram written by Grégory Catellani");
		System.out.println("Organisation: Universität Koblenz-Landau - Institut für Softwaretechnik");
	}
			
	/*---- Object Methods ----*/
	
	private void gatherInformation(){
		start:
		while(true){
			if((arguments[SAXONX_PATH] = openPrompt("Please select the saxonX.jar file", "JAR File", "jar")) == null) continue;
			if((arguments[XML_PATH] = openPrompt("Please select the exported .xml scheme file", "XML File", "xml")) == null) continue;
			if((arguments[XMI2TG_XSL_PATH] = openPrompt("Please select the XMI2TGSchema.xsl file:", "XSL File", "xsl")) == null) continue;
			if((arguments[TG_PATH] = savePrompt("Select the destination of the TG file", "TG File", "tg")) == null) continue;
			if((arguments[SCHEMATICS_NAME] = inputPrompt("Please enter the name of the schematics", "Prompt - Schematics name")) == null) continue;
			do{
				if((arguments[OS] = inputPrompt("Please choose the operating system you want a shell script for", "Prompt - OS Selection", os[0], 2)) == null) continue start;
				else if(!arguments[OS].equals("Windows")) JOptionPane.showMessageDialog(null, "Sorry, but currently only Windows is supported!", "Warning - OS currently unsupported!", JOptionPane.ERROR_MESSAGE);
			}while(!arguments[OS].equals("Windows"));
			{
				int i = arrayIndexOf(arguments[OS], os[0]);
				String osShellScriptFileExtensionDescriptor = (i >= 0 && i < os[1].length) ? os[1][i] : null;
				String osShellScriptFileExtension = (i >= 0 && i < os[2].length) ? os[2][i] : null;
				if((arguments[SHELL_SCRIPT_PATH] = savePrompt("Select the destination of the shell script file", osShellScriptFileExtensionDescriptor, osShellScriptFileExtension)) == null) continue;
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
	
	private void writeShellScriptFile(){
		System.out.println("Writing Shell Script file...");
		try {
			FileWriter shellScriptFile = new FileWriter(arguments[SHELL_SCRIPT_PATH].replace("\\", "\\\\"));
			shellScriptFile.write("java -jar \"" + arguments[SAXONX_PATH] + "\" -s:\"" + arguments[XML_PATH] + "\" -xsl:\"" + arguments[XMI2TG_XSL_PATH] + "\" -o:\"" + arguments[TG_PATH] + "\" schemaName=" + arguments[SCHEMATICS_NAME] + " tool=ea");
			shellScriptFile.close();
			System.out.println("Writing done!\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initiateXMI2TGConversion(){
		System.out.println("Starting XMI2TGConversion...");
		// Parameter Adaptation
		for(int i = 0; i < arguments.length; i++) arguments[i] = arguments[i].replace("\\", "\\\\");
		
		try {
			URLClassLoader urlLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + arguments[SAXONX_PATH] + "!/")});
			Method main = Class.forName("net.sf.saxon.Transform", true, urlLoader).getDeclaredMethod("main", new String[]{}.getClass());
			main.invoke(null, (Object) new String[]{"-s:" + arguments[XML_PATH], "-xsl:" + arguments[XMI2TG_XSL_PATH], "-o:" + arguments[TG_PATH], "schemaName=" + arguments[SCHEMATICS_NAME], "tool=ea"});
			System.out.println("Conversion completed!");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
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
	
	private int arrayIndexOf(String str, Object[] strArray){
		if(str == null || str.length() == 0 || strArray == null) throw new IllegalArgumentException("SearchString and/or array must not be null OR SearchString must not be empty!");
		
		for(int i = 0; i < strArray.length; i++) if(str.equals((String)strArray[i])) return i;

		return -1;
	}

	/*---- Class Methods ----*/
	
	public static EasyStart instanceOf(){
		if(instance == null) instance = new EasyStart();
		return instance;
	}
	
	public static void main(String[] args) {
		instanceOf();
	}
}
