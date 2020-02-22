package com.sujin.payoff;

import com.google.gson.annotations.SerializedName;

public class Balance {

    @SerializedName("value")
    private int value;

    public Balance(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

