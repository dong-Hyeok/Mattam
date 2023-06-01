package com.mattam.hacha.src.main

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mattam.hacha.src.main.dto.Comment
import com.mattam.hacha.src.main.dto.Feed
import com.mattam.hacha.src.main.dto.Photo
import com.mattam.hacha.src.main.dto.User

class MainViewModel: ViewModel() {
    private val _addPhotoList = MutableLiveData<MutableList<Photo>>()
    val addPhotoList: LiveData<MutableList<Photo>> get() = _addPhotoList

    private val _galleryList = MutableLiveData<MutableList<Photo>>()
    val galleryList: LiveData<MutableList<Photo>> get() = _galleryList

    private val _feedList = MutableLiveData<MutableList<Feed>>()
    val feedList: LiveData<MutableList<Feed>> get() = _feedList

    private val _commentList = MutableLiveData<MutableList<Comment>>()
    val commentList: LiveData<MutableList<Comment>> get() = _commentList

    private val _markList = MutableLiveData<MutableList<Long>>()
    val markList: LiveData<MutableList<Long>> get() = _markList

    // markList
    fun initMarkList(markList:MutableList<Long>){
        _markList.value = markList
    }

    fun addMark(feedId:Long){
        _markList.value!!.add(feedId)
    }

    fun removeMark(feedId:Long){
        _markList.value!!.remove(feedId)
    }



    // FeedList
    fun addToAddFeedList(feed: Feed) {
        _feedList.value?.add(feed)
        _feedList.value = _feedList.value
    }

    fun clearFeedList(){
        _feedList.value = arrayListOf()
    }


    // commentList
    var selectFeedCommentList : MutableList<String> = arrayListOf() // 선택 feed의 commentId 리스트
    var selectCommentFeedId : Long = 0; // 해당 comment가 속한 feed의 id

    fun addToCommentList(comment: Comment) {
        _commentList.value?.add(comment)
        _commentList.value = _commentList.value
    }

    fun clearCommentList(){
        _commentList.value = arrayListOf()
    }

    fun removeComment(posi:Int){
        _commentList.value!!.removeAt(posi)
        _commentList.value = _commentList.value
    }

    fun addToAddPhotoList(photo: Photo) {
        _addPhotoList.value?.add(photo)
        _addPhotoList.value = _addPhotoList.value
    }

    fun removeFromAddPhotoList(photo: Photo){
        _addPhotoList.value!!.remove(photo)
        _addPhotoList.value = _addPhotoList.value
    }

    fun addToGalleryList(photo: Photo) {
        _galleryList.value?.add(photo)
        _galleryList.value = _galleryList.value
    }

    fun clearAddPhotoList() {
        _addPhotoList.value?.clear()
        _addPhotoList.value = _addPhotoList.value
    }

    fun clearGalleryList() {
        for(photo in _galleryList.value!!){
            photo.isSelected.value = false
        }
        _galleryList.value = _galleryList.value
    }

    fun emptyGalleryList(){
        _galleryList.value!!.clear()
    }

    var user: User = User() // 현재 로그인한 사용자의 정보
    var editImgUrl: String = "" // 현재 로그인한 사용자의 수정한 프로필 이미지




    //addFragment 내용
    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String>
        get() = _storeName

    private val _storeLocation = MutableLiveData<String>()
    val storeLocation: LiveData<String>
        get() = _storeLocation

    private val _storeTel = MutableLiveData<String>()
    val storeTel: LiveData<String>
        get() = _storeTel

    fun setDescription(value: String) {
        _description.value = value
    }
    fun setStoreName(value: String) {
        _storeName.value = value
    }
    fun setStoreLocation(value: String) {
        _storeLocation.value = value
    }
    fun setStoreTel(value: String) {
        _storeTel.value = value
    }

    private val _imgCnt = MutableLiveData<Int>()
    val imgCnt: LiveData<Int>
        get() = _imgCnt

    fun addImgCnt(){
        val currentCount = _imgCnt.value ?: 0 // 현재 카운트 값을 가져옵니다. 값이 null인 경우 0으로 설정합니다.
        _imgCnt.value = currentCount + 1
    }

    fun clearImgCnt(){
        _imgCnt.value = 0
    }

    //상세 정보 사용되는 feed
    lateinit var storeInfoFeed: Feed
    var modifySelected = false

    var searchModifySelected = false

    private val _myPostSelected = MutableLiveData<Boolean>()
    val myPostSelected: LiveData<Boolean>
        get() = _myPostSelected

    fun setMyPostSelected(boolean: Boolean){
        _myPostSelected.value = boolean
    }

    var modifyFromFragment: String = ""
    var fromModifyBtn: Boolean = false
    var fromCommentFragment: String = ""


    // 삭제 선택시 comment 정보
    lateinit var currentComment: Comment
    var currentCommentPosi = -1


    var searchFeedList : MutableList<Feed> = arrayListOf()
    var searchText: String = ""
    var fromSearch = false

    lateinit var viewModelLifecycleOwner: LifecycleOwner


    var editUserId = ""
    var editIntroduce = ""

    var lockRecyclerView = false

    var fromEditProfileFlag = false


    var preStoreInfoFragment = "homeFragment"
    var fromStoreInfo = 0 //삭제했을 때 온건지
    var selectedSearchFeedPosition = -1

    var isModify = false //수정했는지


    init {
        _addPhotoList.value = mutableListOf()
        _galleryList.value = mutableListOf()
        _feedList.value = mutableListOf()
        _commentList.value = mutableListOf()
        _description.value = ""
        _storeName.value = ""
        _storeLocation.value = ""
        _storeTel.value = ""
        _myPostSelected.value = true
        storeInfoFeed = Feed()
    }


}