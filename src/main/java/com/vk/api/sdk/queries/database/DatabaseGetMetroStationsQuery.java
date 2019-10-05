package com.vk.api.sdk.queries.database;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.database.responses.GetMetroStationsResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Query for Database.getMetroStations method
 */
public class DatabaseGetMetroStationsQuery extends AbstractQueryBuilder<DatabaseGetMetroStationsQuery, GetMetroStationsResponse> {
    /**
     * Creates a AbstractQueryBuilder instance that can be used to build api request with various parameters
     *
     * @param client VK API client
     * @param actor actor with access token
     * @param cityId value of "city id" parameter. Minimum is 0.
     */
    public DatabaseGetMetroStationsQuery(VkApiClient client, UserActor actor, int cityId) {
        super(client, "database.getMetroStations", GetMetroStationsResponse.class);
        accessToken(actor.getAccessToken());
        cityId(cityId);
    }

    /**
     * Creates a AbstractQueryBuilder instance that can be used to build api request with various parameters
     *
     * @param client VK API client
     * @param actor actor with access token
     * @param cityId value of "city id" parameter. Minimum is 0.
     */
    public DatabaseGetMetroStationsQuery(VkApiClient client, ServiceActor actor, int cityId) {
        super(client, "database.getMetroStations", GetMetroStationsResponse.class);
        accessToken(actor.getAccessToken());
        clientSecret(actor.getClientSecret());
        cityId(cityId);
    }

    /**
     * Set city id
     *
     * @param value value of "city id" parameter. Minimum is 0.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    protected DatabaseGetMetroStationsQuery cityId(int value) {
        return unsafeParam("city_id", value);
    }

    /**
     * Set offset
     *
     * @param value value of "offset" parameter. Minimum is 0.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public DatabaseGetMetroStationsQuery offset(Integer value) {
        return unsafeParam("offset", value);
    }

    /**
     * Set count
     *
     * @param value value of "count" parameter. Maximum is 500. Minimum is 0. By default 100.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public DatabaseGetMetroStationsQuery count(Integer value) {
        return unsafeParam("count", value);
    }

    /**
     * Set extended
     *
     * @param value value of "extended" parameter. By default false.
     * @return a reference to this {@code AbstractQueryBuilder} object to fulfill the "Builder" pattern.
     */
    public DatabaseGetMetroStationsQuery extended(Boolean value) {
        return unsafeParam("extended", value);
    }

    @Override
    protected DatabaseGetMetroStationsQuery getThis() {
        return this;
    }

    @Override
    protected List<String> essentialKeys() {
        return Arrays.asList("city_id", "access_token");
    }
}
