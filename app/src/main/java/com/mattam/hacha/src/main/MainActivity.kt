package com.mattam.hacha.src.main

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.mattam.hacha.R
import com.mattam.hacha.databinding.ActivityMainBinding
import com.mattam.hacha.src.main.add.AddFragment
import com.mattam.hacha.src.main.add.GalleryFragment
import com.mattam.hacha.src.main.add.ProfileGalleryFragment
import com.mattam.hacha.src.main.comment.CommentFragment
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.hacha.src.main.dto.Photo
import com.mattam.hacha.src.main.dto.User
import com.mattam.hacha.src.main.dto.testFeed
import com.mattam.hacha.src.main.home.HomeFragment
import com.mattam.hacha.src.main.home.StoreInfoFragment
import com.mattam.hacha.src.main.profile.ProfileEditFragment
import com.mattam.hacha.src.main.profile.ProfileFragment
import com.mattam.hacha.src.main.search.SearchFragment
import com.mattam.hacha.util.LoadingFragment
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    lateinit var userInfo: MutableList<testFeed>
    lateinit var userData: MutableList<User>
    lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel:MainViewModel by viewModels()
    var previousTabId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("하동혁", "onCreate:초기 유저값 ${viewModel.user}")


        requestStoragePermission()
        bottomNavigationView = binding.bottomNavigation

        bottomNavigationView.isEnabled = false

        bottomNavigationView.apply {
            setOnItemSelectedListener { item ->
                if (!viewModel.lockRecyclerView) {
                    when (item.itemId) {
                        R.id.navigation_page_home -> {
                            changeFragment("home")
                            true
                        }
                        R.id.navigation_page_search -> {
                            changeFragment("search")
                            true
                        }
                        R.id.navigation_page_add -> {
                            viewModel.clearAddPhotoList()
                            viewModel.clearGalleryList()
                            changeFragment("add")
                            true
                        }
                        R.id.navigation_page_person -> {
                            changeFragment("profile")
                            true
                        }
                        else -> false
                    }
                }
                true
            }
        }


        CoroutineScope(Dispatchers.Main).launch {
            getUserInfo()
        }


    }

    // User 데이터 얻기
    fun getUserInfo(){
        val db = FirebaseFirestore.getInstance()
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        Log.d("MainActivity", "onCreate: ${userToken}")

        // User 데이터 받기
        val usersCollection = db.collection("users").document("${userToken}")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                viewModel.user = querySnapshot.toObject(User::class.java)!!
                Log.d("MainActivity", "onCreate userInfo: ${viewModel.user}")
                viewModel.initMarkList(viewModel.user.markList)
                // 초기 feed정보 불러오기
                getFeedList()

            }
            .addOnFailureListener { exception ->
                // 오류 처리
                Log.d("FireStore", "onCreate: 받아오기 실패")
            }
    }



    // User 데이터 얻은 후 Feed 정보 불러오기
    fun getFeedList(){
        viewModel.clearFeedList()
        Log.d("MainActivity", "getFeedInfo!!: getFeedList() 호출 ")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)


        // FireStore START -----------------------------------------------------
        val db = FirebaseFirestore.getInstance()
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        Log.d("MainActivity", "onCreate: ${userToken}")

        /* ------------- Feeds 정보 받기 (가장 최근에 등록된 것 부터 10개씩) ------------- */
        val feedsCollectionRef = db.collection("feeds")

        // 초기 쿼리
        var query = feedsCollectionRef
            .orderBy("time", Query.Direction.DESCENDING)
//            .limit(5)

        // 이미 10개의 문서를 가져왔다면, 마지막으로 가져온 문서의 time 값을 이용하여 쿼리를 조정
//        viewModel.lastDocumentSnapshot?.let { snapshot ->
//            Log.d("MainActivity", "getFeedInfo!!: lastDocumentSnapshot 쿼리 수행")
//            query = feedsCollectionRef
//                .orderBy("time", Query.Direction.DESCENDING)
//                .startAfter(snapshot)
//                .limit(5)
//        }

        query.get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val feedData = document.data

                    if(feedData != null){
                        val feedId = feedData["feedId"]!! as Long
                        val userId = feedData["userId"]!! as String
                        val userProfileImg = (feedData["userProfileImg"] ?: "") as String
                        val time = feedData["time"]!! as Long
                        val description = (feedData["description"] ?: "") as String
                        val likeCnt = (feedData["likeCnt"] ?: 0) as Long
                        val mark = (feedData["mark"] ?: false) as Boolean
                        val imgList = feedData["imgList"]!! as List<String>
                        val storeName = feedData["storeName"]!! as String
                        val storeLocation = feedData["storeLocation"]!! as String
                        val storeTel = feedData["storeTel"]!! as String
                        val commentList = feedData["commentList"] as MutableList<String>

                        val feed = Feed(
                            feedId,
                            userId,
                            userProfileImg,
                            time,
                            description,
                            likeCnt,
                            mark,
                            imgList,
                            storeName,
                            storeLocation,
                            storeTel,
                            commentList
                        )
                        Log.d("MainActivity", "getFeedInfo chadong: ${feed}")
                        viewModel.addToAddFeedList(feed)

                    }
                }

                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)


                Log.d("차선호", "getFeedList에서 current: $currentFragment")
                Log.d("차선호", "getFeedList에서 isModify: ${viewModel.isModify}")
                if(currentFragment is StoreInfoFragment) {
                    if (viewModel.isModify){
                        viewModel.isModify = false
                    }else if (viewModel.fromStoreInfo == 0) { //홈에서 삭제했을 경우
                        changeFragment("home")
                    } else if (viewModel.fromStoreInfo == 1) { //프로필에서 삭제했을 경우
                        changeFragment("profile")
                    } else if (viewModel.fromStoreInfo == 2){
                        changeFragment("search")
                    }
                }else if(!viewModel.fromEditProfileFlag && !viewModel.searchModifySelected) {
                    changeFragment("home")
                }

                viewModel.fromEditProfileFlag = false
//                viewModel.fromStoreInfo = false
//                if(currentFragment is HomeFragment)(
//                    hideGetFeedLoading()
//
//                )
                if(currentFragment is HomeFragment){
                    // 바텀 네비 활성
                    hideGetFeedLoading()
                }
                if(viewModel.searchModifySelected) viewModel.searchModifySelected = false



                // 마지막으로 가져온 문서의 스냅샷을 저장합니다.
//                if (snapshot.documents.isNotEmpty()) {
//                    viewModel.lastDocumentSnapshot = snapshot.documents.last()
//                    Log.d("MainActivity", "getFeedList!!: lastDocumentSnapshot 마지막 스냅샷 저장 ${viewModel.lastDocumentSnapshot}")
//                }
            }
            .addOnFailureListener { exception ->
                // 오류 처리
                Log.e("Firestore", "쿼리 중 오류 발생: $exception")
            }
        /* ---------------------------------------------------------------------------------------- */
    }


    fun requestStoragePermission() {
        viewModel.emptyGalleryList()
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    // 권한이 허용된 경우 다음 작업을 수행할 수 있습니다.
                    //갤러리 이미지--------------------맨 처음에 메인에서 한 번만 불러서 viewModel에 저장
                    val projection = arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA
                    )

                    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                    val cursor = contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder
                    )

                    cursor?.use { cursor ->
                        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        val nameColumnIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                        val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                        while (cursor.moveToNext()) {
                            val imageId = cursor.getLong(idColumnIndex)
                            val imageName = cursor.getString(nameColumnIndex)
                            val imageData = cursor.getString(dataColumnIndex)
                            Log.d("gallery", "loadPhotos: $imageName")
                            // 이미지 데이터를 사용하여 필요한 작업 수행
                            viewModel.addToGalleryList(Photo().apply {
                                imgUrl = imageData
                                isSelected.value = false
                            })
                        }
                    }
                    if(supportFragmentManager.findFragmentById(R.id.frame_layout_main) is ProfileEditFragment ){
                        changeFragment("profileGallery")
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // 권한이 거부된 경우 사용자에게 알림을 표시하거나 다른 조치를 취해야 합니다.
//                    Toast.makeText(this@MainActivity,"권한 요청 해달라고!", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("권한을 허용해주세요.")
            .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }



    fun changeFragment(str: String){
        when(str) {
            "loading" -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.frame_layout_main, LoadingFragment())
                    .commit()
                bottomNavigationView . visibility = View . GONE
            }

            "home" -> {
                viewModel.fromSearch = false
                viewModel.searchText = ""
                viewModel.searchFeedList = arrayListOf()
                bottomNavigationView.menu.findItem(R.id.navigation_page_home)?.isChecked = true
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, HomeFragment())
                    .commit()
                bottomNavigationView . visibility = View.VISIBLE
            }

            "search" -> {
                viewModel.fromSearch = true
                viewModel.fromCommentFragment = "searchFragment"
                viewModel.modifyFromFragment = "searchFragment"
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, SearchFragment())
                    .commit()
                bottomNavigationView.visibility = View.VISIBLE
            }

            "add" -> {
                if(!viewModel.fromSearch) {
                    viewModel.searchText = ""
                    viewModel.searchFeedList = arrayListOf()
                }else{
                    viewModel.fromSearch = false
                }
                val addFragment = AddFragment()
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)
                if(currentFragment !is GalleryFragment) requestStoragePermission()
                if(currentFragment is StoreInfoFragment){
                    val bundle = Bundle()
                    if(!viewModel.fromModifyBtn){
                        viewModel.setDescription("")
                        viewModel.setStoreName("")
                        viewModel.setStoreLocation("")
                        viewModel.setStoreTel("")
                    }
                    if(viewModel.modifySelected) {
                        viewModel.searchModifySelected = true
                        bundle.putString("from", "storeInfoFragment")
                        viewModel.modifySelected = false
                    } // 인자 이름과 값을 설정합니다.
                    else bundle.putString("from", "homeFragment") // 인자 이름과 값을 설정합니다.
                    addFragment.arguments = bundle
                }else if(currentFragment is GalleryFragment){
                    val bundle = Bundle()
                    bundle.putString("from", "galleryFragment") // 인자 이름과 값을 설정합니다.
                    addFragment.arguments = bundle
                } else{
                    viewModel.setDescription("")
                    viewModel.setStoreName("")
                    viewModel.setStoreLocation("")
                    viewModel.setStoreTel("")
                    viewModel.clearImgCnt()
                    val bundle = Bundle()
                    bundle.putString("from", "homeFragment") // 인자 이름과 값을 설정합니다.
                    addFragment.arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, addFragment)
                    .commit()
                bottomNavigationView.visibility = View.GONE
            }

            "profile" -> {
                viewModel.fromSearch = false
                viewModel.searchText = ""
                viewModel.searchFeedList = arrayListOf()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, ProfileFragment())
                    .commit()
                bottomNavigationView.visibility = View.VISIBLE
            }

            "profileEdit" -> {

                val profileEditFragment = ProfileEditFragment()
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)
                if(currentFragment is ProfileGalleryFragment){
                    val bundle = Bundle()
                    bundle.putString("from", "profileGalleryFragment") // 인자 이름과 값을 설정합니다.
                    profileEditFragment.arguments = bundle
                }else if(currentFragment is ProfileFragment){
                    val bundle = Bundle()
                    bundle.putString("from", "profileFragment") // 인자 이름과 값을 설정합니다.
                    profileEditFragment.arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, profileEditFragment)
                    .commit()
                bottomNavigationView.visibility = View.GONE
            }

            "storeInfo" -> {
                var storeInfoFragment = StoreInfoFragment()
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)
                val bundle = Bundle()
                if(currentFragment is ProfileFragment){
                    bundle.putString("from", "profileFragment") // 인자 이름과 값을 설정합니다.
                    viewModel.preStoreInfoFragment = "profileFragment"
                    storeInfoFragment.arguments = bundle
                } else if(currentFragment is HomeFragment){
                    bundle.putString("from", "homeFragment") // 인자 이름과 값을 설정합니다.
                    viewModel.preStoreInfoFragment = "homeFragment"
                    storeInfoFragment.arguments = bundle
                }else if(currentFragment is AddFragment){
                    bundle.putString("from", "addFragment") // 인자 이름과 값을 설정합니다.
                    storeInfoFragment.arguments = bundle
                }else if(currentFragment is CommentFragment){
                    bundle.putString("from", "commentFragment") // 인자 이름과 값을 설정합니다.
                    storeInfoFragment.arguments = bundle
                }else if(currentFragment is SearchFragment){
                    bundle.putString("from", "searchFragment") // 인자 이름과 값을 설정합니다.
                    viewModel.preStoreInfoFragment = "searchFragment"
                    storeInfoFragment.arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, storeInfoFragment)
                        .commit()
                bottomNavigationView.visibility = View.VISIBLE
            }

            "gallery" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, GalleryFragment())
                    .commit()
                bottomNavigationView.visibility = View.GONE
            }

            "profileGallery" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, ProfileGalleryFragment())
                    .commit()
                bottomNavigationView . visibility = View . GONE
            }

            "comments" -> {
                var commentFragment = CommentFragment()
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout_main)
                if(currentFragment is StoreInfoFragment){
                    val bundle = Bundle()
                    bundle.putString("from", "storeInfoFragment") // 인자 이름과 값을 설정합니다.
                    commentFragment.arguments = bundle
                }else if(currentFragment is HomeFragment){
                    val bundle = Bundle()
                    bundle.putString("from", "homeFragment") // 인자 이름과 값을 설정합니다.
                    commentFragment.arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_main, commentFragment)
                    .commit()
                bottomNavigationView . visibility = View . GONE
            }


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    fun showGetFeedLoading(){
        binding.loadingDialog.speed = 1.5f
        binding.loadingDialog.visibility = View.VISIBLE
        binding.loadingDialog.playAnimation()
    }

    fun hideGetFeedLoading(){
        binding.loadingDialog.cancelAnimation()
        binding.loadingDialog.visibility = View.GONE
        viewModel.lockRecyclerView = false
    }




}