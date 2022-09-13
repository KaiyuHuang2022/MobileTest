package com.kaiyu.mobilechallenge.data.dto.product_details

import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo

class ProductDetailDto : ArrayList<ProductDetailDtoItem>()

fun ProductDetailDto.toProductInfo() : ProductInfo {
    val theOnlyItem = this[0]
    return ProductInfo(
        allergyInformation = theOnlyItem.allergyInformation,
        description = theOnlyItem.description,
        id = theOnlyItem.id,
        imgUrl = theOnlyItem.imageUrl,
        price = theOnlyItem.price,
        title = theOnlyItem.title
    )
}