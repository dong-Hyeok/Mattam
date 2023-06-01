package com.mattam.hacha.src.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mattam.hacha.R
import com.mattam.hacha.databinding.ActivityLoginBinding
import com.mattam.hacha.src.main.dto.User
import com.mattam.template.config.ApplicationClass
import com.mattam.template.config.BaseActivity
import kotlinx.coroutines.*

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private lateinit var googleSignInClient: GoogleSignInClient

    private var check = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.loginLayout.setOnClickListener {
            // 구글 로그인 절차 시작
            initAuth()
        }

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

    }

    private fun initAuth() {

        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // 빨간색으로 오류 표시되어도 무시해도됨
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]


        signIn()
    }


    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]



    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Log.d("LoginActivity", "signInWithCredential:success ${user}")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    // [END auth_with_google]


    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]



    // 인증 성공 여부에 따른 화면 처리
    private fun updateUI(user: FirebaseUser?) { // user 는 파이어베이스 유저
        if(user!=null){ // google로그인에 성공시

            CoroutineScope(Dispatchers.Default).launch {
                Log.d("SplashActivity", "onCreate: currentUser : ${auth.currentUser}")
                checkUid(user.uid)
            }

        }else{
            showCustomToast("로그인이 필요합니다.")
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
                    Log.d(TAG, "checkUid: ${documentSnapshot}")
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        // 중복된 ID가 존재함 -> 바로 Main으로
                        check = true

                        ApplicationClass.SharedPreferences.apply {
                            addUserTokenId(uid)
                            addUserId(user!!.userId)
                            addProfileImg(user!!.profileImg)
                            Log.d(TAG, "checkUid: ${user!!.userId} / ${user!!.profileImg}")
                        }

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Log.d(TAG, "checkUid: MainActivity으로 이동")
                    } else {
                        // 중복된 ID가 없음 -> Ready 화면으로 이동해서 id 생성
                        check = false
                        ApplicationClass.SharedPreferences.addUserTokenId(uid)
                        val intent = Intent(this@LoginActivity, ReadyActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Log.d(TAG, "checkUid: ReadyActivity로 이동")
                    }
                }
                .addOnFailureListener { exception ->
                    // 쿼리 실패 처리
                    println("중복 확인 중 오류가 발생했습니다: $exception")
                }
        }

        Log.d(TAG, "checkUid: finish")
    }


    companion object {
        private const val TAG = "LoginActivity ssafy"
        private const val RC_SIGN_IN = 9001
    }

}