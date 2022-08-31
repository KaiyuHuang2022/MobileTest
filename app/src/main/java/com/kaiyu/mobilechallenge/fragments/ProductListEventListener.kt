package com.kaiyu.mobilechallenge.fragments

import androidx.recyclerview.widget.RecyclerView


/**
 * The interface to listen to events from a product list RecyclerView
 */
interface ProductListEventListener {

    /**
     * Handle the event when user click on an item on the product list.
     *
     * @param position the position of the item on the product list view, which can be obtained
     * by calling the [RecyclerView.ViewHolder.getAdapterPosition] method or access the corresponding
     * field directly.
     */
    fun handleProductListOnClickEvent(position: Int)

}