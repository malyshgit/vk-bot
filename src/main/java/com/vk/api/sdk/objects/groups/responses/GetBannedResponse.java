package com.vk.api.sdk.objects.groups.responses;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.groups.OwnerXtrBanInfo;
import java.util.List;
import java.util.Objects;

/**
 * GetBannedResponse object
 */
public class GetBannedResponse {
    /**
     * Total users number
     */
    @SerializedName("count")
    private Integer count;

    @SerializedName("items")
    private List<OwnerXtrBanInfo> items;

    public Integer getCount() {
        return count;
    }

    public GetBannedResponse setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<OwnerXtrBanInfo> getItems() {
        return items;
    }

    public GetBannedResponse setItems(List<OwnerXtrBanInfo> items) {
        this.items = items;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetBannedResponse getBannedResponse = (GetBannedResponse) o;
        return Objects.equals(count, getBannedResponse.count) &&
                Objects.equals(items, getBannedResponse.items);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        final StringBuilder sb = new StringBuilder("GetBannedResponse{");
        sb.append("count=").append(count);
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
