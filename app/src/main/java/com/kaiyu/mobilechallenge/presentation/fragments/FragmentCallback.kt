package com.kaiyu.mobilechallenge.presentation.fragments

import android.os.Bundle

/**
 * The interface that is used to receive callback and extra data from a fragment.
 */
interface FragmentCallback {

    /**
     * Handles callback from a fragment.
     * @param bundle contains extra data from a fragment callback.
     */
    fun onCallbackFromFragment(bundle: Bundle?)


    /**
     * All possible types of extra data that can be provided in a fragment callback.
     */
    enum class BundleContents {
        /**
         * The class name of the fragment. This is usually used to get to know from which fragment the
         * callback is made.
         */
        FragmentClassName
    }
}