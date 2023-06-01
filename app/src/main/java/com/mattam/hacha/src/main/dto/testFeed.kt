package com.mattam.hacha.src.main.dto

data class testFeed (
    val name: String,
    val userId: String,
    val time: Long,
    val img: String,
    val imgList: List<String>,
    val comment: String
) {
    constructor(): this("", "", 0, "", arrayListOf(), "")
}