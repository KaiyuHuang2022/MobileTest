package com.kaiyu.mobilechallenge.data.dto.product_list

import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.mobilechallenge.domain.data_models.ProductList

data class ProductListDto(
    val clusters: List<Cluster>
)

fun ProductListDto.toProductList() : ProductList {
    val list = mutableListOf<ProductInfo>()
    for (cluster in clusters) {
        for (item in cluster.items) {
            list.add(
                ProductInfo(
                id = item.id,
                price = item.price,
                tag = cluster.tag,
                size = item.size,
                title = item.title,
                imgUrl = item.imageUrl
                )
            )
        }
    }

    val productList = ProductList()
    productList.importFromList(list)

    return productList
}