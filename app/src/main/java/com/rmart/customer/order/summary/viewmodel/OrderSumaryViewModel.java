package com.rmart.customer.order.summary.viewmodel;

import android.app.Activity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.rmart.R;
import com.rmart.customer.order.summary.model.OrderedSummaryResponse;
import com.rmart.customer.order.summary.repository.OrderSummaryRepository;
import com.rmart.customer.shops.list.models.CustomerProductsShopDetailsModel;
import com.rmart.customer.views.PaymentOptionsFragment;
import com.rmart.profile.model.MyProfile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OrderSumaryViewModel extends ViewModel {
    public static final String DELIVERY ="Delivery";
    public static final String PICKUP ="Pickup";
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<Boolean> iserror = new MutableLiveData<>();
    public MutableLiveData<Integer> selectedPaymentType = new MutableLiveData<>();
    public MutableLiveData<String> DiscountCode = new MutableLiveData<>();
    public MutableLiveData<OrderedSummaryResponse> orderedSummaryResponseMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<CustomerProductsShopDetailsModel> vendorShoppingCartDetails = new MutableLiveData<>();


    public void showOrderSummary(int vendorId, int shop_id, String user_address_id, String delivery_method,String coupon_code){
        isLoading.setValue(true);

        OrderSummaryRepository.showCartOrderDetails(vendorId,shop_id,user_address_id,delivery_method,coupon_code).observeForever(orderedSummaryResponse -> {
            orderedSummaryResponseMutableLiveData.setValue(orderedSummaryResponse);
            isLoading.postValue(false);
            if(orderedSummaryResponse.getStatus().equalsIgnoreCase("success")) {

                iserror.setValue(false);

            } else {
                iserror.setValue(true);
            }
        });




    }

    public void onClick(final View view) {
        MyProfile myProfile = MyProfile.getInstance();

        switch(view.getId()) {
            case R.id.pick_fromStore:
                showOrderSummary(vendorShoppingCartDetails.getValue().getVendorId(), vendorShoppingCartDetails.getValue().getShopId(), myProfile.getPrimaryAddressId(), PICKUP, DiscountCode.getValue());
                break;
            case R.id.home_delivery:
                showOrderSummary(vendorShoppingCartDetails.getValue().getVendorId(), vendorShoppingCartDetails.getValue().getShopId(), myProfile.getPrimaryAddressId(), DELIVERY, DiscountCode.getValue());
            break;
            case R.id.btnpromocodeapply:
                if(DiscountCode.getValue()!=null && !DiscountCode.getValue().equalsIgnoreCase("")) {
                    showOrderSummary(vendorShoppingCartDetails.getValue().getVendorId(), vendorShoppingCartDetails.getValue().getShopId(), myProfile.getPrimaryAddressId(), orderedSummaryResponseMutableLiveData.getValue().getCustomerOrderedDataResponseModel().deliveryMethod, DiscountCode.getValue());
                } else {

                    Toast.makeText(view.getContext(),"Pease Enter Promocode",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.netBancking:
                selectedPaymentType.setValue(2);
            break;
            case R.id.cash_on_delivery_layout_field:
                selectedPaymentType.setValue(1);
            break;
            case R.id.placeorder:

                if(selectedPaymentType.getValue()!=null && selectedPaymentType.getValue()>0) {
                    FragmentManager fm = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    vendorShoppingCartDetails.getValue().deliveryMethod=orderedSummaryResponseMutableLiveData.getValue().getCustomerOrderedDataResponseModel().deliveryMethod;
                    fragmentTransaction.replace(R.id.base_container, PaymentOptionsFragment.getInstance(vendorShoppingCartDetails.getValue(), selectedPaymentType.getValue()), PaymentOptionsFragment.class.getName());
                    fragmentTransaction.addToBackStack(PaymentOptionsFragment.class.getName());
                    fragmentTransaction.commit();
                } else {
                    Toast.makeText(view.getContext(),"Please select Payment Method",Toast.LENGTH_LONG).show();
                }


                break;
        }
        }

}