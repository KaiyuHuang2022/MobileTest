package com.kaiyu.mobilechallenge.presentation

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.R
import com.kaiyu.mobilechallenge.common.Utils
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.takehometest.presentation.view_model.ProductDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_product_details.*

/**
 * An activity that shows detailed information of a product, including the image, title, price,
 * tag, size and allergy information.
 *
 * The current version of this activity accepts only one extra data of intent, which is
 * with the String "ProductInfo" and a [ProductInfo] instance. All extra data that is acceptable
 * for this activity are listed in the enum class [AcceptableIntentExtras].
 *
 * When this activity is being created, it will gather required data of the product,
 * in 3 steps:
 *
 * (1) Get data from the intent (if the intent is from [ProductListActivity], there will be some data
 * that were already downloaded by it, such as title, image and price), and load these data to the
 * corresponding views.
 *
 * (2) Check if there is any missing data (In general, intents from the [ProductListActivity]
 *     contains only part of data of the product, which is not enough to show the product
 *     details). If so, go to (3).
 *
 * (3) If the network is available, it will connect to the database server, send a request
 * to get complete data of the product and load them to corresponding views. The request URL would be:
 * "{endpointBase}/product?id={productID}"
 *
 * When user clicks the "Dismiss" button, if there are new data downloaded, these data will be
 * transferred back to the launcher activity. So that if the current page is opened again in the
 * future, these information would not need to be downloaded again.
 *
 * This activity uses the third-party library [Glide] to load image, with the LRU memory cache
 * feature enabled.
 */
@AndroidEntryPoint
class ProductDetailsActivity : AppCompatActivity() {

    private val viewModel: ProductDetailViewModel by viewModels()

    /** A [ProductInfo] instance stores all or part of data of the current product */
    private var productInfo: ProductInfo? = null

    private var downloadingProgressDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        // Set the onclick listener for the dismiss button
        button_dismiss.setOnClickListener {
            dismissButtonOnClickListener()
        }

        // Initialise the downloading progress dialog
        downloadingProgressDialog = Dialog(this)
        downloadingProgressDialog?.setContentView(R.layout.dialog_downloading_progress)

        // Get product information from the intent
        getDataFromIntent()

        // Load the data to corresponding views
        refreshAllViews()

        if (!noMissingData()) {
            productInfo?.let {
                viewModel.getProductDetail(it.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.state.observe(this) { state ->
            if (state.isLoading) {
                downloadingProgressDialog?.show()
            } else {
                downloadingProgressDialog?.cancel()

                if (state.product != null) {
                    productInfo = Utils.mergeTwoProductInfoInstances(productInfo, state.product!!)
                    refreshAllViews(true)
                } else {
                    handleNoNetworkCase()
                }
            }
        }
    }


    /** Get all the data provided by the intent */
    private fun getDataFromIntent() {
        productInfo = intent.getParcelableExtra(AcceptableIntentExtras.ProductInfo.name)
    }


    /**
     * Load all information stored in the [productInfo] instance to corresponding views.
     *
     * When adding a new view to the activity, here's the right place to set up it.
     *
     * @param isFinalLoad if this flag is true, the content of corresponding fields will
     *        be set to "unknown".
     */
    private fun refreshAllViews(isFinalLoad: Boolean = false) {

        // Load product title
        loadInfoToViewHelper(textView_title,
            RequiredFields.Title.fieldName,
            getString(R.string.product_details_title_default),
            isFinalLoad
        )

        // Load the price
        loadInfoToViewHelper(textView_price,
            RequiredFields.Price.fieldName,
            null,
            isFinalLoad
        ) { view, price ->
            if (price != null) {
                (view as TextView).text = getString(R.string.currency_symbol, price)
            } else {
                (view as TextView).text = getString(R.string.product_list_item_price_default)
            }
        }

        // Load the product description
        loadInfoToViewHelper(textView_description,
            RequiredFields.Description.fieldName,
            getString(R.string.product_details_description_default),
            isFinalLoad
        )

        // Load the tag information
        loadInfoToViewHelper(textView_tag,
            RequiredFields.Tag.fieldName,
            getString(R.string.product_details_tag_default),
            isFinalLoad
        )

        // Load the size information
        loadInfoToViewHelper(textView_size,
            RequiredFields.Size.fieldName,
            getString(R.string.product_details_size_default),
            isFinalLoad
        )

        // Load the allergy information
        loadInfoToViewHelper(textView_allergy_info,
            RequiredFields.AllergyInformation.fieldName,
            getString(R.string.product_details_allergy_default),
            isFinalLoad
        )

        // Load the picture of the product
        loadInfoToViewHelper(imageView_product,
            RequiredFields.ImageURL.fieldName,
            null,
            isFinalLoad
        )
    }


    /**
     * The helper function that get the required data from [productInfo] and use it to set
     * up the [targetView].
     *
     * @param targetView the target view to which the data will be loaded. The current version of
     * this function supports [TextView] and [ImageView].
     *
     * @param fieldName the corresponding name of the declared fields in the class [ProductInfo],
     * (such as "title", which is the name of [ProductInfo.title]). All corresponding fields of
     * required information are declared in the enum class [RequiredFields].
     *
     * @param defaultInfo the String that will be loaded to the [targetView] if failed to get data
     * from the instance [productInfo].
     *
     * @param isFinalLoad if this flag is true, this function will set up the text field of
     * [targetView] with [R.string.no_such_information] instead of [defaultInfo].
     *
     * @param customisedLoader if it is not null, the process of setting up the view
     * will be done by it, otherwise will be done by the default loader, which just simply do
     * "view.text = data".
     */
    private fun loadInfoToViewHelper(
        targetView: View,
        fieldName: String,
        defaultInfo: String?,
        isFinalLoad: Boolean = false,
        customisedLoader: ((View, String?) -> Unit)? = null
    ) {
        // The data that will be loaded to the targetView
        var fieldData: String? = null

        // Get information from the productInfo instance
        if (productInfo != null) {
            // Iterate over all declared fields of the class ProductInfo
            for (field in ProductInfo::class.java.declaredFields) {
                // If find a field that matches the fieldName provided
                if (field.name == fieldName) {
                    // Get the data and store it to fieldData
                    field.isAccessible = true
                    field.get(productInfo)?.let {
                        fieldData = it as String
                    }
                }
            }
        }

        // If the customised loader is provided, use it to set up the targetView
        if (customisedLoader != null) {
            customisedLoader(targetView, fieldData)
        // Otherwise, use the default way to set up the targetView
        } else {
            when (targetView) {
                // Setting up the TextView
                is TextView -> {
                    val safeErrorInfo: String = if (isFinalLoad) {
                        getString(R.string.no_such_information)
                    } else {
                        defaultInfo ?: ""
                    }
                    // If the fieldData is not Null, load it to targetView.
                    // Otherwise, load a error information.
                    targetView.text = fieldData ?: safeErrorInfo
                }

                // Setting up the ImageView.
                // The fieldData can be NULL, the Glide will handle it automatically.
                is ImageView -> {
                    Utils.loadImage(this, fieldData, targetView)
                }
            }
        }
    }

    private fun noMissingData() : Boolean {
        var ret = true
        for (field in ProductInfo::class.java.declaredFields) {
            field.isAccessible = true
            if (field.get(productInfo) == null) {
                ret = false
            }
        }
        return ret
    }

    /**
     *  Handles the case when there is no network connection.
     *
     *  In this case, only limited information of the product will be displayed.
     */
    private fun handleNoNetworkCase() {
        Toast.makeText(this, getString(R.string.no_network_prompt), Toast.LENGTH_LONG).show()
        refreshAllViews(true)
    }


    /**
     * When the user click the dismiss button, all newly downloaded data will be put into the
     * result (combined with the old data). The [productInfo] instance will be included to the result with
     * the result string of [Results.NewProductInfo.name].
     */
    private fun dismissButtonOnClickListener() {
        // Set up the result
        val resultIntent = Intent()
        resultIntent.putExtra(Results.NewProductInfo.name, productInfo)
        setResult(Results.NewProductInfo.resultCode, resultIntent)
        // End this activity
        finish()
    }


    /**
     * All extra data of an intent that can be accepted by this activity.
     */
    enum class AcceptableIntentExtras {
        ProductInfo
    }


    /**
     * All results that will be returned from this activity.
     */
    enum class Results(val resultCode: Int) {
        NewProductInfo(0)
    }


    /**
     * All fields of the class [ProductInfo] that are required to display product information on
     * views of this activity.
     *
     * @param fieldName the corresponding name string of fields of [ProductInfo].
     */
    enum class RequiredFields(val fieldName: String){
        Title("title"),
        Price("price"),
        Size("size"),
        Tag("tag"),
        Description("description"),
        AllergyInformation("allergyInformation"),
        ImageURL("imgUrl")
    }

}