package com.expedia.bookings.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;

import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class PopupMenuNative extends AbsPopupMenu {
	private android.widget.PopupMenu mPopupMenu;
	private MenuWrapper mMenu;

	public PopupMenuNative(Context context, View anchor) {
		super(context, anchor);
		mPopupMenu = new android.widget.PopupMenu(context, anchor);
	}

	@Override
	public Menu getMenu() {
		final android.view.Menu menu = mPopupMenu.getMenu();
		if (mMenu == null || menu != mMenu.unwrap()) {
			mMenu = new MenuWrapper(menu);
		}

		return mMenu;
	}

	@Override
	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	@Override
	public void inflate(int menuRes) {
		mPopupMenu.inflate(menuRes);
	}

	@Override
	public void show() {
		mPopupMenu.show();
	}

	@Override
	public void dismiss() {
		mPopupMenu.dismiss();
	}

	@Override
	public void setOnMenuItemClickListener(final IcsPopupMenu.OnMenuItemClickListener listener) {
		mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(android.view.MenuItem item) {
				return listener.onMenuItemClick(new MenuItemWrapper(item));
			}
		});
	}

	@Override
	public void setOnDismissListener(final IcsPopupMenu.OnDismissListener listener) {
		mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
			@Override
			public void onDismiss(PopupMenu menu) {
				listener.onDismiss(PopupMenuNative.this);
			}
		});
	}
}
