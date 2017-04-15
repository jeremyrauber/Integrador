package com.projetos.ifpr.integrador;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Crash on 14/04/2017.
 */

public class Coordenadas extends Service {
    public static final String BROADCAST_ACTION = "com.guards.anshul.hindguard.CUSTOM_INTENT";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public  String editTextValue;
    Intent intent;
    String restoredText;
    private final static int MY_PERMISSION_FINE_LOCATION = 101;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        if(editTextValue!=null){

            editTextValue = intent.getStringExtra("B");
            SharedPreferences.Editor editor = getSharedPreferences("Guard_Id", MODE_PRIVATE).edit();
            editor.putString("guard_id", editTextValue);
            editor.apply();
        }else{
            SharedPreferences prefs = getSharedPreferences("Guard_Id", MODE_PRIVATE);
            restoredText = prefs.getString("guard_id", null);
        }
        Toast.makeText(Coordenadas.this.getApplicationContext(),restoredText,Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(Coordenadas.this.getApplicationContext(),"Need Permission",Toast.LENGTH_SHORT).show();

            return Service.START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, (LocationListener) listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        return Service.START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {

            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;


        if (isSignificantlyNewer) {
            return true;

        } else if (isSignificantlyOlder) {
            return false;
        }


        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;


        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2){
        if(provider1==null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }





    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location loc)
        {

            if(isBetterLocation(loc, previousBestLocation)) {
                double v =   loc.getLatitude();
                double b =  loc.getLongitude();

                //Log.e("<<a--b>>>>>>",String.valueOf(v)+ String.valueOf(b));

                Toast.makeText(getApplicationContext(), String.valueOf(v)+String.valueOf(b), Toast.LENGTH_SHORT).show();
                intent.setAction("com.guards.anshul.hindguard.CUSTOM_INTENT");
                intent.putExtra("latitude",v);
                intent.putExtra("longitude", b);
                intent.putExtra("A",restoredText);
                intent.putExtra("Provider", loc.getProvider());
                sendBroadcast(intent);

            }

        }


        public void onProviderDisabled(String provider)
        {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}
