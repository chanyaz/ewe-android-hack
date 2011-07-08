package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.expedia.bookings.R;


/**
 * This class is meant to serve as a workaround 
 * to the problem of the error popup only being 
 * as wide as the field for which it exists. 
 * Consequently, when you have really narrow 
 * fields for items such as expiration month of a 
 * credit card, what you have is an extremely narrow
 * pop window within which to fit the error text.
 * (See bug #6864)
 * 
 * This class defines a minimum width for the popup. 
 * 
 * Most of the code has been inspired from TextView.java
 *
 */
public class CustomEditText extends EditText {

	private ErrorPopup mCustomPopup;
	private CharSequence mCustomError;
	private boolean mShowErrorAfterAttach;
	private boolean mTemporaryDetach;

	private int MINIMUM_WIDTH_IN_DP = 100;
	private int MINIMUM_PADDING_IN_DP = 60;
    	
	/*
	 * This textwatcher is responsible for 
	 * dismissing the error when the user starts 
	 * typing something
	 */
    private TextWatcher textWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s.length() > 0) {
				setError(null, null);
			}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) { }
		
		@Override
		public void afterTextChanged(Editable s) { }
	};
	
    /**
     * User interface state that is stored by TextView for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {
        CharSequence error;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            if (error == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                TextUtils.writeToParcel(error, out, flags);
            }
        }

        @Override
        public String toString() {
            String str = "EditTextWithCustomErrorPopup.SavedState{";
            if(error != null) {
            	str += " error=" + error;
            }
         
            return str + "}";
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            if (in.readInt() != 0) {
                error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
        }
    }
	
	////////////
	// Constructors
	///////////
	
	public CustomEditText(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		addTextChangedListener(textWatcher);
	}

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		addTextChangedListener(textWatcher);
	}

	public CustomEditText(Context context) {
		super(context);
		addTextChangedListener(textWatcher);
	}
	
	////////////
	// Overriden Methods
	///////////
	
	@Override
	public CharSequence getError() {
		return mCustomError;
	}
	
	@Override
	public void setError(CharSequence error) {
			if(error == null) {
				setError(null, null);
			} else {
	            Drawable dr = getContext().getResources().
                getDrawable(R.drawable.indicator_input_error);

	            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
	            setError(error, dr);
			}
	}	
	
	@Override
	public void setError(CharSequence error, Drawable icon) {
		error = TextUtils.stringOrSpannedString(error);
		mCustomError = error;
		
		final Drawable[] dr = getCompoundDrawables();
		if(dr != null) {
			setCompoundDrawables(dr[0], dr[1], icon, dr[3]);
		} else {
			setCompoundDrawables(null, null, icon, null);
		}
		
		if(error == null) {
            if (mCustomPopup != null) {
                if (mCustomPopup.isShowing()) {
                    mCustomPopup.dismiss();
                }

                mCustomPopup = null;
            }
        } else {
        	if(isFocused()) {
        		showError();
        	}
        }
	}

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);

        if (mCustomPopup != null && mCustomError != null) {
            TextView tv = (TextView) mCustomPopup.getContentView();
            chooseSize(mCustomPopup, mCustomError, tv);
            mCustomPopup.update(this, getErrorX(), getErrorY(),
                          mCustomPopup.getWidth(), mCustomPopup.getHeight());
        }
        
        return result;
    }
	
    @Override
    protected void onDetachedFromWindow() {
        if(mCustomError != null) {
        	hideError();
        }
        super.onDetachedFromWindow();
    }
    
    @Override
    protected void onAttachedToWindow() {
    	super.onAttachedToWindow();
        mTemporaryDetach = false;

    	if(mShowErrorAfterAttach) {
    		showError();
    		mShowErrorAfterAttach = false;
    	}
    }
    
    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        // Only track when onStartTemporaryDetach() is called directly,
        // usually because this instance is an editable field in a list
        mTemporaryDetach = true;
    }
    
    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        // Only track when onStartTemporaryDetach() is called directly,
        // usually because this instance is an editable field in a list
        mTemporaryDetach = false;
    }
    
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    	if(mTemporaryDetach) {
            // If we are temporarily in the detach state, then do nothing.
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            return;
    	}

    	if(focused) { 
    		if(mCustomError != null) {
    			showError();
    		}
    	} else {
            if (mCustomError != null) {
                hideError();
            }
    	}
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

    }
    
    @Override
    public Parcelable onSaveInstanceState() {
    	Parcelable superState = super.onSaveInstanceState();
    	
    	SavedState ss = new SavedState(superState);
    	ss.error = mCustomError;
    	
    	return ss;
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
    	if (!(state instanceof SavedState)) {
    		super.onRestoreInstanceState(state);
    		return;
    	}
    	
    	SavedState ss = (SavedState) state;
    	super.onRestoreInstanceState(ss.getSuperState());
    	
    	if(ss.error != null) {
    		final CharSequence error = ss.error;
    		
    		//Display the error later, after the first layout pass
    		post(new Runnable() {
    			public void run() {
    				setError(error);
    			}
    		});
    	}
    }
    
	////////////
	// Private Methods
	///////////

    private void hideError() {
        if (mCustomPopup != null) {
            if (mCustomPopup.isShowing()) {
            	mCustomPopup.dismiss();
            }
        }
        mShowErrorAfterAttach = false;
    }
    
	private void showError() {

		if (getWindowToken() == null) {
            mShowErrorAfterAttach = true;
            return;
        }

        if (mCustomPopup == null) {
            inflatePopup();
        }

        final TextView tv = (TextView) mCustomPopup.getContentView();
        chooseSize(mCustomPopup, mCustomError, tv);
        tv.setText(mCustomError);
   
        mCustomPopup.showAsDropDown(this, getErrorX(), getErrorY());
        mCustomPopup.fixDirection(mCustomPopup.isAboveAnchor());
	}

	private void inflatePopup() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		final TextView err = (TextView) inflater.inflate(R.layout.textview_hint, null);
		final float scale = getResources().getDisplayMetrics().density;
		mCustomPopup = new ErrorPopup(err, (int) (200 * scale + 0.5f),
		        (int) (50 * scale + 0.5f));
		mCustomPopup.setFocusable(false);
		// The user is entering text, so the input method is needed.  We
		// don't want the popup to be displayed on top of it.
		mCustomPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
	}
	
    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
    	/*
    	 * For some reason the popup doesn't pick up the padding the right way the first time, 
    	 * so I define a minimal padding here. It might be because the layout is not complete 
    	 * for the textview, however, I am also unable to set a layout changed listener 
    	 * for the textview as I get a class not found error for the EditTextWithCustomErrorPopup. 
    	 */
        int wid = Math.max(tv.getPaddingLeft() + tv.getPaddingRight(), MINIMUM_PADDING_IN_DP);
        int ht =  Math.max(tv.getPaddingTop() + tv.getPaddingBottom(), MINIMUM_PADDING_IN_DP);
        
        /*
         * Figure out how big the text would be if we laid it out to the
         * full width of this view minus the border.
         */
        int cap = getWidth() - wid;
        if (cap < MINIMUM_WIDTH_IN_DP) {
            cap = 200; // We must not be measured yet -- setFrame() will fix it.
        }

        Layout l = new StaticLayout(text, tv.getPaint(), cap,
                                    Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
        float max = MINIMUM_WIDTH_IN_DP;
        for (int i = 0; i < l.getLineCount(); i++) {
            max = Math.max(max, l.getLineWidth(i));
        }
        
        /*
         * Now set the popup size to be big enough for the text plus the border.
         */
        pop.setWidth(wid + (int) Math.ceil(max));
        pop.setHeight(ht + l.getHeight());
    }
    
    /**
     * Returns the Y offset to make the pointy top of the error point
     * at the middle of the error icon.
     */
    private int getErrorX() {
        /*
         * The "25" is the distance between the point and the right edge
         * of the background
         */
        final float scale = getResources().getDisplayMetrics().density;

        final Drawable[] dr = getCompoundDrawables();
        return getWidth() - mCustomPopup.getWidth()
                - getPaddingRight()
                - (dr != null ? dr[2].getBounds().width() : 0) / 2 + (int) (25 * scale + 0.5f);
    }

    /**
     * Returns the Y offset to make the pointy top of the error point
     * at the bottom of the error icon.
     */
    private int getErrorY() {
        /*
         * Compound, not extended, because the icon is not clipped
         * if the text height is smaller.
         */
        int vspace = getBottom() - getTop() -
                     getCompoundPaddingBottom() - getCompoundPaddingTop();

        final Drawable[] dr = getCompoundDrawables();
        int icontop = getCompoundPaddingTop()
                + (vspace - (dr != null ? dr[2].getBounds().height() : 0)) / 2;

        /*
         * The "2" is the distance between the point and the top edge
         * of the background.
         */

        return icontop + (dr != null ? dr[2].getBounds().height() : 0)
                - getHeight() - 2;
    }
	
    private static class ErrorPopup extends PopupWindow {
        private boolean mAbove = false;
        private final TextView mView;

        ErrorPopup(TextView v, int width, int height) {
            super(v, width, height);
            mView = v;
        }

        void fixDirection(boolean above) {
            mAbove = above;

            if (above) {
                mView.setBackgroundResource(R.drawable.popup_inline_error_above);
            } else {
                mView.setBackgroundResource(R.drawable.popup_inline_error);
            }
        }

        @Override
        public void update(int x, int y, int w, int h, boolean force) {
            super.update(x, y, w, h, force);

            boolean above = isAboveAnchor();
            if (above != mAbove) {
                fixDirection(above);
            }
        }
    }
}
