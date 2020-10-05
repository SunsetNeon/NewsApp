package com.android.newsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.newsapp.adapters.RecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.Locale.filter
import kotlin.system.measureTimeMillis

const val BASE_URL = "http://newsapi.org"
var SEARCH_for = "Europe"

class MainActivity : AppCompatActivity() {

    lateinit var countDownTimer: CountDownTimer

    private var titlesList = mutableListOf<String>()
    private var descriptionList = mutableListOf<String>()
    private var imagesList = mutableListOf<String>()
    private var linksList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeAPIRequest()
    }

    private fun fadefromBlack() { // анимация перехода от тёмного экрана к контенту
        v_blackScreen.animate().apply {
            alpha(0f)
            duration = 3000

        }.start()

    }


    private fun setUpRecyclerView() {
        rv_recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        rv_recyclerView.adapter = RecyclerAdapter(titlesList,descriptionList,imagesList,linksList)

    }

    private fun addToList (title: String, descritpion: String, image: String, link: String) {
        titlesList.add(title)
        descriptionList.add(descritpion)
        imagesList.add(image)
        linksList.add(link)
    }



    private fun makeAPIRequest() {// реализация запроса к апи, retrofit + корутины

        progressBar.visibility = View.VISIBLE

        val api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(APIRequest::class.java)

        GlobalScope.launch (Dispatchers.IO) {
            try {
                val response = api.getNews()

                for (article in response.articles) {
                    Log.i("MainActivity", "Result = $article")
                    addToList(article.title, article.description,
                        article.urlToImage as String, article.url)

                }

                withContext(Dispatchers.Main) {
                    setUpRecyclerView()
                    fadefromBlack()
                    progressBar.visibility = View.GONE
                }

            } catch (e: Exception) { //в случае ошибки
                Log.e( "MainActivity", e.toString())

                withContext (Dispatchers.Main) {
                    attemptRequestAgain()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {//реализация поиска
        menuInflater.inflate(R.menu.nav_menu, menu)
        val searchView = menu?.findItem(R.id.nav_search)?.actionView as SearchView

        // поиск работает после нажатия submit (enter)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                SEARCH_for = query as String //query = слово введённое в поиске
                clearLists() //удаляет статьи относящиеся к предыдущему поисковому запросу
                makeAPIRequest()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //пусто, мб в будущем пригодится
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }


    private fun clearLists() { //удаляет старые предметы из листов
        titlesList.clear()
        descriptionList.clear()
        imagesList.clear()
        linksList.clear()
    }

    private fun attemptRequestAgain() { //повторный реквест при неполадках с интернетом
        countDownTimer = object: CountDownTimer(5*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.i("MainActivity", "Не удалось получить данные, повторная попытка через ${millisUntilFinished/1000} секунд")
            }

            override fun onFinish() {
                makeAPIRequest()
                countDownTimer.cancel()
            }
        }
        countDownTimer.start()
    }
}

