package com.mattam.hacha.src.main.add

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mattam.hacha.R
import com.mattam.hacha.databinding.FragmentAddBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.hacha.src.main.dto.Photo
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddFragment : BaseFragment<FragmentAddBinding>(FragmentAddBinding::bind, R.layout.fragment_add)  {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    private val activityViewModel: MainViewModel by activityViewModels()
    lateinit var photoAdapter: PhotoAdapter
    lateinit var fromFragment: String

    var imgList: MutableList<String> = arrayListOf()
    var recentFeedId :Long = 0 // 가장 최근 feedId
    lateinit var addPhotoObserver: Unit



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments //어느 프래그먼트에서 온지 가지고 있는 bundle
        fromFragment = bundle!!.getString("from")!!

        photoAdapter = PhotoAdapter(activityViewModel, fromFragment!!)
        activityViewModel.addPhotoList.observe(viewLifecycleOwner, Observer {
            photoAdapter.submitList(it)
        })


        activityViewModel.fromModifyBtn = false

        binding.apply {

            telEt.inputType = InputType.TYPE_CLASS_NUMBER

            if(fromFragment == "storeInfoFragment"){ //가게 상세정보에서 넘어왔다면
                // done 대신에 modify 버튼 보이게
                // addPhotoList를 infoFeed의 photoList로 가져와야함
                shareBtn.visibility = View.GONE
                modifyBtn.visibility = View.VISIBLE
                addPhotoImg.visibility = View.GONE

                for(imgUrl in activityViewModel.storeInfoFeed.imgList){
                    activityViewModel.addToAddPhotoList(Photo().apply {
                        this.imgUrl = imgUrl
                        this.isSelected.value = true
                    })
                }

                binding.apply {
                    descriptionEt.setText(activityViewModel.storeInfoFeed.description)
                    storeEt.setText(activityViewModel.storeInfoFeed.storeName)
                    locationEt.setText(activityViewModel.storeInfoFeed.storeLocation)
                    telEt.setText(activityViewModel.storeInfoFeed.storeTel)
                }

                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("storeInfo")
                    }
                })

            }else if(fromFragment == "galleryFragment"){
                modifyBtn.visibility = View.GONE
                shareBtn.visibility = View.VISIBLE
                addPhotoImg.visibility = View.VISIBLE
                binding.apply {
                    descriptionEt.setText(activityViewModel.description.value)
                    storeEt.setText(activityViewModel.storeName.value)
                    locationEt.setText(activityViewModel.storeLocation.value)
                    telEt.setText(activityViewModel.storeTel.value)
                }
                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("home")
                    }
                })
            }
            else if(fromFragment == "homeFragment") { //다른 곳에서 넘어온 거
                modifyBtn.visibility = View.GONE
                shareBtn.visibility = View.VISIBLE
                addPhotoImg.visibility = View.VISIBLE

                binding.apply {
                    descriptionEt.setText(activityViewModel.description.value!!)
                    storeEt.setText(activityViewModel.storeName.value!!)
                    locationEt.setText(activityViewModel.storeLocation.value!!)
                    telEt.setText(activityViewModel.storeTel.value!!)
                }

                mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mainActivity.changeFragment("home")
                    }
                })
            }


            addPhotoImg.setOnClickListener {
                //사진 고르는 프래그먼트로 이동
                if (checkStoragePermission()) {
                    // 이미 권한이 허용된 경우
                    mainActivity.changeFragment("gallery")
                } else {
                    // 권한 요청 필요
                    // TedPermission.create().setPermissionListener() 등의 코드 추가
                    mainActivity.requestStoragePermission()
                }
            }

            photoRecyclerview.apply {
                layoutManager = LinearLayoutManager(mainActivity,
                    LinearLayoutManager.HORIZONTAL,false)
                adapter = photoAdapter
            }

            //입력 정보 유지하기//////////////////////////////////////////////////
            if(activityViewModel.description.value!=null && fromFragment == "storeInfoFragment") descriptionEt.setText(activityViewModel.description.value!!)
            descriptionEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    activityViewModel.setDescription(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            if(activityViewModel.storeName.value!=null && fromFragment == "storeInfoFragment") storeEt.setText(activityViewModel.storeName.value!!)
            storeEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    activityViewModel.setStoreName(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            if(activityViewModel.storeLocation.value!=null && fromFragment == "storeInfoFragment") locationEt.setText(activityViewModel.storeLocation.value!!)
            locationEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    activityViewModel.setStoreLocation(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            if(activityViewModel.storeTel.value!=null && fromFragment == "storeInfoFragment") telEt.setText(activityViewModel.storeTel.value!!)
            telEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //before이 줄었을 때 1
                    var str = s.toString()
                    if(count==1 && str.length==4){
                        val formmatStr = "${str.substring(0, str.length - 1)}-${str[str.length-1]}"
                        telEt.setText(formmatStr)
                        telEt.setSelection(formmatStr.length)
                        activityViewModel.setStoreTel(formmatStr)
                    }
                    else if(before==1 && str.length==4) {
                        val formmatStr = str.substring(0, str.length - 1)
                        telEt.setText(formmatStr)
                        telEt.setSelection(formmatStr.length)
                        activityViewModel.setStoreTel(formmatStr)
                    }
                    else if(count==1 && str.length==9){
                        val formmatStr = "${str.substring(0, str.length - 1)}-${str[str.length-1]}"
                        telEt.setText(formmatStr)
                        telEt.setSelection(formmatStr.length)
                        activityViewModel.setStoreTel(formmatStr)
                    }
                    else if(before==1 && str.length==9){
                        val formmatStr = str.substring(0, str.length - 1)
                        telEt.setText(formmatStr)
                        telEt.setSelection(formmatStr.length)
                        activityViewModel.setStoreTel(formmatStr)
                    }
                    activityViewModel.setStoreTel(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })


            shareBtn.setOnClickListener {
                // 키보드 숨기기
                val inputMethodManager = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                scrollview.requestFocus(View.FOCUS_UP)
                inputMethodManager.hideSoftInputFromWindow(scrollview.windowToken, 0)


                if(activityViewModel.storeLocation.value == "" || activityViewModel.storeName.value == "" || activityViewModel.addPhotoList.value!!.size == 0 || activityViewModel.storeTel.value == ""){
                    showCustomToast("입력 양식을 완성해 주세요!")
                    telEt.clearFocus()
                }
                else {

                    // 가장 최근에 등록된 feedId 가지고 옴
                    CoroutineScope(Dispatchers.Main).launch {
                        mainActivity.changeFragment("loading")
                        getRecentFeed()
                        //imgList가 이미지 리스트
                        for (photo in activityViewModel.addPhotoList.value!!) {
                            putImage(photo.imgUrl)
                        }
                        activityViewModel.imgCnt.observe(viewLifecycleOwner) { count ->
                            if (count == activityViewModel.addPhotoList.value!!.size) {
                                val addFeedInfo = Feed(
                                    recentFeedId + 1,
                                    activityViewModel.user.userId,
                                    activityViewModel.user.profileImg,
                                    System.currentTimeMillis(),
                                    activityViewModel.description.value!!,
                                    0,
                                    false,
                                    imgList,
                                    activityViewModel.storeName.value!!,
                                    activityViewModel.storeLocation.value!!,
                                    activityViewModel.storeTel.value!!,
                                    arrayListOf()
                                )

                                activityViewModel.addToAddFeedList(addFeedInfo)
                                addNewPost(addFeedInfo)


                            }
                        }
                    }
                }

            }

            modifyBtn.setOnClickListener {
                // 키보드 숨기기
                val inputMethodManager = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                scrollview.requestFocus(View.FOCUS_UP)
                inputMethodManager.hideSoftInputFromWindow(scrollview.windowToken, 0)

                if(activityViewModel.storeLocation.value == "" || activityViewModel.storeName.value == "" || activityViewModel.addPhotoList.value!!.size == 0 || activityViewModel.storeTel.value == ""){
                    showCustomToast("입력 양식을 완성해 주세요!")
                    telEt.clearFocus()
                }else {
                    //여기선 수정 데이터는 똑같이 가져오고 fireStroe 상에서 뒤집어 써지기만 하면 됨 새로 추가하는 게 아니라
                    Log.d("AddFragment", "feedUpdate: modifyBtn clicked ")
                    feedUpdate()
                }
            }

            backImg.setOnClickListener {
                if(fromFragment == "storeInfoFragment"){
                    mainActivity.changeFragment("storeInfo")
                }else if(fromFragment == "homeFragment"){
                    mainActivity.changeFragment("home")
                }
            }

        }

    }



    // feed 수정
    private fun feedUpdate(){
        val updateFeedId = activityViewModel.storeInfoFeed.feedId
        Log.d("AddFragment", "feedUpdate: id ${updateFeedId}")
        val db = FirebaseFirestore.getInstance().collection("feeds").document("feed${updateFeedId}")

        val fieldUpdates = hashMapOf<String, Any>(
            "description" to activityViewModel.description.value!!,
            "storeName" to activityViewModel.storeName.value!!,
            "storeLocation" to activityViewModel.storeLocation.value!!,
            "storeTel" to activityViewModel.storeTel.value!!,
        )
        
        db.update(fieldUpdates)
            .addOnSuccessListener {
                Log.d("AddFragment", "feedUpdate: 성공")
                activityViewModel.isModify = true
                mainActivity.getFeedList()
                activityViewModel.setDescription(activityViewModel.description.value!!)
                activityViewModel.setStoreName(activityViewModel.storeName.value!!)
                activityViewModel.setStoreLocation(activityViewModel.storeLocation.value!!)
                activityViewModel.setStoreTel(activityViewModel.storeTel.value!!)
                activityViewModel.storeInfoFeed.description = activityViewModel.description.value!!
                activityViewModel.storeInfoFeed.storeName = activityViewModel.storeName.value!!
                activityViewModel.storeInfoFeed.storeLocation = activityViewModel.storeLocation.value!!
                activityViewModel.storeInfoFeed.storeTel = activityViewModel.storeTel.value!!
                mainActivity.changeFragment("storeInfo")
            }
            .addOnFailureListener {
                Log.d("AddFragment", "feedUpdate: 실패")
            }

    }




    // storage에 img를 저장
    private fun putImage(imgUrl:String){
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
                imgList.add(downloadUri.toString())
                activityViewModel.addImgCnt()
            } else {
                // 업로드 실패 시 처리
                Log.d("AddFragment", "putImage 업로드 실패 ")
            }
        }
    }


    // 가장 최근에 등록된 feed 번호를 얻기
    private fun getRecentFeed(){
        val db = FirebaseFirestore.getInstance().collection("feeds")

        db.orderBy("time", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val recentFeed = doc.toObject(Feed::class.java)

                    // 가져온 데이터에 대한 처리
                    Log.d("AddFragment", "getRecentFeed : ${recentFeed}")
                    recentFeedId = recentFeed!!.feedId

                } else {
                    Log.d("AddFragment", "피드가 없습니다.")
                }
            }
            .addOnFailureListener { error ->
                Log.e("AddFragment", "데이터를 가져오는 도중 오류가 발생했습니다.", error)
            }
    }



    // 1. firestore에 new Post 데이터 저장
    // 2. users - user의 feedList에 feed 번호 등록
    private fun addNewPost(newFeed:Feed){
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        val feedsRef = FirebaseFirestore.getInstance().collection("feeds").document("feed${recentFeedId+1}")
        val userInfo =  FirebaseFirestore.getInstance().collection("users").document(userToken)

        activityViewModel.user.feedNumList.add("feed${recentFeedId+1}")
        // Feed 추가
        feedsRef.set(newFeed)
            .addOnSuccessListener { documentReference ->
                mainActivity.getFeedList()
                activityViewModel.setDescription("")
                activityViewModel.setStoreName("")
                activityViewModel.setStoreLocation("")
                activityViewModel.setStoreTel("")
                activityViewModel.clearImgCnt()
                mainActivity.changeFragment("home")
                Log.d("AddFragment", "새로운 피드가 성공적으로 추가되었습니다.")
            }
            .addOnFailureListener { error ->
                Log.e("AddFragment", "피드 추가 중 오류가 발생했습니다.", error)
            }

        // Feed ID 추가
        userInfo.update("feedNumList", FieldValue.arrayUnion("feed${recentFeedId+1}"))
            .addOnSuccessListener {
                Log.d("AddFragment", "새로운 이미지Id가 성공적으로 추가되었습니다.")
            }
            .addOnFailureListener { error ->
                Log.e("AddFragment", "이미지Id 추가 중 오류가 발생했습니다.", error)
            }
    }

    private fun checkStoragePermission(): Boolean {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val result = ContextCompat.checkSelfPermission(mainActivity, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}