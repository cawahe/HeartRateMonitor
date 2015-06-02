package eu.vranckaert.heart.rate.monitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import eu.vranckaert.hear.rate.monitor.shared.model.ActivityState;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 01/06/15
 * Time: 17:27
 *
 * @author Dirk Vranckaert
 */
public class UserPreferences {
    private static final String KEY_HAS_RUN_BEFORE = "has_run_before";
    private static final String KEY_LATEST_ACTIVITY = "latest_activity";
    private static final String KEY_ACCEPTED_ACTIVITY = "accepted_activity";
    private static final String KEY_ALL_MEASUREMENTS = "all_measurements";
    private static final String KEY_LATEST_MEASUREMENTS = "latest_measurements";

    private static UserPreferences INSTANCE;
    
    private final SharedPreferences mSharedPreferences;
    private final Editor mEditor;
    
    private UserPreferences() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HearRateApplication.getContext());
        mEditor = mSharedPreferences.edit();
    }
    
    public static UserPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserPreferences();
        }
        return INSTANCE;
    }

    public void setHasRunBefore() {
        mEditor.putBoolean(KEY_HAS_RUN_BEFORE, true);
        mEditor.commit();
    }

    public boolean hasRunBefore() {
        return mSharedPreferences.getBoolean(KEY_HAS_RUN_BEFORE, false);
    }

    public void storeLatestActivity(int activityState) {
        mEditor.putInt(KEY_LATEST_ACTIVITY, activityState);
        mEditor.commit();
    }

    public int getLatestActivity() {
        return mSharedPreferences.getInt(KEY_LATEST_ACTIVITY, ActivityState.STILL);
    }

    public void setAcceptedActivity(int activityState) {
        mEditor.putInt(KEY_ACCEPTED_ACTIVITY, activityState);
        mEditor.commit();
    }

    public int getAcceptedActivity() {
        return mSharedPreferences.getInt(KEY_ACCEPTED_ACTIVITY, ActivityState.STILL);
    }

    public List<Measurement> getAllMeasurements() {
        String value = mSharedPreferences.getString(KEY_ALL_MEASUREMENTS, null);
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        } else {
            return Measurement.fromJSONList(value);
        }
    }

    public Measurement getLatestMeasurment() {
        String value = mSharedPreferences.getString(KEY_LATEST_MEASUREMENTS, null);
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return Measurement.fromJSON(value);
    }

    public List<Measurement> addMeasurement(Measurement measurement) {
        measurement.setActivity(getAcceptedActivity());
        String latestMeasurement = measurement.toJSON();
        Log.d("dirk", "Adding heart rate measurement: " + latestMeasurement);
        List<Measurement> measurements = getAllMeasurements();
        measurements.add(0, measurement);
        String measurementList = Measurement.toJSONList(measurements);

        mEditor.putString(KEY_LATEST_MEASUREMENTS, latestMeasurement);
        mEditor.putString(KEY_ALL_MEASUREMENTS, measurementList);
        mEditor.commit();

        return measurements;
    }
}