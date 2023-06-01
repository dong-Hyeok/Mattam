package com.mattam.hacha.src.main.dto

data class Feed (
    var feedId: Long,
    var userId: String,
    var userProfileImg: String,
    var time: Long,
    var description: String,
    var likeCnt: Long,
    var mark: Boolean,
    var imgList: List<String>,
    var storeName: String,
    var storeLocation: String,
    var storeTel:  String,
    var commentList: MutableList<String>
) {
    constructor(): this(
        0,
        "",
        "",
        0,
        "",
        0,
        false,
        arrayListOf(),
        "",
        "",
        "",
        arrayListOf())
}
