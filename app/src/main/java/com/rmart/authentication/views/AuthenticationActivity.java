package com.rmart.authentication.views;

import android.os.Bundle;

import com.rmart.R;
import com.rmart.authentication.OnAuthenticationClickedListener;
import com.rmart.baseclass.views.BaseActivity;

public class AuthenticationActivity extends BaseActivity implements OnAuthenticationClickedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticatin);
        addFragment(LoginFragment.newInstance("",""), "login", false);
    }

    @Override
    public void showBadge(boolean b) {

    }

    @Override
    public void updateBadgeCount() {

    }

    @Override
    public void goToHomeActivity() {

    }

    @Override
    public void goToForgotPassword() {
        replaceFragment(ForgotPasswordFragment.newInstance("", ""), "ForgotPasswordFragment", true);
    }

    @Override
    public void goToRegistration() {
        replaceFragment(RegistrationFragment.newInstance("", ""), "RegistrationFragment", true);
    }

    @Override
    public void validateOTP() {
        replaceFragment(OTPFragment.newInstance("", ""), "OTPFragment", true);
    }

    @Override
    public void changePassword() {

    }

    @Override
    public void goToProfileActivity() {

    }
}