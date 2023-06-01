package com.mattam.hacha.src.main.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.databinding.ItemAddGalleryBinding
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Photo


class GalleryAdapter(var lifecycleOwner: LifecycleOwner, var activityViewModel: MainViewModel) : RecyclerView.Adapter<GalleryAdapter.GalleryHolder>(){

    inner class GalleryHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var itemImage = itemView.findViewById<ImageView>(R.id.item_img)
        var checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)

        fun bindInfo(product : Photo){

            val binding: ItemAddGalleryBinding = DataBindingUtil.bind(itemView)!!
            product.isSelected.observe(lifecycleOwner) { isSelected ->
                // isSelected 값이 변경되었을 때 실행되는 코드
                binding.photo = product
            }

            Glide.with(itemView)
                .load(product.imgUrl)
                .into(itemImage)


            checkbox.setOnClickListener {//체크박스 선택시 값 변경
                product.isSelected.value = !product.isSelected.value!!
            }


//            itemView.setOnClickListener{
//                itemClickListner.onClick(it, layoutPosition, productList[layoutPosition].id)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryAdapter.GalleryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_gallery, parent, false)
        return GalleryHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryHolder, position: Int) {
        holder.apply{
            bindInfo(activityViewModel.galleryList.value!!.get(position))
        }
    }

    override fun getItemCount(): Int {
        return activityViewModel.galleryList.value!!.size
    }

//    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
//    interface ItemClickListener {
//        fun onClick(view: View,  position: Int, productId:Int)
//    }
//    //클릭리스너 선언
//    private lateinit var itemClickListner: ItemClickListener
//    //클릭리스너 등록 매소드
//    fun setItemClickListener(itemClickListener: ItemClickListener) {
//        this.itemClickListner = itemClickListener
//    }
}