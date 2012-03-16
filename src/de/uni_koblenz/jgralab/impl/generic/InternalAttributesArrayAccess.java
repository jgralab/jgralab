package de.uni_koblenz.jgralab.impl.generic;

import de.uni_koblenz.jgralab.AttributedElement;

/**
 * Allows for accessing the internal attributes array in terms of
 * {@link OnAttributesFunction} objects. Used by FunnyQT's in-place schema
 * modifying transformations.
 *
 * @author ist@uni-koblenz.de
 */
public interface InternalAttributesArrayAccess {

	/**
	 * Represents a function that receives the an {@link AttributedElement} with
	 * its internal attributes array, acts on it, and returns a new or modified
	 * array which is then set as the element's attributes array.
	 */
	interface OnAttributesFunction {
		/**
		 * Invokes this {@link OnAttributesFunction}
		 *
		 * @param ae
		 *            an {@link AttributedElement}
		 * @param attributes
		 *            <code>ae</code>'s internal attributes array
		 * @return a possibly modified or new array replacing the original
		 *         <code>attributes</code> array that was passed in
		 */
		Object[] invoke(AttributedElement<?, ?> ae, Object[] attributes);
	}

	/**
	 * Invokes the given {@link OnAttributesFunction} on the attributed
	 * element's attributes array, and sets that to the return value of the
	 * function.
	 *
	 * @param fn
	 *            an {@link OnAttributesFunction}
	 */
	void invokeOnAttributesArray(OnAttributesFunction fn);
}
