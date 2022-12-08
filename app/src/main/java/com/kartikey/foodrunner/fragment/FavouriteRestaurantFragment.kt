package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.DashboardFragmentAdapter
import com.kartikey.foodrunner.database.RestaurantDatabase
import com.kartikey.foodrunner.database.RestaurantEntity
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.utils.ConnectionManager
import org.json.JSONException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class FavouriteRestaurantFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favouriteAdapter: DashboardFragmentAdapter
    lateinit var progressDialog: RelativeLayout
    lateinit var noFavouritesLayout: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()
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
        val view = inflater.inflate(R.layout.fragment_favourite_restaurant, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewFavouriteRestaurant)
        progressDialog = view.findViewById(R.id.favouriteRestaurantProgressDialog)
        noFavouritesLayout = view.findViewById(R.id.noFavouriteRestaurantsLayout)

        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_favourite_restaurant, container, false)
    }


    fun fetchData() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {

            progressDialog.visibility = View.VISIBLE
            noFavouritesLayout.visibility = View.INVISIBLE
            try {

                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener
                    {
                        val responseJsonObjectData = it.getJSONObject("data")
                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            restaurantInfoList.clear()
                            val data = responseJsonObjectData.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantEntity = RestaurantEntity(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name")
                                )

                                if (DBAsynTask(contextParam, restaurantEntity, 1).execute().get()) {
                                    val restaurantObject = Restaurant(
                                        restaurantJsonObject.getString("id"),
                                        restaurantJsonObject.getString("name"),
                                        restaurantJsonObject.getString("rating"),
                                        restaurantJsonObject.getString("cost_for_one"),
                                        restaurantJsonObject.getString("image_url")
                                    )

                                    restaurantInfoList.add(restaurantObject)
                                    favouriteAdapter = DashboardFragmentAdapter(
                                        activity as Context,
                                        restaurantInfoList
                                    )

                                    recyclerView.adapter = favouriteAdapter
                                    recyclerView.layoutManager = layoutManager
                                }
                            }
                            //Edge case. No items found
                            if (restaurantInfoList.size == 0) {
                                noFavouritesLayout.visibility = View.VISIBLE
                            }
                        }
                        progressDialog.visibility = View.GONE
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            activity as Context,
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
                    activity as Context,
                    "¡Ocurrió un error inesperado!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } else {

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

    }

    class DBAsynTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {
            /*
            * Mode 1-> check if restaurant is in favourites
            * Mode 2-> Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant
            */
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                else ->
                    return false

            }
        }
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            fetchData()
        } else {
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
//         * @return A new instance of fragment FavouriteRestaurantFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            FavouriteRestaurantFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}