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
import com.mattam.hacha.databinding.FragmentFeedBottomsheetBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.template.config.ApplicationClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedBottomsheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFeedBottomsheetBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    lateinit var mainActivity : MainActivity
    var dialog: BottomSheetDialog? = null

    companion object {
        fun newInstance(): FeedBottomsheetFragment {
            return FeedBottomsheetFragment()
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
        _binding = FragmentFeedBottomsheetBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            confirmBtn.setOnClickListener {
                feedDelete(activityViewModel.storeInfoFeed.feedId)
                userFeedCommentUpdate()
                userFeedListUpdate(activityViewModel.storeInfoFeed.feedId)
                FeedCommentDelete()
                userMarkFeedDelete(activityViewModel.storeInfoFeed.feedId)
                CoroutineScope(Dispatchers.Main).launch {
                    // 여기 loading 애니메이션 적용
                    delay(1000)
//                    if(activityViewModel.preStoreInfoFragment == "homeFragment"){ //삭제하고 홈으로 보내라
//
//                    }else if(activityViewModel.preStoreInfoFragment == "profileFragment"){ //삭제하고 프로필로 보내라
//
//                    }else{ // 삭제하고 search로 보내라
//
//                    }
                    mainActivity.getFeedList()
                    delay(500)
                    dialog!!.dismiss()

//                    mainActivity.changeFragment("home")
                }
            }
        }

    }

    private fun feedDelete(feedId:Long){
        //feed 삭제 코드 구현
        val db = FirebaseFirestore.getInstance()

        val feedDeleteRef = db.collection("feeds").document("feed${activityViewModel.storeInfoFeed.feedId}")
        feedDeleteRef.delete()
            .addOnSuccessListener {
                // 문서 삭제 성공
                Log.d("차선호", "feedDelete: ${activityViewModel.preStoreInfoFragment}")
                if(activityViewModel.preStoreInfoFragment == "searchFragment") {
                    activityViewModel.searchFeedList.removeAt(activityViewModel.selectedSearchFeedPosition)
                }
                Log.d("FeedBottomsheetFragment", "onViewCreated: 삭제 성공 ")
            }
            .addOnFailureListener { error ->
                // 문서 삭제 실패
                Log.d("FeedBottomsheetFragment", "onViewCreated: 삭제 실패 ")
            }
    }

    private fun userFeedCommentUpdate(){
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val db = FirebaseFirestore.getInstance()
        val userCommentDeleteRef = db.collection("users").document(userToken)

        // 업데이트할 필드와 값을 설정합니다.
        val field = "commentList"
        val valuesToRemove = activityViewModel.storeInfoFeed.commentList // 삭제하고 싶은 값들의 리스트

        // 문서를 가져온 후, 필드에서 특정 값을 제거한 뒤 업데이트합니다.
        db.runTransaction { transaction ->
            val user1Doc = transaction.get(userCommentDeleteRef)
            val commentList = user1Doc.get("commentList") as MutableList<String>?

            // 특정 값들을 제거합니다.
            commentList?.removeAll(valuesToRemove)

            // 업데이트된 필드를 문서에 설정합니다.
            transaction.update(userCommentDeleteRef, field, commentList)

            // 트랜잭션 성공으로 표시합니다.
            null
        }.addOnSuccessListener {
            // 문서 업데이트 성공
            Log.d("FeedBottomsheetFragment", "userFeedCommentUpdate: 삭제 성공 ")
        }.addOnFailureListener { error ->
            // 문서 업데이트 실패
            Log.d("FeedBottomsheetFragment", "userFeedCommentUpdate: 삭제 실패 ")
        }
    }

    private fun userFeedListUpdate(feedId:Long){
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val db = FirebaseFirestore.getInstance()

        val userRef = db.collection("users").document(userToken)

        // 업데이트할 필드와 값을 설정합니다.
        val field = "feedNumList"
        val valueToRemove = "feed${feedId}" // 제거하고 싶은 값

        // 문서를 가져온 후, 필드에서 특정 값을 제거한 뒤 업데이트합니다.
        db.runTransaction { transaction ->
            val user1Doc = transaction.get(userRef)
            val feedNumList = user1Doc.get("feedNumList") as MutableList<String>?

            // 특정 값을 제거합니다.
            feedNumList?.remove(valueToRemove)

            // 업데이트된 필드를 문서에 설정합니다.
            transaction.update(userRef, field, feedNumList)

            // 트랜잭션 성공으로 표시합니다.
            null
        }.addOnSuccessListener {
            // 문서 업데이트 성공
            Log.d("FeedBottomsheetFragment", "userFeedListUpdate: 삭제 성공 ")
        }.addOnFailureListener { error ->
            // 문서 업데이트 실패
            Log.d("FeedBottomsheetFragment", "userFeedListUpdate: 삭제 실패 ")
        }
    }

    private fun FeedCommentDelete(){
        val db = FirebaseFirestore.getInstance()

        val documentIdsToDelete = activityViewModel.storeInfoFeed.commentList // 삭제하고자 하는 문서들의 자동완성 아이디 값들

        for (documentId in documentIdsToDelete) {
            val documentRef = db.collection("comments").document(documentId)
            documentRef.delete()
                .addOnSuccessListener {
                    // 문서 삭제 성공
                    Log.d("FeedBottomsheetFragment","comments 문서 삭제 성공: $documentId")
                }
                .addOnFailureListener { error ->
                    // 문서 삭제 실패
                    Log.d("FeedBottomsheetFragment"," $documentId, 오류: $error")
                }
        }
    }

    private fun userMarkFeedDelete(feedId:Long) {
        activityViewModel.user.markList.remove(feedId)
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("users")

        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (documentSnapshot in querySnapshot) {
                    val documentRef = documentSnapshot.reference
                    val markList = documentSnapshot.get("markList") as? MutableList<Long>

                    // 특정 값을 삭제할 때
                    val valueToRemove = feedId
                    markList?.remove(valueToRemove)

                    // 변경된 필드를 업데이트
                    documentRef.update("markList", markList)
                        .addOnSuccessListener {
                            // 문서 삭제 성공
                            Log.d("userMarkFeedDelete", "mark 문서 삭제 성공")
                        }
                        .addOnFailureListener { error ->
                            // 문서 삭제 실패
                            Log.d("userMarkFeedDelete", "mark, 오류: $error")
                        }
                }
            }
            .addOnFailureListener { error ->
                // 문서 가져오기 실패
                Log.d("userMarkFeedDelete", "문서가지고 오기, 오류: $error")
            }
    }

}