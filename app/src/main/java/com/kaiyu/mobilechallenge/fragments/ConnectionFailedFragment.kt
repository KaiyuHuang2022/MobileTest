package com.kaiyu.mobilechallenge.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kaiyu.mobilechallenge.R
import kotlinx.android.synthetic.main.fragment_connection_failed.*


/**
 * This fragment shows error messages about the connection, and a "Retry" button.
 * @param title the title of the connection error.
 * @param details the details of the connection error.
 * @param fragmentCallback will be called when the user hit the "Retry" button.
 */
class ConnectionFailedFragment() : Fragment() {

    private var title: String? = null
    private var details: String? = null
    private var fragmentCallback: FragmentCallback? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_failed, container, false)
    }

    /**
     * Initialise views on this fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView_connectionFailedTitle.text = title
        textView_connectionDetails.text = details

        // Set the onOnClickListener for the "Retry" button
        button_retry.setOnClickListener {
            buttonRetryOnClickListener()
        }

    }


    /**
     * When user hit the "Retry" button, this fragment will call back to the parent activity,
     * with an indication of where the callback comes from.
     */
    private fun buttonRetryOnClickListener() {
        val bundle = Bundle()
        bundle.putString(
            FragmentCallback.BundleContents.FragmentClassName.name,
            ConnectionFailedFragment::class.java.name)
        fragmentCallback?.onCallbackFromFragment(bundle)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param title Parameter 1.
         * @param details Parameter 2.
         * @return A new instance of fragment ConnectionFailedFragment.
         */
        @JvmStatic
        fun newInstance(title: String, details: String, fragmentCallback: FragmentCallback) =
            ConnectionFailedFragment().apply {
                this.title = title
                this.details = details
                this.fragmentCallback = fragmentCallback
            }
    }
}