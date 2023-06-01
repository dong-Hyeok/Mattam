package com.mattam.hacha.src.main.comment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentWithdrawalBottomsheetBinding
import com.mattam.hacha.src.main.LoginActivity
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.template.config.ApplicationClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WithdrawalBottomsheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentWithdrawalBottomsheetBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    lateinit var mainActivity : MainActivity
    var dialog: BottomSheetDialog? = null

    companion object {
        fun newInstance(): WithdrawalBottomsheetFragment {
            return WithdrawalBottomsheetFragment()
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
        _binding = FragmentWithdrawalBottomsheetBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            confirmBtn.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    mainActivity.showGetFeedLoading()
                    Withdrawal()
                }
            }
        }

    }

    // 회원탈퇴
    private fun Withdrawal(){
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        // comments
        val commentsCollection = db.collection("comments")
        for (commentId in activityViewModel.user.commentList) {
            val commentRef = commentsCollection.document(commentId)
            batch.delete(commentRef)
        }
//        batch.commit()

        // feeds
        val feedsCollection = db.collection("feeds")

        for (feedId in activityViewModel.user.feedNumList) {
            val feedRef = feedsCollection.document(feedId)
            batch.delete(feedRef)
        }
//        batch.commit()

        // users
        val usersCollection = db.collection("users")
        val userRef = usersCollection.document(ApplicationClass.SharedPreferences.getUserTokenId())
        batch.delete(userRef)


        batch.commit()
            .addOnSuccessListener {
                ApplicationClass.SharedPreferences.clearUserInfo()

                val intent = Intent(mainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
    }

}