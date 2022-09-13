package com.kaiyu.mobilechallenge.domain.repository

/**
 * Handles response from the database server. The [Repository] will parse the original response
 * data and call proper methods in this interface according to different situations.
 *
 * @param T The data class of which the hierarchical structure matches the JSON response
 * from the database server.
 */
interface RepositoryCallback<T> {

    /**
     * Invoked for successfully received and parsed the response from database server.
     */
    fun onDataReady(parsedResponse: T)

    /** Invoke when failed to parse the data. That might because the server returns
     * a JSON string that does not match the hierarchical structure of [T], or the server returns
     * responses of application-level failures, such as 404 or 500.*/
    fun onDataParseError()

    /** Invoke when the connection to the database server is failed. This might because of the
     * connection timeout, or uncaught exceptions */
    fun onConnectionError(responseMessage: String)

}