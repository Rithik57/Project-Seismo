package com.example.earthquakedata;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class quakeSettings extends AppCompatActivity {
    private final String LOGTAG = "quake Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Log.e(LOGTAG,"the quake settings activity was run");
    }

    public static class SettingsFragment extends PreferenceFragment implements Preference

            .OnPreferenceChangeListener{
        String LOGTAG = "settings Fragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.e(LOGTAG,"the settings fragment was run");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);

            Preference orderBy =  findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

        }

        @Override
        public void onPause() {
            Log.e(LOGTAG,"the settings fragment was paused");
            super.onPause();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Log.e(LOGTAG,"the preferenceChange method was called in the settings activity");
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            Log.e(LOGTAG,"the bindPreference method was called in the settings activity");
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}