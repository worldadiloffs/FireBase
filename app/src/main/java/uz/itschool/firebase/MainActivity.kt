package uz.itschool.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import uz.itschool.firebase.model.UserData
import uz.itschool.firebase.ui.theme.FireBaseTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    auth = FirebaseAuth.getInstance()

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_client_id))
                        .requestEmail()
                        .build()

                    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            val signInIntent = mGoogleSignInClient.signInIntent
                            startActivityForResult(signInIntent, 1)

                        }) {
                            Text(text = "Sign In Google")
                        }
                        Button(onClick = {
                            mGoogleSignInClient.signOut()

                        }) {
                            Text(text = "Sign Out")
                        }


                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
                Log.d("TAG", "onActivityResult: ")
            } catch (e: ApiException) {
                Log.d("TAG", "error: $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val userData = UserData(
                        user?.displayName,
                        user?.uid,
                        user?.email,
                        user?.photoUrl.toString()
                    )
                    setUser(userData)

                } else {
                    Log.d("TAG", "error: Authentication Failed.")
                }
            }
    }

    private fun setUser(userData: UserData) {
            val userIdReference = Firebase.database.reference
                .child("users").child(userData.uid ?: "")
            userIdReference.setValue(userData).addOnSuccessListener {
                val i = Intent(this@MainActivity, ContactActivity::class.java)
                i.putExtra("uid", userData.uid)
                startActivity(i)
        }
    }
}
