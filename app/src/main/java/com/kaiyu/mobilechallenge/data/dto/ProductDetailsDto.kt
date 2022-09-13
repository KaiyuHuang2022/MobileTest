package com.kaiyu.mobilechallenge.data.dto

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import java.io.Serializable

/**
 * A data class of which the hierarchical structure matches the format of the JSON response of
 * [ProductDatabaseQueries.productDetails] from the database server.
 *
 * It is only used to parse the JSON string with [Gson].
 */
data class ProductDetailsDto(
    val id: String,
    val price: String,
    val title: String,
    val imageUrl: String,
    val description: String,
    val allergyInformation: String
) : Serializable {

    /**
     * Convert itself to a [ProductInfo] instance.
     */
    fun convertToProductInfo() : ProductInfo {

        return ProductInfo(
            id=id,
            price = price,
            tag = null,
            title = title,
            size = null,
            imgUrl = imageUrl,
            allergyInformation = allergyInformation,
            description = description
        )

    }

    companion object {
        /**
         * A customised parser that can properly parse the JSON string, which should be
         * passed as a argument to [ProductDatabaseAccessor.download].
         *
         * @param content the JSON string.
         * @return a nullable instance of this class.
         */
        fun parseFromJsonString(content: String?): ProductDetailsDto? {
            // Get the type token of List<ProductDetailsResponse>
            val typeToken = object : TypeToken<List<ProductDetailsDto>>() {}.type
            // Parse the JSON string by using Gson with the type token
            val parsedContent = Gson().fromJson<List<ProductDetailsDto>>(content, typeToken)
            // Because the JSON response from the database server is a list that contains only one
            // JSON object, so the parsed content is a list with only one ProductInfo instance, so
            // the following code uses parsedContent[0] to access it.
            return if (parsedContent.isNotEmpty()) parsedContent[0] else null
        }
    }

}