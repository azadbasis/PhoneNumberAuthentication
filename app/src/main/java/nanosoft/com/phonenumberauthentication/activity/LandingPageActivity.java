package nanosoft.com.phonenumberauthentication.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import nanosoft.com.phonenumberauthentication.R;
import nanosoft.com.phonenumberauthentication.app.App;
import nanosoft.com.phonenumberauthentication.config.Config;
import nanosoft.com.phonenumberauthentication.dialogs.PopupMessage;
import nanosoft.com.phonenumberauthentication.listeners.PermissionListener;
import nanosoft.com.phonenumberauthentication.model.BasicBean;
import nanosoft.com.phonenumberauthentication.util.AppConstants;


public class LandingPageActivity extends BaseAppCompatNoDrawerActivity.BaseAppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener, com.google.android.gms.location.LocationListener,
        android.location.LocationListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final int DISPLACEMENT = 10;
    private static final String TAG = "LandingPA";

    private static final LocationRequest mLocationRequest = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private static final int REQ_SEARCH_SOURCE_SELECT = 0;
    private static final int REQ_SEARCH_DESTINATION_SELECT = 1;
    private static final int REQ_SEARCH_DESTINATION_ESTIMATE_SELECT = 2;
    private static final int REQ_REQUEST_RIDE = 3;
    private static final int REQ_ESTIMATED_DESTINATION = 4;
    private static final int LOCATION_SOURCE = 0;
    private static final int LOCATION_DESTINATION = 1;

    private static GoogleMapOptions options = new GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .compassEnabled(true)
            .rotateGesturesEnabled(true)
            .tiltGesturesEnabled(true)
            .zoomControlsEnabled(true)
            .scrollGesturesEnabled(true)
            .mapToolbarEnabled(true);

    //    private GoogleApiClient mGoogleApiClient;
    private Location LastLocation;
    private GoogleMap mMap;
    private Toolbar toolbarHome;
    private TextView txtActionSearch;
    private FrameLayout framePickup;
    private ImageView ivMarker;
    private ImageView ivBottomMarker;
    private LinearLayout llLandingBottomBar;
    private ImageView ivLocationButton;
    private SupportMapFragment mapFragment;
    //    private View lytBottom;
    private TextView txtTime;
    private TextView txtMaxSize;
    private TextView txtFare;
    private String carType = String.valueOf(-1);
    //    private int searchPlaceType = AppConstants.SEARCH_SOURCE;
    private TextView txtSource;
    private LinearLayout llConfirmation;
    private boolean isConfirmationPage = false;
    private boolean isCameraMoved;
    private CardView cvConfirmationPage;
    private TextView txtDestination;
    private TextView txtTotalFare;
    private RelativeLayout rlFare;
    private View viewDottedLine;
    private TextView txtCarOne;
    private TextView txtCarTwo;
    private TextView txtCarThree;
    private TextView carFour;
    private TextView txtFareEstimate;
    private TextView txtTo;
    private LinearLayout llDestinationEstimated;
    private TextView txtEstimatedDestination;
    private Button btnRequest;
    private View.OnClickListener snackBarRefreshOnClickListener;

    private int searchType;
    private Polyline polyLine;
    private LatLngBounds bounds;
    private LatLng newLatLng1;
    private LatLng newLatLng2;
    private ImageView carOneImage;
    private ImageView carTwoImage;
    private ImageView carThreeImage;
    private ImageView carFourImage;
    private TextView txtCarAvailability;
    private String time;
    private String distance;
    private boolean isDestinationEstimateSelect = false;
    private LinearLayout llFare;
    private TextView txtCarArrivalEstimatedTime;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private ViewGroup.LayoutParams param;
    private FrameLayout flLandingPage;
    private ViewGroup.LayoutParams param1;
    private TextView txtEstimatedFare;
    private boolean isMapInit = true;
    private GoogleApiClient mGoogleApiClient;
    private TextView txtFareLabel;
    private LinearLayout llProgressBar;
    private LinearLayout llEstimation;
    private LinearLayout llConfirmationProgress;
    private boolean isInit = true;

    private RecyclerView rvCarTypes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing_page);

        isGetLocationEnabled = false;


       /* if (!checkForLocationPermissions()) {
            getLocationPermissions();
        } else {
            checkLocationSettingsStatus();
        }

        if (!checkForReadWritePermissions()) {
            getReadWritePermissions();
        }else{
            isGetLocationEnabled=true;
        }*/

        initViews();
        initMap();

        setProgressScreenVisibility(true, true);
//        getData();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        initFCM();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isConfirmationPage) {
                onBackClick();
            } else {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    onBackPressed();
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openOptionsMenu();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkForLocationPermissions()) {
            if (!isConfirmationPage && sourceBean == null) {
                if (checkPlayServices()) {
                    getCurrentLocation();
//            buildGoogleApiClient();
//            createLocationRequest();
                }
            }
        }
    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    private void getData() {
        if (App.isNetworkAvailable()) {
            fetchLandingPageDetails();
        } else {
            setProgressScreenVisibility(true, false);
            Snackbar.make(coordinatorLayout, AppConstants.NO_NETWORK_AVAILABLE, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.btn_retry, snackBarRefreshOnClickListener).show();
        }
    }

    public void initViews() {

        snackBarRefreshOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                //  mVibrator.vibrate(25);
                getData();
            }
        };

        btnRequest = (Button) findViewById(R.id.btn_request);

        rlFare = (RelativeLayout) findViewById(R.id.rl_fare);

        coordinatorLayout.removeView(toolbar);
//      toolbar.setVisibility(View.GONE);
        toolbarHome = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_landing_page, toolbar);
        coordinatorLayout.addView(toolbarHome, 0);
        setSupportActionBar(toolbarHome);


        rvCarTypes = (RecyclerView) findViewById(R.id.rv_bottom_sheet_landing_car_types);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvCarTypes.setLayoutManager(layoutManager);

        ivBottomMarker = (ImageView) findViewById(R.id.iv_bottom_marker);

        llConfirmationProgress = (LinearLayout) findViewById(R.id.ll_confirmation_progress);

        txtFareEstimate = (TextView) findViewById(R.id.txt_fare_estimate);
        txtTo = (TextView) findViewById(R.id.txt_to);
        llDestinationEstimated = (LinearLayout) findViewById(R.id.ll_destination_estimated);

        carOneImage = (ImageView) findViewById(R.id.iv_car_one);
        carTwoImage = (ImageView) findViewById(R.id.iv_car_two);
        carThreeImage = (ImageView) findViewById(R.id.iv_car_three);
        carFourImage = (ImageView) findViewById(R.id.iv_car_four);

        txtCarOne = (TextView) findViewById(R.id.txt_la_go);
        txtCarTwo = (TextView) findViewById(R.id.txt_la_x);
        txtCarThree = (TextView) findViewById(R.id.txt_la_xl);
        carFour = (TextView) findViewById(R.id.txt_la_xll);

        txtCarArrivalEstimatedTime = (TextView) findViewById(R.id.txt_min_time);

//        ivActionSearch = (ImageView) toolbarHome.findViewById(R.id.ic_action_search);

        txtCarAvailability = (TextView) findViewById(R.id.txt_cars_available);
        txtSource = (TextView) findViewById(R.id.txt_source);
        txtDestination = (TextView) findViewById(R.id.txt_destination);

        txtTime = (TextView) findViewById(R.id.txt_time);
        txtMaxSize = (TextView) findViewById(R.id.txt_max_size);
        txtFare = (TextView) findViewById(R.id.txt_fare);
        txtEstimatedDestination = (TextView) findViewById(R.id.txt_estimated_destination);

        cvConfirmationPage = (CardView) findViewById(R.id.cv_confirmation_page);

        txtEstimatedFare = (TextView) findViewById(R.id.txt_estimated_fare);

        llProgressBar = (LinearLayout) findViewById(R.id.ll_landing_progress_bar);
        llEstimation = (LinearLayout) findViewById(R.id.ll_estimation);
        llFare = (LinearLayout) findViewById(R.id.ll_fare);

        flLandingPage = (FrameLayout) findViewById(R.id.fl_landing_page);

        framePickup = (FrameLayout) findViewById(R.id.frame_pickup_landing_page);
        ivMarker = (ImageView) findViewById(R.id.iv_marker);

        llLandingBottomBar = (LinearLayout) findViewById(R.id.ll_landing_estimation_bottom_sheet);
        ivLocationButton = (FloatingActionButton) findViewById(R.id.fab_location_button);

        txtActionSearch = (TextView) toolbarHome.findViewById(R.id.txt_action_search);

        txtTotalFare = (TextView) findViewById(R.id.txt_total_fare);

        viewDottedLine = (View) findViewById(R.id.view_dotted_line);
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            txtActionSearch.setCompoundDrawablesRelative(null,null,null,null);
        }
*/

        llConfirmation = (LinearLayout) findViewById(R.id.ll_confirmation);
        btnRequest = (Button) findViewById(R.id.btn_request);

        txtFareLabel = (TextView) findViewById(R.id.txt_fare_lable);

        setBottomSheetBehavior();

        param1 = flLandingPage.getLayoutParams();
        param1.height = (int) (height - getStatusBarHeight() - mActionBarHeight);
        Log.i(TAG, "onSlide: PAram Height : " + param1.height);
        flLandingPage.setLayoutParams(param1);

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionCheckCompleted(int requestCode, boolean isPermissionGranted) {

                if (requestCode == REQUEST_PERMISSIONS_LOCATION & isPermissionGranted) {

                    Log.i(TAG, "onPermissionCheckCompleted: PERMISSION GRANTED !!!!");
                    if (checkPlayServices()) {
                        getCurrentLocation();
                    }
                }

            }
        };

        setPermissionListener(permissionListener);

    }

    public void setBottomSheetBehavior() {

        bottomSheetBehavior = BottomSheetBehavior.from(llLandingBottomBar);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                /*if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING

                bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_SETTLING){
                    param = myMapFragment.getView().getLayoutParams();
                    param.height = (int) (height - getStatusBarHeight() - mActionBarHeight - bottomSheet.getHeight());
                    Log.i(TAG, "onSlide: PAram Height : " + param.height);
                    myMapFragment.getView().setLayoutParams(param);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    param = myMapFragment.getView().getLayoutParams();
                    param.height = (int) (height - getStatusBarHeight() - mActionBarHeight - bottomSheet.getHeight());
                    Log.i(TAG, "onSlide: PAram Height : " + param.height);
                    myMapFragment.getView().setLayoutParams(param);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    param = myMapFragment.getView().getLayoutParams();
                    param.height = (int) (height - getStatusBarHeight() - mActionBarHeight - bottomSheet.getHeight());
                    Log.i(TAG, "onSlide: PAram Height : " + param.height);
                    myMapFragment.getView().setLayoutParams(param);
                }/*/
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i(TAG, "onSlide: offset : " + slideOffset);
// mapFragmentView.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();

                try {
                    param = mapFragment.getView().getLayoutParams();
                    param.height = (int) (height - getStatusBarHeight() - mActionBarHeight/* - (80 * px * (1 - slideOffset))*/ - bottomSheet.getHeight() * (slideOffset));
//                Log.i(TAG, "onSlide: PAram Height : " + param.height);
                    mapFragment.getView().setLayoutParams(param);

                    param1 = flLandingPage.getLayoutParams();
                    param1.height = (int) (height - getStatusBarHeight() - mActionBarHeight /*- (80 * px * (1 - slideOffset))*/ - bottomSheet.getHeight() * (slideOffset));
                    Log.i(TAG, "onSlide: PAram Height : " + param1.height);
                    flLandingPage.setLayoutParams(param1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_home_map);


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setPadding(0, (int) ((100 * px) + mActionBarHeight + getStatusBarHeight()), 0, (int) (100 * px));

                initMapLoad();

            }
        });
    }

    private void initMapLoad() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getPeekHeight() == 100 * px) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {

                /*if (sourceBean != null & destinationBean != null) {
                    fetchTotalfare();
                    txtFare.setText(fareBean.getTotalFare());
                }*/
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getPeekHeight() == 100 * px) {
                    bottomSheetBehavior.setPeekHeight(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                if (!isConfirmationPage) {
                    mMap.getUiSettings().setScrollGesturesEnabled(true);
                    mMap.setMaxZoomPreference(18f);
                    framePickup.setVisibility(View.INVISIBLE);
                    ivBottomMarker.setVisibility(View.INVISIBLE);
                    ivMarker.setVisibility(View.VISIBLE);
                    ivLocationButton.setVisibility(View.INVISIBLE);

                    isCameraMoved = true;
                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                if (sourceBean != null & destinationBean != null) {
                    if (!isConfirmationPage) {
                        fetchPolyPoints(false);
                    }
                    if (fareBean != null) {
                        txtFare.setText(fareBean.getTotalFare());
                    }
                }

                if (!isConfirmationPage) {

                    CameraPosition postion = mMap.getCameraPosition();
                    LatLng center = postion.target;

                    framePickup.setVisibility(View.VISIBLE);
                    ivBottomMarker.setVisibility(View.VISIBLE);
                    ivMarker.setVisibility(View.INVISIBLE);
                    ivLocationButton.setVisibility(View.VISIBLE);

                    if (bottomSheetBehavior.getPeekHeight() == 0) {
                        bottomSheetBehavior.setPeekHeight((int) (100 * px));
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                        llLandingBottomBar.animate().translationY(00*px).setDuration(1000).start();
                    }

                    Log.i(TAG, "onCameraIdle: GetLocationName Called : " + center);
                    if (isCameraMoved) {

                        getLocationName(String.valueOf(center.latitude), String.valueOf(center.longitude));
//                        getLocationName(center.latitude, center.longitude);

                        if (sourceBean == null)
                            sourceBean = new PlaceBean();
                        sourceBean.setLatitude(String.valueOf(center.latitude));
                        sourceBean.setLongitude(String.valueOf(center.longitude));

                        if (App.isNetworkAvailable()) {
                            fetchLandingPageDetails();
//                            fetchCarDetails();
                        } else {
                            Snackbar.make(coordinatorLayout, AppConstants.NO_NETWORK_AVAILABLE, Snackbar.LENGTH_LONG)
                                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
                        }

                        if (destinationBean != null) {
//                            getEstimatedFare();
                        }
                    }
                    isCameraMoved = false;
                }
            }
        });
    }


    private void initFCM() {

        FCMRegistrationTask fcmRegistrationTask = new FCMRegistrationTask();
        fcmRegistrationTask.setFCMRegistrationTaskListener(new FCMRegistrationTask.FCMRegistrationTaskListener() {
            @Override
            public void dataDownloadedSuccessfully(String fcmToken) {

                Log.i(TAG, "dataDownloadedSuccessfully: FCM TOKEN : " + fcmToken);

                JSONObject postData = getUpdateFCMTokenJSObj(fcmToken);

                DataManager.performUpdateFCMToken(postData, new BasicListener() {
                    @Override
                    public void onLoadCompleted(BasicBean basicBean) {

                    }

                    @Override
                    public void onLoadFailed(String error) {

                    }
                });

            }

            @Override
            public void dataDownloadFailed() {

            }
        });
        fcmRegistrationTask.execute();

    }


    private JSONObject getUpdateFCMTokenJSObj(String fcmToken) {
        JSONObject postData = new JSONObject();

        try {
            postData.put("fcm_token", fcmToken);
//            postData.put("user_id", userBean.getUserID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postData;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SEARCH_DESTINATION_SELECT && resultCode == RESULT_OK) {

            destinationBean = (PlaceBean) data.getSerializableExtra("bean");

            if (sourceBean != null & destinationBean != null
                    && sourceBean.getName() != null & destinationBean.getName() != null) {
                if (sourceBean.getName().equalsIgnoreCase(destinationBean.getName())) {

                    mMap.clear();
                    destinationBean = null;
                    txtDestination.setText("");
                    rlFare.setVisibility(View.GONE);

                    onSourceSelect();

                    Snackbar.make(coordinatorLayout, "Source and Destination are Same!", Snackbar.LENGTH_LONG)
                            .setAction("Dismiss", snackBarDismissOnClickListener).show();

                }
            }

            Log.i(TAG, "onActivityResult: ON DESTINATION SELECT ");

            fetchCarDetails();

            if (destinationBean != null) {
                llFare.setVisibility(View.GONE);
                llConfirmationProgress.setVisibility(View.VISIBLE);
            }

            Log.i(TAG, "onActivityResult: SourceLatitude : " + sourceBean.getDLatitude());
            Log.i(TAG, "onActivityResult: SourceLongitude : " + sourceBean.getDLongitude());

//            Log.i(TAG, "onActivityResult: DestinationLatitude : " + destinationBean.getDLatitude());
//            Log.i(TAG, "onActivityResult: DestinationLongitude : " + destinationBean.getDLongitude());

            if (sourceBean != null && destinationBean != null) {
                onDestinationSelect();
            }
        }

        if (requestCode == REQ_SEARCH_SOURCE_SELECT && resultCode == RESULT_OK) {

            sourceBean = (PlaceBean) data.getSerializableExtra("bean");

            if (sourceBean != null && destinationBean != null) {
                if (sourceBean.getName().equalsIgnoreCase(destinationBean.getName())) {

                    mMap.clear();
                    destinationBean = null;
                    txtDestination.setText("");
                    rlFare.setVisibility(View.GONE);

                    Snackbar.make(coordinatorLayout, "Source and Destination are Same!", Snackbar.LENGTH_LONG)
                            .setAction("Dismiss", snackBarDismissOnClickListener).show();
                }
            }

            Log.i(TAG, "onActivityResult: SourceName" + sourceBean.getName());
//            Log.i(TAG, "onActivityResult: DestinationName" + destinationBean.getName());
            Log.i(TAG, "onActivityResult: SourceLatitude : " + sourceBean.getDLatitude());
            Log.i(TAG, "onActivityResult: SourceLongitude : " + sourceBean.getDLongitude());

            fetchCarDetails();
            if (sourceBean != null) {
                onSourceSelect();
            }
        }

        if (requestCode == REQ_REQUEST_RIDE && resultCode == RESULT_OK) {

            DriverBean driverBean = (DriverBean) data.getSerializableExtra("bean");


        }

        if (requestCode == REQ_ESTIMATED_DESTINATION && resultCode == RESULT_OK) {

            destinationBean = (PlaceBean) data.getSerializableExtra("bean");
            llProgressBar.setVisibility(View.VISIBLE);
            llEstimation.setVisibility(View.GONE);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            fetchPolyPoints(false);
            showFareEstimation(destinationBean.getName());
        }
    }


    private void onSourceSelect() {

        mMap.clear();
        txtSource.setText(sourceBean.getName());
        onPlotLocation(true, LOCATION_SOURCE, sourceBean.getDLatitude(), sourceBean.getDLongitude());
        try {
            if (destinationBean.getDLatitude() != 0 && destinationBean.getDLongitude() != 0) {
                onPlotLocation(true, LOCATION_DESTINATION, destinationBean.getDLatitude(), destinationBean.getDLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (sourceBean.getDLatitude() != 0 && sourceBean.getDLongitude() != 0 && destinationBean.getDLatitude() != 0 && destinationBean.getDLongitude() != 0) {
                rlFare.setVisibility(View.VISIBLE);
                viewDottedLine.setVisibility(View.VISIBLE);
                mapAutoZoom();
                fetchPolyPoints(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDestinationSelect() {
        mMap.clear();
        onPlotLocation(true, LOCATION_SOURCE, sourceBean.getDLatitude(), sourceBean.getDLongitude());
        onPlotLocation(true, LOCATION_DESTINATION, destinationBean.getDLatitude(), destinationBean.getDLongitude());
        txtDestination.setText(destinationBean.getName());
        if (sourceBean.getDLatitude() != 0 && sourceBean.getDLongitude() != 0
                && destinationBean.getDLatitude() != 0 && destinationBean.getDLongitude() != 0) {

            rlFare.setVisibility(View.VISIBLE);
            viewDottedLine.setVisibility(View.VISIBLE);
            mapAutoZoom();
            fetchPolyPoints(true);
        }

    }

    private void showFareEstimation(String location) {
        if (destinationBean != null) {
//            fetchTotalfare();
            txtFareLabel.setText("Estd Fare");
        }
        txtFareEstimate.setVisibility(View.GONE);
        txtTo.setVisibility(View.VISIBLE);
        llDestinationEstimated.setVisibility(View.VISIBLE);
        txtEstimatedDestination.setText(location);
    }


    public void onLocationButtonClick(View view) {

        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        //mVibrator.vibrate(25);

        Log.i(TAG, "onLocationButtonClick: Clicked");

//        displayLocation();

        sourceBean = null;
        if (mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            getCurrentLocation();
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void onFareEstimateClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (mMap != null) {
            LatLng center = mMap.getCameraPosition().target;
            if (sourceBean == null)
                sourceBean = new PlaceBean();
            sourceBean.setLatitude(String.valueOf(center.latitude));
            sourceBean.setLongitude(String.valueOf(center.longitude));
        }

        searchType = AppConstants.SEARCH_ESTIMATED_DESTINATION;


    }

    public void onPickUpLocationClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

/*        CarBean bean1 = landingPageBean.getCars().get(0);
        CarBean bean2 = landingPageBean.getCars().get(1);
        CarBean bean3 = landingPageBean.getCars().get(2);
        CarBean bean4 = landingPageBean.getCars().get(3);*/

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setPeekHeight(0);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        }

       /* if (carType.equalsIgnoreCase("1")) {
            btnRequest.setText("Request " + bean1.getCarName());
        }

        if (carType.equalsIgnoreCase("2")) {
            btnRequest.setText("Request " + bean2.getCarName());
        }

        if (carType.equalsIgnoreCase("3")) {
            btnRequest.setText("Request " + bean3.getCarName());
        }

        if (carType.equalsIgnoreCase("4")) {
            btnRequest.setText("Request " + bean4.getCarName());
        }*/

        btnRequest.setText(getString(R.string.lable_request) + " " + (landingPageBean.getCar(carType) != null
                ? landingPageBean.getCar(carType).getCarName() : getString(R.string.app_name)));


        llFare.setVisibility(View.VISIBLE);
        rlFare.setVisibility(View.GONE);

        LatLng center = mMap.getCameraPosition().target;
//        center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

        if (sourceBean == null) {
            sourceBean = new PlaceBean();
        }

        sourceBean.setLatitude(String.valueOf(center.latitude));
        sourceBean.setLongitude(String.valueOf(center.longitude));

        onPlotLocation(true, LOCATION_SOURCE, sourceBean.getDLatitude(), sourceBean.getDLongitude());

        if (txtDestination.length() > 0) {
            rlFare.setVisibility(View.VISIBLE);
            viewDottedLine.setVisibility(View.VISIBLE);

        }

        if (!isConfirmationPage) {

            layoutConfirmationPage();

            txtActionSearch.setText(R.string.Confirmation);

            if (!llConfirmation.isShown()) {

                cvConfirmationPage.setVisibility(View.VISIBLE);
                llConfirmation.setVisibility(View.VISIBLE);

            }

            if (!btnRequest.isShown()) {
                btnRequest.setVisibility(View.VISIBLE);
            }
            isConfirmationPage = true;

        }

        if (destinationBean != null) {
            mMap.clear();
            llFare.setVisibility(View.GONE);
            llConfirmationProgress.setVisibility(View.VISIBLE);
            txtDestination.setText(destinationBean.getName());

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onDestinationSelect();
                }
            }, 2000);
            llEstimation.setVisibility(View.GONE);
        }
    }

    public void layoutConfirmationPage() {

        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        if (!isConfirmationPage) {
            bottomSheetBehavior.setPeekHeight(0);

            ivBottomMarker.setVisibility(View.GONE);

            ivMarker.setVisibility(View.GONE);

            ivLocationButton.setVisibility(View.GONE);

            framePickup.setVisibility(View.GONE);

        }
    }

    public void onBackClick() {

        mMap.clear();

        fetchLandingPageDetails();

        try {
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = height;
            mapFragment.getView().setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        llConfirmationProgress.setVisibility(View.GONE);

        getCurrentLocation();
//        txtSource.setText("");
        txtDestination.setText("");

        txtFare.setVisibility(View.VISIBLE);

        rlFare.setVisibility(View.GONE);
        llFare.setVisibility(View.GONE);

        viewDottedLine.setVisibility(View.GONE);

        Log.i(TAG, "onBackClick: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        CameraPosition postion = mMap.getCameraPosition();
        LatLng center = postion.target;

        txtActionSearch.setText(Config.getInstance().getCurrentLocation());

        cvConfirmationPage.setVisibility(View.GONE);
//        rvCarList.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setPeekHeight((int) (100 * px));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

//        llEstimation.setVisibility(View.VISIBLE);
        framePickup.setVisibility(View.VISIBLE);
        ivBottomMarker.setVisibility(View.VISIBLE);
        ivMarker.setVisibility(View.GONE);
        ivLocationButton.setVisibility(View.VISIBLE);
        btnRequest.setVisibility(View.GONE);
        llConfirmation.setVisibility(View.GONE);

        mMap.clear();

        sourceBean = null;
        destinationBean = null;

        txtTo.setVisibility(View.GONE);
        llDestinationEstimated.setVisibility(View.GONE);
        txtFareEstimate.setVisibility(View.VISIBLE);

        isConfirmationPage = false;
    }


    public void onLaGoCarClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        carType = landingPageBean.getCars().get(0).getCarID();

        if (App.isNetworkAvailable()) {
            fetchCarDetails();
        } else {
            Snackbar.make(coordinatorLayout, "Network is not Available", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        llProgressBar.setVisibility(View.VISIBLE);
        llEstimation.setVisibility(View.GONE);

        txtCarOne.setBackgroundResource(R.drawable.bg_round_edges);
        txtCarTwo.setBackgroundResource(R.color.transparent);
        txtCarThree.setBackgroundResource(R.color.transparent);
        carFour.setBackgroundResource(R.color.transparent);
        txtCarOne.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        txtCarTwo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        txtCarThree.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        carFour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        if (sourceBean != null & destinationBean != null) {
            fetchPolyPoints(false);
            if (fareBean != null) {
                txtFare.setText(fareBean.getTotalFare());
            }
            txtFareLabel.setText("Estd Fare");
        }
    }

    public void onLaXCarClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        carType = landingPageBean.getCars().get(1).getCarID();

        if (App.isNetworkAvailable()) {
            fetchCarDetails();
        } else {
            Snackbar.make(coordinatorLayout, "Network is not Available", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        llProgressBar.setVisibility(View.VISIBLE);
        llEstimation.setVisibility(View.GONE);

        txtCarTwo.setBackgroundResource(R.drawable.bg_round_edges);
        txtCarOne.setBackgroundResource(R.color.transparent);
        txtCarThree.setBackgroundResource(R.color.transparent);
        carFour.setBackgroundResource(R.color.transparent);
        txtCarTwo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        txtCarOne.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        txtCarThree.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        carFour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        if (sourceBean != null & destinationBean != null) {
            fetchPolyPoints(false);
            if (fareBean != null) {
                txtFare.setText(fareBean.getTotalFare());
            }
            txtFareLabel.setText("Estd Fare");
        }
    }

    public void onCarXlClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        carType = landingPageBean.getCars().get(2).getCarID();

        if (App.isNetworkAvailable()) {
            fetchCarDetails();
        } else {
            Snackbar.make(coordinatorLayout, "Network is not Available", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        llProgressBar.setVisibility(View.VISIBLE);
        llEstimation.setVisibility(View.GONE);

        txtCarThree.setBackgroundResource(R.drawable.bg_round_edges);
        txtCarOne.setBackgroundResource(R.color.transparent);
        txtCarTwo.setBackgroundResource(R.color.transparent);
        carFour.setBackgroundResource(R.color.transparent);
        txtCarThree.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        txtCarTwo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        txtCarOne.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        carFour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        if (sourceBean != null & destinationBean != null) {
            fetchPolyPoints(false);
            if (fareBean != null) {
                txtFare.setText(fareBean.getTotalFare());
            }
            txtFareLabel.setText("Estd Fare");
        }
    }

    public void onCarXxlClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        carType = landingPageBean.getCars().get(3).getCarID();

        if (App.isNetworkAvailable()) {
            fetchCarDetails();
        } else {
            Snackbar.make(coordinatorLayout, "Network is not Available", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        llProgressBar.setVisibility(View.VISIBLE);
        llEstimation.setVisibility(View.GONE);

        carFour.setBackgroundResource(R.drawable.bg_round_edges);
        txtCarOne.setBackgroundResource(R.color.transparent);
        txtCarTwo.setBackgroundResource(R.color.transparent);
        txtCarThree.setBackgroundResource(R.color.transparent);
        carFour.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        txtCarThree.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        txtCarTwo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        txtCarOne.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        if (sourceBean != null & destinationBean != null) {
            fetchPolyPoints(false);
            if (fareBean != null) {
                txtFare.setText(fareBean.getTotalFare());
            }
            txtFareLabel.setText("Estd Fare");
        }
    }


    public void onCarTypeSelected(int position, CarBean bean) {

        carType = bean.getCarID();

        if (App.isNetworkAvailable()) {
            fetchCarDetails();
            llProgressBar.setVisibility(View.VISIBLE);
            llEstimation.setVisibility(View.GONE);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            if (sourceBean != null & destinationBean != null) {
                fetchPolyPoints(false);
                if (fareBean != null) {
                    txtFare.setText(fareBean.getTotalFare());
                }
                txtFareLabel.setText("Estd Fare");
            }
        } else {
            Snackbar.make(coordinatorLayout, AppConstants.NO_NETWORK_AVAILABLE, Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }

    }

    public void onSourceClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        searchType = AppConstants.SEARCH_SOURCE;




    }

    public void onDestinationClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        searchType = AppConstants.SEARCH_DESTINATION;



    }


    private void displayLocation() {

        Log.i(TAG, "displayLocation: OnPlotLocation Called .........>>>>>>>>>>>>>>>>>>>>>>>>..");

        if (LastLocation != null && !isConfirmationPage) {

            onPlotLocation(false, LOCATION_SOURCE, LastLocation.getLatitude(), LastLocation.getLongitude());
            getLocationName(String.valueOf(LastLocation.getLatitude()), String.valueOf(LastLocation.getLongitude()));
//            getLocationName(LastLocation.getLatitude(), LastLocation.getLongitude());
        }
    }

    private void getLocationName(double currentLatitude, double currentLongitude) {

        LocationTask locationTask = new LocationTask(currentLatitude, currentLongitude);
        locationTask.setLocationTaskListener(new LocationTask.LocationTaskListener() {
            @Override
            public void dataDownloadedSuccessfully(PlaceBean placeBean) {

                sourceBean = placeBean;

                if (placeBean != null) {
                    txtActionSearch.setText(placeBean.getName());
                    txtSource.setText(placeBean.getName());
                }
            }

            @Override
            public void dataDownloadFailed() {

            }
        });
        locationTask.execute();


    }

    protected void getLocationName(final String latitude, final String longitude) {

//        swipeView.setRefreshing(true);

        /*String currentLatitude = Config.getInstance().getCurrentLatitude();
        String currentLongitude = Config.getInstance().getCurrentLongitude();

        System.out.println("Current Location : " + currentLatitude + "," + currentLongitude);*/

        HashMap<String, String> urlParams = new HashMap<>();
        //	postData.put("uid", id);
        urlParams.put("latlng", latitude + "," + longitude);
        urlParams.put("sensor", "true");
        urlParams.put("key", getString(R.string.browser_api_key));

        LocationNameTask locationNameTask = new LocationNameTask(urlParams);
        locationNameTask.setLocationNameTaskListener(new LocationNameTask.LocationNameTaskListener() {

            @Override
            public void dataDownloadedSuccessfully(String address) {
                //	System.out.println(landingBean.getStatus());
                if (null != address) {
                    System.out.println("Location Name Retrieved : " + address);
                    Config.getInstance().setCurrentLocation(address);

                    txtActionSearch.setText(address);
                    txtSource.setText(address);
                    if (sourceBean == null)
                        sourceBean = new PlaceBean();
                    sourceBean.setAddress(address);
                    sourceBean.setName(address);
                    sourceBean.setLatitude(latitude);
                    sourceBean.setLongitude(longitude);
                    /*					txtLocation.setText(address);
                    Toast.makeText(CreateActivity.this,"Location Name Retrieved : "+address, Toast.LENGTH_SHORT).show();
					 */
                }
            }

            @Override
            public void dataDownloadFailed() {

            }
        });
        locationNameTask.execute();
    }

    private void fetchCarDetails() {

        if (destinationBean == null) {
            llEstimation.setVisibility(View.GONE);
            llProgressBar.setVisibility(View.VISIBLE);
        }

//        swipeView.setRefreshing(true)
        LatLng center = mMap.getCameraPosition().target;
//        center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();

        if (sourceBean == null) {
            sourceBean = new PlaceBean();
            sourceBean.setLatitude(String.valueOf(center.latitude));
            sourceBean.setLongitude(String.valueOf(center.longitude));
        }


        HashMap<String, String> urlParams = new HashMap<>();
        urlParams.put("car_type", carType);
        urlParams.put("latitude", sourceBean.getLatitude());
        urlParams.put("longitude", sourceBean.getLongitude());

        DataManager.fetchCarAvailability(urlParams, new CarInfoListener() {
            @Override
            public void onLoadCompleted(CarBean carBeanWS) {
                swipeView.setRefreshing(false);
                carBean = carBeanWS;
                populateCarDetails(carBeanWS);

            }

            @Override
            public void onLoadFailed(String error) {
                swipeView.setRefreshing(false);
                txtCarAvailability.setText(R.string.label_no_cars_available);
                txtCarArrivalEstimatedTime.setVisibility(View.GONE);
                Toast.makeText(LandingPageActivity.this, error, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void populateCarDetails(CarBean carBean) {

        if (carBean.getCarsAvailable().equalsIgnoreCase("No cars Available")) {
            txtCarAvailability.setText(carBean.getCarsAvailable());
            txtCarArrivalEstimatedTime.setVisibility(View.GONE);
        } else {
            txtCarArrivalEstimatedTime.setVisibility(View.VISIBLE);
            txtCarAvailability.setText("Set Pick Up Location");
        }

        txtCarArrivalEstimatedTime.setText(carBean.getMinTime());
        txtTime.setText(carBean.getMinTime());
        txtMaxSize.setText(carBean.getMaxSize());
        if (destinationBean == null) {
            txtFare.setText(carBean.getMinFare());
        }

        if (destinationBean == null) {
            llEstimation.setVisibility(View.VISIBLE);
            llProgressBar.setVisibility(View.GONE);
        }
    }

    public void fetchTotalfare() {

        HashMap<String, String> urlParams = null;
        try {
            urlParams = new HashMap<>();
            urlParams.put("car_type", String.valueOf(carType));
            if (sourceBean.getName() != null && !sourceBean.getName().equals("")) {
                urlParams.put("source", sourceBean.getName());
            }
            if (destinationBean.getName() != null && destinationBean.getName().equals("")) {
                urlParams.put("destination", destinationBean.getName());
            }
            urlParams.put("source_latitude", sourceBean.getLatitude());
            urlParams.put("source_longitude", sourceBean.getLongitude());
            urlParams.put("destination_latitude", destinationBean.getLatitude());
            urlParams.put("destination_longitude", destinationBean.getLongitude());
            urlParams.put("distance", String.valueOf(distance));
            urlParams.put("time", String.valueOf(time));

            Log.i(TAG, "fetchTotalfare: Time " + time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataManager.fetchTotalFare(urlParams, new TotalFareListener() {

            @Override
            public void onLoadCompleted(FareBean fareBeanWS) {

                if (isConfirmationPage) {
                    llFare.setVisibility(View.VISIBLE);
                    llConfirmationProgress.setVisibility(View.GONE);

                }
                swipeView.setRefreshing(false);
                fareBean = fareBeanWS;
                populateFareDetails(fareBeanWS);

                txtFare.setVisibility(View.VISIBLE);

                llProgressBar.setVisibility(View.GONE);
                llEstimation.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadFailed(String error) {
                swipeView.setRefreshing(false);

                if (isConfirmationPage) {

                    txtFareEstimate.setVisibility(View.VISIBLE);
                    llProgressBar.setVisibility(View.GONE);
                }

                if (isConfirmationPage) {
                    llFare.setVisibility(View.VISIBLE);
                    rlFare.setVisibility(View.GONE);
                    llConfirmationProgress.setVisibility(View.GONE);
                }

                txtFare.setVisibility(View.GONE);

                txtEstimatedFare.setVisibility(View.GONE);

                llProgressBar.setVisibility(View.GONE);
                llEstimation.setVisibility(View.VISIBLE);

                PopupMessage popupMessage = new PopupMessage(LandingPageActivity.this);
                popupMessage.setPopupActionListener(new PopupMessage.PopupActionListener() {
                    @Override
                    public void actionCompletedSuccessfully(boolean result) {

                        if (!isConfirmationPage) {
                            destinationBean = null;
                            txtTo.setVisibility(View.GONE);
                            txtFareLabel.setText("Min Fare");
                            txtFare.setVisibility(View.VISIBLE);
                            txtFare.setText(carBean.getMinFare());
                            llDestinationEstimated.setVisibility(View.GONE);
                            txtFareEstimate.setVisibility(View.VISIBLE);
                        } else {
                            mMap.clear();
                            onPlotLocation(true, LOCATION_SOURCE, sourceBean.getDLatitude(), sourceBean.getDLongitude());
                            destinationBean = null;
                            txtDestination.setText("");
                            rlFare.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void actionFailed() {

                    }
                });
                popupMessage.show(error, 0);

            }
        });
    }

    private void populateFareDetails(FareBean fareBean) {

        if (fareBean.getTotalFare() != null) {
            txtTotalFare.setText(fareBean.getTotalFare());
            txtFare.setText(fareBean.getTotalFare());
        }
    }

    public void getEstimatedFare() {

        String source = txtActionSearch.getText().toString();
        String destination = txtEstimatedDestination.getText().toString();

        HashMap<String, String> urlParams = new HashMap<>();
        urlParams.put("source", source);
        urlParams.put("destination", destination);

        urlParams.put("car_type", String.valueOf(carType));

        urlParams.put("source_latitude", sourceBean.getLatitude());
        urlParams.put("source_longitude", sourceBean.getLongitude());
        urlParams.put("destination_latitude", destinationBean.getLatitude());
        urlParams.put("destination_longitude", destinationBean.getLongitude());

        urlParams.put("distance", String.valueOf(distance));
        urlParams.put("time", String.valueOf(time));

        Log.i(TAG, "getEstimatedFare: Time " + time);

        DataManager.fetchTotalFare(urlParams, new TotalFareListener() {

            @Override
            public void onLoadCompleted(FareBean fareBean) {
                swipeView.setRefreshing(false);
                populateEstimatedFare(fareBean);
            }

            @Override
            public void onLoadFailed(String error) {
                swipeView.setRefreshing(false);

            }
        });
    }

    public void populateEstimatedFare(FareBean fareBean) {

        txtFare.setText(fareBean.getTotalFare());
    }

    public void onEstimatedDestinationClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        onFareEstimateClick(view);
    }

    public void onRequestRideClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (carBean.getCarsAvailable().equalsIgnoreCase("No cars Available") && txtDestination.getText().length() != 0) {

            Snackbar.make(coordinatorLayout, "No Cars Available for the given Location.", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        } else {

            if (txtDestination.getText().length() == 0) {
                Snackbar.make(coordinatorLayout, "Destination Is Required", Snackbar.LENGTH_SHORT)
                        .setAction("Refresh", snackBarRefreshOnClickListener).show();
            } else {
                if (fareBean != null && sourceBean != null && destinationBean != null) {

                    Toast.makeText(this, "here  ", Toast.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(coordinatorLayout, R.string.message_something_went_wrong, Snackbar.LENGTH_LONG)
                            .setAction(R.string.btn_dismiss, snackBarDismissOnClickListener).show();
                }
            }
        }
    }

    public void fetchLandingPageDetails() {

        Log.i(TAG, "fetchLandingPageDetails: AuthToken" + Config.getInstance().getAuthToken());

        HashMap<String, String> urlParams = new HashMap<>();

        if (mMap != null) {
            LatLng center = mMap.getCameraPosition().target;
            urlParams.put("latitude", String.valueOf(center.latitude));
            urlParams.put("longitude", String.valueOf(center.longitude));
        } else {
            urlParams.put("latitude", Config.getInstance().getCurrentLatitude());
            urlParams.put("longitude", Config.getInstance().getCurrentLongitude());
        }
        DataManager.fetchLandingPageDetails(urlParams, new LandingPageListener() {

            @Override
            public void onLoadCompleted(LandingPageBean landingPageBeanWS) {
                swipeView.setRefreshing(false);
                setProgressScreenVisibility(false, false);
                landingPageBean = landingPageBeanWS;
                populateLandingPageDetails(landingPageBeanWS);

            }

            @Override
            public void onLoadFailed(String error) {
                swipeView.setRefreshing(false);
                setProgressScreenVisibility(true, false);
                Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.btn_retry, snackBarRefreshOnClickListener).show();
            }
        });
    }

    private void populateLandingPageDetails(LandingPageBean landingPageBean) {


        Collections.sort(landingPageBean.getCars());





        if (carType.equalsIgnoreCase("") || landingPageBean.getCar(carType) == null) {
            carType = landingPageBean.getCars() != null && !landingPageBean.getCars().isEmpty()
                    ? landingPageBean.getCars().get(0).getCarID() : "-1";
        }

        fetchCarDetails();
    }

    public void onPlotLocation(boolean isMarkerNeeded, int type, double latitude, double longitude) {

        LatLng newLatLng = null;
        try {
            newLatLng = new LatLng(latitude, longitude);
            if (isMarkerNeeded) {
                switch (type) {
                    case LOCATION_SOURCE:
                        mMap.addMarker(new MarkerOptions().position(newLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_source_marker)));
                        break;
                    case LOCATION_DESTINATION:
                        mMap.addMarker(new MarkerOptions().position(newLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_marker)));
                        break;
                    default:
                        mMap.addMarker(new MarkerOptions().position(newLatLng).icon(BitmapDescriptorFactory.defaultMarker()));
                        break;
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 18));
            Log.i(TAG, "onPlotLocation: Position" + newLatLng);

        } catch (NumberFormatException e) {
            e.printStackTrace();

        }
    }


    public void fetchPolyPoints(final boolean isPolyLineNeeded) {

        HashMap<String, String> urlParams = new HashMap<>();

//        if (sourceBean != null && destinationBean != null) {
        urlParams.put("origin", sourceBean.getLatitude() + "," + sourceBean.getLongitude());
        urlParams.put("destination", destinationBean.getLatitude() + "," + destinationBean.getLongitude());
        urlParams.put("mode", "driving");
        urlParams.put("key", getString(R.string.browser_api_key));
//        }

        DataManager.fetchPolyPoints(urlParams, new PolyPointsListener() {

            @Override
            public void onLoadCompleted(PolyPointsBean polyPointsBeanWS) {
                swipeView.setRefreshing(false);

                polyPointsBean = polyPointsBeanWS;
                time = String.valueOf(polyPointsBean.getTime());
                distance = String.valueOf(polyPointsBean.getDistance());

                Log.i(TAG, "onLoadCompleted: Time Taken" + polyPointsBean.getTimeText());
                Log.i(TAG, "onLoadCompleted: Distance" + polyPointsBean.getDistanceText());

                fetchTotalfare();

                if (isPolyLineNeeded) {
                    if (!isDestinationEstimateSelect)
                        populatePath();
                    isDestinationEstimateSelect = false;
                }
            }

            @Override
            public void onLoadFailed(String error) {
                swipeView.setRefreshing(false);
                Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_LONG)
                        .setAction("Dismiss", snackBarDismissOnClickListener).show();
            }
        });
    }

    private void populatePath() {

        List<List<HashMap<String, String>>> routes = polyPointsBean.getRoutes();

        ArrayList<LatLng> points = null;
        PolylineOptions polyLineOptions = null;

        // traversing through routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<LatLng>();
            polyLineOptions = new PolylineOptions();
            List path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap point = (HashMap) path.get(j);

                double lat = Double.parseDouble((String) point.get("lat"));
                double lng = Double.parseDouble((String) point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            polyLineOptions.addAll(points);
            polyLineOptions.width(8);
            polyLineOptions.color(ContextCompat.getColor(getApplicationContext(), R.color.map_path));

        }

        polyLine = mMap.addPolyline(polyLineOptions);
    }

    public void mapAutoZoom() {


        if (sourceBean != null && destinationBean != null) {
            newLatLng1 = new LatLng(sourceBean.getDLatitude(), sourceBean.getDLongitude());
            newLatLng2 = new LatLng(destinationBean.getDLatitude(), destinationBean.getDLongitude());
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(newLatLng1);
        builder.include(newLatLng2);
        bounds = builder.build();

//        mMap.setPadding(0, (int) (height - getStatusBarHeight() - mActionBarHeight - (px * 160)), 0, (int) (height - getStatusBarHeight() - mActionBarHeight - (px * 120)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int) (40 * px)));
    }

    public void onLayoutClickLandingPage(View view) {

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public int getNavBarHeight() {
        Context context = App.getInstance().getApplicationContext();
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    protected void setUpLocationClientIfNeeded() {
       /* if(!checkForLocationPermissions())
            getLocationPermissions();*/


        if (App.getInstance().getGoogleApiClient() == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(App.getInstance().getApplicationContext())
                    .addApi(LocationServices.API)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            //		mGoogleApiClient = new LocationClient(getApplicationContext(),this,this);
            App.getInstance().setGoogleApiClient(mGoogleApiClient);
        } else {
            mGoogleApiClient = App.getInstance().getGoogleApiClient();
        }

        if (isMapInit) {
            mGoogleApiClient.registerConnectionCallbacks(this);
            mGoogleApiClient.registerConnectionFailedListener(this);
            isMapInit = false;
        }
    }

    protected void getCurrentLocation() {

        setUpLocationClientIfNeeded();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!checkForLocationPermissions())
                getLocationPermissions();
            checkLocationSettingsStatus();
        } else {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

                if (LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
                    Config.getInstance().setCurrentLatitude(""
                            + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude());
                    Config.getInstance().setCurrentLongitude(""
                            + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());
//                    getLocationName();
                }
            }
            /*else{
                System.out.println("Last Location : " + mockLocation);
				currentLatitude = ""+mockLocation.getLatitude();
				currentLongitude = ""+mockLocation.getLongitude();
			}*/

            if ((Config.getInstance().getCurrentLatitude() == null || Config.getInstance().getCurrentLongitude() == null)
                    || (Config.getInstance().getCurrentLatitude().equals("") || Config.getInstance().getCurrentLatitude().equals(""))) {
//            Toast.makeText(BaseAppCompatActivity.this, "Retrieving Current Location...", Toast.LENGTH_SHORT).show();
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }
            } else {
                if (isInit) {
                    getData();
                    isInit = false;
                }
                /*if (mGoogleApiClient != null) {
                    mGoogleApiClient.disconnect();
                }*/
            }

            //			mHandler.postDelayed(periodicTask, 3000);
        }
    }

    @Override
    public void onLocationChanged(Location location) {


        Log.i(TAG, "onLocationChanged: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
        if ((Config.getInstance().getCurrentLatitude() == null || Config.getInstance().getCurrentLongitude() == null)
                || (Config.getInstance().getCurrentLatitude().equals("") || Config.getInstance().getCurrentLatitude().equals(""))) {
            Config.getInstance().setCurrentLatitude("" + location.getLatitude());
            Config.getInstance().setCurrentLongitude("" + location.getLongitude());
        } else {
            Config.getInstance().setCurrentLatitude("" + location.getLatitude());
            Config.getInstance().setCurrentLongitude("" + location.getLongitude());
        }

        if (isInit) {
            getData();
            isInit = false;
        }
        if (sourceBean == null && mMap != null) {
            LastLocation = location;
            displayLocation();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult arg0) {
    }

    @Override
    public void onConnected(Bundle arg0) {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!checkForLocationPermissions())
                    getLocationPermissions();
                checkLocationSettingsStatus();
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                //	mGoogleApiClient.requestLocationUpdates(mLocationRequest,HomeActivity.this);
            }
        } catch (Exception e) {

            e.printStackTrace();
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
    public void onConnectionSuspended(int arg0) {

    }
}




