package com.example.bharatnews.repository

import com.example.bharatnews.api.RetrofitInstance
import com.example.bharatnews.db.ArticleDatabase
import com.example.bharatnews.models.Article

class NewsRepository(val db : ArticleDatabase) {

  suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
      RetrofitInstance.api.getHeadlines(countryCode, pageNumber)


  suspend fun searchNews(searchQuery: String, pageNumber: Int) =
      RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

  suspend fun upseart(article: Article) = db.getArticleDao().upsert(article)

  fun getFavouriteNews() = db.getArticleDao().getALlArticle()


  suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}