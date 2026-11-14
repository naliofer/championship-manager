package com.campeonato.duplas.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.toArgb
import com.campeonato.duplas.R
import com.campeonato.duplas.ui.components.TournamentTopBar
import android.webkit.WebChromeClient
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/** Local HTTPS origin keeps ES modules and DOM storage available completely offline. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Dc500App(onBack: () -> Unit = {}, onSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val defaultTitle = stringResource(R.string.modality_dc500)
    var title by remember { mutableStateOf(defaultTitle) }
    val background = MaterialTheme.colorScheme.background.toArgb()
    val web = remember {
        WebView(context).apply {
            setBackgroundColor(background)
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, value: String?) {
                    title = value?.takeIf { it.isNotBlank() } ?: defaultTitle
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // Native chrome owns the title; keep the web status and all tournament content.
                    if (url.startsWith("https://appassets.androidplatform.net/dc500/")) {
                        view.evaluateJavascript("document.querySelector('header h1')?.setAttribute('hidden','');", null)
                    }
                }
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                    val uri = request.url
                    if (uri.scheme != "https" || uri.host != "appassets.androidplatform.net")
                        return WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), "Blocked".byteInputStream())
                    val path = uri.path.orEmpty().removePrefix("/")
                    if (path.contains("..") || !path.startsWith("dc500/"))
                        return WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", emptyMap(), "Not found".byteInputStream())
                    return try {
                        val mime = when (path.substringAfterLast('.')) {
                            "js" -> "text/javascript"
                            "css" -> "text/css"
                            "png" -> "image/png"
                            else -> "text/html"
                        }
                        WebResourceResponse(mime, "UTF-8", context.assets.open(path))
                    } catch (_: Exception) {
                        WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", emptyMap(), "Not found".byteInputStream())
                    }
                }
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (request.url.host == "appassets.androidplatform.net") return false
                    if (request.url.scheme == "https") context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    return true
                }
            }
            loadUrl("https://appassets.androidplatform.net/dc500/dc500.html")
        }
    }
    DisposableEffect(web) { onDispose { web.destroy() } }
    Column(Modifier.fillMaxSize()) {
        TournamentTopBar(title, stringResource(R.string.modality_dc500), onBack, onSettings)
        AndroidView(factory = { web }, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}
