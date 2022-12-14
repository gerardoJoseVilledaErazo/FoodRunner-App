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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.OrderHistoryAdapter
import com.kartikey.foodrunner.model.OrderHistoryRestaurant
import com.kartikey.foodrunner.utils.ConnectionManager
import org.json.JSONException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class OrderHistoryFragment : Fragment() {

    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: OrderHistoryAdapter
    lateinit var recyclerViewAllOrders: RecyclerView
    lateinit var orderHistoryProgressDialog: RelativeLayout
    lateinit var orderHistoryNoOrders: RelativeLayout
    lateinit var orderHistoryDefaultOrders: LinearLayout
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        recyclerViewAllOrders = view.findViewById(R.id.recyclerViewAllOrders)
        orderHistoryProgressDialog = view.findViewById(R.id.orderHistoryProgressLayout)
        orderHistoryNoOrders = view.findViewById(R.id.orderHistoryNoOrders)
        orderHistoryDefaultOrders = view.findViewById(R.id.linearLayoutOrder)

        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    fun setItemsForEachRestaurant() {
        layoutManager = LinearLayoutManager(activity)

        val orderedRestaurantList = ArrayList<OrderHistoryRestaurant>()
        val sharedPreferences = activity?.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        val userId = sharedPreferences?.getString("user_id", "000")

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            orderHistoryProgressDialog.visibility = View.VISIBLE

            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/orders/fetch_result/${userId}"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener
                    {

                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {
                            val data = responseJsonObjectData.getJSONArray("data")

                            //Edge Case. No orders present
                            if (data.length() == 0) {
                                orderHistoryNoOrders.visibility = View.VISIBLE
                                orderHistoryDefaultOrders.visibility = View.INVISIBLE
                            } else {
                                orderHistoryNoOrders.visibility = View.GONE

                                for (i in 0 until data.length()) {
                                    val restaurantItemJsonObject = data.getJSONObject(i)

                                    val eachRestaurantObject = OrderHistoryRestaurant(
                                        restaurantItemJsonObject.getString("order_id"),
                                        restaurantItemJsonObject.getString("restaurant_name"),
                                        restaurantItemJsonObject.getString("total_cost"),
                                        restaurantItemJsonObject.getString("order_placed_at")
                                            .substring(0, 10)
                                    )

                                    orderedRestaurantList.add(eachRestaurantObject)
                                    menuAdapter = OrderHistoryAdapter(
                                        activity as Context,
                                        orderedRestaurantList
                                    )
                                    recyclerViewAllOrders.adapter = menuAdapter
                                    recyclerViewAllOrders.layoutManager = layoutManager

                                }
                            }
                        }
                        orderHistoryProgressDialog.visibility = View.GONE
                    },
                    Response.ErrorListener
                    {
                        orderHistoryProgressDialog.visibility = View.GONE

                        Toast.makeText(
                            activity,
                            "??Ocurri?? alg??n error!",
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
                    context,
                    "??Ocurri?? un error inesperado!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("Sin internet")
            alterDialog.setMessage("??No se puede establecer la conexi??n a Internet!")
            alterDialog.setPositiveButton("Abrir Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Salir")
            { _, _ ->
                finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            setItemsForEachRestaurant()
        } else {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("Sin internet")
            alterDialog.setMessage("??No se puede establecer la conexi??n a Internet!")
            alterDialog.setPositiveButton("Abrir Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Salir")
            { _, _ ->
                finishAffinity(activity as Activity)
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
//         * @return A new instance of fragment OrderHistoryFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            OrderHistoryFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}