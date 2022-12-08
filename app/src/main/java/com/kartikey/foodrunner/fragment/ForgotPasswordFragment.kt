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

class ForgotPasswordFragment(val contextParam: Context, val mobile_number: String) : Fragment() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmForgotPassword: EditText
    lateinit var forgotPasswordProgressDialog: RelativeLayout
    lateinit var btnSubmit: Button

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
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        etOTP = view.findViewById(R.id.etOTP)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmForgotPassword = view.findViewById(R.id.etConfirmForgotPassword)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        forgotPasswordProgressDialog = view.findViewById(R.id.forgotPasswordProgressDialog)

        btnSubmit.setOnClickListener(View.OnClickListener
        {
            if (etOTP.text.isBlank()) {
                etOTP.setError("OTP missing")
            } else {
                if (etNewPassword.text.isBlank()) {
                    etNewPassword.setError("Password Missing")
                } else {
                    if (etConfirmForgotPassword.text.isBlank()) {
                        etConfirmForgotPassword.setError("Confirm Password Missing")
                    } else {
                        if ((etNewPassword.text.toString()
                                .toInt() == etConfirmForgotPassword.text.toString().toInt())
                        ) {
                            if (ConnectionManager().checkConnectivity(activity as Context)) {
                                forgotPasswordProgressDialog.visibility = View.VISIBLE

                                try {
                                    val loginUser = JSONObject()

                                    loginUser.put("mobile_number", mobile_number)
                                    loginUser.put("password", etNewPassword.text.toString())
                                    loginUser.put("otp", etOTP.text.toString())

                                    val queue = Volley.newRequestQueue(activity as Context)
                                    val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                                    val jsonObjectRequest = object : JsonObjectRequest(
                                        Request.Method.POST,
                                        url,
                                        loginUser,
                                        Response.Listener
                                        {
                                            val responseJsonObjectData = it.getJSONObject("data")
                                            val success =
                                                responseJsonObjectData.getBoolean("success")

                                            if (success) {
                                                val serverMessage =
                                                    responseJsonObjectData.getString("successMessage")

                                                Toast.makeText(
                                                    contextParam,
                                                    serverMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                passwordChanged()
                                            } else {
                                                val responseMessageServer =
                                                    responseJsonObjectData.getString("errorMessage")
                                                Toast.makeText(
                                                    contextParam,
                                                    responseMessageServer.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            forgotPasswordProgressDialog.visibility = View.GONE
                                        },
                                        Response.ErrorListener
                                        {
                                            forgotPasswordProgressDialog.visibility = View.GONE
                                            Toast.makeText(
                                                contextParam,
                                                "¡¡¡Ocurrió algún error!!!",
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
                        } else {
                            etConfirmForgotPassword.setError("Las contraseñas no coinciden")
                        }
                    }
                }
            }

        })

        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    fun passwordChanged() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            LoginFragment(contextParam)
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
//         * @return A new instance of fragment ForgotPasswordFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ForgotPasswordFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}