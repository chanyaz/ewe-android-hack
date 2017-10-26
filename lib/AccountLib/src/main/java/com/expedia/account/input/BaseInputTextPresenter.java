package com.expedia.account.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.expedia.account.R;
import com.expedia.account.presenter.BufferedPresenter;
import com.expedia.account.util.StatusObservableWrapper;
import com.expedia.account.util.Utils;
import com.expedia.account.view.AccessibleEditText;


public class BaseInputTextPresenter extends BufferedPresenter {

	protected AccessibleEditText editText;

	public static final int NO_CHANGE = -1;
	public static final int WAITING = 0;
	public static final int PROGRESS = 1;
	public static final int GOOD = 2;
	public static final int BAD = 3;

	protected InputValidator mValidator;
	protected StatusObservableWrapper mStatusObservable;

	// States
	public static final String STATE_WAITING = "STATE_WAITING";
	public static final String STATE_PROGRESS = "STATE_PROGRESS";
	public static final String STATE_GOOD = "STATE_GOOD";
	public static final String STATE_BAD = "STATE_BAD";

	protected String errorText = "";
	protected String errorContDesc = "";
	protected String hintText = "";
	protected int inputType = InputType.TYPE_CLASS_TEXT;
	// 0 == No options... Must be defined somewhere, which would be better than hard coding.
	protected int imeOptions = 0;

	public void requestFocus(boolean forward) {
		editText.requestFocus();
	}

	public void forceCheckWithFocus(boolean focus) {
		showInternal(mValidator.onFocusChanged(editText.getText().toString(), focus));
	}

	public boolean isTextValid() {
		return mValidator.onNewText(editText.getText().toString()) == GOOD;
	}

	public BaseInputTextPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.acct__ErrorableInputText, 0, 0);
		try {
			if (ta.hasValue(R.styleable.acct__ErrorableInputText_acct__eit_errorString)) {
				errorText = ta.getString(R.styleable.acct__ErrorableInputText_acct__eit_errorString);
			} else if (ta.hasValue(R.styleable.acct__ErrorableInputText_acct__eit_errorStringPassword)) {
				errorText = Utils.obtainPasswordErrorMessage(context, ta.getResourceId(R.styleable.acct__ErrorableInputText_acct__eit_errorStringPassword, -1)).toString();
			}
			errorContDesc = ta.getString(R.styleable.acct__ErrorableInputText_acct__eit_errorContDesc);
			hintText = ta.getString(R.styleable.acct__ErrorableInputText_android_hint);
			inputType = ta.getInt(R.styleable.acct__ErrorableInputText_android_inputType, inputType);
			imeOptions = ta.getInt(R.styleable.acct__ErrorableInputText_android_imeOptions, imeOptions);
		}
		finally {
			ta.recycle();
		}

	}

	protected void createAndAddTransitions() {
		addDefaultTransition(new DefaultTransition(STATE_WAITING) {
			@Override
			public void finalizeTransition(boolean forward) {
				super.finalizeTransition(forward);
				showWaiting();
			}
		});

		addTransition(new Transition(STATE_WAITING, STATE_PROGRESS) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showProgress();
				}
				else {
					showWaiting();
				}
			}
		});

		addTransition(new Transition(STATE_PROGRESS, STATE_GOOD) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showGood();
				}
				else {
					showProgress();
				}
			}
		});

		addTransition(new Transition(STATE_PROGRESS, STATE_BAD) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showError();
				}
				else {
					showProgress();
				}
			}
		});

		addTransition(new Transition(STATE_WAITING, STATE_GOOD, null, 0) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showGood();
				}
				else {
					showWaiting();
				}
			}
		});

		addTransition(new Transition(STATE_GOOD, STATE_BAD, null, 0) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showError();
				}
				else {
					showGood();
				}
			}
		});

		addTransition(new Transition(STATE_WAITING, STATE_BAD, null, 0) {
			@Override
			public void startTransition(boolean forward) {
				if (forward) {
					showError();
				}
				else {
					showWaiting();
				}
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		editText.setEnabled(enabled);
	}

	/**
	 * Returns this EditText's text as a String. We never need it as an Editable.
	 */
	public String getText() {
		return editText.getText().toString();
	}

	public void setText(CharSequence text) {
		editText.setText(text);
	}

	public void addTextChangedListener(TextWatcher watcher) {
		editText.addTextChangedListener(watcher);
	}

	public void setOnEditorActionListener(TextView.OnEditorActionListener l) {
		editText.setOnEditorActionListener(l);
	}

	public void moveCursorToEnd() {
		editText.setSelection(editText.getText().length());
	}

	public void setEditTextContentDescription(String description) {
		editText.setContentDescription(description);
	}

	/////
	// Validation
	/////

	protected void changeEditText(final int color, final int backgroundResource) {
		//This is posted to avoid destroying the keyboard calls from coming up.
		this.post(new Runnable() {
			@Override
			public void run() {
				int[] padding = {
					editText.getPaddingLeft(),
					editText.getPaddingTop(),
					editText.getPaddingRight(),
					editText.getPaddingBottom()
				};
				editText.setBackgroundResource(backgroundResource);
				editText.setPadding(padding[0], padding[1], padding[2], padding[3]);
				if (color != -1) {
					Drawable bg = editText.getBackground();
					bg.setColorFilter(color, PorterDuff.Mode.SRC_IN);
				}
			}
		});
	}

	protected void showError() {}
	protected void showGood() {}
	protected void showWaiting() {}
	protected void showProgress() {}

	protected void showInternal(int newState) {
		switch (newState) {
		case WAITING:
			show(STATE_WAITING);
			break;
		case PROGRESS:
			show(STATE_PROGRESS);
			break;
		case GOOD:
			show(STATE_GOOD);
			break;
		case BAD:
			show(STATE_BAD);
			break;
		}
		//Otherwise don't change.
	}

	protected boolean isGood() {
		String state = getCurrentState();
		return state != null && state.equals(STATE_GOOD);
	}

	public void suppressIfEmpty() {
		if (editText.getText().length() == 0) {
			show(STATE_WAITING);
		}
	}

	// We don't want no stinkin' monospace
	protected void fixPasswordFont() {
		if (isPasswordInputType(editText.getInputType())) {
			editText.setTypeface(Typeface.DEFAULT);
		}
	}


	// Copied from AOSP : TextView. What a pretty tower
	protected static boolean isPasswordInputType(int inputType) {
		final int variation =
			inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
		return variation
			== (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
			|| variation
			== (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
			|| variation
			== (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
	}


	/**
	 * Set the validator to be a default InputValidator with the supplied rule.
	 *
	 * @param rule The rule for the default InputValidator to use.
	 */
	public void setValidator(InputRule rule) {
		mValidator = new InputValidator(rule);
	}

	/**
	 * Set the validator to be used to determine which state to show in reaction to which input.
	 * <p/>
	 * If you would like to use the default InputValidator, you can just supply a rule to this
	 * function instead of an InputValidator class.
	 *
	 * @param validator The InputValidator to use for this class.
	 */
	public void setValidator(InputValidator validator) {
		mValidator = validator;
	}

	public StatusObservableWrapper getStatusObservable() {
		return mStatusObservable;
	}

	/////
	// State saving (from http://trickyandroid.com/saving-android-view-state-correctly/)
	/////

	@Override
	@NonNull
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.childrenStates = new SparseArray();
		for (int i = 0; i < getChildCount(); i++) {
			SparseArray<Parcelable> container = cast(ss.childrenStates);
			getChildAt(i).saveHierarchyState(container);
		}
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		for (int i = 0; i < getChildCount(); i++) {
			SparseArray<Parcelable> container = cast(ss.childrenStates);
			getChildAt(i).restoreHierarchyState(container);
		}
	}

	// https://weblogs.java.net/blog/emcmanus/archive/2007/03/getting_rid_of.html
	@SuppressWarnings("unchecked")
	private static <T> T cast(Object x) {
		return (T) x;
	}

	@Override
	protected void dispatchSaveInstanceState(@NonNull SparseArray<Parcelable> container) {
		dispatchFreezeSelfOnly(container);
	}

	@Override
	protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
		dispatchThawSelfOnly(container);
	}

	static class SavedState extends BaseSavedState {
		SparseArray childrenStates;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in, ClassLoader classLoader) {
			super(in);
			childrenStates = in.readSparseArray(classLoader);
		}

		@Override
		public void writeToParcel(@NonNull Parcel out, int flags) {
			super.writeToParcel(out, flags);
			SparseArray<Object> val = cast(childrenStates);
			out.writeSparseArray(val);
		}

		public static final ClassLoaderCreator<SavedState> CREATOR
			= new ClassLoaderCreator<SavedState>() {
			@Override
			@NonNull
			public SavedState createFromParcel(@NonNull Parcel source, ClassLoader loader) {
				return new SavedState(source, loader);
			}

			@Override
			public SavedState createFromParcel(@NonNull Parcel source) {
				return null;
			}

			@NonNull
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
