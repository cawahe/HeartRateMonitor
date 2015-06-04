package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import eu.vranckaert.heart.rate.monitor.BusinessService;
import eu.vranckaert.heart.rate.monitor.R;

/**
 * Date: 04/06/15
 * Time: 07:40
 *
 * @author Dirk Vranckaert
 */
public class GoogleFitSetupActivity extends Activity implements OnClickListener {
    private static final int REQUEST_OAUTH = 1;
    private static final int REQUEST_SUBSCRIPTION = 2;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.google_fit);

        findViewById(R.id.connect).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            setupGoogleFit();
        }
    }

    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void setupGoogleFit() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i("dirk", "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Subscribe to some data sources!
                                subscribe();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i("dirk", "Connection lost.  Cause: Network Lost.");
                                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i("dirk", "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i("dirk", "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), GoogleFitSetupActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                try {
                                    Log.i("dirk", "Attempting to resolve failed connection");
                                    result.startResolutionForResult(GoogleFitSetupActivity.this, REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e("dirk", "Exception while starting resolution activity", e);
                                }
                            }
                        }
                )
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Subscribe to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     */
    public void subscribe() {
        final AlertDialog progress = new ProgressDialog.Builder(this)
                .setMessage(R.string.google_fit_setup_setup_fit)
                .setCancelable(false)
                .show();

        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        progress.dismiss();

                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i("dirk", "Existing subscription for activity detected.");
                            } else {
                                Log.i("dirk", "Successfully subscribed!");
                            }
                            findViewById(R.id.connect).setVisibility(View.GONE);
                        } else {
                            Log.i("dirk", "There was a problem subscribing.");
                            if (status.hasResolution()) {
                                try {
                                    status.startResolutionForResult(GoogleFitSetupActivity.this, REQUEST_SUBSCRIPTION);
                                } catch (SendIntentException e) {
                                    Log.e("dirk", "Exception while starting resolution activity", e);
                                }
                            } else {
                                GooglePlayServicesUtil.getErrorDialog(status.getStatusCode(), GoogleFitSetupActivity.this, 0).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == REQUEST_SUBSCRIPTION) {
            if (resultCode == RESULT_OK) {
                subscribe();
            }
        }
    }
}