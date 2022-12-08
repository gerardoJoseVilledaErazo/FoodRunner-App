package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.DashboardFragmentAdapter
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.utils.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class DashboardFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var dashboardAdapter: DashboardFragmentAdapter
    lateinit var etSearch: EditText
    lateinit var radioButtonView: View
    lateinit var progressDialog: RelativeLayout
    lateinit var rlNoRestaurantFound: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()
    var ratingComparator = Comparator<Restaurant>
    { rest1, rest2 ->

        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }

    }

    var costComparator = Comparator<Restaurant>
    { rest1, rest2 ->
        rest1.costForOne.compareTo(rest2.costForOne, true)
    }
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null

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

        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewDashboard)
        etSearch = view.findViewById(R.id.etSearch)
        progressDialog = view.findViewById(R.id.dashboardProgressDialog)
        rlNoRestaurantFound = view.findViewById(R.id.noRestaurantFound)

        rlNoRestaurantFound.visibility = View.INVISIBLE

        fun filterFun(strTyped: String) {
            rlNoRestaurantFound.visibility = View.INVISIBLE
            val filteredList = arrayListOf<Restaurant>()
            for (item in restaurantInfoList) {
                if (item.restaurantName.toLowerCase(Locale.ROOT)
                        .contains(strTyped.toLowerCase(Locale.ROOT))
                ) {
                    filteredList.add(item)
                }
            }
            if (filteredList.size == 0) {
                rlNoRestaurantFound.visibility = View.VISIBLE
            }
            dashboardAdapter.filterList(filteredList)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            //as the user types the search filter is applied
            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        )
        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            progressDialog.visibility = View.VISIBLE
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
                            val data = responseJsonObjectData.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )
                                restaurantInfoList.add(restaurantObject)

                                dashboardAdapter = DashboardFragmentAdapter(
                                    activity as Context,
                                    restaurantInfoList
                                )
                                recyclerView.adapter = dashboardAdapter
                                recyclerView.layoutManager = layoutManager
                            }
                        }
                        progressDialog.visibility = View.GONE
                    },
                    Response.ErrorListener
                    {
                        progressDialog.visibility = View.GONE
                        Toast.makeText(
                            activity as Context,
                            "¡Ocurrió algún error!",
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {

            R.id.sort -> {
                radioButtonView = View.inflate(
                    contextParam,
                    R.layout.sort_radio_button,
                    null
                )     //radiobutton view for sorting display
                val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                alterDialog.setTitle("¿Ordenar por?")
                alterDialog.setView(radioButtonView)
                alterDialog.setPositiveButton("OK")
                { _, _ ->
                    if (radioButtonView.radioHighToLow.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        restaurantInfoList.reverse()
                        dashboardAdapter.notifyDataSetChanged()     //update the adapter of changes
                    }
                    if (radioButtonView.radioLowToHigh.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        dashboardAdapter.notifyDataSetChanged()     //updates the adapter of changes
                    }
                    if (radioButtonView.radioRating.isChecked) {
                        Collections.sort(restaurantInfoList, ratingComparator)
                        restaurantInfoList.reverse()
                        dashboardAdapter.notifyDataSetChanged()
                    }
                }
                alterDialog.setNegativeButton("CANCELAR")
                { _, _ ->
                    //do nothing
                }
                alterDialog.create()
                alterDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (restaurantInfoList.isEmpty())
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
            alterDialog.setNegativeButton("Salida")
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
//         * @return A new instance of fragment DashboardFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            DashboardFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}