package com.expedia.bookings.section;

public interface ISectionEditable {
	interface SectionChangeListener {
		void onChange();
	}

	/**
	 * Perform validation on the fields of this ISectionEditable. This will
	 * fire exclamation marks if configured and invalid.
	 *
	 * @return whether or not this SectionEditable is valid
	 */
	boolean performValidation();

	void resetValidation();

	void addChangeListener(SectionChangeListener listener);

	void removeChangeListener(SectionChangeListener listener);

	void clearChangeListeners();

	void onChange();
}
