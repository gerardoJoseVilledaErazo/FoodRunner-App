package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.activity.DashboardActivity
import com.kartikey.foodrunner.utils.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class LoginFragment(val contextParam: Context) : Fragment() {
    lateinit var txtSignUp: TextView
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var txtForgotPassword: TextView
    lateinit var btnLogin: Button
    lateinit var loginProgressDialog: RelativeLayout
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etPassword = view.findViewById(R.id.etPassword)
        txtForgotPassword = view.findViewById(R.id.txtForgotPassword)
        txtSignUp = view.findViewById(R.id.txtSignUp)
        btnLogin = view.findViewById(R.id.btnLogin)
        loginProgressDialog = view.findViewById(R.id.loginProgressDialog)

        loginProgressDialog.visibility = View.GONE

        //under line text
        txtForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtSignUp.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        txtForgotPassword.setOnClickListener {
            openForgotPasswordInputFragment()
        }

        txtSignUp.setOnClickListener {
            openRegisterFragment()
        }

        btnLogin.setOnClickListener {

            btnLogin.visibility = View.GONE

            if (etMobileNumber.text.isBlank()) {
                etMobileNumber.setError("Mobile Number Missing")
                btnLogin.visibility = View.VISIBLE
            } else {
                if (etPassword.text.isBlank()) {
                    btnLogin.visibility = View.VISIBLE
                    etPassword.setError("Missing Password")
                } else {
                    loginUserFun()
                }
            }
        }
        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    fun openForgotPasswordInputFragment() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            ForgotPasswordInputFragment(contextParam)
        )
        transaction?.commit()//apply changes
    }


    fun openRegisterFragment() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            RegisterFragment(contextParam)
        )
        transaction?.commit()
    }


    fun loginUserFun() {
        val sharedPreferencess = contextParam.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            loginProgressDialog.visibility = View.VISIBLE
            try {
                val loginUser = JSONObject()
                loginUser.put("mobile_number", etMobileNumber.text)
                loginUser.put("password", etPassword.text)

                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/login/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    loginUser,
                    Response.Listener
                    {
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")


                        if (success) {
                            val data = responseJsonObjectData.getJSONObject("data")
                            sharedPreferencess.edit().putBoolean("user_logged_in", true).apply()
                            sharedPreferencess.edit()
                                .putString("user_id", data.getString("user_id")).apply()
                            sharedPreferencess.edit().putString("name", data.getString("name"))
                                .apply()
                            sharedPreferencess.edit().putString("email", data.getString("email"))
                                .apply()
                            sharedPreferencess.edit()
                                .putString("mobile_number", data.getString("mobile_number")).apply()
                            sharedPreferencess.edit()
                                .putString("address", data.getString("address")).apply()

                            Toast.makeText(
                                contextParam,
                                "Welcome " + data.getString("name"),
                                Toast.LENGTH_LONG
                            ).show()

                            userSuccessfullyLoggedIn()

                        } else {
                            btnLogin.visibility = View.VISIBLE

                            val responseMessageServer =
                                responseJsonObjectData.getString("errorMessage")
                            Toast.makeText(
                                contextParam,
                                responseMessageServer.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loginProgressDialog.visibility = View.GONE
                    },
                    Response.ErrorListener
                    {
                        println(it)
                        btnLogin.visibility = View.VISIBLE
                        loginProgressDialog.visibility = View.GONE

                        Toast.makeText(
                            contextParam,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()


                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "13714ab03e5a4d"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                btnLogin.visibility = View.VISIBLE
                Toast.makeText(
                    contextParam,
                    "Some unexpected error occurred!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            btnLogin.visibility = View.VISIBLE

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.create()
            alterDialog.show()
        }
    }

    fun userSuccessfullyLoggedIn() {
        val intent = Intent(activity as Context, DashboardActivity::class.java)
        startActivity(intent)
        activity?.finish();
    }

    override fun onResume() {
        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment LoginFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            LoginFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}