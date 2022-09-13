package com.kaiyu.mobilechallenge.presentation.view_models

import com.kaiyu.mobilechallenge.domain.data_models.ProductList

data class ProductListState(
    val isLoading: Boolean = false,
    val productList: ProductList? = null,
    val errorMessage: String? = null
)
