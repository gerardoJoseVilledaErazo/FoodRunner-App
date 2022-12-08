package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.utils.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class ForgotPasswordInputFragment(val contextParam: Context) : Fragment() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button
    lateinit var progressDialog: RelativeLayout
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
        val view = inflater.inflate(R.layout.fragment_forgot_password_input, container, false)

        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etEmail = view.findViewById(R.id.etEmail)
        btnNext = view.findViewById(R.id.btnNext)
        progressDialog = view.findViewById(R.id.forgotPasswordInputProgressDialog)

        btnNext.setOnClickListener(View.OnClickListener
        {

            if (etMobileNumber.text.isBlank()) {
                etMobileNumber.setError("Mobile Number Missing")
            } else {
                if (etEmail.text.isBlank()) {
                    etEmail.setError("Email Missing")
                } else {

                    if (ConnectionManager().checkConnectivity(activity as Context)) {
                        try {
                            val loginUser = JSONObject()

                            loginUser.put("mobile_number", etMobileNumber.text)
                            loginUser.put("email", etEmail.text)

                            val queue = Volley.newRequestQueue(activity as Context)
                            val url = "http://13.235.250.119/v2/forgot_password/fetch_result/"

                            progressDialog.visibility = View.VISIBLE

                            val jsonObjectRequest = object : JsonObjectRequest(
                                Request.Method.POST,
                                url,
                                loginUser,
                                Response.Listener
                                {
                                    val responseJsonObjectData = it.getJSONObject("data")
                                    val success = responseJsonObjectData.getBoolean("success")

                                    if (success) {
                                        val firstTry =
                                            responseJsonObjectData.getBoolean("first_try")

                                        if (firstTry) {
                                            Toast.makeText(
                                                contextParam,
                                                "OTP sent",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            callChangePasswordFragment()

                                        } else {
                                            Toast.makeText(
                                                contextParam,
                                                "OTP sent already",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            callChangePasswordFragment()
                                        }

                                    } else {
                                        val responseMessageServer =
                                            responseJsonObjectData.getString("errorMessage")
                                        Toast.makeText(
                                            contextParam,
                                            responseMessageServer.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                    progressDialog.visibility = View.GONE
                                },
                                Response.ErrorListener
                                {
                                    println(it)
                                    Toast.makeText(
                                        context,
                                        "¡Ocurrió algún error!",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    progressDialog.visibility = View.GONE

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
                            Toast.makeText(
                                contextParam,
                                "¡Ocurrió un error inesperado!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val alterDialog =
                            androidx.appcompat.app.AlertDialog.Builder(activity as Context)

                        alterDialog.setTitle("Sin internet")
                        alterDialog.setMessage("¡No se puede establecer la conexión a Internet!")
                        alterDialog.setPositiveButton("Abrir Settings")
                        { _, _ ->
                            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(settingsIntent)

                        }
                        alterDialog.setNegativeButton("Salir")
                        { _, _ ->
                            ActivityCompat.finishAffinity(activity as Activity)
                        }
                        alterDialog.create()
                        alterDialog.show()
                    }
                }
            }
        })

        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_forgot_password_input, container, false)
    }

    fun callChangePasswordFragment() {
        val transaction = fragmentManager?.beginTransaction()

        transaction?.replace(
            R.id.frameLayout,
            ForgotPasswordFragment(contextParam, etMobileNumber.text.toString())
        )
        transaction?.commit()
    }


    override fun onResume() {
        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("Sin internet")
            alterDialog.setMessage("¡No se puede establecer la conexión a Internet!")
            alterDialog.setPositiveButton("Abrir Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Salir")
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
//         * @return A new instance of fragment ForgotPasswordInputFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ForgotPasswordInputFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}