package com.kaiyu.mobilechallenge.data.dto.product_list

import com.kaiyu.mobilechallenge.data.dto.product_list.Item

data class Cluster(
    val items: List<Item>,
    val tag: String
)