package uz.itschool.firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import uz.itschool.firebase.model.UserData
import uz.itschool.firebase.ui.theme.FireBaseTheme

class ContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uid = intent.getStringExtra("uid")

                    val userList = remember {
                        mutableStateListOf(UserData())
                    }

                    val reference = Firebase.database.reference.child("users")
                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            userList.clear()
                            children.forEach {
                                val user = it.getValue(UserData::class.java)
                                if (user != null && user.uid != uid){
                                    userList.add(user ?: UserData())
                                }
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("TAG", "onCancelled: ${error.message}")
                        }

                    })

                    LazyColumn {
                        items(userList) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .clickable {
                                        val i = Intent(
                                            this@ContactActivity,
                                            MessageActivity::class.java
                                        )
                                        i.putExtra("uid", uid)
                                        i.putExtra("useruid", it.uid)
                                        startActivity(i)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(it.photo)
                                        .crossfade(true)
                                        .build(),
                                    placeholder = painterResource(R.drawable.user),
                                    contentDescription = ("no image"),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clip(CircleShape)
                                )
                                Text(
                                    text = it.name ?: "",
                                    Modifier.padding(start = 12.dp),
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



