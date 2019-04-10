package erg4.aoikonom.sdy61.myapplication;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback, ILocationFragment {
    private static final String TAG = "LocationFragment";
    private static final int REQUEST_CODE = 1;
    private Location mCurrentLocation;
    private Location mPreviousLccation;
    private Marker mCurrentPosMarker;


    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private GoogleMap mMap;
    private boolean cameraInitialized = false;
    private TextView latitudeTextView;
    private TextView longitudeTextView;

    public LocationFragment() {
        // Required empty public constructor
    }

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        mapView = rootView.findViewById(R.id.map);

        mapView.getMapAsync(this);

        mapView.onCreate(savedInstanceState);

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int result = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (result != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(result)) {
                apiAvailability.getErrorDialog(getActivity(), result, 2404).show();
            }
        }

        latitudeTextView = rootView.findViewById(R.id.latitude_texxt);
        longitudeTextView = rootView.findViewById(R.id.longitude_text);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Activity activity = getActivity();
        if (activity instanceof TabbedActivity) {
            ((TabbedActivity) activity).setmLocationFragmentInterface(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        Activity activity = getActivity();
        if (activity instanceof TabbedActivity) {
            ((TabbedActivity) activity).setmLocationFragmentInterface(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        mapView.onSaveInstanceState(out);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mListener != null)
            mListener.onFragmentMapReady();
    }


    private void moveCamera(Location location) {
        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void moveCamera(LatLng location) {
        cameraInitialized = true;

        CameraPosition position = CameraPosition.builder()
                .target(location)
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentMapReady();
    }

    @Override
    public void onLocationPermissionsEnabled() {
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "LocationChanged " + location);
        mCurrentLocation = location;
        if (!location.hasSpeed()) {
            location.setSpeed(0f);
            if (mPreviousLccation != null) {
                float distance = location.distanceTo(mPreviousLccation);
                long timeElapsedInSeconds = (location.getTime() - mPreviousLccation.getTime()) / 1000;
                if (timeElapsedInSeconds != 0) {
                    float speed = distance / timeElapsedInSeconds;
                    speed = (float) (Math.round(100.0*speed) / 100.0);
                    location.setSpeed(speed);
                }
            }
        }
        mPreviousLccation = mCurrentLocation;
        new AddressTask().execute(location);
    }

    private void onAddressFound(Pair<Location, String> address) {
        if (address == null) return;
        Location location = address.first;
        moveCamera(mCurrentLocation);
        if (mCurrentPosMarker == null)
            mCurrentPosMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pegman)).
                    position(new LatLng(location.getLatitude(), location.getLongitude())).
                    title(address.second).snippet(Float.toString(address.first.getSpeed())));
        else {
            mCurrentPosMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            mCurrentPosMarker.setTitle(address.second);
            mCurrentPosMarker.setSnippet(Float.toString(address.first.getSpeed()) + "m/s");
        }
        mCurrentPosMarker.showInfoWindow();

        latitudeTextView.setText(Double.toString(location.getLatitude()));
        longitudeTextView.setText(Double.toString(location.getLongitude()));


    }


    private class AddressTask extends AsyncTask<Location, Void, Pair<Location, String>> {
        @Override
        protected Pair<Location, String> doInBackground(Location... locations) {
            String errorMessage = "";
            Location location = locations[0];
            List<Address> addresses = null;
            Geocoder geocoder = new Geocoder(LocationFragment.this.getActivity(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                ioException.printStackTrace();
                Log.e(TAG, errorMessage, ioException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size() == 0) return null;

            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            String result = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
            return Pair.create(location, result);
        }

        @Override
        protected void onPostExecute(Pair<Location,String> address) {
            onAddressFound(address);
        }
    }

}

