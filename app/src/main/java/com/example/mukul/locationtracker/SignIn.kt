package com.example.mukul.locationtracker

import android.arch.persistence.room.Room
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.example.mukul.locationtracker.Data.AppDatabase
import com.example.mukul.locationtracker.Data.FetchCordinates
import com.example.mukul.locationtracker.Data.Users
import kotlinx.android.synthetic.main.activity_login2.*

/**
 * A login screen that offers login via email/password.
 */
class SignIn : AppCompatActivity(){
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */


    lateinit var cordinatesDatabase: AppDatabase
    val mDb = Room.inMemoryDatabaseBuilder(this, AppDatabase::class.java).build()
    val fetchcordinates: FetchCordinates = mDb.getCordinates()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        cordinatesDatabase = Room.databaseBuilder(this, AppDatabase::class.java, MainActivity.dataBaseName).build()




        email_sign_in_button.setOnClickListener {
            if (email.text.toString().length<1)
                email.error = "Email Can't be empty"
           else if (isValidEmail(email.text.toString())){
                if (password.text.toString().length<1)
                    password.error = "Password Cant be Empty"
                else
                {
                    object : AsyncTask<Void, Void, Users>() {
                        override fun doInBackground(vararg params: Void): Users? {
                            try {
                                var user = cordinatesDatabase.getCordinates().getUsersByEmail(email.text.toString())
                                if(user != null){
                                    return user

                                }
                                else{
                                    return null
                                }
                            }
                            catch (e: Exception) {
                                return null
                            }

                        }

                        override fun onPostExecute(result: Users?) {

                            if (result!=null){
                                if(result.password.equals(password.text.toString())){

                                    Log.e("Sucessfully Logged in","user :"+ result.email)

                                    val pref = applicationContext.getSharedPreferences("usersLoggedIn", 0)
                                    val editor = pref.edit()
                                    editor.putBoolean("LoginStatus",true)
                                    editor.putString("email",result.email)
                                    editor.putInt("id", result.userId!!)
                                    editor.putString("name", result.name)

                                    editor.commit()


                                    val intentID : Intent = Intent(this@SignIn,MainActivity::class.java)
                                    intentID.putExtra("id",result.userId.toString())
                                    intentID.putExtra("name",result.name)
                                    startActivity(intentID)

                                }
                                else{
                                    password.error = "Incorrect Password"
                                }

                            }
                            else{
                                email.error = "Email not found try to register"
                            }


                        }
                    }.execute()







                }


            }
            else
                email.error = "Invalid Email"
        }
        SignUp.setOnClickListener{
            startActivity(Intent(this,com.example.mukul.locationtracker.SignUp::class.java))

        }
    }
    fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }



}
