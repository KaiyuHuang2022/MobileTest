package com.kaiyu.mobilechallenge.data_models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

typealias ProductID = String


/**
 * A data class storing part or all information of a single product.
 *
 * This class is parcelable, so that it can be transfer between activities by intent.
 */
@Parcelize
data class ProductInfo(

    /** The unique ID of the product */
    val id: String? = null,

    /** The price of the product (in pounds) */
    var price: String? = null,

    /** The overall description of the product */
    var title: String? = null,

    /** The detailed description of the product */
    var description: String? = null,

    /** Indicates to which category the product belongs */
    var tag: String? = null,

    /** The size information of the product */
    var size: String? = null,

    /** The allergy information of the product */
    var allergyInformation: String? = null,

    /** The URL String of the image of the product */
    var imgUrl: String? = null

) : Parcelable
