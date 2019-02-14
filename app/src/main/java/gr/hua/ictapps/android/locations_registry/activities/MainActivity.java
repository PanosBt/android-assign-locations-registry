package gr.hua.ictapps.android.locations_registry.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gr.hua.ictapps.android.locations_registry.R;
import gr.hua.ictapps.android.locations_registry.broadcast_receivers.AirplaneModeBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    private AirplaneModeBroadcastReceiver airplaneModeBroadcastReceiver;

    // handle onRequestPermissionsResult to check if required permissions are given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // my requestCode for Location permissions
        if (requestCode == 7)
            // if you ask permissions for both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
            // and the permissions are granted by the user,
            // only ACCESS_FINE_LOCATION is passed in grantResults
            // so grantResults.length == 1
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // if the users won't provide permissions inform them w/ a Toast
                // and simply close the app
                Toast.makeText(this,
                        "Location Permissions are mandatory for this app to work",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if location permissions are already granted
        // NOTE: Location permissions are required in the LocationService
        // but I believe it makes more sense to request them in the ui thread
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //if not, request them
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    7
            );
        }

        airplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver();

        final TextView errorText = findViewById(R.id.main_text_view_error);

        // onClickListener for MONITOR button
        findViewById(R.id.main_button_monitor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorText.setVisibility(View.GONE);

                String userid = ((EditText) findViewById(R.id.main_edit_text_userid))
                        .getText()
                        .toString()
                        .trim();

                // no userid was provided, inform the user and return from onClick
                if (TextUtils.isEmpty(userid)) {
                    errorText.setText(R.string.activity_main_error_msg_empty_user_id);
                    errorText.setVisibility(View.VISIBLE);
                    return;
                }

                airplaneModeBroadcastReceiver.setUserid(userid);

                // check if the broadcastReceiver is already registered
                if(airplaneModeBroadcastReceiver.isRegistered()) {
                    errorText.setText("Monitoring has already started");
                    errorText.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    // if not, register the broadcastReceiver
                    // and inform the user
                    String msg;
                    if (airplaneModeBroadcastReceiver.register(MainActivity.this))
                        msg = getString(R.string.activity_main_receiver_registered);
                    else
                        msg = "Airplane mode monitoring failed";

                    errorText.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onPause() {
        // to avoid weird messages to the user when the Activity is paused
        // and Android stops the broadcastReceiver
        // unregister it
        if (airplaneModeBroadcastReceiver.isRegistered()) {
            airplaneModeBroadcastReceiver.unregister(MainActivity.this);
            Toast.makeText(MainActivity.this,
                    getString(R.string.activity_main_receiver_unregistered),
                    Toast.LENGTH_SHORT)
                    .show();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        // if previously registered broadcastReceiver was unregistered
        // due to onPause, reregister it
        if (!airplaneModeBroadcastReceiver.isRegistered()
                && airplaneModeBroadcastReceiver.getUserid() != null) {
            airplaneModeBroadcastReceiver.register(MainActivity.this);
            Toast.makeText(MainActivity.this,
                    getString(R.string.activity_main_receiver_registered),
                    Toast.LENGTH_SHORT)
                    .show();
        }
        super.onResume();
    }
}
