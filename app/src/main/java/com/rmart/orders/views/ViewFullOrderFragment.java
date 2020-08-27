package com.rmart.orders.views;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.rmart.R;
import com.rmart.orders.adapters.ProductListAdapter;
import com.rmart.orders.models.OrderObject;
import com.rmart.orders.viewmodel.MyOrdersViewModel;

import java.util.Objects;

public class ViewFullOrderFragment extends BaseOrderFragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OrderObject mOrderObject;
    private String mParam2;
    MyOrdersViewModel viewModel;
    AppCompatButton mLeftButton, mRightButton;
    private AppCompatTextView tvStatus, dateValue, orderIdValue, tvAmount, tvDeliveryCharges, tvTotalCharges, tvPaymentType,
            deliveryBoyName, deliveryBoyNumber;
    private ProductListAdapter productAdapter;
    private RecyclerView recyclerView;
    private LinearLayout deliveryBoyInfo;

    public ViewFullOrderFragment() {
        // Required empty public constructor
    }


    public static ViewFullOrderFragment newInstance(OrderObject param1, String param2) {
        ViewFullOrderFragment fragment = new ViewFullOrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOrderObject = (OrderObject) getArguments().getSerializable(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).setTitle("Order details");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MyOrdersViewModel.class);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_full_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.product_list);
        mLeftButton = view.findViewById(R.id.left_button);
        mLeftButton.setOnClickListener(this);
        mRightButton = view.findViewById(R.id.right_button);
        mRightButton.setOnClickListener(this);
        //Customer Info
        tvStatus = view.findViewById(R.id.status);
        dateValue = view.findViewById(R.id.date_value);
        orderIdValue = view.findViewById(R.id.order_id_value);

       // delivery boy info
        deliveryBoyInfo = view.findViewById(R.id.delivery_boy_info);
        deliveryBoyInfo.setVisibility(View.GONE);
        deliveryBoyName = view.findViewById(R.id.delivery_boy_name);
        deliveryBoyNumber = view.findViewById(R.id.delivery_boy_number);

        //Payment info
        tvAmount = view.findViewById(R.id.amount);
        tvDeliveryCharges = view.findViewById(R.id.delivery_charges);
        tvTotalCharges = view.findViewById(R.id.total_charges);
        tvPaymentType = view.findViewById(R.id.payment_type);

        setValuesToUI();

        productAdapter = new ProductListAdapter(mOrderObject.getProductObjects(), this);
        recyclerView.setAdapter(productAdapter);

    }
    void setValuesToUI() {
        orderIdValue.setText(mOrderObject.getOrderID());
        dateValue.setText(mOrderObject.getDate());
        tvAmount.setText(mOrderObject.getOrderAmount());
        tvDeliveryCharges.setText(mOrderObject.getCharges());
        tvTotalCharges.setText(mOrderObject.getTotalAmount());
        tvPaymentType.setText(mOrderObject.getModeType());
        updateUI();
    }

    void updateUI() {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.status_order), mOrderObject.getOrderType());
        tvStatus.setText(text);
        setFooter();
    }
    private void setFooter() {
        if(mOrderObject.getOrderType().contains(getResources().getString(R.string.open))) {
            isOpenOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.accepted))) {
            isAcceptedOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.packed))) {
            isPackedOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.shipped))) {
            isShippedOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.delivered))) {
            isDeliveredOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.returned))) {
            isReturnedOrder();
        } else if(mOrderObject.getOrderType().contains(getResources().getString(R.string.canceled))) {
            isCanceledOrder();
        }
    }

    @Override
    public void onClick(View view) {
        viewModel.deleteOrder(mOrderObject, getResources());
        String text  = ((AppCompatButton) view).getText().toString();
        if(text.contains(getResources().getString(R.string.accept))) {
            mOrderObject.setOrderType(getResources().getString(R.string.accepted));
            updateToAccepted();
        } else if(text.contains(getResources().getString(R.string.packed))) {
            mOrderObject.setOrderType(getResources().getString(R.string.packed));
            updateToPacked();
        } else if(text.contains(getResources().getString(R.string.shipped))) {
            mOrderObject.setOrderType(getResources().getString(R.string.shipped));
            updateToShipped();
        } else if(text.contains(getResources().getString(R.string.delivered))) {
            mListener.goToProcessToDelivery(mOrderObject);
            /**/
        } else if(text.contains(getResources().getString(R.string.returned))) {
            mOrderObject.setOrderType(getResources().getString(R.string.returned));
            updateToReturned();
        } else if(text.contains(getResources().getString(R.string.cancel))) {
            mOrderObject.setOrderType(getResources().getString(R.string.canceled));
            updateToCancel();
        }
        updateUI();
        // Objects.requireNonNull(getActivity()).onBackPressed();
    }

    void updateToCancel() {
        viewModel.setCanceledOrders(mOrderObject);
    }
    void updateToAccepted() {
        viewModel.setAcceptedOrders(mOrderObject);
    }
    void updateToPacked() {
        viewModel.setPackedOrders(mOrderObject);
        // Objects.requireNonNull(viewModel.getPackedOrders().getValue()).getOrderObjects().add(mOrderObject);
    }
    void updateToShipped() {
        viewModel.setShippedOrders(mOrderObject);
        // Objects.requireNonNull(viewModel.getShippedOrders().getValue()).getOrderObjects().add(mOrderObject);
    }
    void updateToDelivered() {

        viewModel.setDeliveredOrders(mOrderObject);
        //Objects.requireNonNull(viewModel.getDeliveredOrders().getValue()).getOrderObjects().add(mOrderObject);
    }
    void updateToReturned() {
        viewModel.setReturnedOrders(mOrderObject);
        //Objects.requireNonNull(viewModel.getReturnedOrders().getValue()).getOrderObjects().add(mOrderObject);
    }

    void isOpenOrder() {
        mLeftButton.setBackgroundResource(R.drawable.btn_bg_canceled);
        mLeftButton.setText(R.string.cancel);

        mRightButton.setBackgroundResource(R.drawable.btn_bg_accepted);
        mRightButton.setText(R.string.accept);
    }
    void isCanceledOrder() {
        mLeftButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
    }
    void isAcceptedOrder() {
        mRightButton.setBackgroundResource(R.drawable.btn_bg_shipped);
        mRightButton.setText(R.string.shipped);
        mLeftButton.setBackgroundResource(R.drawable.btn_bg_packed);
        mLeftButton.setText(R.string.packed);
    }
    void isPackedOrder() {
        mLeftButton.setBackgroundResource(R.drawable.btn_bg_shipped);
        mLeftButton.setText(R.string.shipped);
        mRightButton.setVisibility(View.GONE);
    }
    void isShippedOrder() {
        mLeftButton.setBackgroundResource(R.drawable.btn_bg_delivered);
        mLeftButton.setText(R.string.delivered);
        mRightButton.setVisibility(View.GONE);
    }
    void isDeliveredOrder() {
        mLeftButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
        deliveryBoyInfo.setVisibility(View.VISIBLE);
        deliveryBoyNumber.setText(mOrderObject.getDeliveryBoyNumber());
        deliveryBoyName.setText(mOrderObject.getDeliveryBoyName());
    }
    void isReturnedOrder() {
        mLeftButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
    }
}