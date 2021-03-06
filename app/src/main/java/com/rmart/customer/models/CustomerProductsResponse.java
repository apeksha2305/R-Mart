package com.rmart.customer.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rmart.customer.shops.list.models.CustomerProductsShopDetailsModel;
import com.rmart.utilits.BaseResponse;

import java.util.List;

/**
 * Created by Satya Seshu on 08/09/20.
 */
public class CustomerProductsResponse extends BaseResponse {

    @SerializedName("data")
    @Expose
    private CustomerProductsList customerShopsList;

    public CustomerProductsList getCustomerShopsList() {
        return customerShopsList;
    }

    public void setCustomerShopsList(CustomerProductsList customerShopsList) {
        this.customerShopsList = customerShopsList;
    }

    public static class CustomerProductsList {
        @SerializedName("shop_list")
        @Expose
        List<CustomerProductsShopDetailsModel> customerShopsList;
        @SerializedName("shop_total_count")
        @Expose
        private Integer shopTotalCount;

        public List<CustomerProductsShopDetailsModel> getCustomerShopsList() {
            return customerShopsList;
        }

        public void setCustomerShopsList(List<CustomerProductsShopDetailsModel> customerShopsList) {
            this.customerShopsList = customerShopsList;
        }

        public Integer getShopTotalCount() {
            return shopTotalCount;
        }

        public void setShopTotalCount(Integer shopTotalCount) {
            this.shopTotalCount = shopTotalCount;
        }
    }
}
