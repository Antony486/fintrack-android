package com.fintrack.data.remote.api

import com.fintrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>
}

interface TransactionApi {
    @GET("transactions")
    suspend fun getAll(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("currency") currency: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<TransactionDto>>

    @GET("transactions/summary")
    suspend fun getSummary(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<TransactionSummaryDto>

    @GET("transactions/{id}")
    suspend fun getById(@Path("id") id: Int): Response<TransactionDto>

    @POST("transactions")
    suspend fun create(@Body request: TransactionRequestDto): Response<TransactionDto>

    @PUT("transactions/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: TransactionUpdateDto): Response<TransactionDto>

    @DELETE("transactions/{id}")
    suspend fun delete(@Path("id") id: Int): Response<MessageResponseDto>
}

interface BudgetApi {
    @GET("budgets")
    suspend fun getAll(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<List<BudgetDto>>

    @GET("budgets/status")
    suspend fun getStatus(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<List<BudgetStatusDto>>

    @POST("budgets")
    suspend fun create(@Body request: BudgetRequestDto): Response<BudgetDto>

    @PUT("budgets/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: BudgetUpdateDto): Response<BudgetDto>

    @DELETE("budgets/{id}")
    suspend fun delete(@Path("id") id: Int): Response<MessageResponseDto>
}

interface BillApi {
    @GET("bills")
    suspend fun getAll(): Response<List<BillDto>>

    @GET("bills/upcoming")
    suspend fun getUpcoming(@Query("days") days: Int = 7): Response<List<BillDto>>

    @POST("bills")
    suspend fun create(@Body request: BillRequestDto): Response<BillDto>

    @PUT("bills/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: BillUpdateDto): Response<BillDto>

    @PUT("bills/{id}/paid")
    suspend fun markPaid(@Path("id") id: Int): Response<BillDto>

    @DELETE("bills/{id}")
    suspend fun delete(@Path("id") id: Int): Response<MessageResponseDto>
}

interface CurrencyApi {
    @GET("currency/rates")
    suspend fun getRates(@Query("base") base: String = "KES"): Response<RatesResponseDto>

    @GET("currency/convert")
    suspend fun convert(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): Response<ConvertResponseDto>
}

interface MpesaApi {
    @POST("mpesa/stk-push")
    suspend fun stkPush(@Body request: StkPushRequestDto): Response<StkPushResponseDto>

    @GET("mpesa/status/{checkoutRequestId}")
    suspend fun getStatus(@Path("checkoutRequestId") checkoutRequestId: String): Response<MpesaStatusResponseDto>
}

interface StripeApi {
    @POST("stripe/payment-intent")
    suspend fun createPaymentIntent(@Body request: StripePaymentIntentRequestDto): Response<StripePaymentIntentResponseDto>
}
