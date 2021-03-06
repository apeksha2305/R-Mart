package com.rmart.customer_order.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.rmart.R;
import com.rmart.baseclass.views.BaseFragment;
import com.rmart.customer.models.AddToCartResponseDetails;
import com.rmart.customer_order.adapters.ProductListAdapter;
import com.rmart.customer_order.models.OrderAgainProductModel;
import com.rmart.profile.model.MyProfile;
import com.rmart.utilits.LoggerInfo;
import com.rmart.utilits.RetrofitClientInstance;
import com.rmart.utilits.UpdateCartCountDetails;
import com.rmart.utilits.Utils;
import com.rmart.utilits.pojos.customer_orders.CustomerInfo;
import com.rmart.utilits.pojos.customer_orders.CustomerOrderProductList;
import com.rmart.utilits.pojos.customer_orders.CustomerOrderProductResponse;
import com.rmart.utilits.pojos.customer_orders.VendorInfo;
import com.rmart.utilits.pojos.orders.Product;
import com.rmart.utilits.services.CustomerOrderService;
import com.rmart.utilits.services.CustomerProductsService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckAvailableProducts extends BaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private CustomerOrderProductList order;

    private AppCompatTextView vendorName, vendorNumber, vendorAddress,
            customerName, customerNumber, customerAddress;
    private RecyclerView recyclerView;
    private AppCompatButton btnAddToCartField;
    private List<Product> orderedProductsList;
    private List<OrderAgainProductModel> orderAgainProductsList;

    public CheckAvailableProducts() {
        // Required empty public constructor
    }

    public static CheckAvailableProducts newInstance(CustomerOrderProductList order, String param2) {
        CheckAvailableProducts fragment = new CheckAvailableProducts();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, order);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (CustomerOrderProductList) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LoggerInfo.printLog("Fragment", "CheckAvailableProducts");
        return inflater.inflate(R.layout.fragment_view_full_order, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(getString(R.string.product_re_order));
        getServerData();
    }

    private void getServerData() {
        if(Utils.isNetworkConnected(requireActivity())) {
            progressDialog.show();
            CustomerOrderService customerOrderService = RetrofitClientInstance.getRetrofitInstance().create(CustomerOrderService.class);
            MyProfile myProfile = MyProfile.getInstance();
            if (myProfile != null) {
                String userId = myProfile.getUserID();
                String mobileNumber = myProfile.getMobileNumber();
                VendorInfo vendorInfo = order.getVendorInfo();
                if (vendorInfo != null && !TextUtils.isEmpty(vendorInfo.getMobileNumber())) {
                    String vendorMobileNo = order.getVendorInfo().getMobileNumber();
                    customerOrderService.getUpdatedProductDetails(order.getOrderInfo().getOrderID(), userId, mobileNumber, vendorMobileNo).enqueue(new Callback<CustomerOrderProductResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<CustomerOrderProductResponse> call, @NotNull Response<CustomerOrderProductResponse> response) {
                            if (response.isSuccessful()) {
                                CustomerOrderProductResponse data = response.body();
                                if (data != null) {
                                    if (data.getStatus().equalsIgnoreCase(Utils.SUCCESS)) {
                                        ArrayList<Product> productsList = data.getProductList().getProduct();
                                        order.setProduct(productsList);
                                        updateUI();
                                    } else {
                                        showCloseDialog(null, data.getMsg());
                                    }
                                } else {
                                    showCloseDialog(null, getString(R.string.no_information_available));
                                }
                            } else {
                                showCloseDialog(null, response.message());
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(@NotNull Call<CustomerOrderProductResponse> call, @NotNull Throwable t) {
                            if(t instanceof SocketTimeoutException){
                                showDialog("", getString(R.string.network_slow));
                            } else {
                                showDialog("", t.getMessage());
                            }
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    showCloseDialog(null, getString(R.string.no_product_details_found));
                }
            }
        } else {
            showCloseDialog(getString(R.string.error_internet), getString(R.string.error_internet_text));
        }
    }

    private void updateUI() {

        // vendor
        CustomerInfo customerInfo = order.getCustomerInfo();
        if (customerInfo != null) {
            String text = customerInfo.getFirstName() + " " + customerInfo.getLastName();
            customerName.setText(text);
            customerNumber.setText(customerInfo.getMobileNumber());
            customerAddress.setText(customerInfo.getCompleteAddress());
        }

        // vendor
        VendorInfo vendorInfo = order.getVendorInfo();
        if(vendorInfo != null) {
            vendorName.setText(vendorInfo.getShopName());
            vendorNumber.setText(vendorInfo.getMobileNumber());
            vendorAddress.setText(vendorInfo.getCompleteAddress());
        }

        // payment info
        /*tvAmount.setText(order.getOrderInfo().getOrderAmount());
        text = order.getOrderInfo().getOrderCharges();
        tvDeliveryCharges.setText(text);
        tvTotalCharges.setText(order.getOrderInfo().getTotalAmt());
        tvPaymentType.setText(order.getOrderInfo().getModeOfPayment());*/

        orderedProductsList = order.getProduct();
        if (orderedProductsList != null && !orderedProductsList.isEmpty()) {
            Set<Product> set = new HashSet<>(orderedProductsList);
            orderedProductsList.clear();
            orderedProductsList.addAll(set);
            List<Object> lUpdatedProductsList = new ArrayList<>(orderedProductsList);
            ProductListAdapter productAdapter = new ProductListAdapter(requireActivity(), lUpdatedProductsList);
            recyclerView.setAdapter(productAdapter);
        }

        orderAgainProductsList = new ArrayList<>();
        for (Product productOrder : orderedProductsList) {
            if(productOrder.getAvailableQuantity() != 0) {
                OrderAgainProductModel product = new OrderAgainProductModel();
                product.setProductUnitId(productOrder.getProductUnitId());
                product.setProductQuantity(productOrder.getQuantity());
                orderAgainProductsList.add(product);
            }
        }
        if(!orderAgainProductsList.isEmpty()) {
            btnAddToCartField.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.order_status).setVisibility(View.GONE);
        view.findViewById(R.id.status).setVisibility(View.GONE);

        //Payment info
        /*tvAmount = view.findViewById(R.id.amount);
        tvDeliveryCharges = view.findViewById(R.id.delivery_charges);
        tvTotalCharges = view.findViewById(R.id.total_charges);
        tvPaymentType = view.findViewById(R.id.payment_type);*/

        //Vendor Info
        vendorName = view.findViewById(R.id.vendor_name);
        vendorNumber = view.findViewById(R.id.vendor_number);
        vendorAddress = view.findViewById(R.id.vendor_address);

        // Customer Info
        customerName = view.findViewById(R.id.customer_name);
        customerNumber = view.findViewById(R.id.customer_number);
        customerAddress = view.findViewById(R.id.customer_address);
        recyclerView = view.findViewById(R.id.product_list);
        view.findViewById(R.id.delivery_boy_info).setVisibility(View.GONE);
        view.findViewById(R.id.payment_info_layout_field).setVisibility(View.GONE);
        view.findViewById(R.id.custom_details_root).setVisibility(View.GONE);
        btnAddToCartField = view.findViewById(R.id.btn_add_to_cart_field);
        btnAddToCartField.setOnClickListener(v -> {
            btnAddToCartField.setVisibility(View.GONE);
            addToCartSelected();
        });
    }

    private void addToCartSelected() {
        if (Utils.isNetworkConnected(requireActivity())) {
            if (orderedProductsList != null && !orderedProductsList.isEmpty()) {
                try {
                    progressDialog.show();
                    CustomerProductsService customerProductsService = RetrofitClientInstance.getRetrofitInstance().create(CustomerProductsService.class);
                    String clientID = "2";
                    JSONArray jsonArray = new JSONArray();
                    for (OrderAgainProductModel orderAgainProductModel : orderAgainProductsList) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("product_unit_id", orderAgainProductModel.getProductUnitId());
                        jsonObject.put("product_quantity", orderAgainProductModel.getProductQuantity());
                        jsonArray.put(jsonObject);
                    }
                    Call<AddToCartResponseDetails> call = customerProductsService.addReOrderToCart(clientID, order.getVendorInfo().getUserID(), MyProfile.getInstance().getUserID(),
                            jsonArray);
                    call.enqueue(new Callback<AddToCartResponseDetails>() {
                        @Override
                        public void onResponse(@NotNull Call<AddToCartResponseDetails> call, @NotNull Response<AddToCartResponseDetails> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful()) {
                                AddToCartResponseDetails body = response.body();
                                if (body != null) {
                                    if (body.getStatus().equalsIgnoreCase("success")) {
                                        AddToCartResponseDetails.AddToCartDataResponse addToCartDataResponse = body.getAddToCartDataResponse();
                                        if (addToCartDataResponse != null) {
                                            Integer totalCartCount = addToCartDataResponse.getTotalCartCount();
                                            UpdateCartCountDetails.updateCartCountDetails.onNext(totalCartCount);
                                            showDialog(body.getMsg());
                                        } else {
                                            btnAddToCartField.setVisibility(View.VISIBLE);
                                            showDialog(getString(R.string.no_information_available));
                                        }
                                    } else {
                                        btnAddToCartField.setVisibility(View.VISIBLE);
                                        showDialog(body.getMsg());
                                    }
                                } else {
                                    btnAddToCartField.setVisibility(View.VISIBLE);
                                    showDialog(getString(R.string.no_information_available));
                                }
                            } else {
                                btnAddToCartField.setVisibility(View.VISIBLE);
                                showDialog(response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<AddToCartResponseDetails> call, @NotNull Throwable t) {
                            btnAddToCartField.setVisibility(View.VISIBLE);
                            if(t instanceof SocketTimeoutException){
                                showDialog("", getString(R.string.network_slow));
                            } else {
                                showDialog("", t.getMessage());
                            }
                            progressDialog.dismiss();
                        }
                    });
                } catch (Exception ex) {
                    btnAddToCartField.setVisibility(View.VISIBLE);
                    showDialog(ex.getMessage());
                }
            }
        } else {
            btnAddToCartField.setVisibility(View.VISIBLE);
            showDialog(getString(R.string.error_internet), getString(R.string.error_internet_text));
        }
    }

    private void showCloseDialog(String title, String message) {
        if(!requireActivity().isFinishing()) {
            showDialog(title, message, pObject -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }
    }
}