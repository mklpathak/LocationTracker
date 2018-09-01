package com.example.mukul.locationtracker

import android.arch.persistence.room.Room
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mukul.locationtracker.Data.AppDatabase
import com.example.mukul.locationtracker.Data.FetchCordinates
import com.example.mukul.locationtracker.Data.Users
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.android.synthetic.main.activity_login.*







/**
 * A login screen that offers login via email/password.
 */
class SignUp : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var cordinatesDatabase: AppDatabase
    val mDb = Room.inMemoryDatabaseBuilder(this@SignUp, AppDatabase::class.java).build()
    val fetchcordinates: FetchCordinates = mDb.getCordinates()
    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {
        //
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }


    val TAG = "Error"
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]"
    val SiteKey = "6LenrW0UAAAAAOLj1KemvO2ev9XDd6W65VyYIMzO\n"
    val SecretKey = "6LenrW0UAAAAAE2v2u1HzyvJOroi5ny4XEe8ZTkK\n"
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val pref = applicationContext.getSharedPreferences("usersLoggedIn", 0)
        if (pref.getBoolean("LoginStatus",false)){
            val intentID : Intent = Intent(this@SignUp,MainActivity::class.java)
            Log.e("Hello", "Mukul"+pref.getInt("id",-1).toString())

            intentID.putExtra("id",pref.getInt("id",-1).toString())
            intentID.putExtra("name",pref.getString("name",""))
            intentID.putExtra("email",pref.getString("email",""))
            Log.e("Hello", "Mukul")

            startActivity(intentID)

        }
        else {
            
            login.setOnClickListener {
                startActivity(Intent(this,SignIn::class.java))
            }


            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(SafetyNet.API)
                    .addConnectionCallbacks(this@SignUp)
                    .addOnConnectionFailedListener(this@SignUp)
                    .build()

            mGoogleApiClient?.connect()

            capthca.setOnClickListener {
                if (capthca.isChecked) {
                    capthca.isChecked = false

                    SafetyNet.getClient(this).verifyWithRecaptcha("6LenrW0UAAAAAOLj1KemvO2ev9XDd6W65VyYIMzO")
                            .addOnSuccessListener(this) { response ->
                                if (!response.tokenResult.isEmpty()) {

                                    capthca.isChecked = true

                                }
                            }
                            .addOnFailureListener(this) { e ->
                                if (e is ApiException) {
                                    Log.e("Captcha Failure", "Error message: " + CommonStatusCodes.getStatusCodeString(e.statusCode))
                                } else {
                                    Log.e("Captcha Failure", "Unknown type of error: " + e.message)
                                }
                            }
                }

            }

            button.setOnClickListener {

                var emailCheck = true
                var passwordCheck = true
                var captchaCheck = true
                var nameCheck = true


                if (email.text.length < 1) {
                    emailCheck = false
                    email.error = "Email can't be empty"
                } else if (!isValidEmail(email.text)) {
                    email.error = "Email is invalid"
                    emailCheck = false

                } else
                    emailCheck = true

                if (name.text.length < 1) {
                    name.error = "Name Cannot be Empty"
                    nameCheck = false
                } else if (name.text.length < 3) {
                    name.error = "Name should be more then two characters"
                    nameCheck = false
                } else
                    nameCheck = true


                if (password.text.length < 1) {
                    password.error = "Password should not be empty"
                    passwordCheck = false

                } else if (passwordStrength(password.text.toString()) < 33) {
                    password.error = "Weak password"
                    passwordInstruction.visibility = View.VISIBLE
                    passwordInstruction.text =
                            "1.Password length should be more then 8 characters \n" +
                                    "2. Password should be alphanumeric\n" +
                                    "3. Password should contain special charcters (Allowed Chracters . _ @ #)\n" +
                                    "4. Password should contain one or more capital characters"


                    passwordCheck = false
                } else
                    passwordCheck = true

                if (!capthca.isChecked) {
                    capthca.error = "Captcha needs to be checked"
                    captchaCheck = false

                } else
                    captchaCheck = true

                if (emailCheck && passwordCheck && captchaCheck && nameCheck) {

                    Toast.makeText(this@SignUp, "Suceess", Toast.LENGTH_SHORT).show()

                    val user: Users = Users()
                    user.email = email.text.toString()
                    user.password = password.text.toString()
                    user.name = name.text.toString()

                    object : AsyncTask<Void, Void, String>() {
                        override fun doInBackground(vararg params: Void): String? {

                            try {
                                Log.e("Erro", cordinatesDatabase.getCordinates().insertUser(user).toString())
                                for (a: Users in cordinatesDatabase.getCordinates().getUsers()) {
                                    Log.e("Insert User Successful", a.name)
                                }

                            }
                            catch (e: Exception) {
                               return "Email Already Exist"
                            }
                            return "success"
                        }

                        override fun onPostExecute(result: String) {
                            // continue what you are doing...
                            if(result.equals("success"))
                            {
                                startActivity(Intent(this@SignUp, SignIn::class.java))
                            }
                                else
                            email.error = result

                        }
                    }.execute()


                }


            }

            password.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                    if (s.length != 0) {
                        if (passwordStrength(s.toString()) > 33 && passwordStrength(s.toString()) < 66) {
                            progressBar.progress = 66
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#ffcf33")))
                            passwordStrength.setText("Password Strength : Medium")
                        } else if (passwordStrength(s.toString()) > 66) {
                            progressBar.progress = 100
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#00e676")))
                            passwordStrength.setText("Password Strength : Good")
                        } else {
                            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#aa2e25")))
                            progressBar.progress = 33
                            passwordStrength.setText("Password Strength : Weak")
                        }
                    } else
                        progressBar.progress = 0

                }
            })

            provideSampleData()

            cordinatesDatabase = Room.databaseBuilder(this@SignUp, AppDatabase::class.java, MainActivity.dataBaseName).build()

        }

    }

    fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    fun passwordStrength(s: String): Int {

        var strength: Int = 0
        if (s.length > 6)
            strength += 33
        if (s.length > 18)
            strength += 20
        if (s.contains(("[a-z]").toRegex()))
            strength += 10
        if (s.contains(("[A-Z]").toRegex()))
            strength += 20
        if (s.contains(("[0-9]").toRegex()))
            strength += 10
        if (s.contains(("[._#@]").toRegex()))
            strength += 10



        return strength

    }

    fun provideSampleData(){
//        email.setText("mukulpathak987@gmail.com")
//        password.setText("#aacmpp7795AE")
//        capthca.isChecked = true
//        name.setText("Mukul Pathak")

    }

}