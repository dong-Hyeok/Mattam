package com.mattam.hacha.src.main.profile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentProfileEditBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.User
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseFragment
import kotlinx.coroutines.*
import java.io.File


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileEditFragment : BaseFragment<FragmentProfileEditBinding>(FragmentProfileEditBinding::bind, R.layout.fragment_profile_edit) {

    private var param1: String? = null
    private var param2: String? = null
    private val activityViewModel: MainViewModel by activityViewModels()

    private var check = false
    private lateinit var userId: String
    private lateinit var introduce: String
    private lateinit var imgUrl: String
    private lateinit var fromFragment: String

    private lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = arguments //어느 프래그먼트에서 온지 가지고 있는 bundle
        fromFragment = bundle?.getString("from")!!

        binding.apply {


            if(fromFragment == "profileFragment"){
                activityViewModel.editUserId = activityViewModel.user.userId
                activityViewModel.editIntroduce = activityViewModel.user.intro
            }

            nicknameEt.setText(activityViewModel.editUserId)
            introduceEt.setText(activityViewModel.editIntroduce)
            //입력 정보 유지하기//////////////////////////////////////////////////
            nicknameEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    activityViewModel.editUserId = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            introduceEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    activityViewModel.editIntroduce = s.toString()
                }
                override fun afterTextChanged(s: Editable?) {}
            })




            if(fromFragment == "profileFragment"){ //프로필 프래그먼트에서 넘어왔다면
                activityViewModel.editImgUrl = activityViewModel.user.profileImg
                Glide.with(mainActivity)
                    .load(activityViewModel.user.profileImg) // 현재 내 프로필 이미지가 떠야함
                    .circleCrop()
                    .into(profileImg)


            }else if(fromFragment == "profileGalleryFragment") { //프로필갤러리 프래그먼트에서 넘어온 거면
                Glide.with(mainActivity)
                    .load(activityViewModel.editImgUrl)// 내가 선택한 사진이 우선 들어가가있음
                    .circleCrop()
                    .into(profileImg)
            }


            editphotoTv.setOnClickListener {
                mainActivity.requestStoragePermission()
            }

            doneBtn.setOnClickListener {

                // 1. ID 중복체크 확인 후 중복 없으면-> storage(putImage())에 이미지 저장
                // 2. putImage에서 이미지 저장 완료되면 -> updateFeedProfileImg() , updateProfile() 수행
                // 3. updateFeedProfileImg() 완료되면 ->  mainActivity.getFeedList() 수행

                userId = nicknameEt.text.toString()
                introduce = introduceEt.text.toString()
                //중복 체크
                CoroutineScope(Dispatchers.Default).launch {
                    activityViewModel.fromEditProfileFlag = true
                    mainActivity.changeFragment("loading")
                    checkDuplicateId(userId)
                }
            }

            backImg.setOnClickListener{

                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("profile")
                    }
                })
            }

        }

        mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainActivity.changeFragment("profile")
            }
        })
    }

    // storage에 img를 저장
    private fun putImage(imgUrl:String){

        if(imgUrl == activityViewModel.user.profileImg){
            updateFeedProfileImg(activityViewModel.user.profileImg)
            updateProfile(userId, introduce, activityViewModel.user.profileImg)
        }
        else{
            val storageRef = ApplicationClass.storage.reference
            val file = Uri.fromFile(File(imgUrl))
            val imagesRef = storageRef.child("images/${file.lastPathSegment}")
            val uploadTask = imagesRef.putFile(file)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateFeedProfileImg(downloadUri.toString())
                    updateProfile(userId, introduce, downloadUri.toString())
                    updateCommentImg(downloadUri.toString())
                } else {
                    // 업로드 실패 시 처리
                }
            }
        }


    }

    // firestore -> comment 유저 Img 수정 요청
    fun updateCommentImg(imgUrl: String){
        val db = FirebaseFirestore.getInstance().collection("comments")
        val commentList = activityViewModel.user.commentList

        // 업데이트할 필드와 값을 가진 맵 생성
        val feedUpdates = hashMapOf<String, Any>(
            "fromUserImg" to imgUrl
        )

        for (commentId in commentList) {
            val commentRef = db.document(commentId)

            // 문서 업데이트 수행
            commentRef.update(feedUpdates)
                .addOnSuccessListener {
                    // 성공적으로 업데이트됨
                    Log.d("ProfileEditFragment", "updateFeedProfileImg: feed 업데이트 성공")
                }
                .addOnFailureListener { e ->
                    // 업데이트 실패
                    Log.d("ProfileEditFragment", "updateFeedProfileImg: feed 업데이트 실패")
                }
        }
    }


    // firestore -> 유저 정보 수정 요청
    fun updateProfile(userId: String, introduce: String, imgUrl: String){
        val db = FirebaseFirestore.getInstance()
        val userUid = ApplicationClass.SharedPreferences.getUserTokenId()
        val collectionReference = db.collection("users").document(userUid)// user 정보 모두 가지고 옴

        val fieldUpdates = hashMapOf<String, Any>(
            "userId" to userId, // 수정하려는 필드와 새로운 값
            "intro" to introduce, // 다른 필드와 값
            "profileImg" to imgUrl
        )

        collectionReference
            .update(fieldUpdates)
            .addOnSuccessListener {
                Log.d("profileedit", "updateProfile: $imgUrl")
                activityViewModel.user.userId = userId
                activityViewModel.user.intro = introduce
                activityViewModel.user.profileImg = imgUrl
                ApplicationClass.SharedPreferences.addProfileImg(imgUrl)

                mainActivity.getFeedList()
                // 시간 텀 이후에 메인 액티비티의 프래그먼트 변경 작업 수행
                mainActivity.changeFragment("profile")

            }
            .addOnFailureListener { error ->
                println("문서 필드 수정에 실패했습니다: $error")
            }
    }

    // firestore -> feeds에 해당 user의 각 feed에 있는 profileImg도 변경
    fun updateFeedProfileImg(imgUrl: String) {
        val db = FirebaseFirestore.getInstance().collection("feeds")
        val feedNumList = activityViewModel.user.feedNumList

        // 업데이트할 필드와 값을 가진 맵 생성
        val feedUpdates = hashMapOf<String, Any>(
            "userProfileImg" to imgUrl
        )

        for (feedId in feedNumList) {
            val feedDocRef = db.document(feedId)

            // 문서 업데이트 수행
            feedDocRef.update(feedUpdates)
                .addOnSuccessListener {
                    // 성공적으로 업데이트됨
                    Log.d("ProfileEditFragment", "updateFeedProfileImg: feed 업데이트 성공")
                }
                .addOnFailureListener { e ->
                    // 업데이트 실패
                    Log.d("ProfileEditFragment", "updateFeedProfileImg: feed 업데이트 실패")
                }
        }

    }

    // firestore -> ID 중복 체크
    suspend fun checkDuplicateId(documentId: String){
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("users") // user 정보 모두 가지고 옴
        collectionReference
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (documentSnapshot in querySnapshot) {
                    val user = documentSnapshot.toObject(User::class.java)
                    val userId = user.userId
                    val curId = ApplicationClass.SharedPreferences.getUserId()
                    Log.d("profileedit", "checkDuplicateId curId: ${curId}")
                    if(userId == documentId && userId != curId){
                        Log.d("profileedit", "checkDuplicateId: ${userId} ${documentId}")
                        check = true
                        break
                    }
                }

                if (check) {
                    binding.nicknameLayout.error = "ID 중복"
                }else{
//                    updateProfile(userId, introduce, activityViewModel.editImgUrl)
//                    putImage(activityViewModel.editImgUrl)
                    CoroutineScope(Dispatchers.Main).launch {
                        putImage(activityViewModel.editImgUrl)
                        delay(500) // 1초의 딜레이를 줌 (1000 밀리초)

                    }
                }
            }
            .addOnFailureListener { exception ->
                // 쿼리 실패 처리
                println("사용자 정보를 가져오는 중 오류가 발생했습니다: $exception")
            }


    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}