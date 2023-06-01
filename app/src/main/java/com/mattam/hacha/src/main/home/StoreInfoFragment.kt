package com.mattam.hacha.src.main.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentStoreInfoBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.comment.FeedBottomsheetFragment
import com.mattam.hacha.src.main.comment.WebViewBottomsheetFragment
import com.mattam.hacha.src.main.dto.Comment
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.template.config.BaseFragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class StoreInfoFragment : BaseFragment<FragmentStoreInfoBinding>(FragmentStoreInfoBinding::bind, R.layout.fragment_store_info) {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var infoFeed: Feed


    private lateinit var mainActivity: MainActivity
    private val activityViewModel: MainViewModel by activityViewModels()
    private lateinit var fromFragment: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoFeed = activityViewModel.storeInfoFeed
        val bundle = arguments //어느 프래그먼트에서 온지 가지고 있는 bundle
        Log.d("차선호", "onViewCreated: $bundle")
        fromFragment = bundle!!.getString("from")!!
        if(fromFragment != "commentFragment") activityViewModel.fromCommentFragment = fromFragment

        binding.apply {
            Glide.with(mainActivity)
                .load(infoFeed.imgList[0])
                .into(backgroundImg)

            Glide.with(mainActivity)
                .load(infoFeed.userProfileImg)
                .circleCrop()
                .into(infoProfileImg)

            infoIdTv.text = infoFeed.userId

            infoContentTv.text = infoFeed.description
            infoStoreTv.text = infoFeed.storeName
            infoLocationTv.text = infoFeed.storeLocation
            infoTelTv.text = infoFeed.storeTel

            commentImg.setOnClickListener {

                feedCommentList(activityViewModel.storeInfoFeed.feedId)
            }

            if(infoFeed.userId == activityViewModel.user.userId){
                modifyImg.visibility = View.VISIBLE
                deleteImg.visibility = View.VISIBLE
            }else{
                modifyImg.visibility = View.GONE
                deleteImg.visibility = View.GONE
            }
            
            modifyImg.setOnClickListener {
                //addFragment로 이동 ( 사진 추가하는 버튼, 사진 하나당 있는 x 버튼 안보이고, done 대신 modify)
                activityViewModel.clearAddPhotoList()
                activityViewModel.modifySelected = true
                activityViewModel.setDescription(infoFeed.description)
                activityViewModel.setStoreName(infoFeed.storeName)
                activityViewModel.setStoreLocation(infoFeed.storeLocation)
                activityViewModel.setStoreTel(infoFeed.storeTel)
                activityViewModel.fromModifyBtn = true
                mainActivity.changeFragment("add")
            }
            
            deleteImg.setOnClickListener { 
                //삭제할거냐는 DIALOG 띄어야 함
                val bottomSheetFragment = FeedBottomsheetFragment.newInstance()
                bottomSheetFragment.show(mainActivity.supportFragmentManager, bottomSheetFragment.tag)
            }

            searchStore.setOnClickListener {
                //웹뷰 띄우기
                val bottomSheetFragment = WebViewBottomsheetFragment.newInstance()
                // BottomSheetDialog의 Behavior 가져오기
                val dialog = bottomSheetFragment.dialog
                bottomSheetFragment.show(mainActivity.supportFragmentManager, bottomSheetFragment.tag)



            }

        }
        if(fromFragment == "profileFragment"){ //프로필 프래그먼트에서 넘어왔다면
            activityViewModel.fromStoreInfo = 1
            mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainActivity.changeFragment("profile")
                }
            })
        }else if(fromFragment == "homeFragment") { //홈 프래그먼트에서 넘어온 거면
            activityViewModel.fromStoreInfo = 0
            mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainActivity.changeFragment("home")
                }
            })
        } else if(fromFragment == "addFragment") { //add 프래그먼트에서 넘어온 거면
            if(activityViewModel.modifyFromFragment == "profileFragment") {
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            mainActivity.changeFragment("profile")
                        }
                    })
            }else if(activityViewModel.modifyFromFragment == "homeFragment"){
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("home")
                    }
                })
            }else if(activityViewModel.modifyFromFragment == "searchFragment"){
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("search")
                    }
                })
            }
        }else if(fromFragment == "commentFragment"){
            if(activityViewModel.fromCommentFragment == "homeFragment") {
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("home")
                    }
                })
            }else if(activityViewModel.fromCommentFragment == "profileFragment"){
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("profile")
                    }
                })
            }else{
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("search")
                    }
                })
            }
        }else  if(fromFragment == "searchFragment"){
            activityViewModel.fromStoreInfo = 2
            mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //어떻게 현재 이 프래그머트만 내릴까,,,
                    mainActivity.changeFragment("search")
                }
            })
        }

    }

    private fun feedCommentList(feedId:Long){
        // 기존에 댓글 정보를 초기호
        activityViewModel.clearCommentList()

        val feedsRef = FirebaseFirestore.getInstance().collection("feeds").document("feed${feedId}")
        feedsRef.get()
            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val commentList = documentSnapshot.get("commentList") as? MutableList<String>
//                    Log.d("storeInfoFragment", "feedCommentList: ${commentList}")
//                    if(commentList!!.size>0){
//                        getComment(commentList)
//                    }else{
//                        mainActivity.changeFragment("comments")
//                    }
                if (documentSnapshot.exists()) {
                    activityViewModel.selectFeedCommentList = documentSnapshot.get("commentList") as MutableList<String>
                    Log.d("HomeFragment", "feedCommentList: ${activityViewModel.selectFeedCommentList}")
                    if(activityViewModel.selectFeedCommentList!!.size>0){
                        getComment(activityViewModel.selectFeedCommentList)
                    }else{
                        mainActivity.changeFragment("comments")
                    }

                } else {
                    println("해당 문서가 존재하지 않습니다.")
                }
            }
            .addOnFailureListener { e ->
                println("데이터 가져오기 실패: $e")
            }
    }

    // 선택 feed의 commentList 얻기
    private fun getComment(commentList:MutableList<String>){


        val db = FirebaseFirestore.getInstance()

        if(commentList.size > 0){
            db.collection("comments")
                .whereIn(FieldPath.documentId(), commentList)
//                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot) {
                        val commentData = doc.toObject(Comment::class.java)
                        Log.d("HomeFragment", "loadCommentData: ${commentData}")

                        activityViewModel.addToCommentList(commentData)
                    }

                    mainActivity.changeFragment("comments")

                }
                .addOnFailureListener { error ->
                    val errorMessage = error.message
                    Log.d("HomeFragment", "loadCommentData 실패: $errorMessage")
                }

        }else mainActivity.changeFragment("comments")

    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StoreInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}