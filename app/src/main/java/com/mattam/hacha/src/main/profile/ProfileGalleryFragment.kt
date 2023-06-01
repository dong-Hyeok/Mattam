package com.mattam.hacha.src.main.add

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentProfileGalleryBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.profile.ProfileGalleryAdapter
import com.mattam.template.config.BaseFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileGalleryFragment : BaseFragment<FragmentProfileGalleryBinding>(FragmentProfileGalleryBinding::bind, R.layout.fragment_profile_gallery) {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var profileGalleryAdapter: ProfileGalleryAdapter
    private var galleryList: MutableList<String> = arrayListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityViewModel: MainViewModel by activityViewModels()

        activityViewModel.clearGalleryList()

        profileGalleryAdapter = ProfileGalleryAdapter(viewLifecycleOwner,activityViewModel.galleryList)


        binding.apply {
            profileGalleryRecyclerview.apply {
                layoutManager = GridLayoutManager(mainActivity, 3)
                adapter = profileGalleryAdapter
            }

            changeBtn.setOnClickListener {
                for (photo in profileGalleryAdapter.galleryList.value!!) {
                    if (photo.isSelected.value!!) activityViewModel.editImgUrl = photo.imgUrl
                }
                mainActivity.changeFragment("profileEdit")
            }
        }

        mainActivity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainActivity.changeFragment("profileEdit")
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