package uz.itschool.firebase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import uz.itschool.firebase.model.Message
import uz.itschool.firebase.ui.theme.FireBaseTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

private const val TAG = "MessageActivity"

class MessageActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireBaseTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val messageList = remember {
                        mutableStateListOf(Message())
                    }
                    val uid = intent.getStringExtra("uid")
                    val useruid = intent.getStringExtra("useruid")


                    var text = remember {
                        mutableStateOf(TextFieldValue(""))
                    }
                    LazyColumn() {
                        items(messageList) {
                            val backgroundColor = if (it.from == uid) Color.Blue else Color.Gray
                            val paddingStart =
                                if (it.from == uid) TextAlign.End else TextAlign.Start

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .background(
                                            color = backgroundColor,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = it.text ?: "", color = Color.White,
                                        fontSize = 16.sp,
                                        textAlign = paddingStart,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = it.date ?: "", color = Color.White,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                            }

                        }
                    }


                    val ref = Firebase.database.reference.child("users")
                        .child(uid!!)
                        .child("message")
                        .child(useruid!!)
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            messageList.clear()
                            children.forEach {
                                val message = it.getValue(Message::class.java)
                                messageList.add(message ?: Message())
                                Log.d("TAG", "onCreate: ${message?.text}")
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "onCancelled: ${error.message}")
                        }

                    })

                    val sdf = SimpleDateFormat("HH:mm")
                    val currentDateAndTime = sdf.format(Date())
                    val m = Message(useruid, uid, text.value.text, currentDateAndTime)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextField(
                            text.value,
                            onValueChange = {
                                text.value = it
                            })
                        Button(onClick = {
                            val reference = Firebase.database.reference.child("users")
                            val key = reference.push().key.toString()
                            reference.child(uid ?: "")
                                .child("message")
                                .child(useruid ?: "")
                                .child(key)
                                .setValue(m)
                            reference.child(useruid ?: "")
                                .child("message")
                                .child(uid ?: "")
                                .child(key)
                                .setValue(m)

                            text.value = TextFieldValue("")
                        })
                        {
                            Text(text = "send")
                        }

                    }


                }
            }
        }
    }
}

