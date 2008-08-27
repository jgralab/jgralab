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

import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

/**
 * The XML-RPC servlet for the JGraLab facade
 *
 */
public class JGraLabServlet extends XmlRpcServlet {

	private static final long serialVersionUID = 5123366544712154075L;

	/**
	 * Creates an {@code XmlRpcHandlerMapping} on basis of the contents of the
	 * {@code properties} file. These contents say that remote procedure calls
	 * preceded by {@code jgralab} shall be directed to {@code JGraLabFacade}.
	 */
	protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
		URL url;
		XmlRpcHandlerMapping mapping = null;
		
		try {
			url = new URL("file", "localhost", "src/de/uni_koblenz/jgralab/xmlrpc/properties");
		
			mapping = newPropertyHandlerMapping(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mapping;
	}
}
