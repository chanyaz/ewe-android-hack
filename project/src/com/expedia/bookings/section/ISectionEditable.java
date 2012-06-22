package com.expedia.bookings.section;

public interface ISectionEditable {
	public interface SectionChangeListener {
		public void onChange();
	}

	public boolean hasValidInput();

	public void addChangeListener(SectionChangeListener listener);

	public void removeChangeListener(SectionChangeListener listener);

	public void clearChangeListeners();
}
