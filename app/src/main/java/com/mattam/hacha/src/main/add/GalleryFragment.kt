package com.mattam.hacha.src.main.add

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentGalleryBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.template.config.BaseFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::bind, R.layout.fragment_gallery) {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var galleryAdapter: GalleryAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityViewModel: MainViewModel by activityViewModels()

        galleryAdapter = GalleryAdapter(viewLifecycleOwner, activityViewModel)

        binding.apply {
            galleryRecyclerview.apply {
                layoutManager = GridLayoutManager(mainActivity, 3)
                adapter = galleryAdapter
            }

            addBtn.setOnClickListener {
                activityViewModel.clearAddPhotoList()
                for (photo in activityViewModel.galleryList.value!!) {
                    if (photo.isSelected.value!!) activityViewModel.addToAddPhotoList(photo)
                }
                mainActivity.changeFragment("add")
            }
        }

        mainActivity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainActivity.changeFragment("add")
                }
            })

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}