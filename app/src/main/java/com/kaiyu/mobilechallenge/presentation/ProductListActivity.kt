package com.kaiyu.mobilechallenge.presentation

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.R
import com.kaiyu.mobilechallenge.presentation.ui.connection_failed.ConnectionFailedFragment
import com.kaiyu.mobilechallenge.presentation.fragments.FragmentCallback
import com.kaiyu.mobilechallenge.presentation.view_models.ProductListViewModel
import com.kaiyu.mobilechallenge.presentation.ui.product_list.ProductListFragment
import dagger.hilt.android.AndroidEntryPoint


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
@AndroidEntryPoint
class ProductListActivity : AppCompatActivity(), FragmentCallback {

    private val viewModel: ProductListViewModel by viewModels()

    /** A progress dialog indicating that the app is downloading the data */
    private var downloadingProgressDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialise the downloading progress dialog
        downloadingProgressDialog = Dialog(this)
        downloadingProgressDialog?.setContentView(R.layout.dialog_downloading_progress)

    }

    override fun onResume() {
        super.onResume()

        viewModel.state.observe(this) { state ->

            if (state.isLoading) {
                downloadingProgressDialog?.show()
            } else {
                downloadingProgressDialog?.cancel()

                if (state.productList != null) {
                    val productListFragment = ProductListFragment.newInstance(state.productList)
                    transferToFragment(productListFragment)
                } else {
                    val connectionFailedFragment = ConnectionFailedFragment.newInstance(
                        getString(R.string.connection_failed_general_title),
                        state.errorMessage ?: getString(R.string.connection_failed_general_details),
                        this
                    )
                    transferToFragment(connectionFailedFragment)
                }
            }
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
                viewModel.getProductList()
            }
        }
    }
}