package com.rmart.customer.shops.products.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.rmart.R;
import com.rmart.customer.shops.home.adapters.CategoryAdapter;
import com.rmart.customer.shops.home.adapters.ProductsAdapter;
import com.rmart.customer.shops.home.listner.OnClickListner;
import com.rmart.customer.shops.home.model.Category;
import com.rmart.customer.shops.home.model.ProductData;
import com.rmart.customer.shops.home.viewmodel.ShopHomeViewModel;
import com.rmart.customer.shops.list.models.CustomerProductsShopDetailsModel;
import com.rmart.customer.shops.products.model.ProductsResponce;
import com.rmart.customer.shops.products.viewmodel.ProductListViewModel;
import com.rmart.customer.views.ProductCartDetailsFragment;
import com.rmart.databinding.FragmentProductListBinding;
import com.rmart.databinding.FragmentShopHomePageBinding;
import com.rmart.utilits.GridSpacesItemDecoration;

import java.util.ArrayList;


public class ProductListFragment extends Fragment  implements OnClickListner {

    private CustomerProductsShopDetailsModel productsShopDetailsModel;
    private Category category;
    private int PAGE_SIZE=20;
    private static final String ARG_SHOP = "shop_details";
    private static final String CATEGOERY = "categoery";
    ProductsAdapter productsAdapter;
    CategoryAdapter categoryAdapter;
    String searchPrase,sub_category_id;
    int start_page=0;
    int total_product_count;
    public AppCompatButton btn_tryagain;
    ProductListViewModel productListViewModel;

    public ProductListFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ProductListFragment newInstance(CustomerProductsShopDetailsModel customerProductsShopDetailsModel, Category category) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, customerProductsShopDetailsModel);
        args.putSerializable(CATEGOERY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productsShopDetailsModel = (CustomerProductsShopDetailsModel) getArguments().getSerializable(ARG_SHOP);
            category = (Category) getArguments().getSerializable(CATEGOERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        FragmentProductListBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_list, container, false);
         productListViewModel = ViewModelProviders.of(this).get(ProductListViewModel.class);
        binding.setProductsViewModel(productListViewModel);
        binding.setShopDetails(productsShopDetailsModel);
        binding.setLifecycleOwner(this);
        GridLayoutManager layoutManager = (GridLayoutManager) binding.productsListField.getLayoutManager();
        binding.productsListField.addItemDecoration(new GridSpacesItemDecoration(20));
        productListViewModel.loadProductList(productsShopDetailsModel,category.parentCategoryId,searchPrase,start_page+"",sub_category_id);
        productsAdapter = new  ProductsAdapter(getActivity(),new ArrayList<>(),this,false);
        binding.setProductsAdapter(productsAdapter);

        productListViewModel.shopHomePageResponceMutableLiveData.observeForever(productsResponce -> {
            if( productsResponce.getStatus()==200) {
                if (categoryAdapter == null) {
                    productsResponce.results.category.add(0, new Category("All"));
                    categoryAdapter = new CategoryAdapter(getActivity(), productsResponce.results.category, this);
                    binding.setCategoryAdapter(categoryAdapter);
                }
                productsAdapter.addProducts(productsResponce.results.productData);
                total_product_count = productsResponce.results.total_product_count;
            }
        });
        GridLayoutManager.SpanSizeLookup onSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (productsAdapter.getItemViewType(position) == productsAdapter.VIEW_LOADING  ? 2 : 1);
            }
        };
        layoutManager.setSpanSizeLookup(onSpanSizeLookup);
        productListViewModel.isLoading.observeForever(aBoolean -> {
            productsAdapter.setLoading(aBoolean);
        });
        binding.productsListField.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!productListViewModel.isLoading.getValue()) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= productsAdapter.productData.size() && total_product_count>productsAdapter.productData.size()) {
                            productListViewModel.loadProductList(productsShopDetailsModel,category.parentCategoryId,searchPrase,(productsAdapter.productData.size())+"",sub_category_id);

                    }
                }
            }
        });
        binding.simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText!=null && newText.length()>0) {

                    searchPrase=newText;

                } else {

                }
                sub_category_id=null;
                productsAdapter.productData.clear();
                productsAdapter.notifyDataSetChanged();
                productListViewModel.loadProductList(productsShopDetailsModel,category.parentCategoryId,searchPrase,"0",sub_category_id);

                return false;
            }
        });

        btn_tryagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                productListViewModel.loadProductList(productsShopDetailsModel,category.parentCategoryId,searchPrase,start_page+"",sub_category_id);

            }
        });

      return   binding.getRoot();
    }

    @Override
    public void onCategorySelected(Category category2) {
        productsAdapter.productData.clear();
        productsAdapter.notifyDataSetChanged();
        sub_category_id=category2.subcategory_id;
        productListViewModel.loadProductList(productsShopDetailsModel,category.parentCategoryId,searchPrase,"0",sub_category_id);
    }

    @Override
    public void onProductSelected(ProductData productData) {

        ProductCartDetailsFragment baseFragment=  ProductCartDetailsFragment.getInstance2(productData,productsShopDetailsModel );
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.base_container, baseFragment, ProductCartDetailsFragment.class.getName());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }
}