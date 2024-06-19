package com.example.weatherapp.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.data.WeatherModel
import com.example.weatherapp.ui.theme.BlueLight
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ShowBackground() {
    Image(
        painter = painterResource(id = R.drawable.img),
        contentDescription = "im1",
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.5f),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun MainCard(
    currentDay: MutableState<WeatherModel>,
    onClickSync: () -> Unit,
    onClickSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(5.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlueLight),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time,
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:${currentDay.value.icon}",
                        contentDescription = "im2",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 3.dp, end = 8.dp)
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                    text = currentDay.value.town,
                    style = TextStyle(fontSize = 24.sp),
                    color = Color.White
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                    text = "${currentDay.value.currentTemp.toFloat().toInt()}°C",
                    style = TextStyle(fontSize = 65.sp),
                    color = Color.White
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            onClickSearch()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "im3",
                            tint = Color.White
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = "${
                            currentDay.value.maxTemp.toFloat().toInt()
                        }°C/${currentDay.value.minTemp.toFloat().toInt()}°C",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            onClickSync()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "im3",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tabList = listOf("HOURS", "DAYS")
    val state = remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { tabList.size }
    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        TabRow(
            selectedTabIndex = state.intValue,
            containerColor = BlueLight,
            contentColor = Color.White
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = state.intValue == index,
                    onClick = { state.intValue = index },
                    text = { Text(text = text) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

@Composable
fun MainList(list: List<WeatherModel>, currentDay: MutableState<WeatherModel>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list) { _, item ->
            ListItem(item, currentDay)
        }
    }
}

private fun getWeatherByHours(hours: String): List<WeatherModel> {
    if (hours.isEmpty()) return listOf()
    val list = mutableListOf<WeatherModel>()
    val hoursArray = JSONArray(hours)
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°C",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                ""
            )
        )
    }

    return list
}

@Composable
fun ListItem(item: WeatherModel, currentDay: MutableState<WeatherModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
            .clickable {
                currentDay.value
            },
        colors = CardDefaults.cardColors(containerColor = BlueLight),
        shape = RoundedCornerShape(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp, top = 5.dp,
                    bottom = 5.dp
                )
            ) {
                Text(text = item.time, color = Color.White)
                Text(text = item.condition, color = Color.White)
            }
            Text(
                text = item.currentTemp.ifEmpty { "${item.maxTemp}/${item.minTemp}" },
                color = Color.White,
                fontSize = 25.sp
            )
            AsyncImage(
                model = "https:${item.icon}",
                contentDescription = "im4",
                modifier = Modifier
                    .size(35.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun DialogSearch(dialogState: MutableState<Boolean>, onSubmit: (String) -> Unit) {
    val dialogTextState = remember {
        mutableStateOf("")
    }

    AlertDialog(onDismissRequest = {
        dialogState.value = false
    }, confirmButton = {
        TextButton(onClick = {
            onSubmit(dialogTextState.value)
            dialogState.value = false
        }) {
            Text(text = "OK")
        }
    }, dismissButton = {
        TextButton(onClick = {
            dialogState.value = false
        }) {
            Text(text = "Cancel")
        }
    },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Введите название города:")
                TextField(value = dialogTextState.value, onValueChange = {
                    dialogTextState.value = it
                })
            }
        }
    )
}