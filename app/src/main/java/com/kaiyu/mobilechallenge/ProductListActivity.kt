package com.kaiyu.mobilechallenge

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.fragments.ConnectionFailedFragment
import com.kaiyu.mobilechallenge.fragments.ProductListFragment
import com.kaiyu.mobilechallenge.database_accessor.DatabaseAccessor
import com.kaiyu.mobilechallenge.database_accessor.DatabaseCallback
import com.kaiyu.mobilechallenge.fragments.FragmentCallback
import com.kaiyu.mobilechallenge.product_database_accessor.ProductDatabaseAccessor
import com.kaiyu.mobilechallenge.product_database_accessor.ProductListResponse


/**
 * The startup activity of the app, shows a list of available products.
 *
 * When this activity is being created, it will try to connect to the database server to
 * fetch data of the product list:
 *
 * (1) If successfully downloaded the data, it will transfer to a [ProductListFragment] to show
 * the product list on the screen;
 *
 * (2) Otherwise it will transfer to a [ConnectionFailedFragment] to show relevant error messages
 * and wait for further instructions from the user.
 *
 * This activity implements the interface [FragmentCallback] to receive extra data from fragments.
 *
 * This activity uses the third-party library [Glide] to load image, with the LRU memory cache
 * feature enabled.
 */
class ProductListActivity : AppCompatActivity(), FragmentCallback {

    /** A [DatabaseAccessor] instance for getting product list data from the database server */
    private val databaseAccessor: DatabaseAccessor = ProductDatabaseAccessor()

    /** A progress dialog indicating that the app is downloading the data */
    private var downloadingProgressDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialise the downloading progress dialog
        downloadingProgressDialog = Dialog(this)
        downloadingProgressDialog?.setContentView(R.layout.dialog_downloading_progress)

        // Start to get product list from the database server
        fetchProductList()
    }


    /**
     * Download the list of products from database.
     *
     * If the network is available it will send a request via [databaseAccessor],
     * otherwise it will transfer to [ConnectionFailedFragment]
     * and wait for further instructions from the user.
     */
    private fun fetchProductList() {

        // If the network is available, connect to the database to download the product list
        if(Utils.isNetworkAvailable(this)) {

            // Show the downloading progress dialog
            downloadingProgressDialog?.show()

            // Create an anonymous DatabaseCallback instance to handle the response from the
            // database server
            val databaseCallback = object : DatabaseCallback<ProductListResponse> {

                // Invoked for successfully received and parsed the response from database server.
                override fun onDataReady(parsedResponse: ProductListResponse) {
                    downloadingProgressDialog?.cancel()
                    // Load the product list fragment when data ready
                    val productList = parsedResponse.convertToProductInfoList()
                    val productListFragment = ProductListFragment.newInstance(productList)
                    transferToFragment(productListFragment)
                }

                // Invoke when failed to parse the data. That might because the server returns
                // a JSON string that does not match the hierarchy structure of T, or the server
                // returns responses of application-level failures, such as 404 or 500.
                override fun onDataParseError() {
                    downloadingProgressDialog?.cancel()
                    // Prompt that the data parsing was failed.
                    val connectionFailedFragment = ConnectionFailedFragment.newInstance(
                        getString(R.string.data_failed_parse_error_title),
                        getString(R.string.data_failed_parse_error_details),
                        this@ProductListActivity
                    )
                    transferToFragment(connectionFailedFragment)
                }

                // Invoke when the connection to the database server is failed. This might because
                // of the connection timeout, or uncaught exceptions.
                override fun onConnectionError(responseMessage: String) {
                    downloadingProgressDialog?.cancel()
                    // Prompt that the connection to the server was failed
                    val connectionFailedFragment = ConnectionFailedFragment.newInstance(
                        getString(R.string.connection_failed_general_title),
                        responseMessage,
                        this@ProductListActivity
                    )
                    transferToFragment(connectionFailedFragment)
                }
            }

            // Start to download the product list data
            databaseAccessor.download(
                queryParams = null,
                responseDataClass = ProductListResponse::class.java,
                databaseCallback = databaseCallback,
                customisedParser = null
            )

        // Otherwise, transfer to the ConnectionFailedFragment and wait for user's instruction
        } else {
            // Create a ConnectionFailedFragment instance with messages indicating there's
            // no Internet access.
            val connectionFailedFragment = ConnectionFailedFragment.newInstance(
                getString(R.string.connection_failed_no_internet_title),
                getString(R.string.connection_failed_no_internet_details),
                this
            )
            // Transfer to the ConnectionFailedFragment
            transferToFragment(connectionFailedFragment)
        }
    }


    /**
     * Transfer to a target fragment, and show the content of the fragment on the fragment
     * layout [R.id.fragmentLayout_homepage].
     *
     * @param fragment the target fragment instance.
     */
    private fun transferToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentLayout_homepage, fragment)
            commit()
        }
    }

    /**
     * The implementation of function [onCallbackFromFragment] in the interface [FragmentCallback].
     *
     *
     * @param bundle contains extra data returned from the fragment.
     */
    override fun onCallbackFromFragment(bundle: Bundle?) {
        val fragment = bundle?.getString(FragmentCallback.BundleContents.FragmentClassName.name)
        fragment?.let {
            if (it == ConnectionFailedFragment::class.java.name) {
                fetchProductList()
            }
        }
    }
}