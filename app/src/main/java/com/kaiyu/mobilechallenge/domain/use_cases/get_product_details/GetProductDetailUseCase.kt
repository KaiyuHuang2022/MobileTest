package com.kaiyu.mobilechallenge.domain.use_cases.get_product_details


import com.kaiyu.mobilechallenge.common.ExceptionMessages
import com.kaiyu.mobilechallenge.common.InternetResult
import com.kaiyu.mobilechallenge.data.dto.product_details.toProductInfo
import com.kaiyu.mobilechallenge.domain.data_models.ProductID
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.mobilechallenge.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

import javax.inject.Inject

class GetProductDetailUseCase @Inject constructor(
    private val productRepository: Repository
) {

    operator fun invoke(id: ProductID): Flow<InternetResult<ProductInfo>> = flow {
        try {
            emit(InternetResult.Loading())
            val productInfo = productRepository.getProductDetail(id).toProductInfo()
            emit(InternetResult.Success(productInfo))
        } catch (e: HttpException) {
            emit(InternetResult.Error(
                e.localizedMessage ?: ExceptionMessages.UNKNOWN_HTTP_EXCEPTION)
            )
        } catch (e: IOException) {
            emit(InternetResult.Error(
                e.localizedMessage ?: ExceptionMessages.UNKNOWN_IO_EXCEPTION)
            )
        }
    }

}