package de.uni_koblenz.jgralab.utilities.xml;

/**
 * This class contains all constants used by {@link SchemaGraph2XMI}. If a
 * constant was already defined in
 * {@link de.uni_koblenz.jgralab.utilities.rsa.XMIConstants}, it is referred
 * to it.
 * 
 * @author ist@uni-koblenz.de
 */
public class XMIConstants4SchemaGraph2XMI {

	// xml-header information
	static final String XML_VERSION = "1.0";
	static final String XML_ENCODING = "UTF-8";

	// namespaces
	final static String NAMESPACE_XMI = "http://schema.omg.org/spec/XMI/2.1";
	final static String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	final static String NAMESPACE_EECORE = "http://www.eclipse.org/uml2/schemas/Ecore/5";
	final static String NAMESPACE_ECORE = "http://www.eclipse.org/emf/2002/Ecore";
	final static String NAMESPACE_UML = "http://schema.omg.org/spec/UML/2.1.1";

	// schmalocation
	final static String SCHEMALOCATION = "http://www.eclipse.org/uml2/schemas/Ecore/5 pathmap://UML_PROFILES/Ecore.profile.uml#_z1OFcHjqEdy8S4Cr8Rc_NA http://schema.omg.org/spec/UML/2.1.1 http://www.eclipse.org/uml2/2.1.0/UML";

	// namespace prefixes
	static final String NAMESPACE_PREFIX_XMI = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.XMI_NAMESPACE_PREFIX;
	static final String NAMESPACE_PREFIX_UML = "uml";
	static final String NAMESPACE_PREFIX_ECORE = "ecore";
	static final String NAMESPACE_PREFIX_EECORE = "Ecore";
	static final String NAMESPACE_PREFIX_XSI = "xsi";

	// elements defined by the uml namespace
	static final String UML_TAG_MODEL = "Model";

	// elements defined by the xmi namespace
	static final String XMI_ATTRIBUTE_ID = "id";
	static final String XSI_ATTRIBUTE_SCHEMALOCATION = "schemaLocation";
	static final String XMI_ATTRIBUTE_VERSION_VALUE = "2.1";
	static final String XMI_ATTRIBUTE_VERSION = "version";
	static final String XMI_ATTRIBUTE_TYPE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.XMI_TYPE;
	static final String XMI_TAG_XMI = "XMI";

	// comment specific constants
	static final String COMMENT_END = "\r\n</p>";
	static final String COMMENT_NEWLINE = "\r\n</p>\r\n<p>\r\n\t";
	static final String COMMENT_START = "<p>\r\n\t";
	static final String OWNEDCOMMENT_ATTRIBUTE_ANNOTATEDELEMENT = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ANNOTATED_ELEMENT;
	static final String OWNEDCOMMENT_TYPE_VALUE = "uml:Comment";
	static final String TAG_OWNEDCOMMENT = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OWNED_COMMENT;

	// constraint specific constants
	static final String TAG_LANGUAGE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_LANGUAGE;
	static final String TAG_SPECIFICATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_SPECIFICATION;
	static final String OWNEDRULE_ATTRIBUTE_CONSTRAINEDELEMENT = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_CONSTRAINED_ELEMENT;
	static final String OWNEDRULE_TYPE_VALUE = "uml:Constraint";
	static final String TAG_OWNEDRULE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OWNEDRULE;

	// uml element specific constants
	static final String PACKAGEDELEMENT_TYPE_VALUE_CLASS = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_CLASS;
	static final String PACKAGEDELEMENT_TYPE_VALUE_PACKAGE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_PACKAGE;
	static final String PACKAGEDELEMENT_TYPE_VALUE_ASSOCIATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ASSOCIATION;
	static final String PACKAGEDELEMENT_TYPE_VALUE_ASSOCIATIONCLASS = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ASSOCIATION_CLASS;
	static final String TAG_PACKAGEDELEMENT = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_PACKAGED_ELEMENT;
	static final String PACKAGEDELEMENT_ATTRIBUTE_ISABSTRACT = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_IS_ABSRACT;
	static final String PACKAGEDELEMENT_ATTRIBUTE_MEMBEREND = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_MEMBER_END;
	static final String PACKAGEDELEMENT_ATTRIBUTE_TYPE = "type";
	static final String PACKAGEDELEMENT_ATTRIBUTE_ASSOCIATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_ASSOCIATION;
	static final String TAG_UPPERVALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_UPPER_VALUE;
	static final String TAG_LOWERVALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_LOWER_VALUE;
	static final String OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_AGGREGATION;
	static final String OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION_VALUE_SHARED = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_SHARED;
	static final String OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION_VALUE_COMPOSITE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_COMPOSITE;
	static final String TAG_OWNEDEND = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OWNEDEND;

	// generalization specific constants
	static final String TAG_GENERALIZATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_GENERALIZATION;
	static final String GENERALIZATION_TYPE_VALUE = "uml:Generalization";
	static final String GENERALIZATION_ATTRIBUTE_GENERAL = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_GENERAL;

	// attribute specific constants
	static final String TYPE_HREF_VALUE_STRING = NAMESPACE_UML
			+ "/uml.xml#String";
	static final String TYPE_HREF_VALUE_INTEGER = NAMESPACE_UML
			+ "/uml.xml#Integer";
	static final String TYPE_HREF_VALUE_BOOLEAN = NAMESPACE_UML
			+ "/uml.xml#Boolean";
	static final String TAG_DEFAULTVALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_DEFAULT_VALUE;
	static final String OWNEDATTRIBUTE_VISIBILITY_VALUE_PRIVATE = "private";
	static final String OWNEDATTRIBUTE_ATTRIBUTE_VISIBILITY = "visibility";
	static final String OWNEDATTRIBUTE_TYPE_VALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_PROPERTY;
	static final String TAG_OWNEDATTRIBUTE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OWNED_ATTRIBUTE;
	static final String DEFAULTVALUE_ATTRIBUTE_INSTANCE = "instance";

	// extension specific constants
	static final String REFERENCES_HREF_VALUE = NAMESPACE_UML
			+ "/StandardProfileL2.xmi#_yzU58YinEdqtvbnfB2L_5w";
	static final String REFERENCES_TYPE_VALUE = "ecore:EPackage";
	static final String TAG_REFERENCES = "references";
	static final String DETAILS_ATTRIBUTE_KEY = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_KEY;
	static final String DETAILS_ATTRIBUTE_TYPE_VALUE = "ecore:EStringToStringMapEntry";
	static final String TAG_DETAILS = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_DETAILS;
	static final String EANNOTATIONS_ATTRIBUTE_SOURCE_VALUE = "http://www.eclipse.org/uml2/2.0.0/UML";
	static final String EANNOTATIONS_ATTRIBUTE_SOURCE = "source";
	static final String EANNOTATIONS_TYPE_VALUE = "ecore:EAnnotation";
	static final String TAG_EANNOTATIONS = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_E_ANNOTATIONS;
	static final String ATTRIBUTE_EXTENDER = "extender";
	static final String XMI_TAG_EXTENSION = "Extension";

	// expression types
	static final String TYPE_VALUE_OPAQUEEXPRESSION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OPAQUE_EXPRESSION;
	static final String TYPE_VALUE_LITERALINTEGER = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_LITERAL_INTEGER;
	static final String TYPE_VALUE_LITERALBOOLEAN = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_LITERAL_BOOLEAN;
	static final String TYPE_VALUE_INSTANCEVALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_INSTANCE_VALUE;
	static final String TYPE_VALUE_LITERALUNLIMITEDNATURAL = "uml:LiteralUnlimitedNatural";

	// enumeration specific constants
	static final String PACKAGEDELEMENT_TYPE_VALUE_ENUMERATION = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ENUMERATION;
	static final String TAG_OWNEDLITERAL = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_OWNED_LITERAL;
	static final String OWNEDLITERAL_TYPE_VALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ENUMERATION_LITERAL;
	static final String OWNEDLITERAL_ATTRIBUTE_CLASSIFIER = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_CLASSIFIER;

	// primitive types specific constants
	static final String PACKAGE_PRIMITIVETYPES_NAME = "PrimitiveTypes";
	static final String TYPE_VALUE_PRIMITIVETYPE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_PRIMITIVE_TYPE;

	// profileApplication specific constants
	static final String APPLIEDPROFILE_HREF_VALUE = NAMESPACE_UML
			+ "/StandardProfileL2.xmi#_0";
	static final String APPLIEDPROFILE_TYPE_VALUE = "uml:Profile";
	static final String TAG_APPLIEDPROFILE = "appliedProfile";
	static final String PROFILEAPPLICATION_TYPE_VALUE = "uml:ProfileApplication";
	static final String TAG_PROFILEAPPLICATION = "profileApplication";
	static final String TAG_TYPE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_TYPE;

	// miscellaneous
	static final String TAG_BODY = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_BODY;
	static final String ATTRIBUTE_NAME = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_NAME;
	static final String ATTRIBUTE_HREF = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_HREF;
	static final String ATTRIBUTE_VALUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_ATTRIBUTE_VALUE;
	static final String ATTRIBUTE_VALUE_TRUE = de.uni_koblenz.jgralab.utilities.rsa.XMIConstants.UML_TRUE;

	// packageimport specific constants
	// static final String TAG_PACKAGEIMPORT = "packageImport";
	// static final String PACKAGEIMPORT_TYPE_VALUE = "uml:PackageImport";
	// static final String TAG_IMPORTEDPACKAGE = "importedPackage";
	// static final String IMPORTEDPACKAGE_TYPE_VALUE = "uml:Model";
	// static final String IMPORTEDPACKAGE_HREF_VALUE = NAMESPACE_UML
	// + "/uml.xml#_0";
	//
}
