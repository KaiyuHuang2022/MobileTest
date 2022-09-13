package com.kaiyu.mobilechallenge.data.remote

import android.text.TextUtils
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import retrofit2.*

/**
 * A customised [okhttp3.Dns] trying to return a list of IP addresses where IPv4 addresses are
 * placed on the front, so they will be top choices of the [okhttp3] client, because using IPv6
 * addresses to access "https://my-json-server.typicode.com/" sometimes causes timeout.
 *
 * The [ProductDatabaseAccessor] uses a third-party library [Retrofit] to access the database, which
 * is based on [okhttp3].
 * @see ProductDatabaseAccessor
 */
class CustomisedDNS : Dns {
    /**
     * Returns the IP addresses of `hostname`, in the order they will be attempted by OkHttp. If a
     * connection to an address fails, OkHttp will retry the connection with the next address until
     * either a connection is made, the set of IP addresses is exhausted, or a limit is exceeded.
     */
    override fun lookup(hostname: String): List<InetAddress> {
        if (TextUtils.isEmpty(hostname)) {
            return Dns.SYSTEM.lookup(hostname)
        } else {
            return try {
                val ret = mutableListOf<InetAddress>()
                // Get all IP address by the host name
                val inetAddressList = InetAddress.getAllByName(hostname)
                // Place IPv4 addresses at the front of list, so they can be top choices
                // of the OKHttp client
                for (address in inetAddressList) {
                    if (address is Inet4Address) {
                        ret.add(0, address)
                    } else {
                        ret.add(address)
                    }
                }
                ret
            } catch (e: NullPointerException) {
                Dns.SYSTEM.lookup(hostname)
            }
        }
    }
}