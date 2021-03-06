package com.rmart.inventory.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rmart.R;
import com.rmart.baseclass.CallBackInterface;
import com.rmart.baseclass.Constants;
import com.rmart.baseclass.DateTimeInterface;
import com.rmart.customer.models.ContentModel;
import com.rmart.inventory.adapters.ProductImagesAdapter;
import com.rmart.inventory.adapters.ProductUnitAdapter;
import com.rmart.inventory.models.APIUnitMeasures;
import com.rmart.inventory.models.UnitObject;
import com.rmart.profile.model.MyProfile;
import com.rmart.utilits.CustomDatePickerDialog;
import com.rmart.utilits.DateUtilities;
import com.rmart.utilits.LoggerInfo;
import com.rmart.utilits.RecyclerTouchListener;
import com.rmart.utilits.RetrofitClientInstance;
import com.rmart.utilits.Utils;
import com.rmart.utilits.custom_views.SpacesItemDecoration;
import com.rmart.utilits.pojos.APIBrandListResponse;
import com.rmart.utilits.pojos.APIBrandResponse;
import com.rmart.utilits.pojos.APIStockListResponse;
import com.rmart.utilits.pojos.APIUnitMeasureListResponse;
import com.rmart.utilits.pojos.APIUnitMeasureResponse;
import com.rmart.utilits.pojos.AddProductToInventoryResponse;
import com.rmart.utilits.pojos.ImageURLResponse;
import com.rmart.utilits.pojos.ProductResponse;
import com.rmart.utilits.services.APIService;
import com.rmart.utilits.services.VendorInventoryService;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class AddProductToInventory extends BaseInventoryFragment implements View.OnClickListener {

    private static final String ARG_PRODUCT = "product";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM1 = "param1";
    public static final int INT_ADD_UNIT = 101;
    public static final int INT_UPDATE_UNIT = 102;
    public static final String UNIT_VALUE = "unit_value";

    private ProductResponse mClonedProduct;
    private boolean isEdit;

    ArrayList<APIUnitMeasureResponse> unitMeasurements = new ArrayList<>();
    ArrayList<APIBrandResponse> apiBrandResponses = new ArrayList<>();

    public AppCompatTextView chooseCategory, chooseSubCategory, chooseProduct, productBrand;
    // CustomStringAdapter customStringAdapter;
    // Spinner productBrand;
    AppCompatEditText productRegionalName, deliveryDays, productDescription, tvProductVideoLink;
    AppCompatTextView expiry;
    APIService apiService = RetrofitClientInstance.getRetrofitInstance().create(APIService.class);
    private ArrayList<ImageURLResponse> imagesList;
    private int selectedImagePosition = -1;
    private ProductUnitAdapter unitBaseAdapter;
    private final ArrayList<UnitObject> unitsList = new ArrayList<>();
    private Calendar expiryDateCalendar = Calendar.getInstance();
    private ProductImagesAdapter productImagesAdapter;

    public AddProductToInventory() {
        // Required empty public constructor
    }

    public static AddProductToInventory newInstance(ProductResponse product, boolean isEdit, APIStockListResponse stockListResponse) {
        AddProductToInventory fragment = new AddProductToInventory();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        args.putSerializable(ARG_PARAM1, stockListResponse);
        args.putBoolean(ARG_PARAM2, isEdit);
        fragment.setArguments(args);
        return fragment;
    }

    private final CallBackInterface callBackListener = pObject -> {
        if (pObject instanceof ContentModel) {
            ContentModel contentModel = (ContentModel) pObject;
            String status = contentModel.getStatus();
            Object value = contentModel.getValue();
            if (status.equalsIgnoreCase(Constants.TAG_UPLOAD_NEW_PRODUCT_IMAGE)) {
                //selectedPosition = (int) value;
                photoUploadSelected();
            } else if (status.equalsIgnoreCase(Constants.TAG_EDIT_PRODUCT_IMAGE)) {
                //selectedPosition = (int) value;
                photoUploadSelected();
            } else if (status.equalsIgnoreCase(Constants.TAG_EDIT_UNIT)) {
                UnitObject unitObject = (UnitObject) value;
                mListener.addUnit(unitObject, new APIUnitMeasures(unitMeasurements), this, INT_UPDATE_UNIT);
                // deleteUnits((UnitObject) value);
            }/* else if (status.equalsIgnoreCase(Constants.TAG_EDIT_UNIT)) {
                mListener.addUnit((UnitObject) value,new APIUnitMeasures(unitMeasurements), this, INT_UPDATE_UNIT);
            }*/
        }
    };

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        LoggerInfo.printLog("Fragment", "AddProductToInventory");

        getUnitMeasuresFromAPI();
        //getBrandFromAPI();
        return inflater.inflate(R.layout.fragment_edit_product, container, false);
    }

    private void getUnitMeasuresFromAPI() {
        progressDialog.show();
        apiService.getAPIUnitMeasureList().enqueue(new Callback<APIUnitMeasureListResponse>() {
            @Override
            public void onResponse(@NotNull Call<APIUnitMeasureListResponse> call, @NotNull Response<APIUnitMeasureListResponse> response) {
                if (response.isSuccessful()) {
                    APIUnitMeasureListResponse data = response.body();
                    for (APIUnitMeasureResponse apiUnitMeasureResponse:
                         data.getArrayList()) {
                        apiUnitMeasureResponse.setAttributesName(apiUnitMeasureResponse.getAttributesName().replace(".",""));
                    }
                    if (data != null) {
                        unitMeasurements = data.getArrayList();
                    } else {
                        showDialog(getString(R.string.no_information_available));
                    }
                } else {
                    showDialog("", response.message());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<APIUnitMeasureListResponse> call, @NotNull Throwable t) {
                if(t instanceof SocketTimeoutException){
                    showDialog("", getString(R.string.network_slow));
                } else {
                    showDialog("", t.getMessage());
                }
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ProductResponse mProduct = (ProductResponse) getArguments().getSerializable(ARG_PRODUCT);
            //stockListResponse = (APIStockListResponse) getArguments().getSerializable(ARG_PARAM1);

            if (null == mProduct) {
                mClonedProduct = new ProductResponse();
                //mProduct = new ProductResponse();
            } else {
                try {
                    mClonedProduct = new ProductResponse(mProduct);
                    for (ImageURLResponse imageURLResponse: mClonedProduct.getImageDataObject()) {
                        if (null != imageURLResponse.getDisplayImage() && imageURLResponse.getDisplayImage().length()>10)
                        imageURLResponse.setImageURL(imageURLResponse.getDisplayImage());
                    }
                } catch (Exception e) {
                    showDialog("", e.getMessage());
                }
            }
            isEdit = getArguments().getBoolean(ARG_PARAM2);
        }
    }

    private void updateUI() {
        //
        if (null != mClonedProduct) {
            chooseSubCategory.setText(mClonedProduct.getSubCategory());
            chooseCategory.setText(mClonedProduct.getCategory());
            chooseProduct.setText(mClonedProduct.getProductName());
            // productBrand.setText(mClonedProduct.getBrand());
            productRegionalName.setText(mClonedProduct.getRegionalName());
            if (isEdit) {
                productDescription.setText(mClonedProduct.getDescription());
            }
            String expiryDate = mClonedProduct.getExpiry_date();
            if (!TextUtils.isEmpty(expiryDate)) {
                if (expiryDate.equalsIgnoreCase("1970-01-01") || expiryDate.equalsIgnoreCase("01-01-1970")) {
                    expiry.setText("");
                } else {
                    expiryDateCalendar = DateUtilities.getCalendarFromString(mClonedProduct.getExpiry_date());
                    expiry.setText(DateUtilities.getDateStringFromCalendar(expiryDateCalendar));
                }
            }

            deliveryDays.setText(mClonedProduct.getDelivery_days());
            productBrand.setText(mClonedProduct.getBrandName());
            tvProductVideoLink.setText(mClonedProduct.getVideoLInk());
            updateList();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chooseCategory = view.findViewById(R.id.choose_category);
        chooseSubCategory = view.findViewById(R.id.choose_sub_category);
        chooseProduct = view.findViewById(R.id.choose_product);
        productBrand = view.findViewById(R.id.product_brand);

        RecyclerView productImagesListField = view.findViewById(R.id.product_images_list_field);
        productImagesListField.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        productImagesListField.setHasFixedSize(false);
        productImagesListField.setNestedScrollingEnabled(false);
        productImagesListField.setItemAnimator(new DefaultItemAnimator());
        productImagesListField.addItemDecoration(new SpacesItemDecoration(10));

        productRegionalName = view.findViewById(R.id.product_regional_name);

        productDescription = view.findViewById(R.id.product_description);
        expiry = view.findViewById(R.id.expiry);
        expiry.setOnClickListener(this);
        deliveryDays = view.findViewById(R.id.delivery_days);
        RecyclerView unitsRecyclerView = view.findViewById(R.id.unit_base);
        tvProductVideoLink = view.findViewById(R.id.product_video_link);

        unitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // addUnit = view.findViewById(R.id.add_unit);
        AppCompatButton save = view.findViewById(R.id.save);
        if (isEdit) {
            save.setText(getString(R.string.update));
        } else {
            save.setText(getString(R.string.save));
        }
        save.setOnClickListener(this);
        view.findViewById(R.id.add_unit).setOnClickListener(this);

        imagesList = new ArrayList<>();
        if (mClonedProduct != null) {
            List<ImageURLResponse> clonedImagesList = mClonedProduct.getImageDataObject();
            for(ImageURLResponse clonedImageDetails : clonedImagesList) {
                clonedImageDetails.setIsImageUpdated(0); // image url from server
                imagesList.add(clonedImageDetails);
            }

            if (imagesList.size() != 5) {
                ImageURLResponse imageURLResponse = new ImageURLResponse();
                imageURLResponse.setImageURL("");
                imageURLResponse.setId(-1);
                imagesList.add(imageURLResponse);
            }
        }

        productImagesAdapter = new ProductImagesAdapter(requireActivity(), imagesList);
        productImagesListField.setAdapter(productImagesAdapter);

        productImagesListField.addOnItemTouchListener(new RecyclerTouchListener(requireActivity(), "", productImagesListField, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                selectedImagePosition = position;
                photoUploadSelected();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        unitsList.clear();
        for (UnitObject unitObject : mClonedProduct.getUnitObjects()) {
            unitObject.setProductUpdated(true);
            unitsList.add(unitObject);
        }

        unitBaseAdapter = new ProductUnitAdapter(unitsList, callBackListener, true);
        unitsRecyclerView.setAdapter(unitBaseAdapter);

        updateUI();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.add_unit:
                UnitObject newObject = new UnitObject();
                newObject.setProductUnitID("");
                newObject.setProductUpdated(false);
                mListener.addUnit(newObject, new APIUnitMeasures(unitMeasurements), this, INT_ADD_UNIT);
                break;
            case R.id.save:
                saveSelected();
                break;
            case R.id.expiry:
                expiryDateSelected();
                break;
            default:
                break;
        }
    }

    private void expiryDateSelected() {
        CustomDatePickerDialog customDatePickerDialog = CustomDatePickerDialog.getInstance(Constants.CALENDAR_MIN_TYPE,
                expiryDateCalendar.get(Calendar.YEAR), expiryDateCalendar.get(Calendar.MONTH), expiryDateCalendar.get(Calendar.DAY_OF_MONTH));
        customDatePickerDialog.setCallBack(new DateTimeInterface() {
            @Override
            public void onDateSet(int year, int month, int dayOfMonth) {
                expiryDateCalendar.set(Calendar.YEAR, year);
                expiryDateCalendar.set(Calendar.MONTH, month);
                expiryDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                expiry.setText(DateUtilities.getDateStringFromCalendar(expiryDateCalendar));
            }

            @Override
            public void onTimeSet(int hour, int minutes, String amOrPm) {

            }
        });
        if(!requireActivity().isFinishing()) {
            customDatePickerDialog.show(requireActivity().getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
        }
    }

    private void saveSelected() {
        if (TextUtils.isEmpty(Objects.requireNonNull(productRegionalName.getText()).toString().trim())) {
            showDialog("", getString(R.string.error_regional_name));
            return;
        }
        if (TextUtils.isEmpty(Objects.requireNonNull(productDescription.getText()).toString().trim())) {
            showDialog("", getString(R.string.error_product_description));
            return;
        }
        mClonedProduct.setUnitObjects(unitsList);
        if (Objects.requireNonNull(mClonedProduct).getUnitObjects().size() < 1) {
            showDialog("", getString(R.string.unit_required));
            return;
        }
        /*if (TextUtils.isEmpty(expiry.getText().toString())) {
            showDialog("", getString(R.string.error_expiry_date));
            return;
        }*/
        /*if (TextUtils.isEmpty(Objects.requireNonNull(tvProductVideoLink.getText()).toString().trim())) {
            showDialog("", getString(R.string.error_video_link));
            return;
        }*/
        if(!Utils.isNetworkConnected(requireActivity())) {
            showDialog(getString(R.string.error_internet), getString(R.string.error_internet_text));
            return;
        }
        progressDialog.show();
        mClonedProduct.setExpiry_date(expiry.getText().toString());
        mClonedProduct.setRegionalName(productRegionalName.getText().toString());
        //mClonedProduct.setDelivery_days(Objects.requireNonNull(MyProfile.getInstance().getDeliveryInDays()));
        mClonedProduct.setDescription(productDescription.getText().toString());
        ArrayList<ImageURLResponse> updateImagesList = new ArrayList<>();
        setImageURL(updateImagesList);
        mClonedProduct.setImageDataObject(updateImagesList);
        if(updateImagesList.size()>0) {
            for (ImageURLResponse imageURLResponse : updateImagesList) {
                imageURLResponse.setImageShow("0");
            }
            updateImagesList.get(0).setImageShow("1");
        } else {
            progressDialog.dismiss();
            showDialog("", getString(R.string.error_image_thumb));
            return;
        }

        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement = gson.toJsonTree(mClonedProduct);
        JsonObject jsonObject = (JsonObject) jsonElement;
        jsonObject.addProperty("stock_id", "5");
        jsonObject.addProperty("quantity", "2");
        jsonObject.addProperty("brand", 2);
        jsonObject.addProperty("expiry_date", mClonedProduct.getExpiry_date());
        jsonObject.addProperty("delivery_days", MyProfile.getInstance().getDeliveryInDays());
        jsonObject.addProperty("client_id", "2");
        jsonObject.addProperty("user_id", MyProfile.getInstance().getUserID());
        jsonObject.addProperty("product_video_link", Objects.requireNonNull(tvProductVideoLink.getText()).toString().trim());

        VendorInventoryService vendorInventoryService = RetrofitClientInstance.getRetrofitInstance().create(VendorInventoryService.class);
        if (isEdit) {
            vendorInventoryService.editProductToInventory(jsonObject).enqueue(new Callback<AddProductToInventoryResponse>() {
                @Override
                public void onResponse(@NotNull Call<AddProductToInventoryResponse> call, @NotNull Response<AddProductToInventoryResponse> response) {
                    if (response.isSuccessful()) {
                        AddProductToInventoryResponse data = response.body();
                        if (data != null) {
                            if (data.getStatus().equalsIgnoreCase(Utils.SUCCESS)) {
                                showDialog(String.format("%s %s", mClonedProduct.getProductName(), getString(R.string.updated_success_product)),
                                        pObject -> requireActivity().getSupportFragmentManager().popBackStack());
                            } else {
                                showDialog(data.getMsg());
                            }
                        } else {
                            showDialog(getString(R.string.no_information_available));
                        }
                    } else {
                        showDialog(response.message());
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NotNull Call<AddProductToInventoryResponse> call, @NotNull Throwable t) {
                    if(t instanceof SocketTimeoutException){
                        showDialog("", getString(R.string.network_slow));
                    } else {
                        showDialog("", t.getMessage());
                    }
                    progressDialog.dismiss();
                }
            });
        } else {
            vendorInventoryService.addProductToInventory(jsonObject).enqueue(new Callback<AddProductToInventoryResponse>() {
                @Override
                public void onResponse(@NotNull Call<AddProductToInventoryResponse> call, @NotNull Response<AddProductToInventoryResponse> response) {
                    if (response.isSuccessful()) {
                        AddProductToInventoryResponse data = response.body();
                        if (data != null) {
                            if (data.getStatus().equalsIgnoreCase(Utils.SUCCESS)) {
                                showDialog(String.format("%s %s", mClonedProduct.getProductName(), getString(R.string.add_success_product)),
                                        pObject -> requireActivity().getSupportFragmentManager().popBackStack());
                            } else {
                                showDialog(data.getMsg());
                            }
                        } else {
                            showDialog(getString(R.string.no_information_available));
                        }
                    } else {
                        showDialog(response.message());
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NotNull Call<AddProductToInventoryResponse> call, @NotNull Throwable t) {
                    if(t instanceof SocketTimeoutException){
                        showDialog("", getString(R.string.network_slow));
                    } else {
                        showDialog("", t.getMessage());
                    }
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void setImageURL(ArrayList<ImageURLResponse> lUpdateImagesList) {
        for (ImageURLResponse imageURLResponse : imagesList) {
            Bitmap bitmap = null;
            try {
                String imageUri = imageURLResponse.getImageUri();
                if (imageUri != null) {
                    Uri uri = Uri.fromFile(new File(imageUri));
                    if (uri != null) {
                        InputStream imageStream = requireActivity().getContentResolver().openInputStream(uri);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 900, 900, true);

                        imageURLResponse.setImageRawData(getEncodedImage(bitmap));
                        lUpdateImagesList.add(imageURLResponse);
                    }
                } else if((null != imageURLResponse.getDisplayImage() && imageURLResponse.getDisplayImage().length() >10) ||
                        (null != imageURLResponse.getImageURL() && imageURLResponse.getImageURL().length() >10) ) {
                    lUpdateImagesList.add(imageURLResponse);
                }
            } catch (Exception ex) {
                LoggerInfo.printLog("image error", "exception " + ex.getMessage());
            } finally {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
    }

    private void updateList() {
        unitBaseAdapter.updateItems(unitsList);
        unitBaseAdapter.notifyDataSetChanged();
    }

    private void photoUploadSelected() {
        CropImage.activity()
                .start(requireActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                try {
                    Uri profileImageUri = result.getUri();
                    if (profileImageUri != null) {
                        updateImage(profileImageUri);
                    }
                } catch (Exception ex) {
                    showDialog("", ex.getMessage());
                }
            }
        } else if (requestCode == INT_ADD_UNIT) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    UnitObject unitData = (UnitObject) bundle.getSerializable(UNIT_VALUE);
                    unitsList.add(unitData);
                    updateList();
                }
            }
        } else if (requestCode == INT_UPDATE_UNIT) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                   if (bundle.getBoolean("IS_DELETED", false)) {
                       UnitObject unitData = (UnitObject) bundle.getSerializable(UNIT_VALUE);
                       if (unitData != null) {
                           unitsList.remove(unitData.getPosition());
                           unitBaseAdapter.updateItems(unitsList);
                           unitBaseAdapter.notifyDataSetChanged();
                       }
                       /*if (unitData != null) {
                           if (unitData.getTimeStamp() != -1) {
                               for (int i = 0; i < unitsList.size(); i++) {
                                   if (unitsList.get(i).getTimeStamp() == unitData.getTimeStamp()) {
                                       unitsList.remove(i);
                                       unitBaseAdapter.updateItems(unitsList);
                                       unitBaseAdapter.notifyDataSetChanged();
                                       break;
                                   }
                               }
                           } else {
                               for (int i = 0; i < unitsList.size(); i++) {
                                   if (unitsList.get(i).getProductUnitID().equalsIgnoreCase(unitData.getProductUnitID())) {
                                       unitsList.remove(i);
                                       unitBaseAdapter.updateItems(unitsList);
                                       unitBaseAdapter.notifyDataSetChanged();
                                       break;
                                   }
                               }
                           }
                       }*/
                   } else {
                       UnitObject unitData = (UnitObject) bundle.getSerializable(UNIT_VALUE);
                       if (unitData != null) {
                           unitsList.set(unitData.getPosition(), unitData);
                           unitBaseAdapter.updateItems(unitsList);
                           unitBaseAdapter.notifyDataSetChanged();
                           /*if (unitData.getTimeStamp() != -1) {
                               for (int i = 0; i < unitsList.size(); i++) {
                                   if (unitsList.get(i).getTimeStamp() == unitData.getTimeStamp()) {
                                       unitsList.set(i, unitData);
                                       unitBaseAdapter.updateItems(unitsList);
                                       unitBaseAdapter.notifyDataSetChanged();
                                       break;
                                   }
                               }
                           } else {
                               for (int i = 0; i < unitsList.size(); i++) {
                                   if (unitsList.get(i).getProductUnitID().equalsIgnoreCase(unitData.getProductUnitID())) {
                                       unitsList.set(i, unitData);
                                       unitBaseAdapter.updateItems(unitsList);
                                       unitBaseAdapter.notifyDataSetChanged();
                                       break;
                                   }
                               }
                           }*/
                       }
                   }
                    // unitsList.add(unitData);
                    updateList();
                }
            }
        }
    }

    private void updateImage(Uri imageUri) {
        if (selectedImagePosition < imagesList.size() - 1) {
            ImageURLResponse imageUrlResponse = imagesList.get(selectedImagePosition);
            imageUrlResponse.setImageUri(imageUri.getPath());
            imagesList.set(selectedImagePosition, imageUrlResponse);
            productImagesAdapter.notifyItemChanged(selectedImagePosition);
        } else {
            ImageURLResponse imageUrlResponse = new ImageURLResponse();
            imageUrlResponse.setImageUri(imageUri.getPath());
            imageUrlResponse.setId(-1);
            int size = imagesList.size();
            imagesList.add(selectedImagePosition, imageUrlResponse);
            productImagesAdapter.notifyItemInserted(selectedImagePosition);
            int updatedSize = imagesList.size();
            if (updatedSize == 6) {
                imagesList.remove(size);
                productImagesAdapter.notifyItemRemoved(size);
            }
        }
    }

    private String getEncodedImage(Bitmap bitmap) {
        try {
            if (bitmap != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] b = byteArrayOutputStream.toByteArray();
                return Base64.encodeToString(b, Base64.DEFAULT);
            }
        } catch (Exception ex) {
            LoggerInfo.errorLog("encode image exception", ex.getMessage());
        }
        return "";
    }

    private void getBrandFromAPI() {
        progressDialog.show();
        apiService.getAPIBrandList().enqueue(new Callback<APIBrandListResponse>() {
            @Override
            public void onResponse(@NotNull Call<APIBrandListResponse> call, @NotNull Response<APIBrandListResponse> response) {
                if (response.isSuccessful()) {
                    APIBrandListResponse data = response.body();
                    if (data != null) {
                        apiBrandResponses = data.getArrayList();
                        for (APIBrandResponse apiBrandResponse : apiBrandResponses) {
                            //availableBrands.add(apiBrandResponse.getBrandName());
                        }
                    } else {
                        showDialog(getString(R.string.no_information_available));
                    }
                } else {
                    showDialog("", response.message());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<APIBrandListResponse> call, @NotNull Throwable t) {
                if(t instanceof SocketTimeoutException){
                    showDialog("", getString(R.string.network_slow));
                } else {
                    showDialog("", t.getMessage());
                }
                progressDialog.dismiss();
            }
        });
    }

    public void handleBackButton() {
        if (isEdit)
            showConfirmationDialog(getString(R.string.product_discard_alert), pObject -> requireActivity().getSupportFragmentManager().popBackStack());
        else requireActivity().getSupportFragmentManager().popBackStack();
    }
}