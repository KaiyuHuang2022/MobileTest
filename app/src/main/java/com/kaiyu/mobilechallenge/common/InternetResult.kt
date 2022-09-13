package com.kaiyu.mobilechallenge.common

sealed class InternetResult<T> (
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : InternetResult<T>(data)
    class Error<T>(message: String) : InternetResult<T>(null, message)
    class Loading<T>() : InternetResult<T>(null)
}