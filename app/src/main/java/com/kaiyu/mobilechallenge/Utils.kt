package com.kaiyu.mobilechallenge

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kaiyu.mobilechallenge.data_models.ProductInfo

object Utils {


    /**
     * Load an image from remote server into an ImageView.
     *
     * @param context the current context, must be an [Activity].
     * @param imageURL the URL of the image. If it is null or an invalid URL, the default image
     * [R.drawable.ic_product_no_image] will be loaded instead.
     * @param imageView the target [ImageView] into which the image will be loaded.
     */
    fun loadImage(context: Context, imageURL: String?, imageView: ImageView) {
        val activity = context as Activity
        if (!activity.isDestroyed && !activity.isFinishing) {
            Glide.with(activity)
                .load(imageURL)
                .error(R.drawable.ic_product_no_image)
                .into(imageView)
        }
    }

    /**
     * Merge fields of two [ProductInfo] instances.
     *
     * If a field is not null in [newInstance], the corresponding field in [oldInstance] will be
     * overwritten even it is not null.
     *
     * @param oldInstance the older instance, which will be updated and returned.
     * @param newInstance the instance containing new information.
     * @param specifiedFields if it is not null, only fields in it will be updated.
     * @return the updated [oldInstance]
     */
    fun mergeTwoProductInfoInstances(oldInstance: ProductInfo,
                                     newInstance: ProductInfo,
                                     specifiedFields: HashSet<String>? = null
    ) : HashSet<String> {

        val ret = HashSet<String>()

        // Return the oldInstance as it is if trying to merge two instances with different id
        if (oldInstance.id != newInstance.id) {
            return ret
        }

        for (field in ProductInfo::class.java.declaredFields) {
            // The field "id" is a val which cannot be changed
            if (field.name == "id") {
                continue
            }
            // If the specifiedFields is NULL, update all fields that are non-NULL in the
            // newInstance, otherwise, update the specified fields only (a field will not be
            // updated if it is NULL in the newInstance).
            if (specifiedFields == null || specifiedFields.contains(field.name)) {
                field.isAccessible = true
                // If a field of newInstance is not NULL, it will be assigned to the oldInstance,
                // this will overwrite the data in oldInstance if of which the field is not NULL.
                val newData = field.get(newInstance)
                newData.let {
                    field.set(oldInstance, newData)
                    ret.add(field.name)
                }
            }
        }

        return ret

    }

    /**
     * Check the network state.
     * @param context The current context.
     * @return true if the network is available for the current moment, otherwise returns false.
     */
    fun isNetworkAvailable(context: Context) : Boolean {

        var ret = false

        // Get the Connectivity Manager
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        // If the version of API is greater than 23 (M)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            // If the activeNetwork is null, then there will be no network,
            // otherwise check network capabilities.
            if (activeNetwork != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                // If any one of Ethernet, WiFi and Cellular connections is available,
                // then the network is available.
                if(networkCapabilities != null) {
                    ret = when{
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                                -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                -> true
                        else -> false
                    }
                }
            }
        // If the version of API is lower than 23 (M)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            ret = (networkInfo != null) && networkInfo.isConnectedOrConnecting
        }

        return ret
    }

}