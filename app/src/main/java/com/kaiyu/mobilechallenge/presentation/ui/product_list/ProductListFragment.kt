package com.kaiyu.mobilechallenge.presentation.ui.product_list

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.mobilechallenge.domain.data_models.ProductList
import com.kaiyu.mobilechallenge.presentation.ProductDetailsActivity
import com.kaiyu.mobilechallenge.R
import com.kaiyu.mobilechallenge.common.Utils


/**
 * A fragment that uses a [RecyclerView] to show product list. Use the
 * [ProductListFragment.newInstance] factory method to create an instance of this fragment.
 *
 * When user click on an item on the product list, this fragment will navigate the user to
 * the corresponding page showing the details of the product.
 *
 * The ViewAdapter of the product list RecyclerView is an instance of [ProductListViewAdapter].
 *
 * This fragment implements the [ProductListEventListener] interface, to receive events like
 * user's operations from the product list RecyclerView.
 */
class ProductListFragment() : Fragment(), ProductListEventListener {

    /** A [ProductList] instance that stores data of all products */
    private var allProducts: ProductList = ProductList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the RecyclerView to show the product list
        val productListView = getView()?.findViewById<RecyclerView>(R.id.product_list_view)

        // Create a ViewAdapter and use it to set up the productListView
        val productListViewAdapter = ProductListViewAdapter(requireContext(), allProducts, this)
        productListView?.adapter = productListViewAdapter
        productListView?.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * Defines operations to be performed when user click on an item on the product list.
     *
     * When user click on an item on the product list, an instance of [ProductDetailsActivity] will
     * be created and launched, with extra data of an instance of [ProductInfo] of the corresponding
     * product.
     */
    override fun handleProductListOnClickEvent(position: Int) {
        // Create an intent launching a ProductDetailsActivity
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        // Put the corresponding ProductInfo instance as extra data of the intent
        intent.putExtra(
            ProductDetailsActivity.AcceptableIntentExtras.ProductInfo.name,
            allProducts[position]
        )
        // Launch the activity with the intent (for result)
        resultLauncher.launch(intent)
    }


    /** An ActivityResultLauncher that launches the [ProductDetailsActivity] and get an
     * updated [ProductInfo] instance from it.
     *
     * Normally, because the [ProductDetailsActivity] will download more information of the product,
     * this launcher will update the corresponding [ProductInfo] instance on the [allProducts] using the
     * instance returned from the [ProductDetailsActivity] which contains more information.
     * So that when the user click on this product again in the future, there is no need to download
     * these information again.
     *
     * No updating operation will be performed if the returned ProductInfo instance :
     *
     * (1) is null;
     *
     * (2) without an id (the id of which is null);
     *
     * (3) has an id that is different from the old instance. See the document
     * of Utils.mergeTwoProductInfoInstances();
     *
     * (4) is valid but the old ProductIndo instance has been removed from the product list.
     *
     * @see Utils.mergeTwoProductInfoInstances
     */
    private val resultLauncher = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()) { result ->
        // Get results with corresponding result codes
        if(result.resultCode == ProductDetailsActivity.Results.NewProductInfo.resultCode) {
            // Get the updated ProductInfo instance from result by using the pre-defined string in
            // the ProductDetailsActivity.Results.NewProductInfo
            val newProductInfo = result.data?.getParcelableExtra<ProductInfo>(
                ProductDetailsActivity.Results.NewProductInfo.name
            )

            // If the instance returned from the ProductDetailsActivity is not null and
            // has a valid id
            newProductInfo?.id?.let { newID ->
                // Get the instance from the product list
                val oldProductInfo = allProducts.getProductByID(newID)
                // If the old ProductInfo instance is still on the product list
                oldProductInfo?.let {
                    // Update the data of the old ProductInfo instance.
                    Utils.mergeTwoProductInfoInstances(oldProductInfo, newProductInfo)
                }
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param productList The list of [ProductInfo] instances.
         *
         * @return A new instance of fragment ProductList.
         */
        @JvmStatic
        fun newInstance(productList: ProductList) =
            ProductListFragment().apply {
                allProducts = productList
            }


    }
}