package com.sujin.payoff;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateTransaction {

    @SerializedName("amount")
    private List<String> amount;

    @SerializedName("fromAddress")
    private String from;

    @SerializedName("toAddress")
    private String to;

    @SerializedName("value")
    private int value;

    @SerializedName("time")
    private String time;

    public CreateTransaction(List<String> amount,String from, String to,int value,String time){
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.value = value;
        this.time = time;
    }

    public List<String> getAmount() {
        return amount;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getValue() {
        return value;
    }

    public String getTime() {
        return time;
    }

    public void setAmount(List<String> amount) {
        this.amount = amount;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
