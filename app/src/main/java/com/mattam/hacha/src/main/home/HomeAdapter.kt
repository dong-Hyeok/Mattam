package com.mattam.hacha.src.main.home

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.mattam.hacha.R
import com.mattam.hacha.databinding.ItemHomeRecyclerviewBinding
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Feed
import java.util.concurrent.TimeUnit


class HomeAdapter(val lifecycleOwner: LifecycleOwner, val activityViewModel: MainViewModel, val mainActivity: MainActivity, var feedInfo : LiveData<MutableList<Feed>>)
    : RecyclerView.Adapter<HomeAdapter.CustomViewHolder>(){

//    companion object StringComparator : DiffUtil.ItemCallback<String>(){
//        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
//            return oldItem.hashCode() == newItem.hashCode()
//        }
//        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
//            return oldItem == newItem
//        }
//    }


    inner class CustomViewHolder(binding: ItemHomeRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root){

        private val userImg = binding.profileImg
        private val userId = binding.userid
        private val addTime = binding.addTime
        private val viewPager = binding.viewPager
        private val description = binding.descriptionTv
        private val seeStore = binding.seeStore
        private val tabLayout = binding.tabMainBanner
        private val bookmark = binding.bookmarkImg
        private val commentImg = binding.commentImg
        fun bindInfo(feed: Feed){
            Glide.with(itemView.context)
//                .load(Uri.parse(feed.userProfileImg))
                .load(feed.userProfileImg)
                .circleCrop()
                .into(userImg)

            userId.text = feed.userId

            description.text = feed.description


            // 시간 세팅
            val currentTimeMillis = System.currentTimeMillis()
            val timeDifMillis = currentTimeMillis-feed.time

            if(timeDifMillis >= TimeUnit.DAYS.toMillis(1)){
                val pastTime = TimeUnit.MILLISECONDS.toDays(currentTimeMillis-feed.time)
                addTime.text = "${pastTime}일 전"
            }
            else if (timeDifMillis >= TimeUnit.HOURS.toMillis(1)) {
                val pastTime = TimeUnit.MILLISECONDS.toHours(currentTimeMillis-feed.time)
                addTime.text = "${pastTime}시간 전"
            }
            else {
                val pastTime = TimeUnit.MILLISECONDS.toMinutes(currentTimeMillis-feed.time)
                addTime.text = "${pastTime}분 전"
            }

            // mark 표시
            activityViewModel.markList.observe(lifecycleOwner){
                if(it.contains(feed.feedId)){
                    bookmark.setBackgroundResource(R.drawable.selected_bookmark)
                }else{
                    bookmark.setBackgroundResource(R.drawable.unselected_bookmark)
                }
            }


            val pagerAdapter = ViewPagerAdapter(feed.imgList)
            viewPager.adapter = pagerAdapter


            TabLayoutMediator(tabLayout,viewPager){tab, postition ->
                viewPager.setCurrentItem(tab.position)
            }.attach()

            seeStore.setOnClickListener {
                activityViewModel.storeInfoFeed = feed
                activityViewModel.setDescription(feed.description)
                activityViewModel.setStoreName(feed.storeName)
                activityViewModel.setStoreLocation(feed.storeLocation)
                activityViewModel.setStoreTel(feed.storeTel)
                activityViewModel.modifyFromFragment = "homeFragment"
                mainActivity.changeFragment("storeInfo")
            }

            bookmark.setOnClickListener {
                bookMarkClickListener.onClick(it as ImageView, layoutPosition, feed)
            }

            commentImg.setOnClickListener {
                commentClickListener.onClick(layoutPosition, feed.feedId)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding : ItemHomeRecyclerviewBinding = ItemHomeRecyclerviewBinding.inflate(LayoutInflater.from(parent.context))
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bindInfo(feedInfo.value!![position])
    }

    override fun getItemCount(): Int {
        return feedInfo.value!!.size
    }

    interface BookMarkClickListener {
        fun onClick(view: ImageView,  position: Int, feed: Feed)
    }
    //    //클릭리스너 선언
    lateinit var bookMarkClickListener: BookMarkClickListener


    interface CommentClickListener {
        fun onClick(position: Int, feedId: Long)
    }
    //    //클릭리스너 선언
    lateinit var commentClickListener: CommentClickListener

    interface MenuClickListener {
        fun onClick(position: Int, feedId: Long)
    }
    //    //클릭리스너 선언
    lateinit var menuClickListener: MenuClickListener

}


// viewPager2 는 RecyclerViewAdapter를 사용해야 함!!!
// viewPager1 은 PagerAdapter를 사용하면 됨!!!
class ViewPagerAdapter(private val imageList: List<String>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_view: ImageView = itemView.findViewById(com.mattam.hacha.R.id.viewpager_item)
        fun bind(imageUrl: String) {
            Log.d("viewPager", "bind: ${imageUrl}")
            Glide.with(itemView.context)
                .load(Uri.parse(imageUrl))
                .placeholder(R.drawable.mattam_white3) // 로딩 중에 표시할 이미지
                .centerCrop()
                .into(item_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.mattam.hacha.R.layout.item_viewpager, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageList[position]
        holder.bind(imageUrl)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

}





