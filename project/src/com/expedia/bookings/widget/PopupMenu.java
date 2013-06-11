package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Build;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class PopupMenu {
	final AbsPopupMenu mAbsPopupMenu;

	public PopupMenu(Context context, View anchor) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mAbsPopupMenu = new PopupMenuNative(context, anchor);
		}
		else {
			mAbsPopupMenu = new IcsPopupMenu(context, anchor);
		}
	}

	public Menu getMenu() {
		return mAbsPopupMenu.getMenu();
	}

	public MenuInflater getMenuInflater() {
		return mAbsPopupMenu.getMenuInflater();
	}

	public void inflate(int menuRes) {
		mAbsPopupMenu.inflate(menuRes);
	}

	public void show() {
		mAbsPopupMenu.show();
	}

	public void dismiss() {
		mAbsPopupMenu.dismiss();
	}

	public void setOnMenuItemClickListener(AbsPopupMenu.OnMenuItemClickListener listener) {
		mAbsPopupMenu.setOnMenuItemClickListener(listener);
	}

	public void setOnDismissListener(AbsPopupMenu.OnDismissListener listener) {
		mAbsPopupMenu.setOnDismissListener(listener);
	}
}
