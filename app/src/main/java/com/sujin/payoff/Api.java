package com.sujin.payoff;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {

    String BASE_URL = "http://192.168.43.232:9000";

    @POST("/api")
    Call<MoneyPayload> sendMoney(@Body MoneyPayload moneyPayload);

    @GET("/api/customer/balance/{id}")
    Call<Balance> checkBalance(@Path("id") String id);

    @GET("/api/customer/getDetails/{id}")
    Call<Wallet> checkWallet(@Path("id") String id);

    @POST("/api/transaction/create")
    Call<CreateTransaction> createTransaction(@Body CreateTransaction createTransaction);

    @GET("/api/transaction/{id}")
    Call<CheckTransaction> checkTransaction(@Path("id") String id);

    @GET("/api/init/{id}")
    Call<Initialization> initialize(@Path("id") String id);


}
