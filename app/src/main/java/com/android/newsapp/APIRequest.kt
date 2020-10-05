package com.android.newsapp

import com.android.newsapp.api.NewsApiJSON
import retrofit2.http.GET
import retrofit2.http.Query

interface APIRequest {

    @GET("/v2/everything")
    //@GET("/v2/top-headlines?country=ru&apiKey=f1cf990c198f4fcd8fdb0a3abff97bac")
    suspend fun getNews(
        @Query("q")
        searchQuery: String = SEARCH_for,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = "f1cf990c198f4fcd8fdb0a3abff97bac"
    ) : NewsApiJSON
}