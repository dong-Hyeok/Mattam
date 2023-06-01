package com.mattam.hacha.src.main.dto

data class User ( // user는 google 로그인 token으로 객체를 생성
    var userId: String,
    var intro: String,
    var profileImg: String,
    var feedNumList: MutableList<String>,
    var markList: MutableList<Long>,
    var commentList: MutableList<String>
){
    constructor(): this("","", "", arrayListOf(), arrayListOf(), arrayListOf())
}