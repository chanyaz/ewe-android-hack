package com.expedia.bookings.fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelTextSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelSectionExpandableText;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class HotelDetailsDescriptionFragment extends Fragment {

	public static HotelDetailsDescriptionFragment newInstance() {
		return new HotelDetailsDescriptionFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_description, container, false);
		populateViews(view);
		return view;
	}

	public void populateViews() {
		populateViews(getView());
	}

	private void populateViews(View view) {
		setupAmenities(view, Db.getHotelSearch().getSelectedProperty());
		// #4761 AB Test: Collapse Amenities, Policies, and fees on Infosite
		boolean isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCollapseAmenities);
		if (isUserBucketedForTest) {
			setupDescriptionWithCollapsableSections(view, Db.getHotelSearch().getSelectedProperty());
		}
		else {
			setupDescriptionSections(view, Db.getHotelSearch().getSelectedProperty());
		}
	}

	private void setupAmenities(View view, Property property) {
		if (property == null) {
			return;
		}

		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);
		disableOverScrollMode(amenitiesScrollView);

		ViewGroup amenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		amenitiesContainer.removeAllViews();
		LayoutUtils.addAmenities(getActivity(), property, amenitiesContainer);

		// Hide the text that indicated no amenities because there are amenities
		if (property.hasAmenities()) {
			view.findViewById(R.id.amenities_none_text).setVisibility(View.GONE);

			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
			view.findViewById(R.id.amenities_divider).setVisibility(View.VISIBLE);
		}
		else {
			//findViewById(R.id.amenities_none_text).setVisibility(View.VISIBLE);

			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.GONE);
			view.findViewById(R.id.amenities_divider).setVisibility(View.GONE);
		}

	}

	// Have to disable overscroll mode via reflection, since it's only in API 9+
	private void disableOverScrollMode(HorizontalScrollView view) {
		try {
			Field f = HorizontalScrollView.class.getField("OVER_SCROLL_NEVER");
			Method m = HorizontalScrollView.class.getMethod("setOverScrollMode", int.class);
			m.invoke(view, f.getInt(null));
		}
		catch (NoSuchFieldError e) {
			// Ignore; this will just happen pre-9
		}
		catch (NoSuchMethodException e) {
			// Ignore; this will just happen pre-9
		}
		catch (Exception e) {
			Log.w("Something went wrong trying to disable overscroll mode.", e);
		}
	}

	private void setupDescriptionSections(View view, Property property) {
		if (property == null) {
			return;
		}

		LinearLayout allSectionsContainer = Ui.findView(view, R.id.description_details_sections_container);
		allSectionsContainer.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText();

		if (sections != null && sections.size() > 1) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 1; i < sections.size(); i++) {
				HotelTextSection section = sections.get(i);
				View sectionContainer = inflater.inflate(R.layout.include_hotel_description_section,
						allSectionsContainer, false);

				TextView titleText = Ui.findView(sectionContainer, R.id.title_text);
				TextView bodyText = Ui.findView(sectionContainer, R.id.body_text);
				titleText.setVisibility(View.VISIBLE);
				titleText.setText(section.getNameWithoutHtml());
				bodyText.setText(HtmlCompat.fromHtml(section.getContentFormatted(getActivity())));
				allSectionsContainer.addView(sectionContainer);
			}
		}
	}

	private void setupDescriptionWithCollapsableSections(View view, Property property) {
		if (property == null) {
			return;
		}

		LinearLayout allSectionsContainer = Ui.findView(view, R.id.description_details_sections_container);
		allSectionsContainer.removeAllViews();

		List<HotelTextSection> sections = property.getAllHotelText();
		if (sections == null || sections.size() == 0) {
			return;
		}

		HotelSectionExpandableText hotelExpandableText;
		// We should preserve the order in which the texts are laid out. See property.getAllHotelText() for the order.
		// Adding hotel overview text section(s)
		if (property.getOverviewText() != null) {
			// Skipping 1st overviewText since it appears to be the same as intro (as is)
			for (int i = 1; i < property.getOverviewText().size(); i++) {
				HotelTextSection section = property.getOverviewText().get(i);
				hotelExpandableText = new HotelSectionExpandableText(getActivity());
				hotelExpandableText.setHotelSection(section);
				allSectionsContainer.addView(hotelExpandableText);
			}
		}

		// Adding hotel amenities text section
		if (property.getAmenitiesText() != null) {
				hotelExpandableText = new HotelSectionExpandableText(getActivity());
				hotelExpandableText.setHotelSection(property.getAmenitiesText());
				hotelExpandableText.setAlwaysCut(true);
				allSectionsContainer.addView(hotelExpandableText);
		}

		// Adding hotel policies text section
		if (property.getPoliciesText() != null) {
			hotelExpandableText = new HotelSectionExpandableText(getActivity());
			hotelExpandableText.setHotelSection(property.getPoliciesText());
			hotelExpandableText.showMinBulletPoints(2);
			allSectionsContainer.addView(hotelExpandableText);
		}

		// Adding hotel fees text section
		if (property.getFeesText() != null) {
			hotelExpandableText = new HotelSectionExpandableText(getActivity());
			hotelExpandableText.setHotelSection(property.getFeesText());
			hotelExpandableText.showMinBulletPoints(2);
			allSectionsContainer.addView(hotelExpandableText);
		}

		// Adding hotel mandatory fees text section
		if (property.getMandatoryFeesText() != null) {
			hotelExpandableText = new HotelSectionExpandableText(getActivity());
			hotelExpandableText.setHotelSection(property.getMandatoryFeesText());
			allSectionsContainer.addView(hotelExpandableText);
		}

		// Adding hotel renovation text section
		if (property.getRenovationText() != null) {
			hotelExpandableText = new HotelSectionExpandableText(getActivity());
			hotelExpandableText.setHotelSection(property.getRenovationText());
			allSectionsContainer.addView(hotelExpandableText);
		}
	}

}
