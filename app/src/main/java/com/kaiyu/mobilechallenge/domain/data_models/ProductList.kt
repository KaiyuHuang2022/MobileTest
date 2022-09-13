package com.kaiyu.mobilechallenge.domain.data_models

import java.lang.Exception
import java.util.*
import kotlin.collections.HashSet


/**
 * A list of [ProductInfo] instances with different [ProductID]. This class can be accessed by
 * index like an array, or by [ProductID] by calling the [getProductByID] method.
 *
 * A Hashtable is used to store all the instances.
 *
 * The current version does not support sorting products.
 */
class ProductList {

    /** A Hashtable that is used to store all [ProductInfo] instances */
    private val products: Hashtable<ProductID, ProductInfo> = Hashtable()


    /** A list storing the id of all products, sorted in some kinds of orders. */
    private val sortedList : MutableList<ProductID> = mutableListOf()


    /** A HashSet that is used to guarantee that all [ProductInfo] instances have different id. */
    private val idSet: HashSet<ProductID> = HashSet()


    /** A calculation property storing how many [ProductInfo] instances are currently stored in
     * this instance. */
    val size: Int get() {
        return sortedList.size
    }



    /**
     * Import [ProductInfo] instances from a List. Instances that already exist, or do not have
     * an id, will not be imported.
     *
     * @param productList a List or any other collections of [ProductInfo] with the index accessing operator .
     */
    fun importFromList(productList: List<ProductInfo>) {
        productList.forEach {
            val id = it.id
            // If the current instance has a valid ID
            id?.let { validID ->
                // Only add instances that have not been added before
                if (!idSet.contains(validID)) {
                    sortedList.add(validID)
                    products[validID] = it
                }
            }
        }
    }


    /** The index-accessing operator.
     *  @param index the index on the sorted product list. Make sure that the index is equal or greater
     *  than zero and less than the size of the product list.
     *  @return a [ProductInfo] instance at position [index] on the [sortedList].
     *
     *  @throws IndexOutOfBoundsException when passing in an invalid index.
     */
    operator fun get(index: Int) : ProductInfo {
        // If the [index] is out of bounds
        if (index < 0 || index > sortedList.size) {
            throw Exception(IndexOutOfBoundsException("ProductList: Index out of bounds."))
        }
        // About the Non-null assertion: The sortedList and the Hashtable products are synchronised,
        // so the value can be guaranteed to be Non-Null.
        return products[sortedList[index]]!!
    }


    /**
     * Get a [ProductInfo] instance by its [ProductID].
     *
     * @param id the [ProductID] of a product.
     * @return if the [id] is valid (the corresponding instance is stored in this list), returns
     * the corresponding [ProductInfo] instance, otherwise returns null.
     */
    fun getProductByID(id: ProductID) : ProductInfo? {
        return products[id]
    }
}