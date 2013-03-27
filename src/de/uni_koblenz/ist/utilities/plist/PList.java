/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.ist.utilities.plist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class PList {
	static DateFormat dateFormat;

	static {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private PListDict dict;
	private String filename;

	public PList() {
		dict = new PListDict();
	}

	public PList(String filename) throws PListException {
		this.filename = filename;
		try {
			dict = loadFrom(filename);
		} catch (PListException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				dict = new PListDict();
			} else {
				throw e;
			}
		}
	}

	public void store() throws PListException {
		storeTo(filename);
	}

	public void store(boolean makeBackup) throws PListException {
		storeTo(filename, makeBackup);
	}

	public PListDict loadFrom(String filename) throws PListException {
		try {
			PListProcessor parser = new PListProcessor();
			parser.process(filename);
			return parser.getDict();
		} catch (Exception e) {
			throw new PListException("Error reading property list '" + filename
					+ "'", e);
		}
	}

	public void storeTo(String filename) throws PListException {
		storeTo(new File(filename), false);
	}

	public void storeTo(String filename, boolean makeBackup)
			throws PListException {
		storeTo(new File(filename), makeBackup);
	}

	public void storeTo(File file, boolean makeBackup) throws PListException {
		XMLStreamWriter writer = null;
		try {
			if (makeBackup && file.exists()) {
				File backupFile = new File(file.getCanonicalPath() + "~");
				if (backupFile.exists()) {
					if (!backupFile.delete()) {
						throw new PListException("Can't delete old backup "
								+ backupFile.getCanonicalPath());
					}
				}
				if (!file.renameTo(backupFile)) {
					throw new PListException("Can't rename "
							+ file.getCanonicalPath() + " to "
							+ backupFile.getCanonicalPath());
				}
			}
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
					new BufferedOutputStream(new FileOutputStream(file)),
					"UTF-8");
			print(writer);
			writer.close();
		} catch (IOException e) {
			throw new PListException("Error writing property list '" + filename
					+ "'", e);
		} catch (XMLStreamException e) {
			throw new PListException("Error writing property list '" + filename
					+ "'", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
					throw new PListException("Error writing property list '"
							+ filename + "'", e);
				}
			}
		}
	}

	public void print(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument();
		writer.writeDTD("\n<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
		writer.writeStartElement("plist");
		writer.writeAttribute("version", "1.0");
		print(writer, dict);
		writer.writeEndElement();
		writer.writeEndDocument();
	}

	private void print(XMLStreamWriter writer, Object o)
			throws XMLStreamException {
		if (o instanceof PListDict) {
			Set<Map.Entry<String, Object>> entries = ((PListDict) (o))
					.entrySet();
			if (entries.size() == 0) {
				writer.writeEmptyElement("dict");
			} else {
				writer.writeStartElement("dict");
				for (Map.Entry<String, Object> entry : entries) {
					writer.writeStartElement("key");
					writer.writeCharacters(entry.getKey());
					writer.writeEndElement();
					print(writer, entry.getValue());
				}
				writer.writeEndElement();
			}
		} else if (o instanceof List) {
			List<?> l = (List<?>) o;
			if (l.size() == 0) {
				writer.writeEmptyElement("array");
			} else {
				writer.writeStartElement("array");
				for (Iterator<?> iter = l.iterator(); iter.hasNext();) {
					Object element = iter.next();
					print(writer, element);
				}
				writer.writeEndElement();
			}
		} else if (o instanceof Integer) {
			writer.writeStartElement("integer");
			writer.writeCharacters(o.toString());
			writer.writeEndElement();
		} else if (o instanceof String) {
			writer.writeStartElement("string");
			writer.writeCharacters(o.toString());
			writer.writeEndElement();
		} else if (o instanceof Double) {
			writer.writeStartElement("real");
			writer.writeCharacters(o.toString());
			writer.writeEndElement();
		} else if (o instanceof Boolean) {
			writer.writeEmptyElement(o.toString());
		} else if (o instanceof Date) {
			String s;
			synchronized (dateFormat) {
				s = dateFormat.format((Date) o);
			}
			writer.writeStartElement("date");
			writer.writeCharacters(s);
			writer.writeEndElement();
		}
	}

	public PListDict getDict() {
		return dict;
	}
}
