package com.rmart.authentication.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.rmart.R;
import com.rmart.authentication.OnAuthenticationClickedListener;
import com.rmart.baseclass.views.BaseActivity;
import com.rmart.customer.models.RSAKeyResponseDetails;
import com.rmart.customer.views.CustomerHomeActivity;
import com.rmart.mapview.MyLocation;
import com.rmart.orders.views.OrdersActivity;
import com.rmart.profile.model.MyProfile;
import com.rmart.profile.views.MyProfileActivity;

import static com.rmart.fcm.MyFirebaseMessagingService.ORDER_ID;

public class AuthenticationActivity extends BaseActivity implements OnAuthenticationClickedListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private SupportMapFragment mapFragment;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private LocationManager locationManager;
    private int REQUEST_CHECK_SETTINGS = 199;
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticatin);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            orderID = extras.getString(ORDER_ID);
        }
        if (getIntent().getBooleanExtra(getString(R.string.change_password), false)) {
            addFragment(ChangePassword.newInstance("", MyProfile.getInstance().getMobileNumber()), "changePassword", false);
        } else {
            addFragment(LoginFragment.newInstance("", ""), "login", false);
        }

        AuthenticationActivity.super.requestAppPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                R.string.runtime_permissions_txt, 100);
    }

    @Override
    public void onPermissionsGranted(Integer requestCode) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
            } else {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS();
                } else {
                    getLocation();
                }
            }
        });
        task.addOnFailureListener(exception -> {
            try {
                if (exception instanceof ResolvableApiException) {
                    ((ResolvableApiException) exception).startResolutionForResult(AuthenticationActivity.this, REQUEST_CHECK_SETTINGS);
                }
            } catch (IntentSender.SendIntentException ex) {
                // Ignore the error.
            }
        });
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("No", (dialog, which) -> dialog.cancel());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {


            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    latitude = task.getResult().getLatitude();
                    longitude = task.getResult().getLongitude();
                } else {
                   /* boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    if (isGPSEnabled) {

                    } else if (isNetworkEnabled) {

                    }*/
                    if (ActivityCompat.checkSelfPermission(AuthenticationActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(AuthenticationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (locationGPS != null) {
                        currentLocation = locationGPS;
                        latitude = locationGPS.getLatitude();
                        longitude = locationGPS.getLongitude();
                    } else {
                        MyLocation myLocation = new MyLocation(AuthenticationActivity.this);
                        myLocation.getLocation(locationResult);
                    }
                }
            });
        }
    }

    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                currentLocation = location;
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        }
    };

    @Override
    public void showBadge(boolean b) {

    }

    @Override
    public void updateBadgeCount() {

    }

    @Override
    public void hideHamburgerIcon() {

    }

    @Override
    public void showHamburgerIcon() {

    }

    @Override
    public void showCartIcon() {

    }

    @Override
    public void hideCartIcon() {

    }

    @Override
    public void goToHomeActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, OrdersActivity.class);
        if(null!=orderID) {
            intent.putExtra(ORDER_ID, orderID);
        }
        // intent.putExtra(ORDER_ID, "106");
        startActivity(intent);
    }

    @Override
    public void goToForgotPassword() {
        replaceFragment(ForgotPasswordFragment.getInstance(), ForgotPasswordFragment.class.getName(), true);
    }

    @Override
    public void goToRegistration() {
        replaceFragment(RegistrationFragment.getInstance(), RegistrationFragment.class.getName(), true);
    }

    @Override
    public void validateOTP(String mobileNumber, boolean closePreviousScreen) {
        if(closePreviousScreen) {
            replaceFragment(OTPFragment.newInstance(mobileNumber),  OTPFragment.class.getName(), false);
        } else {
            replaceFragment(OTPFragment.newInstance(mobileNumber), OTPFragment.class.getName(), true);
        }

    }

    @Override
    public void changePassword(String otp, String mobileNumber) {
        replaceFragment(ChangePassword.newInstance(otp, mobileNumber), ChangePassword.class.getName(), true);
    }

    @Override
    public void goToProfileActivity(boolean isAddressAdded) {
        Intent intent = new Intent(AuthenticationActivity.this, MyProfileActivity.class);
        intent.putExtra("is_edit", true);
        intent.putExtra("IsNewAddress", isAddressAdded);
        startActivity(intent);
    }

    @Override
    public void validateMailOTP() {

    }

    @Override
    public void goToHomePage() {
        Intent in = new Intent(this, AuthenticationActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(in);
        finish();
    }

    @Override
    public void goToCustomerHomeActivity() {
        Intent in = new Intent(this, CustomerHomeActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(in);
        finish();
    }

    @Override
    public void proceedToPayment(RSAKeyResponseDetails mobileNumber) {
        replaceFragment(PaymentFragment.newInstance(mobileNumber, ""), PaymentFragment.class.getName(), true);
    }
}