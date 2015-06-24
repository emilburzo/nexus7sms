package com.emilburzo.nexus7sms.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.emilburzo.nexus7sms.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
