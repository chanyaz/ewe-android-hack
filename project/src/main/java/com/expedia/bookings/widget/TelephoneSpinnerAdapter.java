package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.extensions.TextViewExtensions;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.util.Ui;

public class TelephoneSpinnerAdapter extends ArrayAdapter<String> {
	private static final Map<String, Integer> COUNTRY_CODES = new HashMap<String, Integer>();

	private int[] mCountryPhoneCodes;
	private String[] mCountryNames;
	private int mCurrentPosition;

	public TelephoneSpinnerAdapter(Context context) {
		this(context, R.layout.simple_spinner_item);
	}

	public TelephoneSpinnerAdapter(Context context, int textViewResourceId) {
		this(context, textViewResourceId, R.layout.simple_spinner_dropdown_item);
	}

	public TelephoneSpinnerAdapter(Context context, int textViewResId, int dropDownViewResId) {
		super(context, textViewResId);
		setDropDownViewResource(dropDownViewResId);
		init(context);
	}

	private void init(Context context) {
		final Resources res = context.getResources();
		mCountryPhoneCodes = res.getIntArray(R.array.country_phone_codes);
		mCountryNames = res.getStringArray(R.array.country_names);
		fillCountryCodes(context);
	}

	@Override
	public int getCount() {
		return mCountryPhoneCodes.length;
	}

	@Override
	public String getItem(int position) {
		return String.format(Locale.getDefault(), "%s (%d)", getCountryName(position), getCountryCode(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		CharSequence item = getItem(position);
		Spannable stringToSpan = new SpannableString(String.format(getItem(position), item));
		tv.setText(stringToSpan);
		TextViewExtensions.Companion.setTextColorBasedOnPosition(tv, mCurrentPosition, position);

		return retView;
	}

	public String getCountryName(int position) {
		return mCountryNames[position];
	}

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

	public int getCountryCode(int position) {
		if (COUNTRY_CODES.containsKey(getCountryName(position))) {
			return COUNTRY_CODES.get(getCountryName(position));
		}

		return mCountryPhoneCodes[position];
	}

	public int getCountryCodeFromCountryName(String countryName) {
		return COUNTRY_CODES.get(countryName);
	}

	public int getPositionFromName(String countryName) {
		if (Strings.isEmpty(countryName)) {
			return mCurrentPosition;
		}
		for (int i = 0; i < COUNTRY_CODES.size(); i++) {
			if (getCountryName(i).equals(countryName)) {
				return i;
			}
		}
		return mCurrentPosition;
	}

	private void fillCountryCodes(Context context) {
		final Resources res = context.getResources();

		COUNTRY_CODES.put(res.getString(R.string.country_af), res.getInteger(R.integer.country_phone_code_af));
		COUNTRY_CODES.put(res.getString(R.string.country_al), res.getInteger(R.integer.country_phone_code_al));
		COUNTRY_CODES.put(res.getString(R.string.country_dz), res.getInteger(R.integer.country_phone_code_dz));
		COUNTRY_CODES.put(res.getString(R.string.country_as), res.getInteger(R.integer.country_phone_code_as));
		COUNTRY_CODES.put(res.getString(R.string.country_ad), res.getInteger(R.integer.country_phone_code_ad));
		COUNTRY_CODES.put(res.getString(R.string.country_ao), res.getInteger(R.integer.country_phone_code_ao));
		COUNTRY_CODES.put(res.getString(R.string.country_ai), res.getInteger(R.integer.country_phone_code_ai));
		COUNTRY_CODES.put(res.getString(R.string.country_aq), res.getInteger(R.integer.country_phone_code_aq));
		COUNTRY_CODES.put(res.getString(R.string.country_ag), res.getInteger(R.integer.country_phone_code_ag));
		COUNTRY_CODES.put(res.getString(R.string.country_ar), res.getInteger(R.integer.country_phone_code_ar));
		COUNTRY_CODES.put(res.getString(R.string.country_am), res.getInteger(R.integer.country_phone_code_am));
		COUNTRY_CODES.put(res.getString(R.string.country_aw), res.getInteger(R.integer.country_phone_code_aw));
		COUNTRY_CODES.put(res.getString(R.string.country_au), res.getInteger(R.integer.country_phone_code_au));
		COUNTRY_CODES.put(res.getString(R.string.country_at), res.getInteger(R.integer.country_phone_code_at));
		COUNTRY_CODES.put(res.getString(R.string.country_az), res.getInteger(R.integer.country_phone_code_az));
		COUNTRY_CODES.put(res.getString(R.string.country_bs), res.getInteger(R.integer.country_phone_code_bs));
		COUNTRY_CODES.put(res.getString(R.string.country_bh), res.getInteger(R.integer.country_phone_code_bh));
		COUNTRY_CODES.put(res.getString(R.string.country_bd), res.getInteger(R.integer.country_phone_code_bd));
		COUNTRY_CODES.put(res.getString(R.string.country_bb), res.getInteger(R.integer.country_phone_code_bb));
		COUNTRY_CODES.put(res.getString(R.string.country_by), res.getInteger(R.integer.country_phone_code_by));
		COUNTRY_CODES.put(res.getString(R.string.country_be), res.getInteger(R.integer.country_phone_code_be));
		COUNTRY_CODES.put(res.getString(R.string.country_bz), res.getInteger(R.integer.country_phone_code_bz));
		COUNTRY_CODES.put(res.getString(R.string.country_bj), res.getInteger(R.integer.country_phone_code_bj));
		COUNTRY_CODES.put(res.getString(R.string.country_bm), res.getInteger(R.integer.country_phone_code_bm));
		COUNTRY_CODES.put(res.getString(R.string.country_bt), res.getInteger(R.integer.country_phone_code_bt));
		COUNTRY_CODES.put(res.getString(R.string.country_bo), res.getInteger(R.integer.country_phone_code_bo));
		COUNTRY_CODES.put(res.getString(R.string.country_ba), res.getInteger(R.integer.country_phone_code_ba));
		COUNTRY_CODES.put(res.getString(R.string.country_bw), res.getInteger(R.integer.country_phone_code_bw));
		COUNTRY_CODES.put(res.getString(R.string.country_br), res.getInteger(R.integer.country_phone_code_br));
		COUNTRY_CODES.put(res.getString(R.string.country_io), res.getInteger(R.integer.country_phone_code_io));
		COUNTRY_CODES.put(res.getString(R.string.country_bn), res.getInteger(R.integer.country_phone_code_bn));
		COUNTRY_CODES.put(res.getString(R.string.country_bg), res.getInteger(R.integer.country_phone_code_bg));
		COUNTRY_CODES.put(res.getString(R.string.country_bf), res.getInteger(R.integer.country_phone_code_bf));
		COUNTRY_CODES.put(res.getString(R.string.country_bi), res.getInteger(R.integer.country_phone_code_bi));
		COUNTRY_CODES.put(res.getString(R.string.country_kh), res.getInteger(R.integer.country_phone_code_kh));
		COUNTRY_CODES.put(res.getString(R.string.country_cm), res.getInteger(R.integer.country_phone_code_cm));
		COUNTRY_CODES.put(res.getString(R.string.country_ca), res.getInteger(R.integer.country_phone_code_ca));
		COUNTRY_CODES.put(res.getString(R.string.country_cv), res.getInteger(R.integer.country_phone_code_cv));
		COUNTRY_CODES.put(res.getString(R.string.country_ky), res.getInteger(R.integer.country_phone_code_ky));
		COUNTRY_CODES.put(res.getString(R.string.country_cf), res.getInteger(R.integer.country_phone_code_cf));
		COUNTRY_CODES.put(res.getString(R.string.country_td), res.getInteger(R.integer.country_phone_code_td));
		COUNTRY_CODES.put(res.getString(R.string.country_cl), res.getInteger(R.integer.country_phone_code_cl));
		COUNTRY_CODES.put(res.getString(R.string.country_cn), res.getInteger(R.integer.country_phone_code_cn));
		COUNTRY_CODES.put(res.getString(R.string.country_cx), res.getInteger(R.integer.country_phone_code_cx));
		COUNTRY_CODES.put(res.getString(R.string.country_cc), res.getInteger(R.integer.country_phone_code_cc));
		COUNTRY_CODES.put(res.getString(R.string.country_co), res.getInteger(R.integer.country_phone_code_co));
		COUNTRY_CODES.put(res.getString(R.string.country_km), res.getInteger(R.integer.country_phone_code_km));
		COUNTRY_CODES.put(res.getString(R.string.country_cg), res.getInteger(R.integer.country_phone_code_cg));
		COUNTRY_CODES.put(res.getString(R.string.country_cd), res.getInteger(R.integer.country_phone_code_cd));
		COUNTRY_CODES.put(res.getString(R.string.country_ck), res.getInteger(R.integer.country_phone_code_ck));
		COUNTRY_CODES.put(res.getString(R.string.country_cr), res.getInteger(R.integer.country_phone_code_cr));
		COUNTRY_CODES.put(res.getString(R.string.country_hr), res.getInteger(R.integer.country_phone_code_hr));
		COUNTRY_CODES.put(res.getString(R.string.country_cy), res.getInteger(R.integer.country_phone_code_cy));
		COUNTRY_CODES.put(res.getString(R.string.country_cz), res.getInteger(R.integer.country_phone_code_cz));
		COUNTRY_CODES.put(res.getString(R.string.country_ci), res.getInteger(R.integer.country_phone_code_ci));
		COUNTRY_CODES.put(res.getString(R.string.country_dk), res.getInteger(R.integer.country_phone_code_dk));
		COUNTRY_CODES.put(res.getString(R.string.country_dj), res.getInteger(R.integer.country_phone_code_dj));
		COUNTRY_CODES.put(res.getString(R.string.country_dm), res.getInteger(R.integer.country_phone_code_dm));
		COUNTRY_CODES.put(res.getString(R.string.country_do), res.getInteger(R.integer.country_phone_code_do));
		COUNTRY_CODES.put(res.getString(R.string.country_ec), res.getInteger(R.integer.country_phone_code_ec));
		COUNTRY_CODES.put(res.getString(R.string.country_eg), res.getInteger(R.integer.country_phone_code_eg));
		COUNTRY_CODES.put(res.getString(R.string.country_sv), res.getInteger(R.integer.country_phone_code_sv));
		COUNTRY_CODES.put(res.getString(R.string.country_gq), res.getInteger(R.integer.country_phone_code_gq));
		COUNTRY_CODES.put(res.getString(R.string.country_er), res.getInteger(R.integer.country_phone_code_er));
		COUNTRY_CODES.put(res.getString(R.string.country_ee), res.getInteger(R.integer.country_phone_code_ee));
		COUNTRY_CODES.put(res.getString(R.string.country_et), res.getInteger(R.integer.country_phone_code_et));
		COUNTRY_CODES.put(res.getString(R.string.country_fk), res.getInteger(R.integer.country_phone_code_fk));
		COUNTRY_CODES.put(res.getString(R.string.country_fo), res.getInteger(R.integer.country_phone_code_fo));
		COUNTRY_CODES.put(res.getString(R.string.country_fj), res.getInteger(R.integer.country_phone_code_fj));
		COUNTRY_CODES.put(res.getString(R.string.country_fi), res.getInteger(R.integer.country_phone_code_fi));
		COUNTRY_CODES.put(res.getString(R.string.country_fr), res.getInteger(R.integer.country_phone_code_fr));
		COUNTRY_CODES.put(res.getString(R.string.country_gf), res.getInteger(R.integer.country_phone_code_gf));
		COUNTRY_CODES.put(res.getString(R.string.country_pf), res.getInteger(R.integer.country_phone_code_pf));
		COUNTRY_CODES.put(res.getString(R.string.country_ga), res.getInteger(R.integer.country_phone_code_ga));
		COUNTRY_CODES.put(res.getString(R.string.country_gm), res.getInteger(R.integer.country_phone_code_gm));
		COUNTRY_CODES.put(res.getString(R.string.country_ge), res.getInteger(R.integer.country_phone_code_ge));
		COUNTRY_CODES.put(res.getString(R.string.country_de), res.getInteger(R.integer.country_phone_code_de));
		COUNTRY_CODES.put(res.getString(R.string.country_gh), res.getInteger(R.integer.country_phone_code_gh));
		COUNTRY_CODES.put(res.getString(R.string.country_gi), res.getInteger(R.integer.country_phone_code_gi));
		COUNTRY_CODES.put(res.getString(R.string.country_gr), res.getInteger(R.integer.country_phone_code_gr));
		COUNTRY_CODES.put(res.getString(R.string.country_gl), res.getInteger(R.integer.country_phone_code_gl));
		COUNTRY_CODES.put(res.getString(R.string.country_gd), res.getInteger(R.integer.country_phone_code_gd));
		COUNTRY_CODES.put(res.getString(R.string.country_gp), res.getInteger(R.integer.country_phone_code_gp));
		COUNTRY_CODES.put(res.getString(R.string.country_gu), res.getInteger(R.integer.country_phone_code_gu));
		COUNTRY_CODES.put(res.getString(R.string.country_gt), res.getInteger(R.integer.country_phone_code_gt));
		COUNTRY_CODES.put(res.getString(R.string.country_gg), res.getInteger(R.integer.country_phone_code_gg));
		COUNTRY_CODES.put(res.getString(R.string.country_gn), res.getInteger(R.integer.country_phone_code_gn));
		COUNTRY_CODES.put(res.getString(R.string.country_gw), res.getInteger(R.integer.country_phone_code_gw));
		COUNTRY_CODES.put(res.getString(R.string.country_gy), res.getInteger(R.integer.country_phone_code_gy));
		COUNTRY_CODES.put(res.getString(R.string.country_ht), res.getInteger(R.integer.country_phone_code_ht));
		COUNTRY_CODES.put(res.getString(R.string.country_va), res.getInteger(R.integer.country_phone_code_va));
		COUNTRY_CODES.put(res.getString(R.string.country_hn), res.getInteger(R.integer.country_phone_code_hn));
		COUNTRY_CODES.put(res.getString(R.string.country_hk), res.getInteger(R.integer.country_phone_code_hk));
		COUNTRY_CODES.put(res.getString(R.string.country_hu), res.getInteger(R.integer.country_phone_code_hu));
		COUNTRY_CODES.put(res.getString(R.string.country_is), res.getInteger(R.integer.country_phone_code_is));
		COUNTRY_CODES.put(res.getString(R.string.country_in), res.getInteger(R.integer.country_phone_code_in));
		COUNTRY_CODES.put(res.getString(R.string.country_id), res.getInteger(R.integer.country_phone_code_id));
		COUNTRY_CODES.put(res.getString(R.string.country_ir), res.getInteger(R.integer.country_phone_code_ir));
		COUNTRY_CODES.put(res.getString(R.string.country_iq), res.getInteger(R.integer.country_phone_code_iq));
		COUNTRY_CODES.put(res.getString(R.string.country_ie), res.getInteger(R.integer.country_phone_code_ie));
		COUNTRY_CODES.put(res.getString(R.string.country_im), res.getInteger(R.integer.country_phone_code_im));
		COUNTRY_CODES.put(res.getString(R.string.country_il), res.getInteger(R.integer.country_phone_code_il));
		COUNTRY_CODES.put(res.getString(R.string.country_it), res.getInteger(R.integer.country_phone_code_it));
		COUNTRY_CODES.put(res.getString(R.string.country_jm), res.getInteger(R.integer.country_phone_code_jm));
		COUNTRY_CODES.put(res.getString(R.string.country_jp), res.getInteger(R.integer.country_phone_code_jp));
		COUNTRY_CODES.put(res.getString(R.string.country_je), res.getInteger(R.integer.country_phone_code_je));
		COUNTRY_CODES.put(res.getString(R.string.country_jo), res.getInteger(R.integer.country_phone_code_jo));
		COUNTRY_CODES.put(res.getString(R.string.country_kz), res.getInteger(R.integer.country_phone_code_kz));
		COUNTRY_CODES.put(res.getString(R.string.country_ke), res.getInteger(R.integer.country_phone_code_ke));
		COUNTRY_CODES.put(res.getString(R.string.country_ki), res.getInteger(R.integer.country_phone_code_ki));
		COUNTRY_CODES.put(res.getString(R.string.country_kp), res.getInteger(R.integer.country_phone_code_kp));
		COUNTRY_CODES.put(res.getString(R.string.country_kr), res.getInteger(R.integer.country_phone_code_kr));
		COUNTRY_CODES.put(res.getString(R.string.country_kw), res.getInteger(R.integer.country_phone_code_kw));
		COUNTRY_CODES.put(res.getString(R.string.country_kg), res.getInteger(R.integer.country_phone_code_kg));
		COUNTRY_CODES.put(res.getString(R.string.country_la), res.getInteger(R.integer.country_phone_code_la));
		COUNTRY_CODES.put(res.getString(R.string.country_lv), res.getInteger(R.integer.country_phone_code_lv));
		COUNTRY_CODES.put(res.getString(R.string.country_lb), res.getInteger(R.integer.country_phone_code_lb));
		COUNTRY_CODES.put(res.getString(R.string.country_ls), res.getInteger(R.integer.country_phone_code_ls));
		COUNTRY_CODES.put(res.getString(R.string.country_lr), res.getInteger(R.integer.country_phone_code_lr));
		COUNTRY_CODES.put(res.getString(R.string.country_ly), res.getInteger(R.integer.country_phone_code_ly));
		COUNTRY_CODES.put(res.getString(R.string.country_li), res.getInteger(R.integer.country_phone_code_li));
		COUNTRY_CODES.put(res.getString(R.string.country_lt), res.getInteger(R.integer.country_phone_code_lt));
		COUNTRY_CODES.put(res.getString(R.string.country_lu), res.getInteger(R.integer.country_phone_code_lu));
		COUNTRY_CODES.put(res.getString(R.string.country_mo), res.getInteger(R.integer.country_phone_code_mo));
		COUNTRY_CODES.put(res.getString(R.string.country_mk), res.getInteger(R.integer.country_phone_code_mk));
		COUNTRY_CODES.put(res.getString(R.string.country_mg), res.getInteger(R.integer.country_phone_code_mg));
		COUNTRY_CODES.put(res.getString(R.string.country_mw), res.getInteger(R.integer.country_phone_code_mw));
		COUNTRY_CODES.put(res.getString(R.string.country_my), res.getInteger(R.integer.country_phone_code_my));
		COUNTRY_CODES.put(res.getString(R.string.country_mv), res.getInteger(R.integer.country_phone_code_mv));
		COUNTRY_CODES.put(res.getString(R.string.country_ml), res.getInteger(R.integer.country_phone_code_ml));
		COUNTRY_CODES.put(res.getString(R.string.country_mt), res.getInteger(R.integer.country_phone_code_mt));
		COUNTRY_CODES.put(res.getString(R.string.country_mh), res.getInteger(R.integer.country_phone_code_mh));
		COUNTRY_CODES.put(res.getString(R.string.country_mq), res.getInteger(R.integer.country_phone_code_mq));
		COUNTRY_CODES.put(res.getString(R.string.country_mr), res.getInteger(R.integer.country_phone_code_mr));
		COUNTRY_CODES.put(res.getString(R.string.country_mu), res.getInteger(R.integer.country_phone_code_mu));
		COUNTRY_CODES.put(res.getString(R.string.country_yt), res.getInteger(R.integer.country_phone_code_yt));
		COUNTRY_CODES.put(res.getString(R.string.country_mx), res.getInteger(R.integer.country_phone_code_mx));
		COUNTRY_CODES.put(res.getString(R.string.country_fm), res.getInteger(R.integer.country_phone_code_fm));
		COUNTRY_CODES.put(res.getString(R.string.country_md), res.getInteger(R.integer.country_phone_code_md));
		COUNTRY_CODES.put(res.getString(R.string.country_mc), res.getInteger(R.integer.country_phone_code_mc));
		COUNTRY_CODES.put(res.getString(R.string.country_mn), res.getInteger(R.integer.country_phone_code_mn));
		COUNTRY_CODES.put(res.getString(R.string.country_me), res.getInteger(R.integer.country_phone_code_me));
		COUNTRY_CODES.put(res.getString(R.string.country_ms), res.getInteger(R.integer.country_phone_code_ms));
		COUNTRY_CODES.put(res.getString(R.string.country_ma), res.getInteger(R.integer.country_phone_code_ma));
		COUNTRY_CODES.put(res.getString(R.string.country_mz), res.getInteger(R.integer.country_phone_code_mz));
		COUNTRY_CODES.put(res.getString(R.string.country_mm), res.getInteger(R.integer.country_phone_code_mm));
		COUNTRY_CODES.put(res.getString(R.string.country_na), res.getInteger(R.integer.country_phone_code_na));
		COUNTRY_CODES.put(res.getString(R.string.country_nr), res.getInteger(R.integer.country_phone_code_nr));
		COUNTRY_CODES.put(res.getString(R.string.country_np), res.getInteger(R.integer.country_phone_code_np));
		COUNTRY_CODES.put(res.getString(R.string.country_nl), res.getInteger(R.integer.country_phone_code_nl));
		COUNTRY_CODES.put(res.getString(R.string.country_an), res.getInteger(R.integer.country_phone_code_an));
		COUNTRY_CODES.put(res.getString(R.string.country_nc), res.getInteger(R.integer.country_phone_code_nc));
		COUNTRY_CODES.put(res.getString(R.string.country_nz), res.getInteger(R.integer.country_phone_code_nz));
		COUNTRY_CODES.put(res.getString(R.string.country_ni), res.getInteger(R.integer.country_phone_code_ni));
		COUNTRY_CODES.put(res.getString(R.string.country_ne), res.getInteger(R.integer.country_phone_code_ne));
		COUNTRY_CODES.put(res.getString(R.string.country_ng), res.getInteger(R.integer.country_phone_code_ng));
		COUNTRY_CODES.put(res.getString(R.string.country_nu), res.getInteger(R.integer.country_phone_code_nu));
		COUNTRY_CODES.put(res.getString(R.string.country_nf), res.getInteger(R.integer.country_phone_code_nf));
		COUNTRY_CODES.put(res.getString(R.string.country_mp), res.getInteger(R.integer.country_phone_code_mp));
		COUNTRY_CODES.put(res.getString(R.string.country_no), res.getInteger(R.integer.country_phone_code_no));
		COUNTRY_CODES.put(res.getString(R.string.country_om), res.getInteger(R.integer.country_phone_code_om));
		COUNTRY_CODES.put(res.getString(R.string.country_pk), res.getInteger(R.integer.country_phone_code_pk));
		COUNTRY_CODES.put(res.getString(R.string.country_pw), res.getInteger(R.integer.country_phone_code_pw));
		COUNTRY_CODES.put(res.getString(R.string.country_ps), res.getInteger(R.integer.country_phone_code_ps));
		COUNTRY_CODES.put(res.getString(R.string.country_pa), res.getInteger(R.integer.country_phone_code_pa));
		COUNTRY_CODES.put(res.getString(R.string.country_pg), res.getInteger(R.integer.country_phone_code_pg));
		COUNTRY_CODES.put(res.getString(R.string.country_py), res.getInteger(R.integer.country_phone_code_py));
		COUNTRY_CODES.put(res.getString(R.string.country_pe), res.getInteger(R.integer.country_phone_code_pe));
		COUNTRY_CODES.put(res.getString(R.string.country_ph), res.getInteger(R.integer.country_phone_code_ph));
		COUNTRY_CODES.put(res.getString(R.string.country_pn), res.getInteger(R.integer.country_phone_code_pn));
		COUNTRY_CODES.put(res.getString(R.string.country_pl), res.getInteger(R.integer.country_phone_code_pl));
		COUNTRY_CODES.put(res.getString(R.string.country_pt), res.getInteger(R.integer.country_phone_code_pt));
		COUNTRY_CODES.put(res.getString(R.string.country_pr), res.getInteger(R.integer.country_phone_code_pr));
		COUNTRY_CODES.put(res.getString(R.string.country_qa), res.getInteger(R.integer.country_phone_code_qa));
		COUNTRY_CODES.put(res.getString(R.string.country_ro), res.getInteger(R.integer.country_phone_code_ro));
		COUNTRY_CODES.put(res.getString(R.string.country_ru), res.getInteger(R.integer.country_phone_code_ru));
		COUNTRY_CODES.put(res.getString(R.string.country_rw), res.getInteger(R.integer.country_phone_code_rw));
		COUNTRY_CODES.put(res.getString(R.string.country_re), res.getInteger(R.integer.country_phone_code_re));
		COUNTRY_CODES.put(res.getString(R.string.country_bl), res.getInteger(R.integer.country_phone_code_bl));
		COUNTRY_CODES.put(res.getString(R.string.country_sh), res.getInteger(R.integer.country_phone_code_sh));
		COUNTRY_CODES.put(res.getString(R.string.country_kn), res.getInteger(R.integer.country_phone_code_kn));
		COUNTRY_CODES.put(res.getString(R.string.country_lc), res.getInteger(R.integer.country_phone_code_lc));
		COUNTRY_CODES.put(res.getString(R.string.country_mf), res.getInteger(R.integer.country_phone_code_mf));
		COUNTRY_CODES.put(res.getString(R.string.country_pm), res.getInteger(R.integer.country_phone_code_pm));
		COUNTRY_CODES.put(res.getString(R.string.country_vc), res.getInteger(R.integer.country_phone_code_vc));
		COUNTRY_CODES.put(res.getString(R.string.country_ws), res.getInteger(R.integer.country_phone_code_ws));
		COUNTRY_CODES.put(res.getString(R.string.country_sm), res.getInteger(R.integer.country_phone_code_sm));
		COUNTRY_CODES.put(res.getString(R.string.country_st), res.getInteger(R.integer.country_phone_code_st));
		COUNTRY_CODES.put(res.getString(R.string.country_sa), res.getInteger(R.integer.country_phone_code_sa));
		COUNTRY_CODES.put(res.getString(R.string.country_sn), res.getInteger(R.integer.country_phone_code_sn));
		COUNTRY_CODES.put(res.getString(R.string.country_rs), res.getInteger(R.integer.country_phone_code_rs));
		COUNTRY_CODES.put(res.getString(R.string.country_sc), res.getInteger(R.integer.country_phone_code_sc));
		COUNTRY_CODES.put(res.getString(R.string.country_sl), res.getInteger(R.integer.country_phone_code_sl));
		COUNTRY_CODES.put(res.getString(R.string.country_sg), res.getInteger(R.integer.country_phone_code_sg));
		COUNTRY_CODES.put(res.getString(R.string.country_sk), res.getInteger(R.integer.country_phone_code_sk));
		COUNTRY_CODES.put(res.getString(R.string.country_si), res.getInteger(R.integer.country_phone_code_si));
		COUNTRY_CODES.put(res.getString(R.string.country_sb), res.getInteger(R.integer.country_phone_code_sb));
		COUNTRY_CODES.put(res.getString(R.string.country_so), res.getInteger(R.integer.country_phone_code_so));
		COUNTRY_CODES.put(res.getString(R.string.country_za), res.getInteger(R.integer.country_phone_code_za));
		COUNTRY_CODES.put(res.getString(R.string.country_gs), res.getInteger(R.integer.country_phone_code_gs));
		COUNTRY_CODES.put(res.getString(R.string.country_es), res.getInteger(R.integer.country_phone_code_es));
		COUNTRY_CODES.put(res.getString(R.string.country_lk), res.getInteger(R.integer.country_phone_code_lk));
		COUNTRY_CODES.put(res.getString(R.string.country_sd), res.getInteger(R.integer.country_phone_code_sd));
		COUNTRY_CODES.put(res.getString(R.string.country_sr), res.getInteger(R.integer.country_phone_code_sr));
		COUNTRY_CODES.put(res.getString(R.string.country_sj), res.getInteger(R.integer.country_phone_code_sj));
		COUNTRY_CODES.put(res.getString(R.string.country_sz), res.getInteger(R.integer.country_phone_code_sz));
		COUNTRY_CODES.put(res.getString(R.string.country_se), res.getInteger(R.integer.country_phone_code_se));
		COUNTRY_CODES.put(res.getString(R.string.country_ch), res.getInteger(R.integer.country_phone_code_ch));
		COUNTRY_CODES.put(res.getString(R.string.country_sy), res.getInteger(R.integer.country_phone_code_sy));
		COUNTRY_CODES.put(res.getString(R.string.country_tw), res.getInteger(R.integer.country_phone_code_tw));
		COUNTRY_CODES.put(res.getString(R.string.country_tj), res.getInteger(R.integer.country_phone_code_tj));
		COUNTRY_CODES.put(res.getString(R.string.country_tz), res.getInteger(R.integer.country_phone_code_tz));
		COUNTRY_CODES.put(res.getString(R.string.country_th), res.getInteger(R.integer.country_phone_code_th));
		COUNTRY_CODES.put(res.getString(R.string.country_tl), res.getInteger(R.integer.country_phone_code_tl));
		COUNTRY_CODES.put(res.getString(R.string.country_tg), res.getInteger(R.integer.country_phone_code_tg));
		COUNTRY_CODES.put(res.getString(R.string.country_tk), res.getInteger(R.integer.country_phone_code_tk));
		COUNTRY_CODES.put(res.getString(R.string.country_to), res.getInteger(R.integer.country_phone_code_to));
		COUNTRY_CODES.put(res.getString(R.string.country_tt), res.getInteger(R.integer.country_phone_code_tt));
		COUNTRY_CODES.put(res.getString(R.string.country_tn), res.getInteger(R.integer.country_phone_code_tn));
		COUNTRY_CODES.put(res.getString(R.string.country_tr), res.getInteger(R.integer.country_phone_code_tr));
		COUNTRY_CODES.put(res.getString(R.string.country_tm), res.getInteger(R.integer.country_phone_code_tm));
		COUNTRY_CODES.put(res.getString(R.string.country_tc), res.getInteger(R.integer.country_phone_code_tc));
		COUNTRY_CODES.put(res.getString(R.string.country_tv), res.getInteger(R.integer.country_phone_code_tv));
		COUNTRY_CODES.put(res.getString(R.string.country_ug), res.getInteger(R.integer.country_phone_code_ug));
		COUNTRY_CODES.put(res.getString(R.string.country_ua), res.getInteger(R.integer.country_phone_code_ua));
		COUNTRY_CODES.put(res.getString(R.string.country_ae), res.getInteger(R.integer.country_phone_code_ae));
		COUNTRY_CODES.put(res.getString(R.string.country_gb), res.getInteger(R.integer.country_phone_code_gb));
		COUNTRY_CODES.put(res.getString(R.string.country_us), res.getInteger(R.integer.country_phone_code_us));
		COUNTRY_CODES.put(res.getString(R.string.country_um), res.getInteger(R.integer.country_phone_code_um));
		COUNTRY_CODES.put(res.getString(R.string.country_uy), res.getInteger(R.integer.country_phone_code_uy));
		COUNTRY_CODES.put(res.getString(R.string.country_uz), res.getInteger(R.integer.country_phone_code_uz));
		COUNTRY_CODES.put(res.getString(R.string.country_vu), res.getInteger(R.integer.country_phone_code_vu));
		COUNTRY_CODES.put(res.getString(R.string.country_ve), res.getInteger(R.integer.country_phone_code_ve));
		COUNTRY_CODES.put(res.getString(R.string.country_vn), res.getInteger(R.integer.country_phone_code_vn));
		COUNTRY_CODES.put(res.getString(R.string.country_vg), res.getInteger(R.integer.country_phone_code_vg));
		COUNTRY_CODES.put(res.getString(R.string.country_vi), res.getInteger(R.integer.country_phone_code_vi));
		COUNTRY_CODES.put(res.getString(R.string.country_wf), res.getInteger(R.integer.country_phone_code_wf));
		COUNTRY_CODES.put(res.getString(R.string.country_eh), res.getInteger(R.integer.country_phone_code_eh));
		COUNTRY_CODES.put(res.getString(R.string.country_ye), res.getInteger(R.integer.country_phone_code_ye));
		COUNTRY_CODES.put(res.getString(R.string.country_zm), res.getInteger(R.integer.country_phone_code_zm));
		COUNTRY_CODES.put(res.getString(R.string.country_zw), res.getInteger(R.integer.country_phone_code_zw));
		COUNTRY_CODES.put(res.getString(R.string.country_ax), res.getInteger(R.integer.country_phone_code_ax));
	}
}
