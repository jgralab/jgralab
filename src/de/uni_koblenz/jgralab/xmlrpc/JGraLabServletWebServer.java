/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.xmlrpc;

import org.apache.xmlrpc.webserver.ServletWebServer;

/**
 * The {@code main} method of this class starts the JGraLab XML-RPC server.
 * 
 * @author ist@uni-koblenz.de
 */
public class JGraLabServletWebServer {

	/**
	 * the port for accessing the JGraLab XML-RPC Server
	 */
	private static final int port = 12345;

	/**
	 * Starts the JGraLab XML-RPC server.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("======================");
		System.out.println("JGraLab XML-RPC Server");
		System.out.println("======================");
		JGraLabServlet servlet = new JGraLabServlet();
		ServletWebServer webServer = new ServletWebServer(servlet, port);
		System.out.println("Listening on port " + port);
		webServer.start();
	}

}
