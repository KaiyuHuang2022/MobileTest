package com.kaiyu.mobilechallenge.product_database_accessor

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This is a Retrofit service interface containing the base URL and queries for getting JSON
 * response.
 */

interface ProductDatabaseQueries {

    /** The query to get product list */
    @GET("ocadotechnology/mobile-challenge/products")
    fun productList() : Call<String>

    /** The query to get detailed information of a single product */
    @GET("ocadotechnology/mobile-challenge/product")
    fun productDetails(@Query("id") id: String) : Call<String>
}