package com.mattam.hacha.src.main.comment

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentWebViewBottomsheetBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel


class WebViewBottomsheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentWebViewBottomsheetBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private lateinit var webView: WebView
    var dialog: BottomSheetDialog? = null
    var behavior: BottomSheetBehavior<FrameLayout>? = null
    private lateinit var mainActivity: MainActivity
    private var deviceHeight = 0

    companion object {
        fun newInstance(): WebViewBottomsheetFragment {
            return WebViewBottomsheetFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(true)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                webView.goBack()
                true
            } else {
                false
            }
        }

        val win = mainActivity.windowManager.currentWindowMetrics
        deviceHeight = win.bounds.height()

        behavior = dialog!!.behavior.apply {
            isDraggable = false
            peekHeight = (deviceHeight*0.8).toInt()
        }

        return dialog!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 해당 프래그먼트의 레이아웃을 인플레이트합니다.

        _binding = FragmentWebViewBottomsheetBinding.inflate(layoutInflater,container,false)
        webView = binding.webview
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            var webView = webview
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()

            // 특정 URL 로드
            val url = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=${activityViewModel.storeInfoFeed.storeLocation} ${activityViewModel.storeInfoFeed.storeName}" // 표시할 웹 페이지의 URL
            webView.loadUrl(url)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // WebView 페이지 로딩 완료 시에 뒤로가기 기록 추가
                    webView.canGoBack()
                }
            }
        }
    }
}

//https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=구미 범맥주