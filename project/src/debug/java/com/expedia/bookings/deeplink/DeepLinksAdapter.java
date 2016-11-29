package com.expedia.bookings.deeplink;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.data.abacus.AbacusUtils;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeepLinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_SECTION_HEADER = 0;
	private static final int TYPE_LINK = 1;
	private static final int TYPE_LINK_WITH_PACKAGE = 2;
	private static final int TYPE_LINK_CUSTOM = 3;
	private static final int TYPE_LINK_WITH_AB_TESTS = 4;

	private static final DeepLink[] DEEP_LINKS = new DeepLink[] {
		new DeepLinkSection("Hotels", R.color.hotels_primary_color),
		new DeepLink("Ewa Beach, Hawaii", "expda://hotelSearch?location=Ewa+Beach%2C+Hawaii%2C+United+States+of+America&seocid=Google"),
		new DeepLink("Las Vegas, NV", "expda://hotelSearch?location=Las%20Vegas,%20NV"),
		new DeepLink("New York, NY", "expda://hotelSearch?location=New%20York%2C%20NY"),
		new DeepLink("Portland, Maine", "expda://hotelSearch?location=Portland,%20Maine"),
		new DeepLink("Region (Arc de Triomphe)", "expda://hotelSearch?location=Arc%20de%20Triomphe%20-%20Palais%20des%20Congres%20(17%20arr.)%2C%20Paris%2C%20France"),
		new DeepLink("Region (ID800103)", "expda://hotelSearch?location=ID800103"),
		new DeepLink("Attraction (Academy of Paris)", "expda://hotelSearch?location=Academy%20of%20Paris%2C%20Paris%2C%20France"),
		new DeepLink("Airport (BVA)", "expda://hotelSearch?location=Paris%2C%20France%20(BVA-Beauvais)"),
		new DeepLink("Intent", "intent://hotelSearch?location=Orlando,%20FL/#Intent;package=com.expedia.bookings;scheme=expda;end"),
		new DeepLink("Current Location Search", "expda://hotelSearch"),
		new DeepLink("Hotel ID 11562190", "expda://hotelSearch?hotelId=11562190&seocid=Google"),
		new DeepLink("Hotel ID 9046290", "expda://hotelSearch/?hotelId=9046290"),
		new DeepLink("Hotel ID 1819759", "expda://hotelSearch/?hotelId=1819759&cid=SEO.Google"),
		new DeepLink("Future Dates", "expda://hotelSearch?hotelId=12539", "checkInDate", 14, "checkOutDate", 18),
		new DeepLink("Out-dated Search", "expda://hotelSearch?location=San%20Diego,%20CA", "checkInDate", -365, "checkOutDate", -360),
		new DeepLink("Check Out Only", "expda://hotelSearch?location=Key%20West,%20FL", "checkOutDate", 10),
		new DeepLink("Check In Only", "expda://hotelSearch?location=Key%20West,%20FL", "checkInDate", 7),
		new DeepLink("28+ Day Stay", "expda://hotelSearch?location=Austin,%20TX", "checkInDate", 10, "checkOutDate", 40),
		new DeepLink("Check Out Before Check In", "expda://hotelSearch?location=Nashville,%20TN", "checkInDate", 10, "checkOutDate", 7),
		new DeepLink("Dates and 3 Guests", "expda://hotelSearch?location=Baltimore,%20MD&numAdults=3", "checkInDate", 7, "checkOutDate", 10),
		new DeepLink("2 Adults", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2"),
		new DeepLink("2 Adults, 1 Child", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2&childAges=2"),
		new DeepLink("2 Adults, 1 Child, 1 Infant", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2&childAges=2,1"),
		new DeepLink("1 Adult, 2 Infants", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=1&childAges=1,1"),
		new DeepLink("Deposits v1 Hotel", "expda://hotelSearch/?hotelId=18000", "checkInDate", 8, "checkOutDate", 10),
		new DeepLink("Sold Out (maybe)", "expda://hotelSearch?hotelId=4183598&cid=SEO.Google"),
		new DeepLink("Sold Out complex (maybe)", "expda://hotelSearch?hotelId=4183598&numAdults=3&childAges=3,2&cid=SEO.Google", "checkInDate", 1, "checkOutDate", 2),

		new DeepLinkSection("Flights", R.color.flights_lob_btn),
		new DeepLink("Flight Search", "expda://flightSearch"),
		new DeepLink("Flight Search (Round trip)", "expda://flightSearch?origin=SFO&destination=SEA&departureDate=2017-02-01&returnDate=2017-02-03&numAdults=1"),
		new DeepLink("Flight Search (One Way)", "expda://flightSearch?origin=SFO&destination=SEA&departureDate=2017-02-01&numAdults=1"),
		new DeepLink("Flight Search (3 adults, roundTrip)", "expda://flightSearch?origin=SFO&destination=SEA&departureDate=2017-02-01&returnDate=2017-02-03&numAdults=3"),
		new DeepLink("Flight Travel Guide", "expda://flightSearch?destination=ATH&seocid=Google"),


		new DeepLinkSection("Activities", R.color.lx_primary_color),
		new DeepLink("Activity Search", "expda://activitySearch"),
		new DeepLink("San Francisco", "expda://activitySearch?location=San%20Francisco", "startDate", 3),

		new DeepLinkSection("Cars", R.color.cars_primary_color),
		new DeepLink("Car Search", "expda://carSearch"),

		new DeepLinkSection("Deferred", R.color.launch_screen_primary),
		new DeepLink("Parc 55 San Francisco, a Hilton Hotel",
			"https://169006.measurementapi.com/serve?action=click&publisher_id=169006&site_id=107678&invoke_url=expda%3A%2F%2FhotelSearch%3FhotelId%3D12539"),

		new DeepLinkSection("Other", R.color.gt_primary_color),
		new DeepLink("Home", "expda://home"),
		new DeepLink("Sign In", "expda://signIn"),
		new DeepLink("Unsupported URL Scheme", "george://noworky"),
		new DeepLink("Request Push Permissions", "expda://requestNotificationPermission"),
		new DeepLink("Destination (Muzei Plugin)", "expda://destination/?displayName=Orlando,+FL&searchType=CITY&hotelId=178294&airportCode=ORL&regionId=178294&latitude=28.541290&longitude=-81.379040&imageCode=fun-orlando"),
		new DeepLink("Support Email", "expda://supportEmail"),
		new DeepLinkWithABTests("Force Bucket", "expda://forceBucket?key={}&value={}"),
		new DeepLinkWithPackage("Empty Data", ""),
		new DeepLinkCustom(),
	};

	@Override
	public int getItemViewType(int position) {
		if (DEEP_LINKS[position] instanceof DeepLinkSection) {
			return TYPE_SECTION_HEADER;
		}
		else if (DEEP_LINKS[position] instanceof DeepLinkWithPackage) {
			return TYPE_LINK_WITH_PACKAGE;
		}
		else if (DEEP_LINKS[position] instanceof DeepLinkWithABTests) {
			return TYPE_LINK_WITH_AB_TESTS;
		}
		else if (DEEP_LINKS[position] instanceof DeepLinkCustom) {
			return TYPE_LINK_CUSTOM;
		}
		else {
			return TYPE_LINK;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_SECTION_HEADER) {
			return new HeaderViewHolder(inflate(R.layout.row_deep_link_test_header, parent));
		}
		else if (viewType == TYPE_LINK_WITH_PACKAGE) {
			return new DeepLinkWithPackageViewHolder(inflate(R.layout.row_deep_link_test_with_package, parent));
		}
		else if (viewType == TYPE_LINK_CUSTOM) {
			return new DeepLinkCustomViewHolder(inflate(R.layout.row_deep_link_test_custom, parent));
		}
		else if (viewType == TYPE_LINK_WITH_AB_TESTS) {
			return new DeepLinkWithABTestsViewHolder(inflate(R.layout.row_deep_link_test_with_ab_tests, parent));
		}
		else {
			return new DeepLinkViewHolder(inflate(R.layout.row_deep_link_test, parent));
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof HeaderViewHolder) {
			((HeaderViewHolder) holder).bind((DeepLinkSection) DEEP_LINKS[position]);
		}
		else if (holder instanceof DeepLinkViewHolder) {
			((DeepLinkViewHolder) holder).bind(DEEP_LINKS[position]);
		}
	}

	@Override
	public int getItemCount() {
		return DEEP_LINKS.length;
	}

	private View inflate(@LayoutRes int layoutResId, ViewGroup parent) {
		return LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
	}

	public static class DeepLinkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		@InjectView(R.id.button)
		Button button;
		@InjectView(R.id.link)
		TextView linkTextView;

		public DeepLinkViewHolder(View itemView) {
			super(itemView);

			ButterKnife.inject(this, itemView);

			button.setOnClickListener(this);
		}

		public void bind(DeepLink deepLink) {
			button.setText(deepLink.label);
			linkTextView.setText(deepLink.link);
		}

		@Override
		public void onClick(View v) {
			Intent intent;
			String link = linkTextView.getText().toString();
			if (link.startsWith("intent:")) {
				try {
					intent = Intent.parseUri(link, Intent.URI_INTENT_SCHEME);
				}
				catch (URISyntaxException e) {
					Toast.makeText(v.getContext(), "Unable to parse intent", Toast.LENGTH_LONG).show();
					return;
				}
			}
			else {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			}

			safeStartActivity(v.getContext(), intent);
		}

		protected void safeStartActivity(Context context, Intent intent) {
			try {
				context.startActivity(intent);
			}
			catch (ActivityNotFoundException e) {
				Toast.makeText(context, "No activity found to handle intent", Toast.LENGTH_LONG).show();
			}
		}
	}

	public static class DeepLinkWithPackageViewHolder extends DeepLinkViewHolder implements View.OnClickListener {

		@InjectView(R.id.app_package_spinner)
		Spinner packageSpinner;

		public DeepLinkWithPackageViewHolder(View itemView) {
			super(itemView);
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClassName(packageSpinner.getSelectedItem().toString(), "com.expedia.bookings.activity.DeepLinkRouterActivity");
			String link = linkTextView.getText().toString();
			if (link.length() > 0) {
				intent.setData(Uri.parse(link));
			}

			safeStartActivity(v.getContext(), intent);
		}
	}

	public static class DeepLinkWithABTestsViewHolder extends DeepLinkViewHolder {

		@InjectView(R.id.app_ab_tests_spinner)
		Spinner abTestsSpinner;

		@InjectView(R.id.test_variant_edit_text)
		EditText testVariantEditText;

		public DeepLinkWithABTestsViewHolder(View itemView) {
			super(itemView);
			List<Integer> testIDList = AbacusUtils.getActiveTests();

			//add 0 to reset the test map
			testIDList.add(0);

			Collections.sort(testIDList);
			ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(itemView.getContext(), android.R.layout.simple_spinner_item, testIDList); //selected item will look like a spinner set from XML
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			abTestsSpinner.setAdapter(spinnerArrayAdapter);
		}

		@Override
		public void onClick(View view) {
			Intent intent = new Intent();
			String link = "expda://forceBucket?key=" + abTestsSpinner.getSelectedItem() + "&value=" + testVariantEditText.getText();

			Toast.makeText(view.getContext(), link, Toast.LENGTH_SHORT).show();
			intent.setData(Uri.parse(link));
			safeStartActivity(view.getContext(), intent);
		}
	}

	public static class DeepLinkCustomViewHolder extends DeepLinkViewHolder implements View.OnClickListener {
		public DeepLinkCustomViewHolder(View itemView) {
			super(itemView);
		}
	}

	public static class HeaderViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		public HeaderViewHolder(View itemView) {
			super(itemView);
			textView = (TextView) itemView;
		}

		public void bind(DeepLinkSection deepLinkSection) {
			textView.setText(deepLinkSection.label);
			textView.setBackgroundColor(textView.getResources().getColor(deepLinkSection.colorResId));
		}
	}

	static class DeepLink {
		String label;
		String link;

		DeepLink(String label, String link) {
			this.label = label;
			this.link = link;
		}

		DeepLink(String label, String link, String dateField, int dayOffset) {
			this.label = label;

			LocalDate date = LocalDate.now().plusDays(dayOffset);
			Uri.Builder linkBuilder = Uri.parse(link).buildUpon().appendQueryParameter(dateField, date.toString("yyyy-MM-dd"));
			this.link = linkBuilder.build().toString();
		}

		DeepLink(String label, String link, String startDateField, int startDayOffset, String endDateField, int endDayOffset) {
			this.label = label;

			LocalDate date1 = LocalDate.now().plusDays(startDayOffset);
			Uri.Builder linkBuilder = Uri.parse(link).buildUpon().appendQueryParameter(startDateField, date1.toString("yyyy-MM-dd"));
			LocalDate date2 = LocalDate.now().plusDays(endDayOffset);
			linkBuilder.appendQueryParameter(endDateField, date2.toString("yyyy-MM-dd"));

			this.link = linkBuilder.build().toString();
		}
	}

	static class DeepLinkWithPackage extends DeepLink {
		DeepLinkWithPackage(String label, String link) {
			super(label, link);
		}
	}

	static class DeepLinkWithABTests extends DeepLink {
		DeepLinkWithABTests(String label, String link) {
			super(label, link);
		}
	}

	static class DeepLinkCustom extends DeepLink {
		DeepLinkCustom() {
			super("Go!", "expda://");
		}
	}

	static class DeepLinkSection extends DeepLink {
		@ColorRes int colorResId;

		DeepLinkSection(String label, @ColorRes int colorResId) {
			super(label, "");
			this.colorResId = colorResId;
		}
	}

}
