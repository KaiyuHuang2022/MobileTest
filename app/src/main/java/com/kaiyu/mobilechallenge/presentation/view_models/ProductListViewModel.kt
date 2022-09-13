package com.kaiyu.mobilechallenge.presentation.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaiyu.mobilechallenge.common.ExceptionMessages
import com.kaiyu.mobilechallenge.common.InternetResult
import com.kaiyu.mobilechallenge.domain.use_cases.get_product_list.GetProductListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductListUseCase: GetProductListUseCase
) : ViewModel()  {

    /** The state of Product List View */
    private val _state = MutableLiveData(ProductListState())

    /** The immutable state instance for accessing from outside the class */
    val state: LiveData<ProductListState> = _state


    init {
        getProductList()
    }

    /** Get the product list from repository and load it to the state */
    fun getProductList() {

        viewModelScope.launch {
            getProductListUseCase().collect { result ->
                when(result) {
                    is InternetResult.Success -> {
                        _state.value = ProductListState(productList = result.data)
                    }
                    is InternetResult.Error -> {
                        _state.value = ProductListState(
                            errorMessage = result.message ?: ExceptionMessages.UNKNOWN_ERROR
                        )
                    }
                    is InternetResult.Loading -> {
                        _state.value = ProductListState(isLoading = true)
                    }
                }
            }
        }
    }

}