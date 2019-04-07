package erg4.aoikonom.sdy61.myapplication;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment implements OnMapReadyCallback, ILocationFragment {
    private Location mCurrentLocation;
    private Marker mCurrentPosMarker;


    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private GoogleMap mMap;
    private boolean cameraInitialized = false;

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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof TabbedActivity) {
            ((TabbedActivity) activity).setmLocationFragmentInterface(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity instanceof TabbedActivity) {
            ((TabbedActivity) activity).setmLocationFragmentInterface(null);
        }
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
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        moveCamera(mCurrentLocation);
        if (mCurrentPosMarker == null)
            mCurrentPosMarker = mMap.addMarker(new MarkerOptions().
                    position(new LatLng(location.getLatitude(), location.getLongitude())).
                    title("Current Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pegman)));
        else
            mCurrentPosMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }
}
