package com.HRGames.HorseBasketball.courtside

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
internal fun WebView.configureCourtsidePage() {
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(this@configureCourtsidePage, true)
    }

    isFocusable = true
    isFocusableInTouchMode = true

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        loadWithOverviewMode = false
        useWideViewPort = true
        builtInZoomControls = false
        displayZoomControls = false
        setSupportZoom(false)
        allowFileAccess = true
        allowContentAccess = true
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        javaScriptCanOpenWindowsAutomatically = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        mediaPlaybackRequiresUserGesture = false
        loadsImagesAutomatically = true
        blockNetworkImage = false
        setSupportMultipleWindows(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            safeBrowsingEnabled = false
        }
        userAgentString = userAgentString.replace("; wv", "").replace("Version/4.0", "")
    }
}

internal class CourtsideNavigation(
    private val pageFinished: () -> Unit,
    private val rendererStopped: () -> Unit
) : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        pageFinished()
        CookieManager.getInstance().flush()
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        CookieManager.getInstance().flush()
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        val scheme = uri.scheme ?: return false

        if (scheme in listOf("http", "https")) {
            return false
        }

        return try {
            val externalIntent = if (scheme == "intent") {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }

            view?.context?.startActivity(externalIntent)
            true
        } catch (_: Exception) {
            true
        }
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        rendererStopped()
        return true
    }
}
