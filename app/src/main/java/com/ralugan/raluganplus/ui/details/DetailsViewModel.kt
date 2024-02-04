package com.ralugan.raluganplus.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ralugan.raluganplus.api.ApiClient
import com.ralugan.raluganplus.api.WikidataApi
import com.ralugan.raluganplus.dataclass.DetailsItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

class DetailsViewModel(private val title: String) : ViewModel() {

    private val wikidataApi: WikidataApi = ApiClient.getWikidataApi()

    private val _detailsItemList = MutableLiveData<List<DetailsItem>>()
    val detailsItemList: LiveData<List<DetailsItem>> get() = _detailsItemList

    fun fetchDetails() {
        val sparqlQuery = """
            SELECT ?itemLabel ?pic ?note ?award ?cost ?date ?dir ?duration ?episodes ?seasons ?genre
            WHERE {
            {
                ?seriesItem wdt:P1476 ?itemLabel. # Title
                ?seriesItem wdt:P2047 ?duration.
                ?seriesItem wdt:P57 ?dir.
                ?seriesItem wdt:P136 ?genre.
                ?seriesItem wdt:P31 wd:Q5398426.  # Television series
                ?seriesItem wdt:P750 wd:Q54958752.  # Platform = Disney+

                FILTER(CONTAINS(UCASE(?itemLabel), UCASE('$title')))
                OPTIONAL{
                  ?seriesItem wdt:P1113 ?episodes.  # Episodes
                  }.
                OPTIONAL{
                  ?seriesItem wdt:P2437 ?seasons.  # Seasons
                  }.
                OPTIONAL{
                      ?seriesItem wdt:P154 ?pic}.
                      OPTIONAL{
                      ?seriesItem wdt:P1258 ?note}.
                        OPTIONAL{
                      ?seriesItem wdt:P166 ?award}.
                          OPTIONAL{
                      ?seriesItem wdt:P2130 ?cost}.
                            OPTIONAL{
                      ?seriesItem wdt:P580 ?date}.
            }
            UNION
            {
                ?filmItem wdt:P1476 ?itemLabel. # Title
                ?filmItem wdt:P2047 ?duration.
                ?filmItem wdt:P57 ?dir.
                ?filmItem wdt:P136 ?genre.
                ?filmItem wdt:P31 wd:Q11424.  # Film
                ?filmItem wdt:P750 wd:Q54958752.  # Platform = Disney+

                FILTER(CONTAINS(UCASE(?itemLabel), UCASE('$title')))
                OPTIONAL{
                      ?filmItem wdt:P154 ?pic}.
                      OPTIONAL{
                      ?filmItem wdt:P1258 ?note}.
                        OPTIONAL{
                      ?filmItem wdt:P166 ?award}.
                          OPTIONAL{
                      ?filmItem wdt:P2130 ?cost}.
                            OPTIONAL{
                      ?filmItem wdt:P580 ?date}.
            }
            }
            ORDER BY DESC (?pic)
        """.trimIndent()

        // Appel de l'API dans une coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = wikidataApi.getDisneyPlusInfo(sparqlQuery, "json").execute()

                // Utilisez Dispatchers.Main pour passer au thread principal
                withContext(Dispatchers.Main) {
                    handleApiResponse(response)
                }
            } catch (e: Exception) {
                // Gérer l'exception
            }
        }
    }

    private fun handleApiResponse(response: Response<ResponseBody>) {
        if (response.isSuccessful) {
            try {
                val jsonResult = JSONObject(response.body()?.string())

                if (jsonResult.has("results")) {
                    val results = jsonResult.getJSONObject("results")
                    val bindings = results.getJSONArray("bindings")
                    val detailsItemList = mutableListOf<DetailsItem>()

                    if (bindings.length() > 0) {
                        val firstBinding = bindings.getJSONObject(0) // Récupérez le premier élément seulement

                        val itemLabel = firstBinding.getJSONObject("itemLabel").optString("value", "N/A")
                        val pic = firstBinding.optJSONObject("pic")?.optString("value", "N/A")
                        val note = firstBinding.optJSONObject("note")?.optString("value", "N/A")
                        val award = firstBinding.optJSONObject("award")?.optString("value", "N/A")
                        val cost = firstBinding.optJSONObject("cost")?.optString("value", "N/A")
                        val date = firstBinding.optJSONObject("date")?.optString("value", "N/A")
                        val dir = firstBinding.optJSONObject("dir")?.optString("value", "N/A")
                        val duration = firstBinding.optJSONObject("duration")?.optString("value", "N/A")
                        val episodes = firstBinding.optJSONObject("episodes")?.optString("value", "N/A")
                        val seasons = firstBinding.optJSONObject("seasons")?.optString("value", "N/A")
                        val genre = firstBinding.optJSONObject("genre")?.optString("value", "N/A")

                        val detailsItem = DetailsItem(
                            itemLabel ?: "N/A",
                            pic ?: "N/A",
                            note ?: "N/A",
                            award ?: "N/A",
                            cost ?: "N/A",
                            date ?: "N/A",
                            dir ?: "N/A",
                            duration ?: "N/A",
                            episodes ?: "N/A",
                            seasons ?: "N/A",
                            genre ?: "N/A"
                        )
                        detailsItemList.add(detailsItem)
                    }

                    _detailsItemList.postValue(detailsItemList)
                }
            } catch (e: JSONException) {
                // Handle JSON parsing error
            }
        } else {
            // Handle API call failure
        }
    }
}
