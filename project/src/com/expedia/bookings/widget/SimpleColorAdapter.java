package com.expedia.bookings.widget;import java.util.Random;import com.expedia.bookings.R;import android.content.Context;import android.os.Handler;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.view.ViewGroup.LayoutParams;import android.widget.BaseAdapter;import android.widget.ListView;import android.widget.TextView;public class SimpleColorAdapter extends BaseAdapter {	private Context mContext;	private int[] mColors;	private int mRowHeight;	private int mNumItems;	public SimpleColorAdapter(Context context, int rowHeight, int numItems, int[] colors) {		mContext = context;		mRowHeight = rowHeight;		mNumItems = numItems;		mColors = colors;	}	public void enableSizeChanges(final int maxNumItems, final int intervalMs){		final Random rand = new Random();		final Handler handler = new Handler();		handler.postDelayed(new Runnable(){			@Override			public void run() {				mNumItems = rand.nextInt(maxNumItems);				notifyDataSetChanged();				handler.postDelayed(this, intervalMs);			}		}, intervalMs);	}	@Override	public int getCount() {		return mNumItems;	}	@Override	public Object getItem(int position) {		return mColors[position];	}	@Override	public long getItemId(int position) {		return 0;	}	@Override	public View getView(int position, View convertView, ViewGroup parent) {		TextView v = (TextView) convertView;		if (v == null) {			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			v = (TextView) inflater.inflate(R.layout.row_simple_color_text, null);		}		LayoutParams layoutParams = v.getLayoutParams();		if (layoutParams == null) {			layoutParams = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, mRowHeight);		}		layoutParams.height = mRowHeight;		v.setLayoutParams(layoutParams);		int color = mColors[position % mColors.length];		v.setBackgroundColor(color);		v.setText("Row #" + position);		return v;	}}