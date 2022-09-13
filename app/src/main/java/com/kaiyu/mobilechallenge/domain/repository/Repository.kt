package com.kaiyu.mobilechallenge.domain.repository

import com.kaiyu.mobilechallenge.data.dto.product_details.ProductDetailDto
import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.data.dto.product_list.ProductListDto

/**
 * The DatabaseAccessor Interface: All modules requiring data from database will use this interface to
 * access the database and get the data.
 */
interface Repository {

    suspend fun getProductList() : ProductListDto

    suspend fun getProductDetail(id: ProductID) : ProductDetailDto

}