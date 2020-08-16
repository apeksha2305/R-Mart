package com.rmart.orders.views;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.rmart.R;
import com.rmart.baseclass.views.BaseNavigationDrawerActivity;
import com.rmart.orders.OnOrdersInteractionListener;
import com.rmart.orders.viewmodel.MyOrdersViewModel;
import com.rmart.orders.models.OrderObject;
import com.rmart.orders.models.SelectedOrderGroup;

public class OrdersActivity extends BaseNavigationDrawerActivity implements OnOrdersInteractionListener {

    MyOrdersViewModel myOrdersViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myOrdersViewModel = new ViewModelProvider(this).get(MyOrdersViewModel.class);
        addFragment(OrderHomeFragment.newInstance("", ""), "OrderHomeFragment", false);
    }

    @Override
    public void goToHome() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void goToViewFullOrder(OrderObject orderObject) {
        replaceFragment(ViewFullOrderFragment.newInstance(orderObject, ""), "ViewFullOrderFragment", true);
    }

    @Override
    public void goToOTPValidation(OrderObject orderObject) {
        replaceFragment(CustomerVerificationFragment.newInstance(orderObject, ""), "CustomerVerificationFragment", true);
    }

    @Override
    public void goToProcessToDelivery(OrderObject orderObject) {
        replaceFragment(ProcessToDeliveryFragment.newInstance(orderObject, ""), "ProcessToDeliveryFragment", true);
    }

    @Override
    public void showOrderList() {
        replaceFragment(OrderListFragment.newInstance(""), "OrderListFragment", true);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.orders) {
            getToActivity(view.getId(), false);
        } else {
            getToActivity(view.getId(), true);
        }
    }
}