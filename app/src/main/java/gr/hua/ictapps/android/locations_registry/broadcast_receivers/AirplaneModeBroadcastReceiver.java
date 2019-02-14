package gr.hua.ictapps.android.locations_registry.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import gr.hua.ictapps.android.locations_registry.activities.MainActivity;
import gr.hua.ictapps.android.locations_registry.services.LocationService;

public class AirplaneModeBroadcastReceiver extends BroadcastReceiver {

    private String userid;
    private boolean serviceRunning;
    private boolean registered;
    private IntentFilter filter;

    public AirplaneModeBroadcastReceiver() {
        // this will be used to pass userid given from the MainActivity to the LocationService
        // through the Intent
        this.userid = null;
        // Location won't be running before the receiver is created
        serviceRunning = false;
        registered = false;
        filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Intent to start/stop service when airplaneMode is toggled
        Intent serviceIntent = new Intent(context.getApplicationContext(), LocationService.class);
        serviceIntent.putExtra("userid", userid);

        if (serviceRunning) {
            serviceRunning = false;
            context.stopService(serviceIntent);
        }

        else
            if (userid != null) {
                serviceRunning = true;
                context.startService(serviceIntent);
            }

    }

    /**
     * registers the bc receiver on the given context
     * fails if the receiver is already registered or if
     * userid is null
     * @param context the Context to register the receiver
     * @return true upon success, else false
     */
    public boolean register(Context context) {
        if (isRegistered() || userid == null)
            return false;
        context.registerReceiver(this, filter);
        return registered = true;
    }

    /**
     * unregisters the bc receiver off the given context
     * fails if the receiver is not registered
     * @param context the Context to register the receiver
     * @return true upon success, else false
     */
    public boolean unregister(Context context) {
        if (!isRegistered())
            return false;
        context.unregisterReceiver(this);
        registered = false;
        return true;
    }

}
