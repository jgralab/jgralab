/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Mini Wegb Server / SimpleWebServer.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: ServerSideScriptEngine.java,v 1.4 2004/02/01 13:37:35 pjm2 Exp $

 */

package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright Paul Mutton http://www.jibble.org/
 * 
 */
public class RequestThread extends Thread {

	private static File workspace;
	private final StateRepository rep;
	public static Long MAXIMUM_FILE_SIZE;
	public static long MAXIMUM_WORKSPACE_SIZE;

	private final Socket _socket;

	private static final HashSet<String> svgToDelete = new HashSet<String>();

	public RequestThread(Socket socket, String path) {
		_socket = socket;
		if (path == null) {
			workspace = null;
		} else {
			workspace = new File(path);
		}
		rep = new StateRepository(workspace);
	}

	private static void sendHeader(BufferedOutputStream out, int code,
			String contentType, long contentLength, long lastModified)
			throws IOException {
		out.write(("HTTP/1.0 "
				+ code
				+ " OK\r\n"
				+ "Date: "
				+ new Date().toString()
				+ "\r\n"
				+ "Server: JibbleWebServer/1.0\r\n"
				+ "Content-Type: "
				+ contentType
				+ "\r\n"
				+ "Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n"
				+ ((contentLength != -1) ? "Content-Length: " + contentLength
						+ "\r\n" : "") + "Last-modified: "
				+ new Date(lastModified).toString() + "\r\n" + "\r\n")
				.getBytes());
	}

	private static void sendError(BufferedOutputStream out, int code,
			String message) throws IOException {
		message = message + "<hr>" + TGraphBrowserServer.VERSION;
		sendHeader(out, code, "text/html", message.length(), System
				.currentTimeMillis());
		out.write(message.getBytes());
		out.flush();
		out.close();
	}

	@Override
	public void run() {
		InputStream reader = null;
		try {
			_socket.setSoTimeout(30000);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					_socket.getInputStream()));
			BufferedOutputStream out = new BufferedOutputStream(_socket
					.getOutputStream());
			String firstLine = in.readLine();
			String request = URLDecoder.decode(firstLine != null ? firstLine
					: "", "UTF-8");
			if ((request == null)
					|| (!request.startsWith("GET ") && !request
							.startsWith("POST "))
					|| !(request.endsWith(" HTTP/1.0") || request
							.endsWith("HTTP/1.1"))) {
				// Invalid request type (no "GET")
				sendError(out, 500, "Invalid Method.");
				return;
			}
			if (request.startsWith("POST ")) {
				// POST-Request
				// Decide if it is a file upload
				String line;
				Long contentLength = null;
				String contentType = null;
				do {
					line = in.readLine();
					if (line.startsWith("Content-Type")) {
						contentType = line;
					} else if (line.startsWith("Content-Length:")) {
						contentLength = Long.parseLong(line.substring(16));
					}
				} while (contentLength == null || contentType == null);
				if (contentType.contains("multipart/form-data")) {
					// file upload
					TGraphBrowserServer.logger.info(request);
					// determine overwrite
					boolean shouldOverwrite = request
							.contains("overwrite=true");
					// determine boundary
					String boundary = contentType.split("boundary=")[1];
					String[] bounds = boundary.split("-");
					boundary = bounds[bounds.length - 1];
					do {
						line = in.readLine();
					} while (!line.contains("filename="));
					String filename = line.split("filename=")[1];
					// cut off "
					filename = filename.substring(1, filename.length() - 1);
					if (!filename.toLowerCase().endsWith(".tg")) {
						sendFile(out,
								"TGraphBrowser_GraphChoice_AfterError.html",
								"You can only upload .tg files!");
					} else if (!isSizeOk(contentLength)) {
						sendFile(out,
								"TGraphBrowser_GraphChoice_AfterError.html",
								"The .tg file is too big!");
					} else {
						// find beginning of file
						// next in.readLine()is first line of file
						while ((line != null) && !line.equals("")) {
							line = in.readLine();
						}
						// create File which does not exist in the workspace
						// yet
						filename = workspace.toString() + "/"
								+ filename.substring(0, filename.length() - 3);
						File receivedFile = new File(filename + ".tg");
						if (!shouldOverwrite) {
							for (int i = 0; receivedFile.exists(); i++) {
								receivedFile = new File(filename + i + ".tg");
							}
						}
						if (!receivedFile.createNewFile()) {
							TGraphBrowserServer.logger
									.info(receivedFile.toString()
											+ " overwrites an existing file or could not be created.");
						}
						FileWriter fw = new FileWriter(receivedFile);
						line = in.readLine();
						// nextLine is used because before the endline comes
						// there is an empty line
						String nextLine = in.readLine();
						int i = 0;
						while ((nextLine != null)
								&& !nextLine.contains(boundary)) {
							fw.write(line + "\n");
							i++;
							line = nextLine;
							nextLine = in.readLine();
							if (i == 1000) {
								fw.flush();
							}
						}
						fw.flush();
						fw.close();
						// send the answer page
						int sessionId = StateRepository
								.createNewSession(receivedFile);
						sendFile(
								out,
								"TGraphBrowser_GraphLoaded.html",
								"= "
										+ sessionId
										+ ";\n\t\ttimestamp = "
										+ StateRepository.getSession(sessionId).lastAccess);
					}
				} else {
					// no file upload
					// skip rest of header
					do {
						line = in.readLine();
					} while ((line != null) && !line.isEmpty());
					// read Content
					char[] content = new char[contentLength.intValue()];
					int read = in.read(content);
					if (read < contentLength) {
						TGraphBrowserServer.logger.warning("There were only "
								+ read + " chars read instead of "
								+ contentLength + " chars.");
					}
					String body = new String(content);
					String[] bodyparts = Pattern.compile(
							Matcher.quoteReplacement("\n")).split(body);
					// read Content
					String timestampString = bodyparts[0];
					long timestamp = timestampString.equals("undefined") ? Long.MIN_VALUE
							: Long.parseLong(timestampString);
					if (timestamp
							+ TGraphBrowserServer.DeleteUnusedStates.timeout < System
							.currentTimeMillis()) {
						// the state was already deleted because of timeout
						sendErrorMessage(
								out,
								"This session was deleted"
										+ " because there wasn't any communication "
										+ "with the server in the last "
										+ TGraphBrowserServer.DeleteUnusedStates.timeout
										/ 1000 + "sec.");
					} else if (timestamp < TGraphBrowserServer.starttime) {
						// the state wasn't from this instance of the server
						sendErrorMessage(out, "This server was restarted."
								+ " This session is too old.");
					} else {
						// the state wasn't deleted yet.
						String sessionId = bodyparts[1];
						String methodname = bodyparts[2];
						// read parameters
						ArrayList<String> args = new ArrayList<String>();
						args.add(sessionId);
						// each line is a parameter
						for (int i = 3; i < bodyparts.length; i++) {
							args.add(bodyparts[i]);
						}
						/*
						 * Invoke called method.
						 */
						StringBuilder erg = callMethod(out, methodname, args
								.toArray(new String[0]));
						if (erg != null) {
							sendMessage(out, erg);
						}
					}
				}
			} else {
				// GET-Request
				TGraphBrowserServer.logger.info(request);
				String path = request.substring(5, request.length() - 9);
				if (path.isEmpty()) {
					// send TGraphBrowser_GraphChoice.html
					sendFile(out, "TGraphBrowser_GraphChoice.html");
				} else if (path.contains("jgralab-logo.png")) {
					// send JGraLab picture
					sendFile(out, "jgralab-logo.png");
				} else if (path.contains("plus.png")) {
					// send plus picture
					sendFile(out, "plus.png");
				} else if (path.contains("minus.png")) {
					// send minus picture
					sendFile(out, "minus.png");
				} else if (path.endsWith("svg")) {
					// check if svg-file was already created
					if (path.charAt(0) == '_') {
						// the request comes from an Internet Explorer
						if (svgToDelete.contains(path)) {
							// the second request arrived
							svgToDelete.remove(path);
						} else {
							// this is the first request but there will be send
							// a second request
							svgToDelete.add(path);
						}
					}
					String fileName = System.getProperty("java.io.tmpdir")
							+ File.separator
							+ "tGraphBrowser"
							+ File.separator
							+ (path.charAt(0) == '_' ? path.substring(1) : path);
					File svg = new File(fileName);
					// System.out.println("path = " + path);
					// System.out.println("svg = " + svg.getCanonicalPath());
					long sleepTime = System.currentTimeMillis() + 10000;
					while (!svg.exists()
							&& (System.currentTimeMillis() <= sleepTime)) {
					}
					// send svgFile
					if (svg.exists()) {
						sendFile(out, fileName);
						if (!svgToDelete.contains(path)) {
							if (!svg.delete()) {
								TGraphBrowserServer.logger.warning(svg
										.toString()
										+ " could not be deleted.");
							}
						}
					} else {
						sendMessage(out, new StringBuilder(svg.toString()
								+ " was not created."));
					}
				} else {
					/*
					 * Split received url
					 */
					// path.split(?) doesn't work because ? is a reserved
					// char in RegExp
					path = path.replace("?", "#");
					// split path into path and method parts
					String[] parts = path.split("#");
					// split method parts into name and args
					String methodname = parts.length > 0 ? parts[0] : null;
					String[] args = null;
					if (parts.length > 1) {
						String[] methodArgs = parts[1].split("&");
						args = new String[methodArgs.length];
						for (int i = 0; i < args.length; i++) {
							args[i] = methodArgs[i].split("=")[1];
						}
					}
					/*
					 * Invoke called method.
					 */
					StringBuilder erg = callMethod(out, methodname, args);
					if (methodname.equals("loadGraphFromURI")
							|| methodname.equals("loadGraphFromServer")) {
						if (erg.toString().equals("-1")) {
							sendFile(
									out,
									"TGraphBrowser_GraphChoice_AfterError.html",
									"The tg.-file is too big!");
						} else {
							sendFile(
									out,
									"TGraphBrowser_GraphLoaded.html",
									"= "
											+ erg
											+ ";\n\t\t timestamp = "
											+ StateRepository
													.getSession(Integer
															.parseInt(erg
																	.toString())).lastAccess);
						}
					} else {
						sendMessage(out, erg);
					}
				}
			}
			// close output
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Calls the received Method.
	 * 
	 * @param out
	 * @param methodname
	 *            name of the method
	 * @param args
	 *            parameters of the method
	 * @throws IOException
	 * @return result of method invocation
	 */
	private StringBuilder callMethod(BufferedOutputStream out,
			String methodname, String[] args) throws IOException {
		// get method
		Method method = StateRepository.definedMethods.get(methodname);
		if (method == null) {
			sendErrorMessage(out, "There does not exist a method with name "
					+ methodname + ".");
			return new StringBuilder();
		}
		// check parameters
		Class<?>[] params = method.getParameterTypes();
		Object[] currentParams = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			if (params[i] == Integer.class) {
				currentParams[i] = Integer.parseInt(args[i]);
			} else if (params[i] == Boolean.class) {
				currentParams[i] = Boolean.parseBoolean(args[i]);
			} else {
				currentParams[i] = args[i];
			}
		}
		// create logging entry
		StringBuilder argsString = new StringBuilder();
		for (int i = 0; (currentParams != null) && (i < currentParams.length); i++) {
			argsString.append((i == 0 ? "" : ", ")
					+ (currentParams[i] instanceof String ? "\"" : "")
					+ currentParams[i]
					+ (currentParams[i] instanceof String ? "\"" : ""));
		}
		TGraphBrowserServer.logger.info("StateRepository." + methodname + "("
				+ argsString.toString() + ")");
		try {
			// call method
			return (StringBuilder) method.invoke(rep, currentParams);
		} catch (IllegalArgumentException e) {
			sendErrorMessage(out, e.toString());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			sendErrorMessage(out, e.toString());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			sendErrorMessage(out, e.getCause().toString());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the size of the file is ok. And if there is enough free space
	 * in the workspace.
	 * 
	 * @param size
	 *            the size of the file in Byte
	 * @return true iff the file is not too large
	 */
	public static synchronized boolean isSizeOk(long size) {
		if (MAXIMUM_FILE_SIZE == null) {
			return true;
		}
		if (size > MAXIMUM_FILE_SIZE) {
			return false;
		}
		return workspace.getTotalSpace() + size <= MAXIMUM_WORKSPACE_SIZE;
	}

	/**
	 * Sends the file <code>file</code> to the client.
	 * 
	 * @param out
	 *            the BufferedOutputStream to the client.
	 * @param file
	 *            the file which should be sent.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void sendFile(BufferedOutputStream out, String file)
			throws IOException, FileNotFoundException {
		if (_socket.isConnected()) {
			sendHeader(out, 200, file.endsWith(".svg") ? "image/svg+xml"
					: (file.endsWith(".png") ? "image/png" : "text/html"), -1,
					System.currentTimeMillis());
			InputStream reader = null;
			if (new File(file).isAbsolute()) {
				reader = new FileInputStream(file);
			} else {
				reader = getClass().getResourceAsStream("resources/" + file);
			}
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = reader.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			reader.close();
		}
	}

	/**
	 * Sends the file <code>file</code> to the client. In the <code>file</code>
	 * /*?* / is replaced by <code>replaceText</code>.
	 * 
	 * @param out
	 *            the BufferedOutputStream to the client.
	 * @param string
	 *            the file which should be sent.
	 * @param replaceText
	 *            the sessionId
	 * @throws IOException
	 */
	private void sendFile(BufferedOutputStream out, String file,
			String replaceText) throws IOException {
		if (_socket.isConnected()) {
			sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream("resources/" + file)));
			boolean isReplaced = false;
			String line;
			for (line = br.readLine(); line != null; line = br.readLine()) {
				if (!isReplaced && line.contains("/*?*/")) {
					line = line.replace("/*?*/", replaceText);
					isReplaced = true;
				}
				if (!isReplaced) {
					out.write((line + "\n").getBytes());
				} else {
					out.write((line + "\n").getBytes());
					// String was replaced. Now the data can be send faster.
					break;
				}
			}
			if (line != null) {
				char[] buffer = new char[4096];
				int bytesRead;
				while ((bytesRead = br.read(buffer)) != -1) {
					out.write(new String(buffer).getBytes(), 0, bytesRead);
				}
			}
			br.close();
		}
	}

	/**
	 * Sends a JSON HTTP-Response to the client:<br>
	 * {<br>
	 * method: <code>method</code> which is executed by the client<br>
	 * }
	 * 
	 * @param out
	 * @param method
	 * @throws IOException
	 */
	private void sendMessage(BufferedOutputStream out, StringBuilder method)
			throws IOException {
		if (_socket.isConnected()) {
			sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
			String send = "{ \"method\": " + method.toString() + " }";
			out.write(send.getBytes());
		}
	}

	/**
	 * Sends a JSON HTTP-Response to the client:<br>
	 * {<br>
	 * method: prints the error message <code>message</code> at the client<br>
	 * }
	 * 
	 * @param out
	 * @param message
	 * @throws IOException
	 */
	private void sendErrorMessage(BufferedOutputStream out, String message)
			throws IOException {
		if (_socket.isConnected()) {
			sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
			String send = "{ \"method\": "
					+ "function() {\n"
					+ "document.getElementById('loadError').innerHTML = \"ERROR: "
					+ message
					+ "\";\n"
					+ "document.getElementById('divError').style.display = \"block\";\n"
					+ "document.getElementById('h2ErrorMessage').innerHTML = \"ERROR: "
					+ message
					+ "\";\n"
					+ "document.getElementById('divNonError').style.display = \"none\";\n"
					+ " } }";
			out.write(send.getBytes());
		}
	}

}