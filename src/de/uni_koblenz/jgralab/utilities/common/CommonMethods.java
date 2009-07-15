package de.uni_koblenz.jgralab.utilities.common;

public class CommonMethods {

	/**
	 * Generates a URI from a qualified Name for using in XML-files. It replaces
	 * all occurrences of "_" with "-", swaps the first and the second element of
	 * the qualified name and appends the remainder as folder structure.
	 * Example: de.uni_koblenz.jgralab.greql2 => uni-koblenz.de/jgralab/greql2
	 * 
	 * @param qualifiedName
	 *            the qualified name to convert.
	 * @return a URI as String according to the given qualified Name.
	 */
	public static String generateURI(String qualifiedName) {
		qualifiedName = qualifiedName.replace('_', '-');
		String[] uri = qualifiedName.split("\\.");

		String namespaceURI = "http://";
		if (uri.length > 1) {
			namespaceURI += uri[1] + "." + uri[0];

			for (int i = 2; i < uri.length; i++) {
				namespaceURI += "/" + uri[i];
			}

		}

		return namespaceURI;
	}

}
