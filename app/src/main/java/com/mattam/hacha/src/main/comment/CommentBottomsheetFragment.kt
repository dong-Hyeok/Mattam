package com.mattam.hacha.src.main.comment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentCommentBottomsheetBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.template.config.ApplicationClass

class CommentBottomsheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentBottomsheetBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private lateinit var mainActivity: MainActivity
    var dialog: BottomSheetDialog? = null
    val db = FirebaseFirestore.getInstance()
    var selectCommentPosi = 0
    var selectCommentId = ""
    companion object {
        fun newInstance(): CommentBottomsheetFragment {
            return CommentBottomsheetFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 해당 프래그먼트의 레이아웃을 인플레이트합니다.
        _binding = FragmentCommentBottomsheetBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 commentList에 있는 선택 comment의 index
        selectCommentPosi = activityViewModel.currentCommentPosi

        // 삭제할 comment의 Id값
        Log.d("CommentBottomSheet", "onViewCreated: feedCommentList${activityViewModel.selectFeedCommentList}")
        selectCommentId = activityViewModel.selectFeedCommentList[selectCommentPosi]


        binding.apply {
            confirmBtn.setOnClickListener {
                //comment 삭제 코드 구현
                Log.d("CommentBottomSheet", "onViewCreated: ${activityViewModel.currentComment}")
                activityViewModel.removeComment(activityViewModel.currentCommentPosi)
                commentDelete()
                feedCommentUpdate()
                userCommentUpdate()
                dialog!!.dismiss()
            }
        }

    }

    // comments에서 comment 삭제
    private fun commentDelete(){
        val commentDeleteRef = db.collection("comments").document(selectCommentId)

        commentDeleteRef.delete()
            .addOnSuccessListener {
                Log.d("CommentBSF", "commentDelete: 삭제 성공")
            }
            .addOnFailureListener {
                Log.d("CommentBSF", "commentDelete: 삭제 실패")
            }

    }

    // user commentList에서 해당 comment 삭제
    private fun feedCommentUpdate(){
        // feeds -> feedId -> feed에 있는 commentList에 삭제할 commentId 삭제
        val feedId = "feed${activityViewModel.currentComment.feedId}"
        val feedCommentListeRef = db.collection("feeds").document(feedId)

        // 업데이트할 필드와 값을 설정합니다.
        val field = "commentList"
        val valueToRemove = selectCommentId // 제거하고 싶은 값

        db.runTransaction { transaction ->
            val feedDoc = transaction.get(feedCommentListeRef)
            val commetList = feedDoc.get("commentList") as MutableList<String>?

            // 특정 값을 제거합니다.
            commetList?.remove(valueToRemove)

            // 업데이트된 필드를 문서에 설정합니다.
            transaction.update(feedCommentListeRef, field, commetList)

            // 트랜잭션 성공으로 표시합니다.
            null
        }.addOnSuccessListener {
            // 문서 업데이트 성공
            Log.d("CommentBSF", "feedCommentUpdate: 삭제 성공 ")
        }.addOnFailureListener { error ->
            // 문서 업데이트 실패
            Log.d("CommentBSF", "feedCommentUpdate: 삭제 실패 ")
        }

    }

    // feed commentList에서 해당 comment 삭제
    private fun userCommentUpdate(){
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val feedCommentListeRef = db.collection("users").document(userToken)

        val field = "commentList"
        val valueToRemove = selectCommentId // 제거하고 싶은 값

        db.runTransaction { transaction ->
            val userDoc = transaction.get(feedCommentListeRef)
            val commetList = userDoc.get("commentList") as MutableList<String>?

            // 특정 값을 제거합니다.
            commetList?.remove(valueToRemove)

            // 업데이트된 필드를 문서에 설정합니다.
            transaction.update(feedCommentListeRef, field, commetList)

            // 트랜잭션 성공으로 표시합니다.
            null
        }.addOnSuccessListener {
            // 문서 업데이트 성공
            Log.d("CommentBSF", "userCommentUpdate: 삭제 성공 ")
        }.addOnFailureListener { error ->
            // 문서 업데이트 실패
            Log.d("CommentBSF", "userCommentUpdate: 삭제 실패 ")
        }

    }

}