package com.kaiyu.mobilechallenge.presentation.view_models

import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo

data class ProductDetailState(
    var isLoading: Boolean = false,
    var product: ProductInfo? = null,
    var error: String? = null,
)
