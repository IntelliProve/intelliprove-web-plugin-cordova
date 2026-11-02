package com.intelliprove.cdvwebview

import android.os.Build
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import androidx.annotation.RequiresApi
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray

import androidx.core.os.bundleOf
import com.intelliprove.webview.IntelliWebViewActivity
import com.intelliprove.webview.IntelliWebViewDelegate

class CDVIntelliProveWebView : CordovaPlugin(), IntelliWebViewDelegate {
    private var webViewCallbackContext: CallbackContext? = null
    // From presentWebView until the host screen is back in front: while set, a renderer kill is ours to handle
    @Volatile private var scanOpen = false
    // The host's WebView died with the scan's renderer, so the host screen must be rebuilt
    private var hostWebViewGone = false

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

                scanOpen = true
                IntelliWebViewActivity.start(cordova.activity, urlString, this@CDVIntelliProveWebView)

                true
            }
            else -> false
        }
    }

    // Every WebView in the app shares one renderer, so when Android kills it during a scan the host's
    // page dies too, and unless a plugin answers true here Chromium kills the whole app. Only answer
    // while our scan is open; the host screen is rebuilt once it is back in front (onResume).
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        if (!scanOpen) return false
        hostWebViewGone = true
        return true
    }

    override fun onResume(multitasking: Boolean) {
        super.onResume(multitasking)
        scanOpen = false
        if (hostWebViewGone) {
            hostWebViewGone = false
            // The callback belongs to the dead page; the rebuilt screen loads the host's start page afresh
            webViewCallbackContext = null
            cordova.activity.recreate()
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