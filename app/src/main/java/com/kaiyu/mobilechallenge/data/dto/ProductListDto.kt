package com.kaiyu.mobilechallenge.data

import com.google.gson.Gson
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import java.io.Serializable

/**
 * A data class of which the hierarchical structure matches the format of the JSON response of
 * [ProductDatabaseQueries.productList] from the database server.
 *
 * It is only used to parse the JSON string with [Gson].
 */

data class ProductListDto(
    val clusters: List<ProductCategory>
) : Serializable {

    /**
     * Convert itself to a List of [ProductInfo] instances.
     */
    fun convertToProductInfoList() : List<ProductInfo> {

        val ret = mutableListOf<ProductInfo>()

        for (category in clusters) {
            val currentTag = category.tag
            for (item in category.items) {
                val product = ProductInfo(
                    id=item.id,
                    price = item.price,
                    tag = currentTag,
                    title = item.title,
                    size = item.size,
                    imgUrl = item.imageUrl,
                    allergyInformation = null,
                    description = null,
                )
                ret.add(product)
            }
        }

        return ret
    }
}

data class ProductCategory(
    val tag: String,
    val items: List<ItemInfo>
) : Serializable

data class ItemInfo(
    val id: String,
    val price: String,
    val title: String,
    val size: String,
    val imageUrl: String
) : Serializable