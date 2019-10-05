package com.vk.api.sdk.objects.messages;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.queries.EnumParam;

/**
 * Button type
 */
public enum KeyboardButtonActionType implements EnumParam {
    @SerializedName("text")
    TEXT("text"),

    @SerializedName("start")
    START("start"),

    @SerializedName("location")
    LOCATION("location"),

    @SerializedName("open_app")
    VK_APPS("open_app"),

    @SerializedName("vkpay")
    VKPAY("vkpay");

    private final String value;

    KeyboardButtonActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toLowerCase();
    }
}
