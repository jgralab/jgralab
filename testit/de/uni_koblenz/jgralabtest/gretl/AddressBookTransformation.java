package de.uni_koblenz.jgralabtest.gretl;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.gretl.Context;
import de.uni_koblenz.jgralab.gretl.CreateAttribute;
import de.uni_koblenz.jgralab.gretl.CreateAttribute.AttributeSpec;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass;
import de.uni_koblenz.jgralab.gretl.CreateEdgeClass.IncidenceClassSpec;
import de.uni_koblenz.jgralab.gretl.CreateVertexClass;
import de.uni_koblenz.jgralab.gretl.Transformation;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class AddressBookTransformation extends Transformation<Graph> {

	public AddressBookTransformation(Context context) {
		super(context);
	}

	@Override
	protected Graph transform() {
		// In the new graph we want only one AddressBook, but with multiple
		// categories. As archetye set we use the set containing only the number
		// 1.
		VertexClass addrBook = new CreateVertexClass(context, "AddressBook",
				"set(1)").execute();

		// For each source AddressBook, we now create a Category in the new
		// AddressBook.
		VertexClass category = new CreateVertexClass(context, "Category",
				"V{AddressBook}").execute();

		// Set the category name to the old address book's name.
		new CreateAttribute(context, new AttributeSpec(category, "name",
				getStringDomain()),
				"from ab : keySet(img_Category) reportMap ab -> ab.name end")
				.execute();

		// Now we connect the Categories with the single AddressBook. As
		// archetype set we use the source AddressBooks. The start vertex is
		// always the image of 1 (the new single AddressBook), the start
		// vertices are the images of the old AddressBooks. Those are the new
		// Categories.
		new CreateEdgeClass(context, "HasCategory", new IncidenceClassSpec(
				addrBook), new IncidenceClassSpec(category,
				AggregationKind.COMPOSITE),
				"from ab : V{AddressBook} reportSet ab, 1, ab end").execute();

		// Copy the Contact vertices, but refactor the old name attribute into
		// firstName and lastName by splitting at spaces.
		VertexClass contact = new CreateVertexClass(context, "Contact",
				"V{Contact}").execute();
		new CreateAttribute(context, new AttributeSpec(contact, "firstName",
				getStringDomain()),
				"from c : V{Contact} reportMap c -> split(c.name, \" \")[0] end")
				.execute();
		new CreateAttribute(context, new AttributeSpec(contact, "lastName",
				getStringDomain()),
				"from c : V{Contact} reportMap c -> split(c.name, \" \")[1] end")
				.execute();

		// Connect the Categories with the Contacts. This is achieved by copying
		// the old Contains edges over, but now the source vertices are
		// Categories, not AddressBooks.
		new CreateEdgeClass(context, "Contains", new IncidenceClassSpec(
				category), new IncidenceClassSpec(contact,
				AggregationKind.COMPOSITE),
				"from c : E{Contains} reportSet c, startVertex(c), endVertex(c) end")
				.execute();

		// Refactor the Contact.address attribute into a new Address vertex
		// class by splitting the 3 parts of an address at spaces.
		VertexClass address = new CreateVertexClass(context, "Address",
				"V{Contact}").execute();
		new CreateAttribute(context, new AttributeSpec(address, "city",
				getStringDomain()),
				"from c : V{Contact} reportMap c -> split(c.address, \",[ ]*\")[0] end")
				.execute();
		new CreateAttribute(context, new AttributeSpec(address, "state",
				getStringDomain()),
				"from c : V{Contact} reportMap c -> split(c.address, \",[ ]*\")[1] end")
				.execute();
		new CreateAttribute(context, new AttributeSpec(address, "country",
				getStringDomain()),
				"from c : V{Contact} reportMap c -> split(c.address, \",[ ]*\")[2] end")
				.execute();

		new CreateEdgeClass(context, "HasAddress", new IncidenceClassSpec(
				contact, 1, Integer.MAX_VALUE), new IncidenceClassSpec(address,
				1, Integer.MAX_VALUE),
				"from c : V{Contact} reportSet c, c, c end").execute();

		return context.getTargetGraph();
	}
}
