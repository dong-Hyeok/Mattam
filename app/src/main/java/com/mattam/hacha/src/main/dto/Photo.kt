package com.mattam.hacha.src.main.dto

import androidx.lifecycle.MutableLiveData

//class Photo {
//    var imgUrl: String =""
//    var isSelected: Boolean = false
//
//}
class Photo {
    var imgUrl: String =""
    var isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

}