package com.example.bharatnews.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bharatnews.models.Article
import com.example.bharatnews.models.NewsResponse
import com.example.bharatnews.repository.NewsRepository
import com.example.bharatnews.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Query
import java.io.IOException
import java.util.Locale.IsoCountryCode

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {

    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinePage = 1
    var headlineResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlineResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinePage++
                if (headlineResponse == null) {
                    headlineResponse = resultResponse
                } else {
                    val oldArticles = headlineResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlineResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    fun addToFavourites(article: Article) = viewModelScope.launch{
        newsRepository.upseart(article)
    }
    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch{
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE)as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                    else->false
                }
            }?: false
        }
    }

    private suspend fun headlinesInternet(countryCode: String){
        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val response = newsRepository.getHeadlines(countryCode, headlinePage)
                headlines.postValue(handleHeadlineResponse(response))
            }else{
                headlines.postValue(Resource.Error("No Internet Sir"))
            }
        }catch (t: Throwable){
            when(t){
                is  IOException ->headlines.postValue(Resource.Error("unable to connect"))
                else -> headlines.postValue(Resource.Error("No Signal"))
            }
        }
    }
    private suspend fun searchNewsInternet(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())){
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleHeadlineResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No internet Connection"))
            }
        }catch (t: Throwable){
            when(t){
                is IOException ->searchNews.postValue(Resource.Error("Unable to Connect"))
                else ->searchNews.postValue(Resource.Error("No Signal"))
            }
        }
    }
}
