package com.mattam.hacha.src.main.dto

data class Comment (
        val feedId : Long,
        val fromToken: String, // tokenId
        var fromUserId: String,
        var fromUserImg: String,
        var message: String,
        var time: Long,
){
        constructor(): this(0,"", "", "","", 0)
}