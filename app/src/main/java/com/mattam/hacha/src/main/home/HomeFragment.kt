package com.mattam.hacha.src.main.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentHomeBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Comment
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var homeAdapter: HomeAdapter
    lateinit var mainActivity : MainActivity
    private val activityViewModel: MainViewModel by activityViewModels()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated feedList: ${activityViewModel.feedList.value}")

        homeAdapter = HomeAdapter(viewLifecycleOwner, activityViewModel, mainActivity,activityViewModel.feedList)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = homeAdapter.apply {

                bookMarkClickListener = object : HomeAdapter.BookMarkClickListener{
                    override fun onClick(view: ImageView, position: Int, feed: Feed) {
                        if(feed.mark){
                            view.setBackgroundResource(R.drawable.unselected_bookmark)
                            feed.mark = false
                            activityViewModel.removeMark(feed.feedId)
                            setMark(feed.mark, feed) //firebase에 mark 정보 수정
                        }else{
                            view.setBackgroundResource(R.drawable.selected_bookmark)
                            feed.mark = true
                            activityViewModel.addMark(feed.feedId)
                            setMark(feed.mark, feed) //firebase에 mark 정보 수정
                        }
                    }
                }

                commentClickListener = object : HomeAdapter.CommentClickListener{
                    override fun onClick(position: Int, feedId: Long) {
                        // firebase firestore에서 데이터를 받아옴 -> 라이브데이터에 받아온 데이터를 넣어줌
                        feedCommentList(position, feedId)
                    }
                }

                menuClickListener = object : HomeAdapter.MenuClickListener{
                    override fun onClick(position: Int, feedId: Long) {
                    }
                }
            }


            recyclerView.apply {

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val firstVisibleItemPosition =
                                layoutManager.findFirstVisibleItemPosition()

                            if (firstVisibleItemPosition == 0) {
                                Log.d("HomeFragment", "onScrollStateChanged: ")
                                CoroutineScope(Dispatchers.Main).launch {
                                    activityViewModel.lockRecyclerView = true
                                    mainActivity.showGetFeedLoading()
                                    delay(1000)
                                    mainActivity.getFeedList()
                                }
                            }
                        }
                    }
                })

                setOnTouchListener { v, event ->
                    return@setOnTouchListener activityViewModel.lockRecyclerView
                }
            }


        }
    }


    // 선택 feed의 mark 데이터 변경
    private fun setMark(clicked:Boolean, feed:Feed){
        val db = FirebaseFirestore.getInstance()
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        // users -> 사용자 markList에 mark 수정
        // 만약 true, false 분기로 나눠서 처리
        // true면 markList에 해당 feedId 추가 / false면 markList에 해당 feedId 삭제


        val markUpdateRef = db.collection("users").document(userToken)


        if(clicked){ // 마커 표시
            markUpdateRef.update("markList", FieldValue.arrayUnion(feed.feedId))
                .addOnSuccessListener {
                    Log.d("HomeFragment", "setMark: 업데이트 성공")
                }
                .addOnFailureListener {
                    Log.d("HomeFragment", "setMark: 업데이트 실패")
                }
        }else{ // 마커 삭제
            markUpdateRef.update("markList", FieldValue.arrayRemove(feed.feedId))
                .addOnSuccessListener {
                    Log.d("HomeFragment", "setMark: 업데이트 성공")
                }
                .addOnFailureListener {
                    Log.d("HomeFragment", "setMark: 업데이트 실패")
                }
        }

    }

    // 특정 feed의 commentList를 얻기
    private fun feedCommentList(position:Int, feedId:Long){
        // 기존에 댓글 정보를 초기화
        activityViewModel.clearCommentList()

        val feedId = activityViewModel.feedList.value!!.get(position).feedId.toString().toInt() // 해당 feed의 commentList
        activityViewModel.selectCommentFeedId = feedId.toLong() // 선택 feedId

        Log.d("하동혁", " feedId ${feedId}")

        val feedsRef = FirebaseFirestore.getInstance().collection("feeds").document("feed${feedId}")
        feedsRef.get()
            .addOnSuccessListener { documentSnapshot ->
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
//                    activityViewModel.selectFeedCommentList = activityViewModel.commentList.value[]
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
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}