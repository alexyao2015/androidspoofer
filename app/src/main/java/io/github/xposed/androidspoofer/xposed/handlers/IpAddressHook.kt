package io.github.xposed.androidspoofer.xposed.handlers

import android.net.wifi.WifiInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.Enumeration

/**
 * Unified hook handler for IP Address methods
 */
object IpAddressHook : XposedHook {
    override fun hook(lpparam: LoadPackageParam, newValue: String) {
        hookWifiIpAddress(lpparam, newValue)
        hookNetworkInterfaceAddresses(lpparam, newValue)
        hookInetAddressHostAddress(lpparam, newValue)
    }

    /**
     * Hooks WifiInfo.getIpAddress to replace the WiFi IP address with a custom value
     * @param lpparam LoadPackageParam
     * @param newIpAddress IP address as String (e.g., "192.168.1.100")
     */
    private fun hookWifiIpAddress(lpparam: LoadPackageParam, newIpAddress: String) {
        try {
            // Convert IP string to int format used by WifiInfo using InetAddress and ByteBuffer
            val ipAddress = InetAddress.getByName(newIpAddress)
            val ipBytes = ipAddress.address
            // WifiInfo uses little-endian format
            val ipInt = ByteBuffer.wrap(ipBytes).order(ByteOrder.LITTLE_ENDIAN).int

            XposedHelpers.findAndHookMethod(
                WifiInfo::class.java,
                "getIpAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val original = param.result as Int
                            // Convert int back to InetAddress for logging using ByteBuffer
                            val originalBytes = ByteBuffer.allocate(4)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .putInt(original)
                                .array()
                            val originalIp = InetAddress.getByAddress(originalBytes).hostAddress

                            param.result = ipInt

                            util.log(
                                TAG,
                                "${lpparam.packageName}: Changed WiFi IP from $originalIp -> $newIpAddress"
                            )
                        } catch (e: Exception) {
                            util.log(TAG, "Error in WifiInfo.getIpAddress hook: ${e.message}")
                        }
                    }
                }
            )
            util.log(TAG, "Successfully hooked WifiInfo.getIpAddress for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook WifiInfo.getIpAddress: ${t.message}")
        }
    }

    /**
     * Hooks NetworkInterface.getInetAddresses to replace network interface IP addresses
     * @param lpparam LoadPackageParam
     * @param newIpAddress IP address as String (e.g., "192.168.1.100")
     */
    private fun hookNetworkInterfaceAddresses(lpparam: LoadPackageParam, newIpAddress: String) {
        try {
            XposedHelpers.findAndHookMethod(
                NetworkInterface::class.java,
                "getInetAddresses",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val original = param.result as Enumeration<InetAddress>

                            // Create a fake InetAddress with the spoofed IP
                            val spoofedAddress = InetAddress.getByName(newIpAddress)

                            // Only spoof IPv4 addresses
                            if (spoofedAddress is Inet4Address) {
                                // Replace the enumeration with our spoofed address
                                param.result = Collections.enumeration(listOf(spoofedAddress))

                                util.log(
                                    TAG,
                                    "${lpparam.packageName}: Replaced NetworkInterface addresses with $newIpAddress"
                                )
                            }
                        } catch (e: Exception) {
                            util.log(
                                TAG,
                                "Error in NetworkInterface.getInetAddresses hook: ${e.message}"
                            )
                        }
                    }
                }
            )
            util.log(
                TAG,
                "Successfully hooked NetworkInterface.getInetAddresses for ${lpparam.packageName}"
            )
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook NetworkInterface.getInetAddresses: ${t.message}")
        }
    }

    /**
     * Hooks InetAddress.getHostAddress to replace the host address string
     * @param lpparam LoadPackageParam
     * @param newIpAddress IP address as String (e.g., "192.168.1.100")
     */
    private fun hookInetAddressHostAddress(lpparam: LoadPackageParam, newIpAddress: String) {
        try {
            XposedHelpers.findAndHookMethod(
                InetAddress::class.java,
                "getHostAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val original = param.result as? String

                            // Only replace IPv4 addresses (check if original is IPv4)
                            if (original != null && !original.contains(":") && !original.startsWith(
                                    "127."
                                )
                            ) {
                                param.result = newIpAddress

                                util.log(
                                    TAG,
                                    "${lpparam.packageName}: Changed InetAddress.getHostAddress from $original -> $newIpAddress"
                                )
                            }
                        } catch (e: Exception) {
                            util.log(TAG, "Error in InetAddress.getHostAddress hook: ${e.message}")
                        }
                    }
                }
            )
            util.log(
                TAG,
                "Successfully hooked InetAddress.getHostAddress for ${lpparam.packageName}"
            )
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook InetAddress.getHostAddress: ${t.message}")
        }
    }
}

