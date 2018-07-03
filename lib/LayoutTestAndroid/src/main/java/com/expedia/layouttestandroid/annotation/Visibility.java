package com.expedia.layouttestandroid.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.support.annotation.IntDef;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

@IntDef({ VISIBLE, INVISIBLE, GONE })
@Retention(RetentionPolicy.SOURCE)
public @interface Visibility {
}