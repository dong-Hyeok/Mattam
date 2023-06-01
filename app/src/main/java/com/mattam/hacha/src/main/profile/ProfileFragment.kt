package com.mattam.hacha.src.main.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.mattam.hacha.databinding.FragmentProfileBinding
import com.mattam.hacha.src.main.LoginActivity
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.comment.WithdrawalBottomsheetFragment
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseFragment
import com.mattam.hacha.R

//https://images.khan.co.kr/article/2022/08/18/news-p.v1.20220818.f3ebf0c1531f4cda9dbbb16e2f5d8905_P1.jpg

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::bind, R.layout.fragment_profile) {
    private var param1: String? = null
    private var param2: String? = null

    private var postAdapter: ProfileMyPostAdpater = ProfileMyPostAdpater(arrayListOf())
    private lateinit var mainActivity: MainActivity
    private val activityViewModel: MainViewModel by activityViewModels()

    private var feedList : MutableList<Feed> = arrayListOf()
    private var feedMarkList : MutableList<Feed> = arrayListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFeeds()

        initData()

        binding.apply {

            Log.d("profileFragment", "onViewCreated: ${ApplicationClass.SharedPreferences.getProfileImg()}")
            Log.d("profileFragment", "onViewCreated: ${ApplicationClass.SharedPreferences.getUserTokenId()}")

            Glide.with(mainActivity)
                .load(ApplicationClass.SharedPreferences.getProfileImg())
                .circleCrop()
                .into(profileImg)

            menuImg.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }

            mypostRecyclerview.apply {
                layoutManager = GridLayoutManager(mainActivity,2)
                Log.d("ProfileFragment", "onViewCreated: $feedList")
                postAdapter.feedList = feedList
                adapter = postAdapter.apply {
                    itemClickListner = object : ProfileMyPostAdpater.ItemClickListener{
                        override fun onClick(view: View, position: Int, feed: Feed) {
                            //여기서 storeInfoFragment로 이동
                            //viewModel에 storeInfoFeed에 feed 넣어놔서 얘로 쓰면 됨
                            activityViewModel.storeInfoFeed = feed
                            activityViewModel.setDescription(feed.description)
                            activityViewModel.setStoreName(feed.storeName)
                            activityViewModel.setStoreLocation(feed.storeLocation)
                            activityViewModel.setStoreTel(feed.storeTel)
                            activityViewModel.modifyFromFragment = "profileFragment"
                            mainActivity.changeFragment("storeInfo")
                        }
                    }
                }
            }





            Glide.with(mainActivity)
                .load(ApplicationClass.SharedPreferences.getProfileImg())
                .circleCrop()
                .into(drawerProfileImg)

            editProfileBtn.setOnClickListener {
                mainActivity.changeFragment("profileEdit")
            }

            mypostTv.setOnClickListener {
                feedList.clear()
                loadFeeds()
            }

            bookmarksTv.setOnClickListener {
                feedMarkList.clear()
                loadMarkFeeds()
            }

            activityViewModel.myPostSelected.observe(viewLifecycleOwner) {
                Log.d("loadMarkFeed", "it이 뭘까: $it")
                if(it){ //myPost 띄우기
                    mypostTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color))
                    bookmarksTv.setTextColor(R.color.black)

                }else{ //bookmark 띄우기
                    bookmarksTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color))
                    mypostTv.setTextColor(R.color.black)
                }
            }


            // 로그아웃
            logoutBtn.setOnClickListener{
                logout()
            }

            // 회원탈퇴
            withdrawalBtn.setOnClickListener {
                val bottomSheetFragment = WithdrawalBottomsheetFragment.newInstance()
                bottomSheetFragment.show(mainActivity.supportFragmentManager, bottomSheetFragment.tag)
            }

        }
    }

    private fun loadMarkFeeds() {
        val db = FirebaseFirestore.getInstance()
        val feedNumList = activityViewModel.markList.value
        val feedNameList: MutableList<String> = feedNumList!!.map { "feed$it" }.toMutableList()
        Log.d("profileFragment", "loadMarkFeeds: $feedNumList")

        if (feedNameList.isNotEmpty()) {
            val batchSize = 10
            val batchCount = Math.ceil(feedNameList.size.toDouble() / batchSize).toInt()
            val batches = mutableListOf<List<String>>()

            for (i in 0 until batchCount) {
                val startIndex = i * batchSize
                val endIndex = minOf(startIndex + batchSize, feedNameList.size)
                val batch = feedNameList.subList(startIndex, endIndex)
                batches.add(batch)
            }

            val tasks = mutableListOf<Task<QuerySnapshot>>()

            for (batch in batches) {
                val task = db.collection("feeds")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                tasks.add(task)
            }

            Tasks.whenAllComplete(tasks)
                .addOnSuccessListener { taskList ->
                    val feedMarkList = mutableListOf<Feed>()

                    for (task in taskList) {
                        val querySnapshot = task.result as QuerySnapshot
                        for (doc in querySnapshot) {
                            val feedData = doc.toObject(Feed::class.java)
                            feedMarkList.add(feedData)
                            // Log.d("profileFragment", "loadMarkFeeds: $feedData")
                        }
                    }

                    postAdapter.feedList = feedMarkList
                    postAdapter.notifyDataSetChanged()
                    activityViewModel.setMyPostSelected(false)
                }
                .addOnFailureListener { error ->
                    println("문서 가져오기 실패: $error")
                }
        } else {
            postAdapter.feedList = feedMarkList
            postAdapter.notifyDataSetChanged()
            activityViewModel.setMyPostSelected(false)
        }
    }

    private fun loadFeeds() {
        val db = FirebaseFirestore.getInstance()
        val feedNumList = activityViewModel.user.feedNumList
        Log.d("profileFragment", "feedNumList: $feedNumList")

        if (feedNumList.isNotEmpty()) {
            val batchSize = 10
            val batchCount = Math.ceil(feedNumList.size.toDouble() / batchSize).toInt()
            val batches = mutableListOf<List<String>>()

            for (i in 0 until batchCount) {
                val startIndex = i * batchSize
                val endIndex = minOf(startIndex + batchSize, feedNumList.size)
                val batch = feedNumList.subList(startIndex, endIndex)
                batches.add(batch)
            }

            val tasks = mutableListOf<Task<QuerySnapshot>>()

            for (batch in batches) {
                val task = db.collection("feeds")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                tasks.add(task)
            }

            Tasks.whenAllComplete(tasks)
                .addOnSuccessListener { taskList ->
                    val feedList = mutableListOf<Feed>()

                    for (task in taskList) {
                        val querySnapshot = task.result as QuerySnapshot
                        for (doc in querySnapshot) {
                            val feedData = doc.toObject(Feed::class.java)
                            feedList.add(feedData)
                            Log.d("profileFragment", "loadFeeds: $feedData")
                        }
                    }

                    postAdapter.feedList = feedList
                    postAdapter.notifyDataSetChanged()
                    activityViewModel.setMyPostSelected(true)
                }
                .addOnFailureListener { error ->
                    println("문서 가져오기 실패: $error")
                }
        }
    }


    // 로그아웃
    private fun logout(){
        ApplicationClass.SharedPreferences.clearUserInfo()

        val intent = Intent(mainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }




    private fun initData(){
        binding.apply {
            idTv.text = activityViewModel.user.userId
            drawerIdTv.text = activityViewModel.user.userId
            introduceTv.text = activityViewModel.user.intro
            if(activityViewModel.user.profileImg != ""){
                Glide.with(mainActivity)
                    .load(activityViewModel.user.profileImg)
                    .circleCrop()
                    .into(profileImg)

                Glide.with(mainActivity)
                    .load(activityViewModel.user.profileImg)
                    .circleCrop()
                    .into(drawerProfileImg)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}