package com.rmart.retiler.inventory.brand.viewmodel;

import com.rmart.retiler.inventory.brand.activities.BrandFilterActivity;
import com.rmart.retiler.inventory.brand.adapters.BrandListAdapter;
import com.rmart.retiler.inventory.brand.model.BrandListResponse;
import com.rmart.retiler.inventory.brand.repositories.BrandListRepository;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class BrandViewModel extends ViewModel {

   public MutableLiveData<BrandListResponse> brandListResponseMutableLiveData = new MutableLiveData<>();
    public void getBransList(String id)
    {
        MutableLiveData<BrandListResponse> resultMutableLiveData= BrandListRepository.getBransList("0","5000",id);
        resultMutableLiveData.observeForever(new Observer<BrandListResponse>() {
            @Override
            public void onChanged(BrandListResponse brandListResponse) {
                brandListResponseMutableLiveData.setValue(brandListResponse);
            }
        });
    }
}
