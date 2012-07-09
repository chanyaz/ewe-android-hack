package com.expedia.bookings.fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.DbPropertyHelper;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class HotelDetailsDescriptionFragment extends Fragment {

	public static HotelDetailsDescriptionFragment newInstance() {
		return new HotelDetailsDescriptionFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hotel_details_description, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		populateViews();
	}

	public void populateViews() {
		View view = getView();
		setupAmenities(view, DbPropertyHelper.getBestAmenityProperty());
		setupDescriptionSections(view, DbPropertyHelper.getBestDescriptionProperty());
	}

	private void setupAmenities(View view, Property property) {
		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);

		// Have to disable overscroll mode via reflection, since it's only in API 9+
		try {
			Field f = HorizontalScrollView.class.getField("OVER_SCROLL_NEVER");
			Method m = HorizontalScrollView.class.getMethod("setOverScrollMode", int.class);
			m.invoke(amenitiesScrollView, f.getInt(null));
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

	private void setupDescriptionSections(View view, Property property) {
		LinearLayout allSectionsContainer = Ui.findView(view, R.id.description_details_sections_container);
		allSectionsContainer.removeAllViews();

		String unparsedDescriptionText = property.getDescriptionText();
		HotelDescription.SectionStrings.initSectionStrings(getActivity());
		HotelDescription hotelDescription = new HotelDescription(getActivity());
		hotelDescription.parseDescription(unparsedDescriptionText);
		List<DescriptionSection> sections = hotelDescription.getSections();

		if (sections != null && sections.size() > 1) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 1; i < sections.size(); i++) {
				DescriptionSection section = sections.get(i);
				View sectionContainer = inflater.inflate(R.layout.include_hotel_description_section,
						allSectionsContainer, false);

				TextView titleText = Ui.findView(sectionContainer, R.id.title_text);
				TextView bodyText = Ui.findView(sectionContainer, R.id.body_text);
				titleText.setVisibility(View.VISIBLE);
				titleText.setText(section.title);
				bodyText.setText(Html.fromHtml(section.description));
				allSectionsContainer.addView(sectionContainer);
			}
		}
	}

}
