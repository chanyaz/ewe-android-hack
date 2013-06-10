package com.expedia.bookings.widget;

import android.content.Context;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public abstract class AbsPopupMenu {
	/**
	 * Interface responsible for receiving menu item click events if the items themselves
	 * do not have individual item click listeners.
	 */
	public interface OnMenuItemClickListener {
		/**
		 * This method will be invoked when a menu item is clicked if the item itself did
		 * not already handle the event.
		 *
		 * @param item {@link com.actionbarsherlock.view.MenuItem} that was clicked
		 * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
		 */
		public boolean onMenuItemClick(MenuItem item);
	}

	/**
	 * Callback interface used to notify the application that the menu has closed.
	 */
	public interface OnDismissListener {
		/**
		 * Called when the associated menu has been dismissed.
		 *
		 * @param menu The IcsPopupMenu that was dismissed.
		 */
		public void onDismiss(AbsPopupMenu menu);
	}

	protected Context mContext;

	public AbsPopupMenu(Context context, View anchor) {
		mContext = context;
	}

	public abstract Menu getMenu();

	public abstract MenuInflater getMenuInflater();

	public abstract void inflate(int menuRes);

	public abstract void show();

	public abstract void dismiss();

	public abstract void setOnMenuItemClickListener(IcsPopupMenu.OnMenuItemClickListener listener);

	public abstract void setOnDismissListener(IcsPopupMenu.OnDismissListener listener);
}
