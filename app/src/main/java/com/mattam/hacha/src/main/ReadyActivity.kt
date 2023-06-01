package com.mattam.hacha.src.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mattam.hacha.databinding.ActivityReadyBinding
import com.mattam.hacha.src.main.dto.User
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadyActivity : BaseActivity<ActivityReadyBinding>(ActivityReadyBinding::inflate) {

    private var check = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            idEt.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.idLayout.error = null
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

            JoinBtn.setOnClickListener {
                val id = idEt.text.toString()

                CoroutineScope(Dispatchers.Default).launch {
                    checkDuplicateId(id.trim())
                }
            }

        }

    }

    fun createUser(id:String){
        ApplicationClass.SharedPreferences.addUserId(id)

        val db = FirebaseFirestore.getInstance()
        val userToken = ApplicationClass.SharedPreferences.getUserTokenId()
        Log.d("ReadyActivity", "onCreate: ${userToken}")
        val usersCollection = db.collection("users").document("${userToken}")
        var userInfo = User(id,"","", arrayListOf(), arrayListOf(), arrayListOf())

        usersCollection.set(userInfo)
            .addOnSuccessListener {
                Log.d("MainActivity", "사용자 데이터가 성공적으로 저장되었습니다.")

            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "사용자 데이터 저장 중 오류가 발생했습니다: $exception")
            }
    }

    // ID 중복 check
    suspend fun checkDuplicateId(documentId: String){
        Log.d("ReadyActivity", "checkDuplicateId:${documentId}")
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("users") // user 정보 모두 가지고 옴
        collectionReference
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (documentSnapshot in querySnapshot) {
                    val user = documentSnapshot.toObject(User::class.java)
                    var userId = user.userId

                    if(userId == documentId){
                        Log.d("ReadyActivity", "checkDuplicateId: /${userId}/ /${documentId}/")
                        check = true
                        break
                    }
                }
                if (check) {
                    check = false
                    binding.idLayout.error = "ID 중복"
                }else{
                    Log.d("before create", "onCreate: $documentId")
                    createUser(documentId)

                    val intent = Intent(this@ReadyActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                // 쿼리 실패 처리
                println("사용자 정보를 가져오는 중 오류가 발생했습니다: $exception")
            }

        Log.d("ReadyActivity", "checkDuplicateId: ${check}")

    }
}