package com.expedia.bookings.section;

public interface ISectionEditable {
	public interface SectionChangeListener {
		public void onChange();
	}

	/**
	 * Perform validation on the fields of this ISectionEditable. This will
	 * fire exclamation marks if configured and invalid.
	 *
	 * @return whether or not this SectionEditable is valid
	 */
	public boolean performValidation();

	public void resetValidation();

	public void addChangeListener(SectionChangeListener listener);

	public void removeChangeListener(SectionChangeListener listener);

	public void clearChangeListeners();

	public void onChange();
}
