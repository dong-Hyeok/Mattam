package com.mattam.hacha.src.main.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.src.main.dto.Feed

class SearchAdapter(var feedList:MutableList<Feed>) : RecyclerView.Adapter<SearchAdapter.SearchHolder>(){

    inner class SearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val feedImg = itemView.findViewById<ImageView>(R.id.feed_img)
        val userId = itemView.findViewById<TextView>(R.id.userid)
        fun bindInfo(product : Feed){

            if(!product.imgList.isEmpty()) {
                Glide.with(itemView)
                    .load(product.imgList[0])
                    .into(feedImg)
            }

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition, product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.apply{
            bindInfo(feedList[position])
        }
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View, position: Int, feed: Feed)
    }
    //    //클릭리스너 선언
    lateinit var itemClickListner: ItemClickListener


}

