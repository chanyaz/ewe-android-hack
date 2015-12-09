package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.ClearPrivateDataUtil;

import java.util.List;

public class DomainPreference extends ListPreference {
	private Context mContext;
	private String mValue;
	private int mSelectedOption;
	private int mPreviouslySelectedOption;

	private CharSequence[] mEntries;
	private CharSequence[] mEntrySubText;
	private CharSequence[] mEntryValues;

	public DomainPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		List<PointOfSale> poses = PointOfSale.getAllPointsOfSale(context);
		int len = poses.size();
		mEntries = new CharSequence[len];
		mEntrySubText = new CharSequence[len];
		mEntryValues = new CharSequence[len];
		for (int a = 0; a < len; a++) {
			PointOfSale info = poses.get(a);
			mEntries[a] = context.getString(info.getCountryNameResId());
			mEntrySubText[a] = info.getUrl();
			mEntryValues[a] = Integer.toString(info.getPointOfSaleId().getId());
		}
	}

	public DomainPreference(Context context) {
		this(context, null);
	}

	@Override
	public CharSequence getSummary() {
		PointOfSale info = PointOfSale.getPointOfSale(mContext);
		final String country = mContext.getString(info.getCountryNameResId());
		final String url = info.getUrl();
		return country + " - " + url;
	}

	@Override
	public CharSequence[] getEntries() {
		return mEntries;
	}

	@Override
	public CharSequence[] getEntryValues() {
		return mEntryValues;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		view.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.preference_ripple));
	}

	@Override
	public int findIndexOfValue(String value) {
		for (int i = 0, n = mEntryValues.length; i < n; ++i) {
			if (mEntryValues[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}

	void setSelectedOption(int v) {
		if (v < 0 || v > getEntryValues().length) {
			mSelectedOption = 0;
		}
		else {
			mSelectedOption = v;
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		DomainAdapter domainAdapter = new DomainAdapter(mContext);
		domainAdapter.setDomains(mEntries, mEntrySubText);
		domainAdapter.setSelected(mSelectedOption);
		mPreviouslySelectedOption = mSelectedOption;
		builder.setAdapter(domainAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setSelectedOption(which);
				DomainPreference.super.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult && mSelectedOption >= 0 && mSelectedOption != mPreviouslySelectedOption
				&& getEntryValues() != null) {
			final String value = getEntryValues()[mSelectedOption].toString();
			setSelectedOption(mSelectedOption);
			Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.dialog_clear_private_data_title);
			if (User.isLoggedIn(mContext)) {
				builder.setMessage(R.string.dialog_sign_out_and_clear_private_data_msg);
			}
			else {
				builder.setMessage(R.string.dialog_clear_private_data_msg);
			}
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// We are breaking contract a bit here; we change the value regardless
					// of what the change listener says.  This is so that we can use the
					// change listener as a reaction to change, rather than as a gateway.
					setValue(value);
					callChangeListener(value);

					ClearPrivateDataUtil.clear(mContext);

					// Inform the men
					Toast.makeText(mContext, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
				}
			});
			builder.setNegativeButton(R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setSelectedOption(mPreviouslySelectedOption);
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					setSelectedOption(mPreviouslySelectedOption);
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		String v = restore ? getPersistedString(mValue) : (String) defaultValue;
		setSelectedOption(findIndexOfValue(v));
		setValue(v);
	}

	public class DomainAdapter extends BaseAdapter {
		private CharSequence[] mNames;
		private CharSequence[] mDomains;
		private LayoutInflater mInflater;
		private int mSelected = 0;

		public DomainAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public void setDomains(CharSequence[] names, CharSequence[] values) {
			mNames = names;
			mDomains = values;
		}

		public void setSelected(int p) {
			mSelected = p;
		}

		private class DomainViewHolder {
			// View stuff
			TextView mNameTextView;
			TextView mDomainTextView;
			RadioButton mRadioButton;
		}

		public class DomainTuple {
			CharSequence mName;
			CharSequence mDomain;
		}

		// Adapter implementation

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getCount() {
			return mNames.length < mDomains.length ? mNames.length : mDomains.length;
		}

		@Override
		public Object getItem(int position) {
			if (mNames != null && mDomains != null && mNames.length > position && mDomains.length > position) {
				DomainTuple t = new DomainTuple();
				t.mName = mNames[position];
				t.mDomain = mDomains[position];
				return t;
			}
			else {
				return null;
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			DomainViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row_domain_preference, parent, false);
				holder = new DomainViewHolder();
				holder.mNameTextView = (TextView) convertView.findViewById(R.id.country_name_text_view);
				holder.mDomainTextView = (TextView) convertView.findViewById(R.id.domain_name_text_view);
				holder.mRadioButton = (RadioButton) convertView.findViewById(R.id.domain_radio_button);
				convertView.setTag(holder);
			}
			else {
				holder = (DomainViewHolder) convertView.getTag();
			}

			if (position == mSelected) {
				holder.mRadioButton.setChecked(true);
			}
			else {
				holder.mRadioButton.setChecked(false);
			}

			DomainTuple d = (DomainTuple) getItem(position);

			holder.mNameTextView.setText(d.mName);
			holder.mDomainTextView.setText(d.mDomain);

			return convertView;
		}
	}

	@Override
	protected void showDialog(Bundle state) {
		if (mEntries != null && mEntries.length > 1) {
			super.showDialog(state);
		}
	}
}
