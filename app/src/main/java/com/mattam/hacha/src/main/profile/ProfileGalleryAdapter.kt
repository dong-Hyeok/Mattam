package com.mattam.hacha.src.main.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.databinding.ItemAddGalleryBinding
import com.mattam.hacha.src.main.dto.Photo


class ProfileGalleryAdapter(var lifecycleOwner: LifecycleOwner, var galleryList:LiveData<MutableList<Photo>>) : RecyclerView.Adapter<ProfileGalleryAdapter.ProfileGlleryHolder>(){

    private lateinit var selectedPhoto: Photo
    private val maxSelectedCnt = 1
    private var selectedCnt = 0

    inner class ProfileGlleryHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
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

            checkbox.setOnClickListener {
                if(selectedCnt == maxSelectedCnt){ //이미 한 개 선택되어 있다면
                    selectedPhoto.isSelected.value = !selectedPhoto.isSelected.value!! //기존에 선택된 애 해제
                    selectedPhoto = product //이번에 선택한 놈이 선택됨!
                    product.isSelected.value = !product.isSelected.value!!
                }else{ //처음 선택하는 것이라면
                    product.isSelected.value = !product.isSelected.value!!
                    selectedPhoto = product
                    selectedCnt++
                }

            }


//            itemView.setOnClickListener{
//                itemClickListner.onClick(it, layoutPosition, productList[layoutPosition].id)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileGlleryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_gallery, parent, false)
        return ProfileGlleryHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileGlleryHolder, position: Int) {
        holder.apply{
            bindInfo(galleryList.value!![position])
        }
    }

    override fun getItemCount(): Int {
        return galleryList.value!!.size
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