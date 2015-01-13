package net.aayush.skooterapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import net.aayush.skooterapp.BaseActivity;
import net.aayush.skooterapp.R;
import net.aayush.skooterapp.data.Post;

import java.util.ArrayList;
import java.util.List;

public class Me extends Fragment implements LocationSource, LocationListener {
    protected List<Post> mPostsList = new ArrayList<Post>();
    protected ArrayAdapter<Post> mPostsAdapter;
    protected ListView mListPosts;
    protected Context mContext;
    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mMap;
    LocationManager myLocationManager = null;
    LocationSource.OnLocationChangedListener myLocationListener = null;
    Criteria myCriteria;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = container.getContext();

        View rootView = inflater.inflate(R.layout.activity_me, container, false);
        int userId = BaseActivity.userId;

        ListView myList = (ListView) rootView.findViewById(R.id.myList);

        ArrayList<String> testData = new ArrayList<String>(3);
        testData.add("My Skoots");
        testData.add("My Replies");
        testData.add("Settings");

        myList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, testData));

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
//                switch (position) {
//                    case 0:
//                        intent = new Intent(MeActivity.this, MePostsActivity.class);
//                        startActivity(intent);
//                        break;
//                    case 1:
//                        intent = new Intent(MeActivity.this, MeCommentsActivity.class);
//                        startActivity(intent);
//                        break;
//                    case 2:
//                        break;
//                }
            }
        });

        setUpMapIfNeeded();

        return rootView;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mSupportMapFragment == null) {
            mSupportMapFragment = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map));
            if (mSupportMapFragment != null) {
                mMap = mSupportMapFragment.getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    CameraUpdate update = getLastKnownLocation();
                    if (update != null) {
                        mMap.moveCamera(update);
                    }
                }
            }
        }
    }

    private CameraUpdate getLastKnownLocation() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(criteria, true);
        if (provider == null) {
            return null;
        }
        Location loc = lm.getLastKnownLocation(provider);
        if (loc != null) {
            return CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 14.0f));
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (myLocationListener != null) {
            myLocationListener.onLocationChanged(location);

            CameraUpdate update = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11.0f));
            if (update != null) {
                mMap.moveCamera(update);
            }
        }
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

    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        myLocationListener = listener;
    }

    @Override
    public void deactivate() {
        myLocationListener = null;
    }
}
