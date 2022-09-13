package com.kaiyu.takehometest.presentation.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaiyu.mobilechallenge.common.ExceptionMessages
import com.kaiyu.mobilechallenge.common.InternetResult
import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.mobilechallenge.domain.use_cases.get_product_details.GetProductDetailUseCase
import com.kaiyu.mobilechallenge.presentation.view_models.ProductDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailUseCase: GetProductDetailUseCase
) : ViewModel() {

    /** The state of Product List View */
    private val _state = MutableLiveData(ProductDetailState())

    /** The immutable state instance for accessing from outside the class */
    val state: LiveData<ProductDetailState> = _state

    fun getProductDetail(id: ProductID) {

        viewModelScope.launch {
            getProductDetailUseCase(id).collect { result ->
                when (result) {
                    is InternetResult.Success -> {
                        result.data?.let {
                            _state.value = ProductDetailState(product = it)
                        }
                    }
                    is InternetResult.Error -> {
                        _state.value = ProductDetailState(
                            error = result.message ?: ExceptionMessages.UNKNOWN_ERROR
                        )
                    }
                    is InternetResult.Loading -> {
                        _state.value = ProductDetailState(isLoading = true)
                    }
                }
            }
        }
    }


}