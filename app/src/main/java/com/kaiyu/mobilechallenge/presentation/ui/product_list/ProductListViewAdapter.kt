package com.kaiyu.mobilechallenge.presentation.ui.product_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.domain.data_models.ProductList
import com.kaiyu.mobilechallenge.R
import com.kaiyu.mobilechallenge.common.Utils


/**
 * The [RecyclerView.Adapter] for the product list RecyclerView.
 *
 * @param context the current context.
 * @param productList a [ProductList] instance containing all the products that will be shown on
 * the screen.
 * @param eventListener a [ProductListEventListener] instance used to pass events to the context.
 */

class ProductListViewAdapter(
    private val context: Context,
    private val productList: ProductList,
    val eventListener: ProductListEventListener? = null
) : RecyclerView.Adapter<ProductListViewAdapter.ViewHolder>() {

    /** The ViewHolder: Grabs views from list item layout and hold them */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Grabs all views from the layout and hold them
        val imageViewProductImage: ImageView = itemView.findViewById(R.id.product_list_item_image)
        val textViewTitle: TextView = itemView.findViewById(R.id.product_list_item_title)
        val textViewPrice: TextView = itemView.findViewById(R.id.product_list_item_price)
        val textViewSize: TextView = itemView.findViewById(R.id.product_list_item_size)

        // Set an on click listener for each itemView
        init {
            eventListener?.let {
                itemView.setOnClickListener {
                    // Call the eventListener to handle the onClick event
                    eventListener.handleProductListOnClickEvent(adapterPosition)
                }
            }
        }
    }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.component_product_list_item, parent, false)
        return ViewHolder(view)
    }


    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     *
     * This method uses the third-party library [Glide] to load pictures.
     * @see Utils.loadImage
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Set the layout height of items to be "Wrap content"
        holder.itemView.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT

        // Update the text content of all TextViews
        holder.textViewTitle.text = productList[position].title
        holder.textViewPrice.text = context.getString(R.string.currency_symbol,
            productList[position].price)
        holder.textViewSize.text = productList[position].size

        // Load the picture of product
        val imgURL = productList[position].imgUrl
        Utils.loadImage(context, imgURL, holder.imageViewProductImage)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return productList.size
    }

}
