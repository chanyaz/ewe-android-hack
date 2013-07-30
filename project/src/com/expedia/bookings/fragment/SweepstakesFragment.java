package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.SettingUtils;

public class SweepstakesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sweepstakes, container, false);

		FontCache.setTypeface((TextView) view.findViewById(R.id.enter_title_text_view), Font.BEBAS_NEUE);
		FontCache.setTypeface((TextView) view.findViewById(R.id.confirmation_title_text_view), Font.BEBAS_NEUE);

		Ui.setOnClickListener(view, R.id.no_thanks_button, mOnClickListener);
		Ui.setOnClickListener(view, R.id.enter_button, mOnClickListener);
		Ui.setOnClickListener(view, R.id.done_button, mOnClickListener);
		Ui.setOnClickListener(view, R.id.terms_text_view, mOnClickListener);

		Ui.setText(view, R.id.terms_text_view, Html.fromHtml(getActivity().getString(R.string.sweepstakes_terms)));

		OmnitureTracking.trackSweepstakesEntry(getActivity());

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		bind();
	}

	public void bind() {
		boolean showEntryLayout = !SettingUtils.get(getActivity(), R.string.setting_hide_sweepstakes, false);

		Ui.findView(getView(), R.id.entry_layout).setVisibility(showEntryLayout ? View.VISIBLE : View.GONE);
		Ui.findView(getView(), R.id.confirmation_layout).setVisibility(showEntryLayout ? View.GONE : View.VISIBLE);

		Ui.setImageResource(getView(), R.id.background_image_view, showEntryLayout ? R.drawable.bg_sweepstakes_entry
				: R.drawable.bg_sweepstakes_confirmation);
	}

	public void enterSweepstakes() {
		if (Db.getUser() == null || !Db.getUser().isLoggedIn(getActivity())) {
			startActivityForResult(LoginActivity.createIntent(getActivity(), LineOfBusiness.ITIN, null), 0);
			return;
		}

		// Send sweepstakes entry
		OmnitureTracking.trackSweepstakesConfirmation(getActivity(), Db.getUser());

		// Save setting to hide sweepstakes
		SettingUtils.save(getActivity(), R.string.setting_hide_sweepstakes, true);

		// Update views
		bind();
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.no_thanks_button: {
				SettingUtils.save(getActivity(), R.string.setting_hide_sweepstakes, true);
				getActivity().finish();
				break;
			}
			case R.id.enter_button: {
				enterSweepstakes();
				break;
			}
			case R.id.done_button: {
				getActivity().finish();
				break;
			}
			case R.id.terms_text_view: {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl("http://expedia.com/tripadayrules");
				startActivity(builder.getIntent());
				break;
			}
			}
		}
	};
}
