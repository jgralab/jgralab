package de.uni_koblenz.jgralab;

import java.io.IOException;
import java.util.Map;

public interface Record {

	public Object getComponent(String name);
	
	public void setComponent(String name, Object component);
	
	public void setComponentValues(Map<String, Object> fields);
	
	public void setComponentValues(Object... fields);
	
	public void readComponentValues(GraphIO io) throws GraphIOException;
	
	public void writeComponentValues(GraphIO io) throws IOException, GraphIOException;
	
}
