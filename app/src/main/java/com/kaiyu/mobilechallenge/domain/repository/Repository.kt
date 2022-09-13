package com.kaiyu.mobilechallenge.domain.repository

import com.google.gson.Gson

/**
 * The DatabaseAccessor Interface: All modules requiring data from database will use this interface to
 * access the database and get the data.
 */
interface Repository {

    /**
     * Modules can get (download) data from the database by calling this method.
     *
     * Implementations of this interface should parse the original response from the database,
     * and call proper methods of [repositoryCallback] to handle the response data.
     *
     * @param queryParams extra parameters required by a specified query. Please see implements
     * for more information.
     * @param responseDataClass a data class of which the hierarchical structure matches the
     * JSON response from the database server. This is used to parse the JSON using the third-party
     * library [Gson].
     * @param repositoryCallback an instance of [RepositoryCallback] that perform operations on the
     * response of the database.
     * @param customisedParser if the response from the database needs a special method to parse,
     * please provide a customised parser which takes the response string as argument, and return
     * an instance of [responseDataClass].
     */
    fun<T> download(queryParams: List<String>?,
                    responseDataClass: Class<T>,
                    repositoryCallback: RepositoryCallback<T>,
                    customisedParser: ((String?) -> T?)?)
}