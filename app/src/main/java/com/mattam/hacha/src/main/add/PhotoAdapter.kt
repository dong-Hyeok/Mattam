package com.mattam.hacha.src.main.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.src.main.MainViewModel
import com.mattam.hacha.src.main.dto.Photo

class PhotoAdapter(val viewModel: MainViewModel, val fromFragment: String) : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>(){

    private var photoList: List<Photo> = emptyList()
    fun submitList(newList: List<Photo>) {
        photoList = newList
        notifyDataSetChanged()
    }

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var itemImage = itemView.findViewById<ImageView>(R.id.item_img)
        var deletePhotoBtn = itemView.findViewById<ImageView>(R.id.delete_photo_btn)

        fun bindInfo(photo : Photo){
            Glide.with(itemView)
                .load(photo.imgUrl)
                .into(itemImage)

            deletePhotoBtn.setOnClickListener {
                photo.isSelected.value = false
                viewModel.removeFromAddPhotoList(photo)
            }

            if(fromFragment == "storeInfoFragment"){
                deletePhotoBtn.visibility = View.GONE
            }else{
                deletePhotoBtn.visibility = View.VISIBLE
            }

//            itemView.setOnClickListener{
//                itemClickListner.onClick(it, layoutPosition, productList[layoutPosition].id)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_photo, parent, false)
        return PhotoHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        holder.apply{
//            bindInfo(viewModel.addPhotoList.value!!.get(position))

            bindInfo(photoList[position])
        }
    }

    override fun getItemCount(): Int {
//        return viewModel.addPhotoList.value!!.size
        return photoList.size
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