package com.expedia.bookings.dialog;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;

import com.mobiata.android.Log;

public class DomainPreference extends ListPreference {
	private Context mContext;
	private String mValue;
	private DomainAdapter mDomainAdapter;
	private int mSelectedOption;

	public DomainPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public DomainPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		DomainAdapter mDomainAdapter = new DomainAdapter(mContext);
		mDomainAdapter.setDomains(super.getEntries(), super.getEntryValues());
		mDomainAdapter.setSelected(mSelectedOption);
		builder.setAdapter(mDomainAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mSelectedOption = which;
				DomainPreference.super.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult && mSelectedOption >= 0 && super.getEntryValues() != null) {
			String value = super.getEntryValues()[mSelectedOption].toString();
			if (super.callChangeListener(value)) {
				super.setValue(value);
			}
		}
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.setValue(restore ? getPersistedString(mValue) : (String) defaultValue);
	}

	public class DomainAdapter extends BaseAdapter {
		private CharSequence[] mNames;
		private CharSequence[] mDomains;
		private LayoutInflater mInflater;
		private int mSelected = 0;

		public DomainAdapter (Context context) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setDomains (CharSequence[] names, CharSequence[] values) {
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
				convertView.setTag(holder);
			}
			else {
				holder = (DomainViewHolder) convertView.getTag();
			}

			if (position == mSelected) {
				convertView.setBackgroundColor(0xFFFFA500);
			}
			else {
				convertView.setBackgroundColor(Color.WHITE);
			}

			DomainTuple d = (DomainTuple) getItem(position);

			holder.mNameTextView.setText(d.mName);
			holder.mDomainTextView.setText(d.mDomain);

			return convertView;
		}
	}
}

