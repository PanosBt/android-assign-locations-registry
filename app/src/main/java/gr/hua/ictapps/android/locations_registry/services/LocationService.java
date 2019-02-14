package gr.hua.ictapps.android.locations_registry.services;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gr.hua.ictapps.android.locations_registry.ext_contract_classes.LocationsContract;

public class LocationService extends Service {


    private ContentResolver resolver;
    private LocationManager locationManager;
    private LocationsContract locationsContract;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        resolver = getContentResolver();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationsContract = new LocationsContract();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Location monitoring started!", Toast.LENGTH_LONG).show();
        final String userid = intent.getStringExtra("userid");

        // check if Locations permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // if not inform the user and stop (this should never happen as
            // MainActivity checks Permissions
            Toast.makeText(getApplicationContext(),
                    "Insufficient permissions, location storing serive failed to start",
                    Toast.LENGTH_SHORT).show();
            stopSelf();
        } else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000,
                    20,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            final ContentValues values = new ContentValues();
                            values.put("userid", userid);
                            values.put("latitude", ((Double) location.getLatitude()).floatValue());
                            values.put("longitude", ((Double) location.getLongitude()).floatValue());
                            final String dt = new SimpleDateFormat(
                                    "dd-MM-yyyy kk:mm:ss",
                                    Locale.getDefault()).format(new Date());
                            values.put("dt", dt);
                        // start a new Thread to get locations from contentResolver
                        // since the service is actually running on the ui thread
                        // (the broadcastReceiver that starts this Service is bound to the MainActivity)
                        new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String msg;
                                    if (resolver.insert(Uri.parse(locationsContract.CONTENT_URL), values) == null)
                                        msg = "Location store failed at " + dt;
                                    else
                                        msg = "Location stored at " + dt;

                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            }).run();
                        }



                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    });

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Location monitoring stoped!", Toast.LENGTH_LONG).show();

        super.onDestroy();
    }
}
