package com.sujin.payoff;

public class SendData {
    String to;
    String value;

    public SendData(String to, String value)
    {
        this.to = to;
        this.value = value;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
