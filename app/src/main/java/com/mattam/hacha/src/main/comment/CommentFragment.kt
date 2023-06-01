package com.mattam.hacha.src.main.comment

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.databinding.FragmentCommentBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.add.GalleryFragment
import com.mattam.hacha.src.main.dto.Comment
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class CommentFragment : BaseFragment<FragmentCommentBinding>(FragmentCommentBinding::bind, com.mattam.hacha.R.layout.fragment_comment) {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var commentAdapter: CommentAdapter
//    private lateinit var commentAdapter: CommentListAdapter// listAdpater
    private val activityViewModel: MainViewModel by activityViewModels()

    lateinit var comment: String
    lateinit var currentComment: Comment

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("차선호", "activityViewModel.commentlist ${activityViewModel.commentList.value}")

        val activityViewModel: MainViewModel by activityViewModels()
        val bundle = arguments //어느 프래그먼트에서 온지 가지고 있는 bundle
        val fromFragment = bundle!!.getString("from")

        commentAdapter = CommentAdapter().apply {
            itemLongClickListener = object : CommentAdapter.ItemLongClickListener {
                override fun onLongClick(view: View, comment: Comment, position:Int) {
                    //drawerLayout 띄우자(내가 쓴 피드이거나 내가 쓴 댓글이면
                    activityViewModel.currentComment = comment
                    activityViewModel.currentCommentPosi = position
                    activityViewModel.selectCommentFeedId = comment.feedId

                    if(checkDelete()) {
                        val bottomSheetFragment = CommentBottomsheetFragment.newInstance()
                        bottomSheetFragment.show(
                            mainActivity.supportFragmentManager,
                            bottomSheetFragment.tag
                        )
                    }
                }
            }
        }
        activityViewModel.commentList.observe(viewLifecycleOwner, Observer {
            commentAdapter.submitList(it)
        })

        binding.apply {
            commentRecyclerview.apply {
                layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
                adapter = commentAdapter

            }
            
            addBtn.setOnClickListener {
                comment = inputEt.text.toString()

                //이 comment 추가하면 됨
                addComments(comment)
                inputEt.setText("")
                inputEt.clearFocus()
                val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(inputEt.windowToken, 0)
            }

            backImg.setOnClickListener {
                if(fromFragment == "storeInfoFragment"){
                    mainActivity.changeFragment("storeInfo")
                }else if(fromFragment == "homeFragment"){
                    mainActivity.changeFragment("home")
                }else{
                    mainActivity.changeFragment("search")
                }
            }


        }

        if(fromFragment == "storeInfoFragment"){
            mainActivity.onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("storeInfo")
                    }
                })
        }else if(fromFragment == "homeFragment"){
            mainActivity.onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("home")
                    }
                })
        }else{
            mainActivity.onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("search")
                    }
                })
        }

        //뒤로가기 분기 태워라


    }



    private fun checkDelete(): Boolean{
        if(activityViewModel.currentComment.fromUserId == activityViewModel.user.userId) return true
        for(feedId in activityViewModel.user.feedNumList){
            Log.d("차선호", "feedId : $feedId  ||  myfeedId : ${activityViewModel.selectCommentFeedId}")
            if(feedId == "feed${activityViewModel.selectCommentFeedId}") return true
        }
        return false
    }

    // Comment 추가 -> users, feeds에 comment번호 / comments에 comment 정보
    private fun addComments(message:String){
        val commentsRef = FirebaseFirestore.getInstance().collection("comments").document()
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val userId = ApplicationClass.SharedPreferences.getUserId()
        val userImg = ApplicationClass.SharedPreferences.getProfileImg()
        val currentFeedId = activityViewModel.selectCommentFeedId

        // 새로운 문서에 필드를 설정합니다.
        val comment = hashMapOf(
            "feedId" to currentFeedId,
            "fromToken" to userToken,
            "fromUserId" to userId,
            "fromUserImg" to userImg,
            "message" to message,
            "time" to System.currentTimeMillis()
        )
        commentsRef.set(comment)
            .addOnSuccessListener {
                // 문서 추가 성공 시 실행되는 코드
                val commentId = commentsRef.id
                userCommentsListUpdate(commentId)
                feedCommentsListUpdate(commentId)
                activityViewModel.selectFeedCommentList.add(commentId)
                activityViewModel.addToCommentList(Comment(currentFeedId, userToken,userId,userImg,message,System.currentTimeMillis()))
            }
            .addOnFailureListener { e ->
                // 문서 추가 실패 시 실행되는 코드
                println("문서 추가 실패: $e")
            }

    }

    private fun userCommentsListUpdate(commetId:String){
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val userCommentsRef = FirebaseFirestore.getInstance().collection("users").document(userToken)

        userCommentsRef.update("commentList", FieldValue.arrayUnion(commetId))
            .addOnSuccessListener {
                Log.d("AddFragment", "userCommentsListUpdate 새로운 commentId가 성공적으로 추가되었습니다. id: ${commetId}")
            }
            .addOnFailureListener { error ->
                Log.e("AddFragment", "userCommentsListUpdate commentId 추가 중 오류가 발생했습니다.", error)
            }
    }

    private fun feedCommentsListUpdate(commetId:String){
        val selectFeedId = "feed${activityViewModel.selectCommentFeedId}"
        val feedCommentsRef = FirebaseFirestore.getInstance().collection("feeds").document(selectFeedId)

        feedCommentsRef.update("commentList", FieldValue.arrayUnion(commetId))
            .addOnSuccessListener {
                Log.d("AddFragment", "feedCommentsListUpdate 새로운 commentId가 성공적으로 추가되었습니다. id: ${commetId}")
            }
            .addOnFailureListener { error ->
                Log.e("AddFragment", "feedCommentsListUpdate commentId 추가 중 오류가 발생했습니다.", error)
            }

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