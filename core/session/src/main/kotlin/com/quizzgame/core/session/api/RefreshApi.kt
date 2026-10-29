package com.quizzgame.core.session.api

import com.quizzgame.core.session.api.dto.RefreshRequestDto
import com.quizzgame.core.session.api.dto.RefreshResponseDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

internal interface RefreshApi {
    @POST
    fun refresh(@Url path: String, @Body request: RefreshRequestDto): Call<RefreshResponseDto>
}
