package com.intelliprove.cdvwebview

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray

import androidx.core.os.bundleOf
import com.intelliprove.webview.IntelliWebViewActivity
import com.intelliprove.webview.IntelliWebViewDelegate

class CDVIntelliProveWebView : CordovaPlugin(), IntelliWebViewDelegate {
    private var webViewCallbackContext: CallbackContext? = null

    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
        return when (action) {
            "presentWebView" -> {
                this.webViewCallbackContext = callbackContext

                // Keep the callback alive
                val pluginResult = PluginResult(PluginResult.Status.NO_RESULT)
                pluginResult.keepCallback = true
                callbackContext.sendPluginResult(pluginResult)

                // Launch your activity
                val urlString = args.optString(0)

                IntelliWebViewActivity.start(cordova.activity, urlString, this@CDVIntelliProveWebView)

                true
            }
            else -> false
        }
    }

    override fun didReceivePostMessage(postMessage: String) {
        webViewCallbackContext?.let {
            val result = PluginResult(PluginResult.Status.OK, postMessage)
            result.keepCallback = true
            it.sendPluginResult(result)
        }
    }
}