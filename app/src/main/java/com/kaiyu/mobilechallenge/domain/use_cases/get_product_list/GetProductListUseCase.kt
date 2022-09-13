package com.kaiyu.mobilechallenge.domain.use_cases.get_product_list

import com.kaiyu.mobilechallenge.common.ExceptionMessages
import com.kaiyu.mobilechallenge.common.InternetResult
import com.kaiyu.mobilechallenge.data.dto.product_list.toProductList
import com.kaiyu.mobilechallenge.domain.data_models.ProductList
import com.kaiyu.mobilechallenge.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetProductListUseCase @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(): Flow<InternetResult<ProductList>> = flow {
        try {
            emit(InternetResult.Loading())
            val productList = repository.getProductList().toProductList()
            emit(InternetResult.Success(productList))
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