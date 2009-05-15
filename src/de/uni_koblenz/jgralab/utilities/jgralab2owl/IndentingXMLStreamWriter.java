package de.uni_koblenz.jgralab.utilities.jgralab2owl;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class IndentingXMLStreamWriter implements XMLStreamWriter {

	/**
	 * the current indentation level
	 */
	int indentationLevel = 0;

	/**
	 * the spaces to insert for each indentation level
	 */
	String indentation = "";

	/**
	 * the {@link XMLStreamWriter} to be delegated to
	 */
	XMLStreamWriter writer;

	/**
	 * 
	 * @param writer
	 *            the {@link XMLStreamWriter} to be delegated to
	 * @param numberSpaces
	 *            the number of spaces to insert for each indentation level;
	 */
	public IndentingXMLStreamWriter(XMLStreamWriter writer, int numberSpaces) {
		this.writer = writer;

		for (int i = 0; i < numberSpaces; i++) {
			indentation = indentation.concat(" ");
		}
	}

	@Override
	public void close() throws XMLStreamException {
		writer.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		writer.flush();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return writer.getPrefix(uri);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return writer.getProperty(name);
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		writer.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
		writer.setNamespaceContext(context);
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		writer.setPrefix(prefix, uri);
	}

	@Override
	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
		writer.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
		writer.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		writeIndentation();
		writer.writeCData(data);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		writeIndentation();
		writer.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		writeIndentation();
		writer.writeCharacters(text, start, len);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		writeIndentation();
		writer.writeComment(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		writeIndentation();
		writer.writeDTD(dtd);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
		writer.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		writeIndentation();
		writer.writeEmptyElement(localName);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		writeIndentation();
		writer.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		writeIndentation();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		indentationLevel--;
		writeIndentation();
		
		writer.writeEndElement();
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		writeIndentation();
		writer.writeEntityRef(name);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
		writer.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		writeIndentation();
		writer.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		writeIndentation();
		writer.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writer.writeStartDocument(version);
	}

	@Override
	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		writer.writeStartDocument(encoding, version);
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		writeIndentation();
		writer.writeStartElement(localName);
		indentationLevel++;
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		writeIndentation();
		writer.writeStartElement(namespaceURI, localName);
		indentationLevel++;
	}

	@Override
	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		writeIndentation();
		writer.writeStartElement(prefix, localName, namespaceURI);
		indentationLevel++;
	}

	/**
	 * Writes indenting spaces.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeIndentation() throws XMLStreamException {
		writer.writeCharacters("\n");
		
		for (int i = 0; i < indentationLevel; i++) {
			writer.writeCharacters(indentation);
		}
	}
}
