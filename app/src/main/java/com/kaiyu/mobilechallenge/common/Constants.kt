package com.kaiyu.mobilechallenge.common

/**
 * Stores configurations for accessing the database
 */

object Constants {

    /** The base endpoint of the database */
    const val DATABASE_URL_BASE: String = "https://my-json-server.typicode.com/"


    /** How many seconds should the app wait for the server to respond */
    const val CONNECTION_TIMEOUT_SECOND: Long = 10

    const val PARAM_PRODUCT_ID = "product_id"
    const val PARAM_PRODUCT_INFO_INSTANCE = "product_info_instance"
}