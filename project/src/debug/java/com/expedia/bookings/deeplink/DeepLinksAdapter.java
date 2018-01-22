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

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeepLinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_SECTION_HEADER = 0;
	private static final int TYPE_LINK = 1;
	private static final int TYPE_LINK_WITH_PACKAGE = 2;
	private static final int TYPE_LINK_CUSTOM = 3;
	private static final int TYPE_LINK_WITH_AB_TESTS = 4;

	private static final DeepLink[] DEEP_LINKS = new DeepLink[] {
		new DeepLinkSection("Hotels", R.color.app_primary),
		new DeepLink("Ewa Beach, Hawaii", "expda://hotelSearch?location=Ewa+Beach%2C+Hawaii%2C+United+States+of+America&seocid=Google"),
		new DeepLink("Las Vegas, NV", "expda://hotelSearch?location=Las%20Vegas,%20NV"),
		new DeepLink("New York, NY", "expda://hotelSearch?location=New%20York%2C%20NY"),
		new DeepLink("Portland, Maine", "expda://hotelSearch?location=Portland,%20Maine"),
		new DeepLink("Selected ID", "expda://hotelSearch?selected=2369616"),
		new DeepLink("Region (Arc de Triomphe)", "expda://hotelSearch?location=Arc%20de%20Triomphe%20-%20Palais%20des%20Congres%20(17%20arr.)%2C%20Paris%2C%20France"),
		new DeepLink("Region (ID800103)", "expda://hotelSearch?location=ID800103"),
		new DeepLink("Attraction (Academy of Paris)", "expda://hotelSearch?location=Academy%20of%20Paris%2C%20Paris%2C%20France"),
		new DeepLink("Airport (BVA)", "expda://hotelSearch?location=Paris%2C%20France%20(BVA-Beauvais)"),
		new DeepLink("Intent", "intent://hotelSearch?location=Orlando,%20FL/#Intent;package=com.expedia.bookings;scheme=expda;end"),
		new DeepLink("Current Location Search", "expda://hotelSearch"),
		new DeepLink("Sort by Recommended", "expda://hotelSearch?sortType=Recommended"),
		new DeepLink("Sort by Discounts", "expda://hotelSearch?sortType=Discounts"),
		new DeepLink("Sort by Price", "expda://hotelSearch?sortType=Price"),
		new DeepLink("Sort by Rating", "expda://hotelSearch?sortType=Rating"),
		new DeepLink("Sort with Location and Dates", "expda://hotelSearch?sortType=Rating&location=Austin,%20TX", "checkInDate", 14, "checkOutDate", 18, "yyyy-MM-dd"),

		new DeepLink("Hotel ID 11562190", "expda://hotelSearch?hotelId=11562190&seocid=Google"),
		new DeepLink("Hotel ID 9046290", "expda://hotelSearch?hotelId=9046290"),
		new DeepLink("Hotel ID 1819759", "expda://hotelSearch?hotelId=1819759&cid=SEO.Google"),
		new DeepLink("Future Dates", "expda://hotelSearch?hotelId=12539", "checkInDate", 14, "checkOutDate", 18, "yyyy-MM-dd"),
		new DeepLink("Out-dated Search", "expda://hotelSearch?location=San%20Diego,%20CA", "checkInDate", -365, "checkOutDate", -360, "yyyy-MM-dd"),
		new DeepLink("Check Out Only", "expda://hotelSearch?location=Key%20West,%20FL", "checkOutDate", 10, "yyyy-MM-dd"),
		new DeepLink("Check In Only", "expda://hotelSearch?location=Key%20West,%20FL", "checkInDate", 7, "yyyy-MM-dd"),
		new DeepLink("28+ Day Stay", "expda://hotelSearch?location=Austin,%20TX", "checkInDate", 10, "checkOutDate", 40, "yyyy-MM-dd"),
		new DeepLink("Check Out Before Check In", "expda://hotelSearch?location=Nashville,%20TN", "checkInDate", 10, "checkOutDate", 7, "yyyy-MM-dd"),
		new DeepLink("Dates and 3 Guests", "expda://hotelSearch?location=Baltimore,%20MD&numAdults=3", "checkInDate", 7, "checkOutDate", 10, "yyyy-MM-dd"),
		new DeepLink("2 Adults", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2"),
		new DeepLink("2 Adults, 1 Child", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2&childAges=2"),
		new DeepLink("2 Adults, 1 Child, 1 Infant", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=2&childAges=2,1"),
		new DeepLink("1 Adult, 2 Infants", "expda://hotelSearch?location=Myrtle%20Beach,%20SC&numAdults=1&childAges=1,1"),
		new DeepLink("Deposits v1 Hotel", "expda://hotelSearch/?hotelId=18000", "checkInDate", 8, "checkOutDate", 10, "yyyy-MM-dd"),
		new DeepLink("Sold Out (maybe)", "expda://hotelSearch?hotelId=4183598&cid=SEO.Google"),
		new DeepLink("Sold Out complex (maybe)", "expda://hotelSearch?hotelId=4183598&numAdults=3&childAges=3,2&cid=SEO.Google", "checkInDate", 1, "checkOutDate", 2, "yyyy-MM-dd"),

		new DeepLinkSection("Flights", R.color.flights_lob_btn),
		new DeepLink("Flight Search", "expda://flightSearch"),
		new DeepLink("Flight Search (Round trip)", "expda://flightSearch?origin=SFO&destination=SEA&numAdults=1", "departureDate", 14, "returnDate", 18, "yyyy-MM-dd"),
		new DeepLink("Flight Search (One Way)", "expda://flightSearch?origin=SFO&destination=SEA&numAdults=1", "departureDate", 14, "yyyy-MM-dd"),
		new DeepLink("Flight Search (3 adults, roundTrip)", "expda://flightSearch?origin=SFO&destination=SEA&numAdults=3", "departureDate", 14, "returnDate", 18, "yyyy-MM-dd"),
		new DeepLink("Flight Travel Guide", "expda://flightSearch?destination=ATH&seocid=Google"),

		new DeepLinkSection("Activities", R.color.app_primary),
		new DeepLink("Activity Search", "expda://activitySearch"),
		new DeepLink("San Francisco", "expda://activitySearch?location=San%20Francisco", "startDate", 3, "yyyy-MM-dd"),

		new DeepLinkSection("Cars", R.color.app_primary),
		new DeepLink("Car Search", "expda://carSearch"),
		new DeepLink("Car Search", "expda://carSearch?pickupLocation=DTW"),
		new DeepLink("Car Search with expired dates (show pop-up msg)", "expda://carSearch?pickupDateTime=2017-03-12T22:30:00&dropoffDateTime=2017-03-15T09:30:00&pickupLocation=DTW"),
		new DeepLink("Car Search with future date", "expda://carSearch?pickupLocation=DTW", "pickupDateTime", 14, "dropoffDateTime", 18, "yyyy-MM-dd", "22:30:00", "09:30:00"),

		new DeepLinkSection("Packages", R.color.app_primary),
		new DeepLink("Package Search", "expda://packageSearch"),

		new DeepLinkSection("Rails", R.color.app_primary),
		new DeepLink("Rail Search", "expda://railSearch"),

		new DeepLinkSection("Deferred", R.color.launch_screen_primary),
		new DeepLink("Parc 55 San Francisco, a Hilton Hotel",
			"https://169006.measurementapi.com/serve?action=click&publisher_id=169006&site_id=107678&invoke_url=expda%3A%2F%2FhotelSearch%3FhotelId%3D12539"),

		new DeepLinkSection("Other", R.color.gt_primary_color),
		new DeepLink("Home", "expda://home"),
		new DeepLink("Sign In", "expda://signIn"),
		new DeepLink("Trips", "expda://trips"),
		new DeepLink("Trips with itin number in Mock Server", "expda://trips?itinNum=1103274148635"),
		new DeepLink("Unsupported URL Scheme", "george://noworky"),
		new DeepLink("Destination (Muzei Plugin)", "expda://destination/?displayName=Orlando,+FL&searchType=CITY&hotelId=178294&airportCode=ORL&regionId=178294&latitude=28.541290&longitude=-81.379040&imageCode=fun-orlando"),
		new DeepLink("Support Email", "expda://supportEmail"),
		new DeepLinkWithABTests("Force Bucket", "expda://forceBucket?key={}&value={}"),
		new DeepLinkWithPackage("Empty Data", ""),
		new DeepLinkCustom(),

		new DeepLinkSection("Universal Links", R.color.hotels_primary_color),
		new DeepLink("Basic Hotel Search", "https://www.expedia.com/mobile/deeplink/Hotels"),
		new DeepLink("Current Location Search", "https://www.expedia.com/mobile/deeplink/Hotel-Search"),
		new DeepLink("Location Search with Location ID", "https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=6119916"),
		new DeepLink("Hotel Search with Adults", "https://www.expedia.com/mobile/deeplink/Hotel-Search?adults=3", "startDate", 90, "endDate", 97, "MM/dd/yyyy"),
		new DeepLink("Hotel Future Dates", "https://www.expedia.com/mobile/deeplink/Hotel-Search?hotelId=12539", "startDate", 90, "endDate", 97, "MM/dd/yyyy"),
		new DeepLink("Hotel Future Dates - Korea PoS", "https://www.expedia.co.kr/mobile/deeplink/Hotel-Search?hotelId=12539", "startDate", 90, "endDate", 97, "yyyy.MM.dd"),
		new DeepLink("Hotel Info Site", "https://www.expedia.com/mobile/deeplink/Krabi-Trang-Hotels-Pakasai-Resort.h1276486.Hotel-Information?rm1=a2:c2:c7:c8", "chkin", 14, "chkout", 18, "MM/dd/yyyy"),
		new DeepLink("Member Only Deals Search", "https://www.expedia.com/mobile/deeplink/Hotels?sort=discounts"),
		new DeepLink("Member Only Deals Search Results", "https://www.expedia.com/mobile/deeplink/Hotels?regionId=6119916&sort=discounts", "startDate", 90, "endDate", 97, "MM/dd/yyyy"),
		new DeepLink("MCTC - Search Paris", "https://www.expedia.com/mobile/deeplink/Hotel-Search?paandi=true&trv_tax=18.66&trv_di=B&ICMDTL=htl.1808319.taid.678590.geoid.187147.testslice..clickid.WLhB8goQHIEAAaxKd8cAAADk.AUDID.10532&trv_curr=USD&ICMCID=Meta.tripa.Expedia_US-DM&SC=2&mctc=9&trv_mbl=L&trv_bp=151.82&startDate=4%2F26%2F2017&endDate=4%2F30%2F2017&adults=2&regionId=179898"),
		new DeepLink("MCTC - Search (Hotel d'Aubusson)", "https://www.expedia.com/mobile/deeplink/Hotel-Search?paandi=true&trv_tax=18.66&trv_di=B&ICMDTL=htl.1808319.taid.678590.geoid.187147.testslice..clickid.WLhB8goQHIEAAaxKd8cAAADk.AUDID.10532&trv_curr=USD&ICMCID=Meta.tripa.Expedia_US-DM&SC=2&mctc=9&trv_mbl=L&trv_bp=151.82&startDate=4%2F26%2F2017&endDate=4%2F30%2F2017&adults=2&selected=564481"),
		new DeepLink("MCTC - InfoSite", "https://www.expedia.com/mobile/deeplink/Paris-Hotels-Hotel-Wilson-Opera.h1808319.Hotel-Information?langid=1033&mctc=5&chid=5bb6a8b1-86c6-4340-9ba1-fd3ef6906566&mrp=1&mdpcid=US.META.TRIVAGO.HOTELSCORESEARCH.HOTEL&mdpdtl=HTL.1808319.PARIS&trv_curr=USD&chkin=4/26/2017&chkout=4/30/2017&rateplanid=200803984_200803984_24&trv_dp=147&rm1=a2&paandi=true"),
		new DeepLink("SEMDTL param search", "https://www.expedia.com/mobile/deeplink/Hotel-Search?regionId=6119916&semdtl=a1416242820.b127031966820.d1115895306900.e1c.f11t1.g1kwd-275058426.h1e.i16139906654.j19033253.k1.l1g.m1.n1"),

		new DeepLink("Basic Flight Search", "https://www.expedia.com/mobile/deeplink/Flights"),
		new DeepLink("Flight Search", "https://www.expedia.com/mobile/deeplink/Flights-Search"),
		new DeepLink("Flight Search Round Trip", "https://www.expedia.com/mobile/deeplink/Flights-Search?trip=roundtrip&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09/27/2017TANYT&leg2=from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.),departure:10/11/2017TANYT"),
		new DeepLink("Flight Search With Passengers", "https://www.expedia.com/mobile/deeplink/Flights-Search?trip=roundtrip&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09/27/2017TANYT&leg2=from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.),departure:10/11/2017TANYT&passengers=children:0,adults:3,seniors:0,infantinlap:Y"),

		new DeepLink("Car Search (should go to Search form)", "https://www.expedia.com/mobile/deeplink/carsearch"),
		new DeepLink("Car Search (should go to Search form)", "https://www.expedia.com/mobile/deeplink/carsearch?time1=700PM&time2=500PM", "date1", 90, "date2", 97, "MM/dd/yyyy"),
		new DeepLink("Car Search with Location", "https://www.expedia.com/mobile/deeplink/carsearch?locn=Bangkok, Thailand (BKK-All Airports)&time1=700PM&time2=500PM", "date1", 90, "date2", 97, "MM/dd/yyyy"),

		new DeepLink("Activity Search", "https://www.expedia.com/mobile/deeplink/things-to-do/search"),
		new DeepLink("Activity Search with Location", "https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity), Thailand"),
		new DeepLink("Activity Search with Categories", "https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity), Thailand&categories=Nightlife|Cruises%20%26%20Water%20Tours", "startDate", 14, "MM/dd/yyyy"),

		new DeepLink("Home", "https://www.expedia.com/mobile/deeplink"),
		new DeepLink("Sign In", "https://www.expedia.com/mobile/deeplink/user/signin"),
		new DeepLink("Sign In - Trailing slash", "https://www.expedia.com/mobile/deeplink/user/signin/"),
		new DeepLink("Trips", "https://www.expedia.com/mobile/deeplink/trips"),
		new DeepLink("Trips in Mock Web Server", "https://www.expedia.com/mobile/deeplink/trips/1103274148635"),
		new DeepLinkTrips(),

		new DeepLink("Member Pricing", "https://www.expedia.com/mobile/deeplink/member-pricing"),

		new DeepLinkSection("Verify Tracking", R.color.app_primary_dark),
		new DeepLink("email", "https://www.expedia.com/mobile/deeplink/Hotel-Search?emlcid=emlcidtest&emldtl=emldtltest"),
		new DeepLink("sem", "https://www.expedia.com/mobile/deeplink/Hotel-Search?semcid=semcidtest&semdtl=semdtltest&gclid=gclidtest"),
		new DeepLink("ola", "https://www.expedia.com/mobile/deeplink/Hotel-Search?olacid=olacidtest&oladtl=oladtltest"),
		new DeepLink("affiliates", "https://www.expedia.com/mobile/deeplink/Hotel-Search?affcid=affcidtest&afflid=afflidtest"),
		new DeepLink("brand", "https://www.expedia.com/mobile/deeplink/Hotel-Search?brandcid=brandcidtest"),
		new DeepLink("seo", "https://www.expedia.com/mobile/deeplink/Hotel-Search?seocid=seocidtest")
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

		DeepLink(String label, String link, String dateField, int dayOffset, String pattern) {
			this.label = label;

			LocalDate date = LocalDate.now().plusDays(dayOffset);
			Uri.Builder linkBuilder = Uri.parse(link).buildUpon().appendQueryParameter(dateField, date.toString(pattern));
			this.link = linkBuilder.build().toString();
		}

		DeepLink(String label, String link, String startDateField, int startDayOffset, String endDateField, int endDayOffset, String pattern) {
			this.label = label;

			LocalDate date1 = LocalDate.now().plusDays(startDayOffset);
			Uri.Builder linkBuilder = Uri.parse(link).buildUpon().appendQueryParameter(startDateField, date1.toString(pattern));
			LocalDate date2 = LocalDate.now().plusDays(endDayOffset);
			linkBuilder.appendQueryParameter(endDateField, date2.toString(pattern));

			this.link = linkBuilder.build().toString();

		}

		DeepLink(String label, String link, String startDateField, int startDayOffset, String endDateField, int endDayOffset, String pattern, String pickUpTime, String dropOffTime) {
			this.label = label;

			LocalDate date1 = LocalDate.now().plusDays(startDayOffset);
			Uri.Builder linkBuilder = Uri.parse(link).buildUpon().appendQueryParameter(startDateField, date1.toString(pattern).concat("T").concat(pickUpTime));
			LocalDate date2 = LocalDate.now().plusDays(endDayOffset);
			linkBuilder.appendQueryParameter(endDateField, date2.toString(pattern).concat("T").concat(dropOffTime));

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

	static class DeepLinkTrips extends DeepLink {
		DeepLinkTrips() {
			super("Go!", "https://www.expedia.com/mobile/deeplink/trips/");
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
