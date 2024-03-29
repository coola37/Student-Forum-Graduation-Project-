package com.yigitkula.studentforum.loginAndRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.yigitkula.studentforum.R
import com.yigitkula.studentforum.home.HomeActivity
import com.yigitkula.studentforum.model.UserDetails
import com.yigitkula.studentforum.model.Users
import com.yigitkula.studentforum.utils.EventbusDataEvents
import com.yigitkula.studentforum.viewModel.RegisterViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginRegisterActivity: TextView
    private lateinit var buttonRegister: MaterialButton
    private lateinit var emailRegister: EditText
    private lateinit var usernameRegister: EditText
    private lateinit var nameRegister: EditText
    private lateinit var surnameRegister: EditText
    private lateinit var passwordRegister: EditText
    private lateinit var registerContainer: FrameLayout
    private lateinit var registerRoot: ConstraintLayout
    private lateinit var userRegisterLoading: ProgressBar


    private lateinit var emailText: String
    private lateinit var passText: String
    private lateinit var usernameText: String
    private lateinit var nameText: String
    private lateinit var surnameText: String

    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var viewModel: RegisterViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        loginRegisterActivity=findViewById(R.id.loginRegisterActivity)
        buttonRegister=findViewById(R.id.buttonRegister)
        emailRegister=findViewById(R.id.emailRegister)
        usernameRegister=findViewById(R.id.usernameRegister)
        nameRegister=findViewById(R.id.nameRegister)
        surnameRegister=findViewById(R.id.surnameRegister)
        passwordRegister=findViewById(R.id.passwordLogin)
        registerContainer=findViewById(R.id.registerContainer)
        registerRoot=findViewById(R.id.registerRoot)
        userRegisterLoading=findViewById(R.id.userRegisterLoading)

        auth = Firebase.auth
        ref=FirebaseDatabase.getInstance().reference

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        setupButtonClick()
    }
    private fun performRegister() {
        val emailText = emailRegister.text.toString()
        val passText = passwordRegister.text.toString()
        val nameText = nameRegister.text.toString()
        val usernameText = usernameRegister.text.toString()
        val surnameText = surnameRegister.text.toString()

        if (emailText.isEmpty() || passText.isEmpty() || usernameText.isEmpty() || nameText.isEmpty() || surnameText.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        userRegisterLoading.visibility = View.VISIBLE

        // Call the suspend function from within a coroutine scope
        lifecycleScope.launch {
            val isRegistered = viewModel.performRegister(emailText, passText, nameText, usernameText, surnameText)
            userRegisterLoading.visibility = View.GONE

            if (isRegistered) {
                Toast.makeText(this@RegisterActivity, "Registration successful.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RegisterActivity, "Registration failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveData(){
        val user = auth.currentUser
        val userID = user!!.uid


        Toast.makeText(this@RegisterActivity, "Registration is succesfull. ", Toast.LENGTH_SHORT).show()

        var userDetailsRegistration =UserDetails(" ", " ",  " ",0)
        var userRegistration = Users(emailText,usernameText,nameText,surnameText,userID,userDetailsRegistration)


        ref.child("users").child(userID).setValue(userRegistration)
            .addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        Log.e("User Register", "Save is succesful")
                        userRegisterLoading.visibility=View.GONE
                    }else{
                        auth.currentUser!!.delete()
                            .addOnCompleteListener(object: OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0.isSuccessful){
                                        userRegisterLoading.visibility=View.GONE
                                        Log.e("User Register", "User deleted")
                                    }
                                }
                            })
                        userRegisterLoading.visibility=View.GONE
                        Log.e("User Register", "Save is not succesful")
                    }
                }
            })

        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        this.finish()

        EventBus.getDefault().postSticky(EventbusDataEvents.getUserInfo(emailText,user!!.uid,passText))
    }

    private fun setupButtonClick(){
        loginRegisterActivity.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            this.finish()
        }
        buttonRegister.setOnClickListener {
            userRegisterLoading.visibility=View.VISIBLE
            performRegister()
        }
    }
}