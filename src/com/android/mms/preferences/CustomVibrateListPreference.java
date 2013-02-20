package com.android.mms.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.mms.R;
import com.android.mms.ui.MessagingPreferenceActivity;

public class CustomVibrateListPreference extends ListPreference {
    private Context mContext;
    private boolean mDialogShowing;

    public CustomVibrateListPreference(Context context) {
        super(context);
        mContext = context;
    }

    public CustomVibrateListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = getValue();
            if (TextUtils.equals(value, "custom")) {
                showDialog();
            }
        }
    }

    private void showDialog() {
        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_vibrate_dialog, null);
        final EditText pattern = (EditText) v.findViewById(R.id.custom_vibrate_pattern);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String setting = prefs.getString(
                MessagingPreferenceActivity.NOTIFICATION_VIBRATE_PATTERN_CUSTOM, null);

        if (setting != null) {
            pattern.setText(setting);
        }

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.pref_title_mms_notification_vibrate_custom)
                .setView(v)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDialogShowing = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final SharedPreferences.Editor editor = prefs.edit();

                        editor.putString(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_PATTERN_CUSTOM,
                                pattern.getText().toString());
                        editor.commit();
                    }
                })
                .show();

        mDialogShowing = true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (mDialogShowing) {
            showDialog();
        }
    }

    @Override
    protected View onCreateDialogView() {
        mDialogShowing = false;
        return super.onCreateDialogView();
    }
}
