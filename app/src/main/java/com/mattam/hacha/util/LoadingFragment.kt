package com.mattam.hacha.util

import android.content.Context
import android.os.Bundle
import android.view.View
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentLoadingBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.template.config.BaseFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LoadingFragment : BaseFragment<FragmentLoadingBinding>(FragmentLoadingBinding::bind, R.layout.fragment_loading) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var mainActivity: MainActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoadingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}