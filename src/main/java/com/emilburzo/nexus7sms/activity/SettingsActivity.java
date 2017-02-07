package com.emilburzo.nexus7sms.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;

import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Constants;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private Preference tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        initUi();

        initHandlers();
    }

    private void initUi() {
        tip = findPreference(Constants.Settings.PRIVACY_POLICY);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF0099CC")));
        getSupportActionBar().setTitle(getString(R.string.action_settings));
    }


    private void initHandlers() {
        tip.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse(Constants.Links.PRIVACY_POLICY);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }
}