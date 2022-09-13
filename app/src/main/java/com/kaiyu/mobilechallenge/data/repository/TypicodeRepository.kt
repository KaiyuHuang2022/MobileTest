package com.kaiyu.mobilechallenge.data.repository


import com.kaiyu.mobilechallenge.data.dto.product_details.ProductDetailDto
import com.kaiyu.mobilechallenge.data.remote.TypicodeAPI
import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.domain.repository.Repository
import com.kaiyu.mobilechallenge.data.dto.product_list.ProductListDto
import retrofit2.*
import javax.inject.Inject

/**
 * An implementation of [Repository] that:
 *
 * (1) provides query interfaces to get product list and product details from the database;
 *
 * (2) provides JSON parsing feature for handling responses from server;
 *
 * (3) uses the third-party library [Retrofit] to access database;
 *
 * @see Repository
 */

class TypicodeRepository @Inject constructor(
    private val api: TypicodeAPI
) : Repository {

    override suspend fun getProductList(): ProductListDto {
        return api.getProductList()
    }

    override suspend fun getProductDetail(id: ProductID): ProductDetailDto {
        return api.getProductDetails(id)
    }

}