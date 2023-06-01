package com.mattam.hacha.src.main.profile


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.src.main.dto.Feed

private const val TAG = "MenuAdapter_싸피"
class ProfileMyPostAdpater(var feedList:MutableList<Feed>) :RecyclerView.Adapter<ProfileMyPostAdpater.MyPostHolder>(){

    inner class MyPostHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var itemImage = itemView.findViewById<ImageView>(R.id.item_img)

        fun bindInfo(product : Feed){
            Log.d(TAG, "bindInfo: ${product}")

            if(!product.imgList.isEmpty()) {
                Glide.with(itemView)
                    .load(product.imgList[0])
                    .into(itemImage)
            }
            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition, product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_list_item, parent, false)
        return MyPostHolder(view)
    }

    override fun onBindViewHolder(holder: MyPostHolder, position: Int) {
        holder.apply{
            bindInfo(feedList[position])
        }
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int, feed: Feed)
    }
//    //클릭리스너 선언
    lateinit var itemClickListner: ItemClickListener


}

