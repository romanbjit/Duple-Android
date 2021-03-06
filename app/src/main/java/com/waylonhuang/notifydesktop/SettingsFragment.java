package com.waylonhuang.notifydesktop;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Date;

import static com.waylonhuang.notifydesktop.MainActivity.PREFS_FILE;
import static com.waylonhuang.notifydesktop.MainActivity.REAL_AD_ID;
import static com.waylonhuang.notifydesktop.MainActivity.TEST_AD_ID;

/**
 * Created by Waylon on 4/25/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements RewardedVideoAdListener {
    public static final int RED_COLOR = 0;
    public static final int BLUE_COLOR = 1;
    public static final int GREEN_COLOR = 2;
    public static final int TEAL_COLOR = 3;
    public static final int AMBER_COLOR = 4;

    private RewardedVideoAd mAd;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        if (fragment.getArguments() == null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    private void loadRewardedVideoAd() {
        mAd.loadAd(REAL_AD_ID, new AdRequest.Builder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");

        mAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        final SharedPreferences settings = getActivity().getSharedPreferences(PREFS_FILE, 0);
        final String appPackageName = getActivity().getPackageName();

        int themeColor = settings.getInt("theme_color", RED_COLOR);
        ListPreference colorPref = (ListPreference) findPreference("theme_color");
        colorPref.setDefaultValue(themeColor);
        colorPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof String) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("theme_color", Integer.parseInt((String) newValue));
                    editor.apply();
                    getActivity().recreate();
                }
                return true;
            }
        });

        CheckBoxPreference historyPref = (CheckBoxPreference) findPreference("history");
        historyPref.setChecked(settings.getBoolean("history", true));
        historyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("history", (Boolean) newValue);
                    editor.apply();
                }
                return true;
            }
        });

        Preference clearHistoryPref = (Preference) findPreference("clear_history");
        clearHistoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                clearHistory();
                return true;
            }
        });

        Preference videoPref = (Preference) findPreference("video");
        videoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (mAd.isLoaded()) {
                    mAd.show();
                }
                return true;
            }
        });

        Preference playStorePref = (Preference) findPreference("rate");
        playStorePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                return true;
            }
        });

        Preference resetPref = (Preference) findPreference("reset");
        resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                clearPreferences();
                return true;
            }
        });

        Preference mailPref = (Preference) findPreference("contact");
        mailPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dupleapp@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Duple App Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Feedback for Duple app:\n");

                startActivity(Intent.createChooser(intent, "Send Email"));
                return true;
            }
        });

        Preference buildPref = (Preference) findPreference("build");
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        buildPref.setSummary(buildDate.toString());

        Preference sourcePref = (Preference) findPreference("source");
        PackageManager packageManager = getContext().getPackageManager();
        String sourceStr = packageManager.getInstallerPackageName(appPackageName);
        if (sourceStr == null) {
            sourceStr = "Side Loaded";
        } else {
            sourceStr = "Google Play";
        }
        sourcePref.setSummary(sourceStr);

        Preference versionPref = (Preference) findPreference("version");
        versionPref.setSummary(BuildConfig.VERSION_NAME);

        Preference privacyPref = (Preference) findPreference("privacy");
        privacyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showPrivacy();
                return true;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void clearPreferences() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Clear all preferences set by the user")
                .setTitle("Reset preferences");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences settings = getActivity().getSharedPreferences(PREFS_FILE, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putBoolean("history", true);
                editor.putString("offApps", "");
                editor.putString("titleOnlyApps", "");
                editor.putInt("theme_color", RED_COLOR);
                editor.apply();

                Toast.makeText(getActivity(), "Preferences reset", Toast.LENGTH_SHORT).show();
                getActivity().recreate();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clearHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Delete all local notification records")
                .setTitle("Clear History");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final NotificationSQLiteHelper helper = NotificationSQLiteHelper.getInstance(getActivity());
                helper.deleteAllItems();
                Snackbar.make(getView(), "History cleared", Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPrivacy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String message = "This application does not collect and/or permanently store any user data and information on private or public servers. " +
                "Notification data is retained for as long as it takes to transmit the information from your device to " +
                "the cloud and to your other devices through Google Firebase. There is a local copy of notifications " +
                "that are stored for user reference and convenience which can be permanently cleared at any time.";
        builder.setMessage(message).setTitle("Privacy Policy");
        builder.setPositiveButton(R.string.ok, null);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Required to reward the user.
    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(getActivity(), "onRewarded! currency: " + reward.getType() + "  amount: " + reward.getAmount(), Toast.LENGTH_SHORT).show();
        // Reward the user.
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
//        Toast.makeText(getActivity(), "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        System.out.println("Failed to load video ad.");
    }

    @Override
    public void onRewardedVideoAdLoaded() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
        System.out.println("Loaded video ad!");
    }

    @Override
    public void onRewardedVideoAdOpened() {
//        Toast.makeText(getActivity(), "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(getActivity(), "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        mAd.resume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        mAd.pause(getActivity());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mAd.destroy(getActivity());
        super.onDestroy();
    }
}
