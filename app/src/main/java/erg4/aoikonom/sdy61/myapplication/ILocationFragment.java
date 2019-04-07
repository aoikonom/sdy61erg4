package erg4.aoikonom.sdy61.myapplication;

import android.location.Location;

public interface ILocationFragment {
    void onLocationPermissionsEnabled();

    void onLocationChanged(Location location);
}
