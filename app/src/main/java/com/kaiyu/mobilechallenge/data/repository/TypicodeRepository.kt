package com.kaiyu.mobilechallenge.data.repository

import android.util.Log
import com.google.gson.Gson
import com.kaiyu.mobilechallenge.common.Configs
import com.kaiyu.mobilechallenge.data.ProductListDto
import com.kaiyu.mobilechallenge.data.dto.ProductDetailsDto
import com.kaiyu.mobilechallenge.data.remote.CustomisedDNS
import com.kaiyu.mobilechallenge.data.remote.TypicodeAPI
import com.kaiyu.mobilechallenge.domain.repository.Repository
import com.kaiyu.mobilechallenge.domain.repository.RepositoryCallback
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import retrofit2.*
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.Exception
import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit


/**
 * An implementation of [Repository] that:
 *
 * (1) provides query interfaces to get product list and product details from the database;
 *
 * (2) provides JSON parsing feature for handling responses from server;
 *
 * (3) uses the third-party library [Retrofit] to access database;
 *
 * @see Repository
 */

class TypicodeRepository : Repository {


    companion object {

        /** This interceptor is mainly used for debugging and logging purpose, which records details
         * of all HTTP request and response from the server. */
        private val loggingInterceptor = HttpLoggingInterceptor {
            Log.i("DatabaseAccessor HTTP", it)
        }.setLevel(HttpLoggingInterceptor.Level.BODY)


        /** The okHttpClient that manages configurations of HTTP requests, and record all relevant
         * events for debugging and logging purposes. */
        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Configs.CONNECTION_TIMEOUT_SECOND, TimeUnit.SECONDS)
            .eventListenerFactory(LoggingEventListener.Factory())
            .dns(CustomisedDNS())
            .build()

        /** The Retrofit client initialised with:
         *
         * (1) the URL of the base endpoint of the database;
         *
         * (2) an configured [OkHttpClient] managing HTTP requests;
         *
         * (3) a [ScalarsConverterFactory] to convert the response body to String.
         */
        private val retrofitClient: Retrofit = Retrofit.Builder()
            .baseUrl(Configs.DATABASE_URL_BASE)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        /** The query API for accessing the server */
        private val queryAPI = retrofitClient.create(TypicodeAPI::class.java)

    }

    /**
     * Download data from the database server, the data content from the server will be parsed into
     * corresponding response data classes.
     *
     * The current version supports two kinds of queries,
     * [TypicodeAPI.productList] and [TypicodeAPI.productDetails].
     * The corresponding [responseDataClass] of them are [ProductListDto] and
     * [ProductDetailsDto] respectively.
     *
     * About extra parameters of queries:
     *
     * (1) No extra parameter is required by the query [TypicodeAPI.productList].
     *
     * (2) Only one extra parameter "product id" is required by the query
     * [TypicodeAPI.productDetails], which should be put on the first place of params.
     *
     * @param queryParams extra parameters required by queries.
     *
     * @param responseDataClass a data class of which the hierarchical structure matches
     * the JSON response from the server, it is used to parse the JSON using the third-party library
     * [Gson]. The method will choose the proper queryAPI according to the type of this parameter.
     *
     * @param repositoryCallback A [RepositoryCallback] handles responses from the database server.
     *
     * @param customisedParser if the response from the database needs a special method to parse,
     * please provide a customised parser which takes the response string as argument, and return
     * an instance of responseDataClass.
     *
     * @throws InvalidParameterException when passing in an unsupported query type or missing extra
     * parameter for a query that requires it.
     */
    override fun<T> download(queryParams: List<String>?,
                             responseDataClass: Class<T>,
                             repositoryCallback: RepositoryCallback<T>,
                             customisedParser: ((String?) -> T?)?) {

        // A Retrofit Callback object defines operations that will be performed after accessing the
        // database server.
        val retrofitCallback = object : Callback<String> {

            // This function will be called when receive HTTP response. BUT the response
            // may indicates an application level failure such as 404 and 500.
            override fun onResponse(call: Call<String>, response: Response<String>) {

                // Check the isSuccessful flag to make sure that the database server has returned
                // the required data. This flag is true if the response code from the database
                // server is 200-OK.
                if (response.isSuccessful) {

                    val content = response.body()
                    // If the server did return some data
                    if(content != null){
                        // Parse the data into corresponding responseDataClass
                        val parsedResponse = if (customisedParser == null) {
                            // Use the default parser
                            getInstanceFromJSONString(content, responseDataClass)
                        } else {
                            // Use the customised parser
                            customisedParser(content)
                        }

                        // If successfully parsed the data, call the OnDataReady method
                        // of the databaseCallback interface.
                        if(parsedResponse != null) {
                            repositoryCallback.onDataReady(parsedResponse)
                        // Otherwise, call the OnDataParseError method instead
                        } else {
                            repositoryCallback.onDataParseError()
                        }
                    // If the server returned 200-OK but does not return any data, call
                    // the onDataParseError method of databaseCallback interface.
                    } else {
                        repositoryCallback.onDataParseError()
                    }
                // If the server returned an application level failure such as 404 and 500,
                // call the onConnectionError method of the databaseCallback interface.
                } else {
                    repositoryCallback.onConnectionError(response.message())
                }

            }

            // This function will be called when a network exception occurred talking to the
            // server or when an unexpected exception occurred creating the request or processing
            // the response.
            override fun onFailure(call: Call<String>, t: Throwable) {
                repositoryCallback.onConnectionError(t.message ?: "Unknown Error")
            }
        }

        // Use different queryAPI for different queries
        when(responseDataClass) {

            // Download the product list
            ProductListDto::class.java -> {
                queryAPI.productList().enqueue(retrofitCallback)
            }

            // Download the details of a single product.
            // An product ID must be provided, otherwise an InvalidParameterException will
            // be thrown.
            ProductDetailsDto::class.java -> {
                if (queryParams!= null && queryParams.isNotEmpty()) {
                    queryAPI.productDetails(queryParams[0]).enqueue(retrofitCallback)
                } else {
                    throw InvalidParameterException("Missing product ID")
                }
            }

            // If the caller is passing other response classes
            else -> {
                throw InvalidParameterException("Unsupported query type")
            }
        }

    }

    /**
     * A helper function uses the third-party library [Gson] to parse the JSON string to the
     * corresponding response data class.
     *
     * @param T a data class with the hierarchical structure that matches
     * the JSON string.
     * @param result the JSON string to parse.
     * @param javaClass the Java class of type [T].
     * @return if the JSON string is successfully parsed, return an instance of type [T], otherwise
     * return null.
     */
    private fun <T> getInstanceFromJSONString(result: String, javaClass: Class<T>, ) : T? {
        val parsedResponse: T? = try {
            Gson().fromJson(result, javaClass)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return parsedResponse
    }

}