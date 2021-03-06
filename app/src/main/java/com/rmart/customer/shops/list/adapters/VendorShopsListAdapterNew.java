package com.rmart.customer.shops.list.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.rmart.BR;
import com.rmart.R;
import com.rmart.baseclass.CallBackInterface;
import com.rmart.baseclass.Constants;
import com.rmart.customer.models.ContentModel;
import com.rmart.customer.shops.list.models.CustomerProductsShopDetailsModel;
import com.rmart.databinding.VederShopItemsBinding;
import com.rmart.utilits.Permisions;
import com.rmart.utilits.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class VendorShopsListAdapterNew extends RecyclerView.Adapter<VendorShopsListAdapterNew.MyViewHolder> implements Filterable {

    private List<CustomerProductsShopDetailsModel> productList;
    private List<CustomerProductsShopDetailsModel> filteredListData;
    private final CallBackInterface callBackListener;
    Activity context;
    private VendorShopsListAdapterNew.MyFilter myFilter;

    public VendorShopsListAdapterNew(Activity context, List<CustomerProductsShopDetailsModel> productList, CallBackInterface callBackListener) {
        this.productList = productList;
        this.context = context;
        filteredListData = new ArrayList<>();
        this.filteredListData.addAll(productList);
        this.callBackListener = callBackListener;
    }

    public void updateItems(List<CustomerProductsShopDetailsModel> listData) {
        this.productList = listData;
        this.filteredListData.clear();
        this.filteredListData.addAll(productList);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VederShopItemsBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.veder_shop_items, parent, false);

        MyViewHolder vh = new MyViewHolder(binding); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final CustomerProductsShopDetailsModel dataModel = filteredListData.get(position);
        holder.binding.getRoot().setOnClickListener(view -> {
            callBackListener.callBackReceived(dataModel);
        });
        holder.binding.ivCallField.setOnClickListener(view -> {
            Utils.openDialPad(view.getContext(),dataModel.getShopMobileNo());
        });
        holder.binding.ivMessageField.setOnClickListener(view -> {
            Utils.openGmailWindow(view.getContext(),dataModel.getEmailId());
        });
        holder.binding.ivFavouriteImage.setOnClickListener(view -> {
            ContentModel contentModel = new ContentModel();
            contentModel.setStatus(Constants.TAG_SHOP_FAVOURITE);
            contentModel.setValue(dataModel);
            callBackListener.callBackReceived(contentModel);
        });
        holder.binding.ivShareField.setOnClickListener(view -> {
            if (Permisions.checkWriteExternlStoragePermission((Activity) holder.binding.getRoot().getContext())) {
                Bitmap bitmap = null;
                try {
                    holder.binding.imageview.setDrawingCacheEnabled(true);
                    bitmap = Bitmap.createBitmap(holder.binding.imageview.getDrawingCache());
                    holder.binding.imageview.setDrawingCacheEnabled(false);

                } catch (Exception e){
                    Toast.makeText(     holder.binding.imageview.getContext(),"pont1",Toast.LENGTH_LONG).show();
                }
                final String appPackageName = context.getPackageName();
                String message= "रोकड मार्ट आता आपल्या शहरामध्ये!!!\n" +
                        "आता "+dataModel.getShopName()+"शॉप रोकड मार्ट सोबत ऑनलाईन झाले आहे. \n" +
                        "नवीन ऑफर्स आणि शॉपिंग साठी खालील लिंक वर क्लिक करा आणि अँप डाउनलोड करा.\n" ;
                Utils.shareImage(bitmap, "shop.png",context ,message+"https://play.google.com/store/apps/details?id=" + appPackageName);
            } else {
                Permisions.requestWriteExternlStoragePermission(holder.binding.getRoot().getContext());
            }
        });
        holder.bind(dataModel);

    }

    @Override
    public int getItemCount() {
        if(filteredListData!=null) {
            return filteredListData.size();
        } else {
            return  0;
        }
    }
    @Override
    public Filter getFilter() {
        if (myFilter == null) {
            myFilter = new VendorShopsListAdapterNew.MyFilter();
        }
        return myFilter;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

         VederShopItemsBinding binding;
        public MyViewHolder(VederShopItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
        public void bind(Object obj) {
            binding.setVariable(BR.shopdata, obj);
           binding.executePendingBindings();
        }
    }

    private class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                filteredListData.clear();
                for (CustomerProductsShopDetailsModel customerProductsModel : productList) {
                    if (customerProductsModel.getShopName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredListData.add(customerProductsModel);
                    }
                }
            }
            results.count = filteredListData.size();
            results.values = filteredListData;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredListData = (List<CustomerProductsShopDetailsModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
