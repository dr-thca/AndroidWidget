package com.example.androidwidget

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder



data class Article(
    val title: String,
    val url: String,
    val urn: String,
    val siteTitle: String,
    val imageUrl: String?
)

/*
 */
class ArticleFetcher {
    /*
    #if PROD || BETA
    static let GRAPHQL_BASE_PATH="https://www.dr.dk/tjenester/steffi/graphql/"
    static let FRONT_PAGE_ID="5d023c534ff3c845599d0953"
    static let BASE_PATH=URL(string: "https://www.dr.dk/")
    #elseif DEVELOPMENT
     */
    val GRAPHQL_BASE_PATH="https://preprod.dr.dk/tjenester/steffi/graphql/"
    val FRONT_PAGE_ID="622214d4fd7bc708176adc32"
    val BASE_PATH=URL("https://preprod.dr.dk/")
    suspend fun fetchArticles(): Array<Article>
    {

        val basePath = GRAPHQL_BASE_PATH
        val graphqlQuery = "query NewsOverview(\$frontPageId: String!) {\n  frontPage(id: \$frontPageId) {\n    topStories(limit: 11) {\n      title\n      image {\n      url\n      }\n      article {\n        urn\n        urlPathId\n        site {\n          title\n        }\n      }\n    }\n  }\n}"
        // url encode the query
        val variables = URLEncoder.encode("{ \"frontPageId\": \"${FRONT_PAGE_ID}\"}", "UTF-8")
        val encodedGraphqlQuery = URLEncoder.encode(graphqlQuery, "UTF-8")
        val url = "${basePath}?query=${encodedGraphqlQuery}&variables=${variables}"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val res = response.body?.string()?.let { JSONObject(it) }
        val topStories = res!!.getJSONObject("data").getJSONObject("frontPage").getJSONArray("topStories")

        val topStoriesArray = (0 until topStories.length()).map { topStories.getJSONObject(it) }
        val filteredTopStories = topStoriesArray.filter { story: JSONObject ->
            val urlPathId = story.getJSONObject("article").getString("urlPathId")
            !urlPathId.startsWith("/drtv/") && !urlPathId.startsWith("/drlyd/")
        }
        return filteredTopStories.map { frontPageArticle: JSONObject ->
            val title = frontPageArticle.getString("title")
            val article = frontPageArticle.getJSONObject("article")
            val articleUrl = URL("${BASE_PATH}${article.getString("urlPathId")}")
            val urn = article.getString("urn")
            val siteTitle = article.getJSONObject("site").getString("title")
            val image = runCatching {  frontPageArticle.getJSONObject("image") }.getOrNull()
            val imageUrl = image?.getString("url")
            Article(title, articleUrl.toString(), urn, siteTitle, imageUrl)
        }.toTypedArray()
    }
}