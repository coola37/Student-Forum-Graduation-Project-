package com.yigitkula.studentforum.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.nostra13.universalimageloader.core.ImageLoader
import com.yigitkula.studentforum.R
import com.yigitkula.studentforum.loginAndRegister.LoginActivity
import com.yigitkula.studentforum.model.Users
import com.yigitkula.studentforum.utils.BottomNavigationViewHelper
import com.yigitkula.studentforum.utils.EventbusDataEvents
import com.yigitkula.studentforum.utils.UniversalImageLoader
import com.yigitkula.studentforum.viewModel.ProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus

class ProfileActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var buttonProfileEdit: MaterialButton
    private lateinit var buttonSettings: MaterialButton
    private lateinit var profileRoot: ConstraintLayout
    private lateinit var tvUsername: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDept: TextView
    private lateinit var tvSchool: TextView
    private lateinit var tvSurname: TextView
    private lateinit var tvRank: TextView
    private lateinit var circleProfileImg: CircleImageView
    private lateinit var progressBarProfilePicture: ProgressBar

    private val ACTIVITY_NO=4
    private val TAG="ProfileActivity"
    private lateinit var auth: FirebaseAuth
    lateinit var mUser:FirebaseUser
    private lateinit var ref: DatabaseReference
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var profileViewModel: ProfileViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        progressBarProfilePicture=findViewById(R.id.progressBarProfilePictureView)
        bottomNavigationView=findViewById(R.id.bottomNavigationView)
        buttonProfileEdit=findViewById(R.id.buttonProfileEdit)
        buttonSettings=findViewById(R.id.buttonSettings)
        profileRoot=findViewById(R.id.profileRoot)
        tvName=findViewById(R.id.tvNameView)
        tvSurname=findViewById(R.id.tvSurnameView)
        tvUsername=findViewById(R.id.tvUsernameView)
        tvDept=findViewById(R.id.tvDeptView)
        tvSchool=findViewById(R.id.tvSchoolView)
        tvRank=findViewById(R.id.tvRankView)
        circleProfileImg=findViewById(R.id.circleProfileImgView)
        setupAuthListener()
        auth=Firebase.auth
        mUser=auth.currentUser!!
        ref=FirebaseDatabase.getInstance().reference

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        profileViewModel.userData.observe(this) { userData ->
            userData?.let {
                tvUsername.text = it.user_name
                tvName.text = it.name
                tvSurname.text = it.surname
                tvSchool.text = it.user_detail?.school
                tvDept.text = it.user_detail?.departmant
                tvRank.text = it.user_detail?.rank.toString()
                it.user_detail?.profile_picture?.let { it1 ->
                    UniversalImageLoader.setImage(
                        it1,
                        circleProfileImg,
                        progressBarProfilePicture,
                        ""
                    )
                }
            }
        }

        profileViewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                // Veriler yüklenirken yapılacak işlemler
                buttonProfileEdit.isEnabled = false
                buttonSettings.isEnabled = false
            } else {
                // Veriler yüklendikten sonra yapılacak işlemler
                buttonProfileEdit.isEnabled = true
                buttonSettings.isEnabled = true
            }
        }
        profileViewModel.getUserData(mUser.uid)


        initImageLoader()

        setupNavigationView()
        setupButtons()


        buttonProfileEdit.setOnClickListener {
            profileRoot.visibility=View.GONE
            var transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.profileContainer,ProfileEditFragment())
            transaction.addToBackStack("addedProfileFragment")

            transaction.commit()
        }
    }
    private fun initImageLoader() {

        var universalImageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(universalImageLoader.config)

    }


    fun setupNavigationView(){
        BottomNavigationViewHelper.setupNavigation(this,bottomNavigationView)
        var menu = bottomNavigationView.menu
        var menuItem = menu.getItem(ACTIVITY_NO)
        menuItem.setChecked(true)
    }

    fun setupButtons(){


        buttonSettings.setOnClickListener {
            var intent = Intent(this,ProfileSettingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

    }
    private fun setupAuthListener() {
        authListener=object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user= FirebaseAuth.getInstance().currentUser
                if(user == null){

                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    this@ProfileActivity.startActivity(intent)
                    finish()

                }else{

                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        if(authListener != null){
            auth.removeAuthStateListener(authListener)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        profileRoot.visibility=View.VISIBLE
        super.onBackPressed()
    }
}