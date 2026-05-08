package com.openstream.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Url

interface ExtensionApiService {
    @GET
    suspend fun fetchRepo(@Url url: String): List<ExtensionDto>
}
