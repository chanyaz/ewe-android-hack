package com.expedia.bookings.section;

import java.util.AbstractList;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.view.View;


/**
 * This is a class for managing SectionField<?,?> instances within an ISection<?>
 *
 * @param <T> the type of data the SectionFields bind to.
 *            e.g. if the fields are of type SectionField<?,Cats> then <T> = <Cats>
 */
public class SectionFieldList<T> extends AbstractList<SectionField<?, T>> {

	private final ArrayList<SectionField<?, T>> mFields = new ArrayList<SectionField<?, T>>();

	@Override
	public SectionField<?, T> get(int location) {
		return mFields.get(location);
	}

	@Override
	public int size() {
		return mFields.size();
	}

	@Override
	public boolean add(SectionField<?, T> field) {
		return mFields.add(field);
	}

	/**
	 * Call bindData for each SectionField
	 *
	 * @param data
	 */
	public void bindDataAll(T data) {
		for (SectionField<?, T> field : mFields) {
			field.bindData(data);
		}
	}

	/**
	 * Call bindField for each SectionField
	 *
	 * @param parent
	 */
	public void bindFieldsAll(View parent) {

		if (!(parent instanceof ISection<?>)) {
			throw new RuntimeException("bindFieldsAll must be called with a parent view that implements ISection<T>");
		}

		for (SectionField<?, T> field : mFields) {
			field.bindField(parent);
		}
	}

	/**
	 * call isValid() on each SectionFieldEditable
	 *
	 * @return true if every field.isValid() returned true, otherwise false
	 */
	public boolean hasValidInput() {
		SectionFieldEditable<?, T> editable;
		boolean valid = true;
		for (SectionField<?, T> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, T>) field;
				boolean newIsValid = editable.isValid();
				valid = (valid && newIsValid);
			}
		}
		return valid;
	}

	public View firstInvalidInputField() {
		SectionFieldEditable<?, T> editable;
		for (SectionField<?, T> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, T>) field;
				boolean isValid = editable.isValid();
				if (!isValid) {
					return editable.getField();
				}
			}
		}
		return null;
	}

	public int getNumberOfInvalidFields() {
		SectionFieldEditable<?, T> editable;
		int numOfinvalidFields = 0;
		for (SectionField<?, T> field : mFields) {
			if (field instanceof SectionFieldEditable) {
				editable = (SectionFieldEditable<?, T>) field;
				if (!editable.isValid()) {
					numOfinvalidFields += 1;
				}
			}
		}
		return numOfinvalidFields;
	}
	/**
	 * call isValid() on a single SectionFieldEditable
	 *
	 * @return true if field.isValid() returned true, otherwise false
	 */
	public boolean hasValidInput(int fieldId) {
		SectionField<?, T> field = getLastFieldWithId(fieldId);
		boolean valid = true;
		if (field != null && field instanceof SectionFieldEditable) {
			SectionFieldEditable<?, T> editable = (SectionFieldEditable<?, T>) field;
			valid = editable.isValid();
		}
		return valid;
	}

	public void setValidationIndicatorState(boolean valid) {
		for (SectionField<?, T> field : mFields) {
			if (field instanceof SectionFieldValidIndicator && field.hasBoundField()) {
				((SectionFieldValidIndicator) field)
					.onPostValidate(((SectionFieldValidIndicator) field).getField(), valid, true);
			}
		}
	}

	public void setValidationIndicatorState(int currentFieldId, boolean valid) {
		for (SectionField<?, T> field : mFields) {
			if ((field instanceof SectionFieldValidIndicator && field.hasBoundField()
				&& field.getFieldId() == currentFieldId)) {
				((SectionFieldValidIndicator) field)
					.onPostValidate(((SectionFieldValidIndicator) field).getField(), valid, true);
			}
		}
	}

	/**
	 * Remove a field via fieldId
	 *
	 * @param fieldId
	 */
	public void removeField(int fieldId) {
		SectionField<?, T> removalField = null;
		for (SectionField<?, T> field : mFields) {
			if (field.hasBoundField() && field.getField().getId() == fieldId) {
				removalField = field;
				break;
			}
		}
		if (removalField != null) {
			removeField(removalField);
		}
	}

	/**
	 * Find a field by resource id
	 * There may be more than one so this method returns the last field found
	 *
	 * @param fieldId
	 */
	public SectionField<?, T> getLastFieldWithId(int fieldId) {
		SectionField foundField = null;
		for (SectionField<?, T> field : mFields) {
			if (field.hasBoundField() && field.getField().getId() == fieldId) {
				foundField = field;
			}
		}
		return foundField;
	}

	/**
	 * Remove field from layout by setting visibility to GONE
	 * Remove field from field list so it is no longer validated against
	 * Fix focus order if we removed a view that someone has set as nextFocus
	 *
	 * @param sectionFieldForRemoval
	 */
	@SuppressLint("NewApi")
	public void removeField(SectionField<?, T> sectionFieldForRemoval) {
		//Remove from fields list
		mFields.remove(sectionFieldForRemoval);

		if (sectionFieldForRemoval.hasBoundField()) {
			View removeView = sectionFieldForRemoval.getField();
			int removeViewId = removeView.getId();

			//Fix focus order
			for (SectionField<?, T> sectionField : mFields) {
				if (sectionField.hasBoundField()) {
					View view = sectionField.getField();
					if (view.getNextFocusDownId() == removeViewId) {
						view.setNextFocusDownId(removeView.getNextFocusDownId());
					}
					if (view.getNextFocusUpId() == removeViewId) {
						view.setNextFocusUpId(removeView.getNextFocusUpId());
					}
					if (view.getNextFocusLeftId() == removeViewId) {
						view.setNextFocusLeftId(removeView.getNextFocusLeftId());
					}
					if (view.getNextFocusRightId() == removeViewId) {
						view.setNextFocusRightId(removeView.getNextFocusRightId());
					}
					if (view.getNextFocusForwardId() == removeViewId) {
						view.setNextFocusForwardId(removeView.getNextFocusForwardId());
					}
				}
			}

			//Hide view
			removeView.setVisibility(View.GONE);
		}
	}

	/**
	 * Setting a field to being enabled means: VISIBLE and VALIDATED AGAINST
	 * Setting a afield to being dispabled means: GONE and NOT VALIDATED AGAINST
	 *
	 * @param sectionField
	 * @param enabled
	 */
	public void setFieldEnabled(SectionField<?, T> sectionField, boolean enabled) {
		if (enabled) {
			if (!mFields.contains(sectionField)) {
				mFields.add(sectionField);
			}
			if (sectionField.hasBoundField()) {
				sectionField.getField().setVisibility(View.VISIBLE);
			}
		}
		else {
			mFields.remove(sectionField);
			if (sectionField.hasBoundField()) {
				sectionField.getField().setVisibility(View.GONE);
			}
		}
	}
}
