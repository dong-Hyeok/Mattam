package com.mattam.hacha.src.main.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mattam.hacha.R
import com.mattam.hacha.src.main.dto.Comment
import java.util.concurrent.TimeUnit

class CommentAdapter() : RecyclerView.Adapter<CommentAdapter.CommentHolder>(){//viewModel 받은 이유는 나중에 댓글 수정 삭제할 때 viewModel에 접근할 수 있어야 함

    private var commentList: MutableList<Comment> = arrayListOf()

    fun submitList(newList: MutableList<Comment>) {
        commentList = newList
        notifyDataSetChanged()
    }

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val fromUserImg = itemView.findViewById<ImageView>(R.id.profile_img)
        val fromUserId = itemView.findViewById<TextView>(R.id.userid)
        val messageTv = itemView.findViewById<TextView>(R.id.message_tv)
        val timeTv = itemView.findViewById<TextView>(R.id.time_tv)
        fun bindInfo(comment : Comment, position: Int){

            Glide.with(itemView.context)
                .load(comment.fromUserImg)
                .circleCrop()
                .into(fromUserImg)

            fromUserId.text = comment.fromUserId
            messageTv.text = comment.message

            val currentTimeMillis = System.currentTimeMillis()
            val timeDifMillis = currentTimeMillis-comment.time

            if(timeDifMillis >= TimeUnit.DAYS.toMillis(1)){
                val pastTime = TimeUnit.MILLISECONDS.toDays(currentTimeMillis-comment.time)
                timeTv.text = "${pastTime}일 전"
            }
            else if (timeDifMillis >= TimeUnit.HOURS.toMillis(1)) {
                val pastTime = TimeUnit.MILLISECONDS.toHours(currentTimeMillis-comment.time)
                timeTv.text = "${pastTime}시간 전"
            }
            else {
                val pastTime = TimeUnit.MILLISECONDS.toMinutes(currentTimeMillis-comment.time)
                timeTv.text = "${pastTime}분 전"
            }

            itemView.setOnLongClickListener {
                itemLongClickListener.onLongClick(it, comment, position)
                true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.CommentHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment_recyclerview, parent, false)
        return CommentHolder(view)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.apply{

            bindInfo(commentList[position], position)
        }
    }

    override fun getItemCount(): Int {

        return commentList.size
    }

    interface ItemLongClickListener {
        fun onLongClick(view: View, comment: Comment, position: Int)
    }
    lateinit var itemLongClickListener: ItemLongClickListener

}