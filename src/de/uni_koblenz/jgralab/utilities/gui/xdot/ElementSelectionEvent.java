package de.uni_koblenz.jgralab.utilities.gui.xdot;

import java.awt.Component;
import java.awt.event.MouseEvent;

import de.uni_koblenz.jgralab.AttributedElement;

public class ElementSelectionEvent extends MouseEvent {
	private static final long serialVersionUID = 7192584515095001000L;

	private final AttributedElement<?, ?> element;

	public ElementSelectionEvent(AttributedElement<?, ?> element, MouseEvent e) {
		super((Component) e.getSource(), e.getID(), e.getWhen(), e
				.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e
				.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e
				.getButton());
		this.element = element;
	}

	public AttributedElement<?, ?> getElement() {
		return element;
	}
}
