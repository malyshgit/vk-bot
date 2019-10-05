package com.vk.api.sdk.objects.search.responses;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.search.Hint;
import java.util.List;
import java.util.Objects;

/**
 * GetHintsResponse object
 */
public class GetHintsResponse {
    @SerializedName("items")
    private List<Hint> items;

    @SerializedName("suggested_queries")
    private List<String> suggestedQueries;

    public List<Hint> getItems() {
        return items;
    }

    public GetHintsResponse setItems(List<Hint> items) {
        this.items = items;
        return this;
    }

    public List<String> getSuggestedQueries() {
        return suggestedQueries;
    }

    public GetHintsResponse setSuggestedQueries(List<String> suggestedQueries) {
        this.suggestedQueries = suggestedQueries;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suggestedQueries, items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetHintsResponse getHintsResponse = (GetHintsResponse) o;
        return Objects.equals(suggestedQueries, getHintsResponse.suggestedQueries) &&
                Objects.equals(items, getHintsResponse.items);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        final StringBuilder sb = new StringBuilder("GetHintsResponse{");
        sb.append("suggestedQueries='").append(suggestedQueries).append("'");
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
