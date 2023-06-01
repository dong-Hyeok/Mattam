package com.mattam.hacha.src.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mattam.hacha.databinding.ActivitySplashBinding
import com.mattam.hacha.src.main.LoginActivity
import com.mattam.hacha.src.main.MainActivity
import com.mattam.hacha.src.main.ReadyActivity
import com.mattam.hacha.src.main.dto.User
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseActivity
import kotlinx.coroutines.*


class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate){

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animationView = binding.animationView
        animationView.speed = 1.5f // 애니메이션 속도를 2배로 설정


        auth = Firebase.auth
        val user = auth.currentUser

        if(user == null){ // 구글 로그인(LoginActivity)으로 이동
            CoroutineScope(Dispatchers.Main).launch {
                delay(2500)
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }else{ // ID 중복 체크 후 -> 중복이라면 MainActivity로
            CoroutineScope(Dispatchers.Default).launch {
                delay(2500)
                Log.d("SplashActivity", "onCreate: currentUser : ${auth.currentUser}")
                checkUid(user.uid)
            }
        }

    }

    suspend fun checkUid(uid:String) = coroutineScope{
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("users") // user 정보 모두 가지고 옴

        launch {
            collectionReference
                .document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.d("SplashActivity", "checkUid: ${documentSnapshot}")
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        // 중복된 ID가 존재함 -> 바로 Main으로
                        ApplicationClass.SharedPreferences.apply {
                            addUserTokenId(uid)
                            addUserId(user!!.userId)
                            addProfileImg(user!!.profileImg)
                            Log.d("SplashActivity", "checkUid: ${user!!.userId} / ${user!!.profileImg}")
                        }

                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Log.d("SplashActivity", "checkUid: MainActivity으로 이동")
                    } else {
                        // 중복된 ID가 없음 -> Ready 화면으로 이동해서 id 생성
                        ApplicationClass.SharedPreferences.addUserTokenId(uid)
                        val intent = Intent(this@SplashActivity, ReadyActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Log.d("SplashActivity", "checkUid: ReadyActivity로 이동")
                    }
                }
                .addOnFailureListener { exception ->
                    // 쿼리 실패 처리
                    println("중복 확인 중 오류가 발생했습니다: $exception")
                }
        }

        Log.d("SplashActivity", "checkUid: finish")
    }
}