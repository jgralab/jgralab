/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

	static final String SVG_WITH_ZOOM_AND_MOVE_SUPPORT = "resources/svgNavigation.svg";
	private File workspace;
	private final StateRepository rep;

	private final Socket _socket;

	private static final HashSet<String> svgToDelete = new HashSet<>();

	public RequestThread(Socket socket, String path) {
		_socket = socket;
		if (path == null) {
			workspace = null;
		} else {
			workspace = new File(path);
		}
		rep = new StateRepository(workspace, this);
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
				+ (contentLength != -1 ? "Content-Length: " + contentLength
						+ "\r\n" : "") + "Last-modified: "
				+ new Date(lastModified).toString() + "\r\n" + "\r\n")
				.getBytes());
	}

	private static void sendError(BufferedOutputStream out, int code,
			String message) throws IOException {
		try {
			message = message + "<hr>" + TGraphBrowserServer.VERSION;
			sendHeader(out, code, "text/html", message.length(),
					System.currentTimeMillis());
			out.write(message.getBytes());
			out.flush();
		} finally {
			out.close();
		}
	}

	@Override
	public void run() {
		try {
			_socket.setSoTimeout(30000);
			InputStream inputStream = _socket.getInputStream();
			DataInputStream in = new DataInputStream(new BufferedInputStream(
					inputStream));
			// BufferedReader in = new BufferedReader(new InputStreamReader(
			// inputStream));
			BufferedOutputStream out = new BufferedOutputStream(
					_socket.getOutputStream());
			String firstLine = readLine(in);
			String request = URLDecoder.decode(firstLine != null ? firstLine
					: "", "UTF-8");
			if (request == null
					|| !request.startsWith("GET ")
					&& !request.startsWith("POST ")
					|| !(request.endsWith(" HTTP/1.0") || request
							.endsWith("HTTP/1.1"))) {
				// Invalid request type (no "GET")
				sendError(out, 500, "Invalid Method.");
				return;
			}
			if (request.startsWith("POST ")) {
				// POST-Request
				handlePostRequest(in, out, request);
			} else {
				// GET-Request
				handleGetRequest(out, request);
			}
			// close output
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param out
	 * @param request
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void handleGetRequest(BufferedOutputStream out, String request)
			throws IOException, FileNotFoundException {
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
			handleRequestForSVG(out, path);
		} else {
			handleGetMethodCall(out, path);
		}
	}

	/**
	 * @param out
	 * @param path
	 * @throws IOException
	 */
	private void handleGetMethodCall(BufferedOutputStream out, String path)
			throws IOException {
		// split received URL

		// split path into path and method parts
		String[] parts = path.split(Pattern.quote("?"));
		// split method parts into name and args
		String methodname = parts.length > 0 ? parts[0] : null;
		if (methodname == null) {
			throw new IllegalArgumentException(
					"Couldn't determine method name from path '" + path + "'");
		}
		String[] args = null;
		if (parts.length > 1) {
			String[] methodArgs = parts[1].split("&");
			args = new String[methodArgs.length];
			for (int i = 0; i < args.length; i++) {
				args[i] = methodArgs[i].split("=")[1];
			}
		}

		// Invoke called method.
		StringBuilder erg = callMethod(out, methodname, args);
		if (methodname.equals("loadGraphFromURI")
				|| methodname.equals("loadGraphFromServer")) {
			if (erg.toString().equals("-1")) {
				sendFile(out, "TGraphBrowser_GraphChoice_AfterError.html",
						"The tg.-file is too big!");
			} else {
				try {
					sendFile(
							out,
							"TGraphBrowser_GraphLoaded.html",
							"= "
									+ erg
									+ ";\n\t\t timestamp = "
									+ StateRepository.getSession(Integer
											.parseInt(erg.toString())).lastAccess);
				} catch (NumberFormatException e) {
					// Extract the error message
					String ergText = erg.toString();
					String[] partOfErg = ergText
							.split(Pattern.quote("ERROR: "));
					if (partOfErg.length > 0) {
						partOfErg = partOfErg[1].split(Pattern.quote("\""));
						ergText = partOfErg[0];
					}

					// there was an exception while loading the graph
					sendFile(out, "TGraphBrowser_GraphChoice_AfterError.html",
							ergText);
				}
			}
		} else {
			sendMessage(out, erg);
		}
	}

	/**
	 * @param out
	 * @param path
	 * @throws IOException
	 */
	private void handleRequestForSVG(BufferedOutputStream out, String path)
			throws IOException {
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
		String fileName = System.getProperty("java.io.tmpdir") + File.separator
				+ "tgraphbrowser" + File.separator
				+ (path.charAt(0) == '_' ? path.substring(1) : path);
		File svg = new File(fileName);
		long sleepTime = System.currentTimeMillis() + 10000;
		while (!svg.exists() && System.currentTimeMillis() <= sleepTime) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// send svgFile
		if (svg.exists()) {
			sendSVG(out, fileName);
			if (!svgToDelete.contains(path)) {
				if (!svg.delete()) {
					TGraphBrowserServer.logger.warning(svg.toString()
							+ " could not be deleted.");
				}
			}
		} else {
			sendMessage(out, new StringBuilder(svg.toString()
					+ " was not created."));
		}
	}

	/**
	 * @param in
	 * @param out
	 * @param request
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void handlePostRequest(DataInputStream in,
			BufferedOutputStream out, String request) throws IOException,
			FileNotFoundException {
		// Decide if it is a file upload
		String line;
		Long contentLength = null;
		String contentType = null;
		do {
			line = readLine(in);
			if (line.startsWith("Content-Type")) {
				contentType = line;
			} else if (line.startsWith("Content-Length:")) {
				contentLength = Long.parseLong(line.substring(16));
			}
		} while (contentLength == null || contentType == null);

		if (contentType.contains("multipart/form-data")) {
			// file upload
			handleFileUpload(in, out, request, contentLength, contentType);
		} else {
			// no file upload
			handleNoFileUpload(in, out, contentLength);
		}
	}

	/**
	 * @param in
	 * @param out
	 * @param contentLength
	 * @throws IOException
	 */
	private void handleNoFileUpload(DataInputStream in,
			BufferedOutputStream out, Long contentLength) throws IOException {
		String line;
		// skip rest of header
		do {
			line = readLine(in);
		} while (line != null && !line.isEmpty());

		// read attribute content
		byte[] content = new byte[contentLength.intValue()];
		if (in.read(content) <= 0) {
			TGraphBrowserServer.logger.info("content could not be read");
		}
		String body = new String(content);
		String[] bodyparts = Pattern.compile(Matcher.quoteReplacement("\n"))
				.split(body);
		// read content of message
		String timestampString = bodyparts[0];
		long timestamp = timestampString.equals("undefined") ? Long.MIN_VALUE
				: Long.parseLong(timestampString);

		if (timestamp + TGraphBrowserServer.DeleteUnusedStates.timeout < System
				.currentTimeMillis()) {
			// the state was already deleted because of timeout
			sendErrorMessage(out, "This session was deleted"
					+ " because there wasn't any communication "
					+ "with the server in the last "
					+ TGraphBrowserServer.DeleteUnusedStates.timeout / 1000
					+ "sec.");
		} else if (timestamp < TGraphBrowserServer.starttime) {
			// the state wasn't from this instance of the server
			sendErrorMessage(out, "This server was restarted."
					+ " This session is too old.");
		} else {
			// the state wasn't deleted yet.
			String sessionId = bodyparts[1];
			String methodname = bodyparts[2];
			// read parameters
			ArrayList<String> args = new ArrayList<>();
			args.add(sessionId);
			// each line is a parameter
			for (int i = 3; i < bodyparts.length; i++) {
				args.add(bodyparts[i]);
			}
			// Invoke called method.
			StringBuilder erg = callMethod(out, methodname,
					args.toArray(new String[0]));
			if (erg != null) {
				sendMessage(out, erg);
			}
		}
	}

	/**
	 * @param in
	 * @param out
	 * @param request
	 * @param contentLength
	 * @param contentType
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void handleFileUpload(DataInputStream in, BufferedOutputStream out,
			String request, Long contentLength, String contentType)
			throws IOException, FileNotFoundException {
		String line;
		TGraphBrowserServer.logger.info(request);

		// determine overwrite
		boolean shouldOverwrite = request.contains("overwrite=true");

		// determine boundary
		String boundary = contentType.split("boundary=")[1];
		String[] bounds = boundary.split("-");
		boundary = bounds[bounds.length - 1];
		do {
			line = readLine(in);
		} while (!line.contains(boundary));

		// +2 because \r\n is cut off
		int sizeOfLinesAlreadyRead = line.length() + 2;
		// read Content-Disposition
		line = readLine(in);
		// extract filename
		sizeOfLinesAlreadyRead += line.length() + 2;
		String filename = line.split("filename=")[1];
		// cut off "
		filename = filename.substring(1, filename.length() - 1);

		// handle file
		if (!filename.toLowerCase().endsWith(".tg")
				&& !filename.toLowerCase().endsWith(".gz")) {
			// it is not a tg or gz file.
			sendFile(out, "TGraphBrowser_GraphChoice_AfterError.html",
					"You can only upload .tg or .gz files!");
		} else if (!StateRepository.isSizeOk(workspace, contentLength)) {
			// the file is too large
			sendFile(out, "TGraphBrowser_GraphChoice_AfterError.html",
					"The .tg file is too big!");
		} else {
			// receive file
			File receivedFile = receiveFile(in, contentLength, line,
					shouldOverwrite, sizeOfLinesAlreadyRead, filename);
			// send the answer page
			int sessionId = rep
					.createNewSession(receivedFile.getAbsolutePath());
			sendFile(
					out,
					"TGraphBrowser_GraphLoaded.html",
					"= " + sessionId + ";\n\t\ttimestamp = "
							+ StateRepository.getSession(sessionId).lastAccess);
		}
	}

	/**
	 * @param in
	 * @param contentLength
	 * @param line
	 * @param shouldOverwrite
	 * @param sizeOfLinesAlreadyRead
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File receiveFile(DataInputStream in, Long contentLength,
			String line, boolean shouldOverwrite, int sizeOfLinesAlreadyRead,
			String filename) throws IOException, FileNotFoundException {
		// find beginning of file
		// next in.readLine()is first line of file
		while (line != null && !line.isEmpty()) {
			line = readLine(in);
			sizeOfLinesAlreadyRead += line.length() + 2;
		}

		// create file which does not exist in the workspace yet
		boolean isCompressed = filename.endsWith(".gz");
		filename = workspace.toString() + "/"
				+ filename.substring(0, filename.length() - 3);
		File receivedFile = new File(filename + (isCompressed ? ".gz" : ".tg"));
		if (!shouldOverwrite) {
			for (int i = 0; receivedFile.exists(); i++) {
				receivedFile = new File(filename + i
						+ (isCompressed ? ".gz" : ".tg"));
			}
		}
		if (!receivedFile.createNewFile()) {
			TGraphBrowserServer.logger.info(receivedFile.toString()
					+ " overwrites an existing file or could not be created.");
		}
		contentLength -= sizeOfLinesAlreadyRead;

		BufferedOutputStream fileOutput = createFile(in, contentLength,
				receivedFile);

		fileOutput.flush();
		fileOutput.close();
		return receivedFile;
	}

	/**
	 * @param in
	 * @param contentLength
	 * @param receivedFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private BufferedOutputStream createFile(DataInputStream in,
			Long contentLength, File receivedFile)
			throws FileNotFoundException, IOException {
		// read the multipart part of the http message
		BufferedOutputStream fileOutput = new BufferedOutputStream(
				new FileOutputStream(receivedFile));
		byte[] content = new byte[contentLength < 4096 ? (int) (contentLength % 4096)
				: 4096];
		int numberOfBytesRead = 1;
		long totalNumberOfBytesRead = 0;
		while (totalNumberOfBytesRead < contentLength - content.length) {
			totalNumberOfBytesRead += numberOfBytesRead = in.read(content);
			if (numberOfBytesRead <= 0) {
				break;
			} else {
				fileOutput.write(content, 0, numberOfBytesRead);
			}
		}
		if (numberOfBytesRead > 0) {
			content = new byte[(int) (contentLength - totalNumberOfBytesRead)
					% (content.length + 1)];
			totalNumberOfBytesRead += numberOfBytesRead = in.read(content);
		}
		// delete the end of the multipart part, which is
		// not part of the file
		int endOfFile = findEndOfFileInMultipart(content, numberOfBytesRead);
		fileOutput.write(content, 0, endOfFile + 1);
		return fileOutput;
	}

	/**
	 * Reads a line from DataInputStream. \r\n or \n is skipped and the line is
	 * trimmed.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String readLine(DataInputStream input) throws IOException {
		StringBuilder line = new StringBuilder();
		String currentByte;
		do {
			byte[] aByte = new byte[1];
			int numberOfBytesRead = input.read(aByte);
			if (numberOfBytesRead <= 0) {
				break;
			}
			currentByte = new String(aByte, "UTF-8");
			if (!currentByte.equals("\n") && !currentByte.equals("\r")) {
				line.append(currentByte);
			}
		} while (!currentByte.equals("\n"));
		return line.toString().trim();
	}

	/**
	 * Returns the index of the last char, which belongs to the file.
	 * 
	 * @param content
	 * @param numberOfBytesRead
	 * @return
	 */
	private int findEndOfFileInMultipart(byte[] content, int numberOfBytesRead) {
		int numberOfCarriageReturn = 0;
		int currentPos;
		// find the last char which belongs to the file
		for (currentPos = numberOfBytesRead - 1; currentPos > 0
				&& numberOfCarriageReturn < 2; currentPos--) {
			if (content[currentPos] == '\r'/* 13? */) {
				numberOfCarriageReturn++;
			}
		}
		return currentPos;
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
		for (int i = 0; currentParams != null && i < currentParams.length; i++) {
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
		InputStream reader = null;
		try {
			if (_socket.isConnected()) {
				sendHeader(out, 200, file.endsWith(".svg") ? "image/svg+xml"
						: file.endsWith(".png") ? "image/png" : "text/html",
						-1, System.currentTimeMillis());

				if (new File(file).isAbsolute()) {
					reader = new FileInputStream(file);
				} else {
					reader = getClass()
							.getResourceAsStream("resources/" + file);
				}
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = reader.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Sends the file <code>file</code> to the client. In the <code>file</code>
	 * /*?* / is replaced by <code>replaceText</code>.
	 * 
	 * @param out
	 *            the BufferedOutputStream to the client.
	 * @param file
	 *            the file which should be sent.
	 * @param replaceText
	 *            the sessionId
	 * @throws IOException
	 */
	private void sendFile(BufferedOutputStream out, String file,
			String replaceText) throws IOException {

		if (_socket.isConnected()) {
			sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(getClass()
						.getResourceAsStream("resources/" + file)));
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
			} finally {
				if (br != null) {
					br.close();
				}
			}
		}
	}

	/**
	 * Sends the resources/svgNavigation.svg and includes the generated svg
	 * <code>fileName</code> into it.
	 * 
	 * @param out
	 * @param fileName
	 *            the name of the generated svg
	 * @throws IOException
	 */
	private void sendSVG(BufferedOutputStream out, String fileName)
			throws IOException {
		sendHeader(out, 200, "image/svg+xml", -1, System.currentTimeMillis());
		if (_socket.isConnected()) {
			StringBuilder contentOfCreatedSVG = getContentOfCreatedSVG(fileName);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					getClass().getResourceAsStream(
							SVG_WITH_ZOOM_AND_MOVE_SUPPORT)));
			boolean isReplaced = false;
			String line;
			// send everything until <!-- -->
			// replace ?viewBox? by the viewBox value of the generated svg
			for (line = br.readLine(); line != null; line = br.readLine()) {
				if (line.contains("?viewBox?")) {
					line = line.replace("?viewBox?", viewBoxDimension);
				}
				if (!isReplaced && line.contains("<!--  -->")) {
					out.write(contentOfCreatedSVG.toString().getBytes());
					isReplaced = true;
					break;
				}
				out.write((line + "\n").getBytes());
			}
			// send rest of file
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

	// saves the viewBox content of the generated svg
	private String viewBoxDimension = "";

	/**
	 * Reads the content of the svg-tag of the generated svg file
	 * <code>fileName</code>.
	 * 
	 * @param out
	 * @param fileName
	 *            the name of the generated svg file
	 * @return {@link StringBuilder} the read svg file
	 * @throws IOException
	 */
	private StringBuilder getContentOfCreatedSVG(String fileName)
			throws IOException {
		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		// skip everything until the first <g
		// and read out the value of viewBox
		while ((line = br.readLine()) != null && !line.startsWith("<g ")) {
			if (line.contains("viewBox=\"")) {
				String subline = line.split(Pattern.quote("viewBox=\""))[1];
				viewBoxDimension = subline.substring(0, subline.indexOf("\""));
			}
		}
		if (line != null) {
			out.append(line + "\n");
		}
		// send content until </svg>
		while (line != null && !line.startsWith("</svg>")) {
			line = br.readLine();
			if (line != null && !line.startsWith("</svg>")) {
				out.append(line + "\n");
			}
		}
		br.close();
		return out;
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
