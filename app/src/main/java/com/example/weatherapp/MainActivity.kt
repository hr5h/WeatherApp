package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.data.WeatherModel
import com.example.weatherapp.screens.DialogSearch
import com.example.weatherapp.screens.MainCard
import com.example.weatherapp.screens.ShowBackground
import com.example.weatherapp.screens.TabLayout
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.json.JSONObject

const val WEATHER_API = "4010ed0be696407c88580527242104"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                            "", "", "0.0", "", "", "0.0", "0.0", ""
                        )
                    )
                }
                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, 3, this, daysList, currentDay)
                    })
                } else {
                    getData("Rostov-on-Don", 3, this, daysList, currentDay)
                }
                ShowBackground()
                Column {
                    MainCard(
                        currentDay,
                        onClickSync = {
                            getData("Rostov-on-Don", 3, this@MainActivity, daysList, currentDay)
                        }, onClickSearch = {
                            dialogState.value = true
                        })
                    TabLayout(daysList, currentDay)
                }
            }
        }
    }
}


private fun getData(
    town: String,
    days: Int,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json" +
            "?key=$WEATHER_API" +
            "&q=$town" +
            "&days=$days" +
            "&aqi=no" +
            "&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val request = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            daysList.value = list
            currentDay.value = list[0]
            Log.d("MyLog", "Response: $response")
        },
        { error ->
            Log.d("MyLog", "Error: $error")
        }
    )
    queue.add(request)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()
    val list = mutableListOf<WeatherModel>()
    val mainObj = JSONObject(response)
    val city = mainObj.getJSONObject("location").getString("name")
    val days = mainObj.getJSONObject("forecast").getJSONArray("forecastday")
    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObj.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObj.getJSONObject("current").getString("temp_c")
    )
    return list
}