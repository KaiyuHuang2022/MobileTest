package com.kaiyu.mobilechallenge.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.R
import com.kaiyu.mobilechallenge.common.Utils
import com.kaiyu.mobilechallenge.domain.data_models.ProductInfo
import com.kaiyu.mobilechallenge.domain.repository.Repository
import com.kaiyu.mobilechallenge.domain.repository.RepositoryCallback
import com.kaiyu.mobilechallenge.data.repository.TypicodeRepository
import com.kaiyu.mobilechallenge.data.dto.ProductDetailsDto
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
class ProductDetailsActivity : AppCompatActivity() {


    /** A [ProductInfo] instance stores all or part of data of the current product */
    private var productInfo: ProductInfo? = null

    /**
     * Records the product data not provided by the intent but are required to show complete
     * details of a product.
     */
    private var missingFieldsOfProduct: HashSet<String> = HashSet()

    /**
     * Records the product data not provided by the intent but downloaded by this activity.
     * Names of declared fields in the class [ProductInfo] of the newly downloaded data will be stored in this HashSet.
     */
    private var updatedFieldsOfProduct: HashSet<String> = HashSet()

    /**
     * A [Repository] instance that is used to get data from the database.
     */
    private val repository: Repository = TypicodeRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        // Set the onclick listener for the dismiss button
        button_dismiss.setOnClickListener {
            dismissButtonOnClickListener()
        }

        // Get product information from the intent
        getDataFromIntent()

        // Load the data to corresponding views
        refreshAllViews()

    }


    /** Get all the data provided by the intent */
    private fun getDataFromIntent() {
        productInfo = intent.getParcelableExtra(AcceptableIntentExtras.ProductInfo.name)
    }


    /**
     * Load all information stored in the [productInfo] instance to corresponding views.
     *
     * If there is any missing data, it will call the method [downloadMissingInformation] to download
     * them, and then it will be called again by the [downloadMissingInformation] method to load
     * downloaded information to the view.
     *
     * When adding a new view to the activity, here's the right place to set up it.
     *
     * @param isFinalLoad if this flag is true, the method [downloadMissingInformation] will not be
     *        called to download missing data, and the content of corresponding fields will
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

        // If there is any missing information, and this is not a final load,
        // download the missing information from the database.
        if(!isFinalLoad && missingFieldsOfProduct.isNotEmpty()) {
            downloadMissingInformation()
        }

    }


    /**
     * The helper function that get the required data from [productInfo] and use it to set
     * up the [targetView]. If any required data are missing in the [productInfo], this function
     * will record them in [missingFieldsOfProduct].
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
            // If failed to get the data corresponding to fieldName, record it
            if (fieldData == null) {
                missingFieldsOfProduct.add(fieldName)
            }
        // If the [productInfo] is NULL
        } else {
            missingFieldsOfProduct.add(fieldName)
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


    /**
     * Download the product data from the database. Send a query request with the id of
     * the product.
     *
     * The request will be sent only if the network is available, and [productInfo] and its [ProductInfo.id]
     * field are not null.
     */
    private fun downloadMissingInformation() {
        // If the network and the product id are available, send the request
        if(Utils.isNetworkAvailable(this)) {
            productInfo?.id?.let {

                // Create an anonymous DatabaseCallback instance to handle the response from the
                // database server
                val repositoryCallback = object : RepositoryCallback<ProductDetailsDto> {

                    // Invoked for successfully received and parsed the response from database server.
                    override fun onDataReady(parsedResponse: ProductDetailsDto) {
                        // Convert the parsed result to a ProductInfo instance.
                        val newProductInfo = parsedResponse.convertToProductInfo()

                        // Update the productInfo and refresh all views with the updated information
                        updateProductInfoHelper(newProductInfo)

                        refreshAllViews(true)
                    }

                    // Invoke when failed to parse the data. That might because the server returns
                    // a JSON string that does not match the hierarchy structure of T, or the server
                    // returns responses of application-level failures, such as 404 or 500.
                    override fun onDataParseError() {
                        refreshAllViews(true)
                    }

                    // Invoke when the connection to the database server is failed. This might because
                    // of the connection timeout, or uncaught exceptions.
                    override fun onConnectionError(responseMessage: String) {
                        refreshAllViews(true)
                    }

                }

                // Start to download the product list data, using a customised parser that is
                // defined in ProductDetailsResponse
                repository.download(
                    queryParams = listOf(it),
                    responseDataClass = ProductDetailsDto::class.java,
                    repositoryCallback = repositoryCallback,
                ) { stringResult ->
                    ProductDetailsDto.parseFromJsonString(stringResult)
                }
            }
        // Otherwise
        } else {
            handleNoNetworkCase()
        }
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
     * Update the [productInfo] instance with new data.
     *
     * Because both the "/products" and "/product?id={productID}" queries provide only part of
     * data about the product, which means only part of fields of [ProductInfo] will be
     * filled by a single query.
     *
     * So what this function doing is to combine the newly downloaded [newProductInfo] and
     * the old [productInfo] instance. If a field of [productInfo] has already assigned with a
     * value, it will be replaced with the new value from [newProductInfo].
     *
     * @param newProductInfo the new instance of [ProductInfo] with newly downloaded data.
     */
    private fun updateProductInfoHelper(newProductInfo: ProductInfo) {

        // Combine [newProductInfo] and the old [productInfo]
        productInfo.let {
            updatedFieldsOfProduct = Utils.mergeTwoProductInfoInstances(
                productInfo!!,
                newProductInfo,
                missingFieldsOfProduct
            )
        }

        // If a field was updated, remove it from [missingFieldsOfProduct]
        missingFieldsOfProduct.subtract(updatedFieldsOfProduct)
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