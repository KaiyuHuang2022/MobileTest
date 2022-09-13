package com.kaiyu.mobilechallenge.data.remote

import com.kaiyu.mobilechallenge.data.dto.product_details.ProductDetailDto
import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.data.dto.product_list.ProductListDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This is a Retrofit service interface containing the base URL and queries for getting JSON
 * response.
 */

interface TypicodeAPI {

    /** The query to get product list */
    @GET("ocadotechnology/mobile-challenge/products")
    suspend fun getProductList() : ProductListDto

    /** The query to get detailed information of a single product */
    @GET("ocadotechnology/mobile-challenge/product")
    suspend fun getProductDetails(@Query("id") id: ProductID) : ProductDetailDto

}