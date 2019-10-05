package com.vk.api.sdk.objects.groups.responses;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.groups.Group;
import java.util.List;
import java.util.Objects;

/**
 * GetCatalogResponse object
 */
public class GetCatalogResponse {
    /**
     * Total communities number
     */
    @SerializedName("count")
    private Integer count;

    @SerializedName("items")
    private List<Group> items;

    public Integer getCount() {
        return count;
    }

    public GetCatalogResponse setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<Group> getItems() {
        return items;
    }

    public GetCatalogResponse setItems(List<Group> items) {
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
        GetCatalogResponse getCatalogResponse = (GetCatalogResponse) o;
        return Objects.equals(count, getCatalogResponse.count) &&
                Objects.equals(items, getCatalogResponse.items);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        final StringBuilder sb = new StringBuilder("GetCatalogResponse{");
        sb.append("count=").append(count);
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
