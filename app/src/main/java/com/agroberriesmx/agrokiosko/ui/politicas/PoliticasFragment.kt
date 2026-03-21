package com.agroberriesmx.agrokiosko.ui.politicas

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.agroberriesmx.agrokiosko.databinding.FragmentPoliticasBinding
import java.net.URLEncoder

class PoliticasFragment : Fragment() {

    private var _binding: FragmentPoliticasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoliticasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val webView: WebView = binding.webView
        val progressBar: ProgressBar = binding.progressBar // Asegúrate de tener esta línea

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = View.GONE

                // Muestra un mensaje al usuario si hay un error
                Toast.makeText(requireContext(), "Error al cargar la política. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()

                // Opcionalmente, puedes regresar al fragmento anterior
                // findNavController().popBackStack()
            }
        }

        // Aquí es donde la URL se carga dinámicamente
        val pdfUrl = arguments?.getString("pdf_url")
        if (!pdfUrl.isNullOrEmpty()) {
            val encodedUrl = URLEncoder.encode(pdfUrl, "UTF-8")
            val googleDocsViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"
            webView.loadUrl(googleDocsViewerUrl)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}