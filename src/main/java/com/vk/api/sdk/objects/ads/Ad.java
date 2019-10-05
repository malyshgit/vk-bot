package com.vk.api.sdk.objects.ads;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.BoolInt;
import java.util.Objects;

/**
 * Ad object
 */
public class Ad {
    /**
     * Ad format
     */
    @SerializedName("ad_format")
    private Integer adFormat;

    /**
     * Ad platform
     */
    @SerializedName("ad_platform")
    private JsonObject adPlatform;

    /**
     * Total limit
     */
    @SerializedName("all_limit")
    private Integer allLimit;

    @SerializedName("approved")
    private AdApproved approved;

    /**
     * Campaign ID
     */
    @SerializedName("campaign_id")
    private Integer campaignId;

    /**
     * Category ID
     */
    @SerializedName("category1_id")
    private Integer category1Id;

    /**
     * Additional category ID
     */
    @SerializedName("category2_id")
    private Integer category2Id;

    @SerializedName("cost_type")
    private AdCostType costType;

    /**
     * Cost of a click, kopecks
     */
    @SerializedName("cpc")
    private Integer cpc;

    /**
     * Cost of 1000 impressions, kopecks
     */
    @SerializedName("cpm")
    private Integer cpm;

    /**
     * Information whether disclaimer is enabled
     */
    @SerializedName("disclaimer_medical")
    private BoolInt disclaimerMedical;

    /**
     * Information whether disclaimer is enabled
     */
    @SerializedName("disclaimer_specialist")
    private BoolInt disclaimerSpecialist;

    /**
     * Information whether disclaimer is enabled
     */
    @SerializedName("disclaimer_supplements")
    private BoolInt disclaimerSupplements;

    /**
     * Ad ID
     */
    @SerializedName("id")
    private Integer id;

    /**
     * Impressions limit
     */
    @SerializedName("impressions_limit")
    private Integer impressionsLimit;

    /**
     * Information whether impressions are limited
     */
    @SerializedName("impressions_limited")
    private BoolInt impressionsLimited;

    /**
     * Ad title
     */
    @SerializedName("name")
    private String name;

    @SerializedName("status")
    private AdStatus status;

    /**
     * Information whether the ad is a video
     */
    @SerializedName("video")
    private BoolInt video;

    public Integer getAdFormat() {
        return adFormat;
    }

    public Ad setAdFormat(Integer adFormat) {
        this.adFormat = adFormat;
        return this;
    }

    public JsonObject getAdPlatform() {
        return adPlatform;
    }

    public Ad setAdPlatform(JsonObject adPlatform) {
        this.adPlatform = adPlatform;
        return this;
    }

    public Integer getAllLimit() {
        return allLimit;
    }

    public Ad setAllLimit(Integer allLimit) {
        this.allLimit = allLimit;
        return this;
    }

    public AdApproved getApproved() {
        return approved;
    }

    public Ad setApproved(AdApproved approved) {
        this.approved = approved;
        return this;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public Ad setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
        return this;
    }

    public Integer getCategory1Id() {
        return category1Id;
    }

    public Ad setCategory1Id(Integer category1Id) {
        this.category1Id = category1Id;
        return this;
    }

    public Integer getCategory2Id() {
        return category2Id;
    }

    public Ad setCategory2Id(Integer category2Id) {
        this.category2Id = category2Id;
        return this;
    }

    public AdCostType getCostType() {
        return costType;
    }

    public Ad setCostType(AdCostType costType) {
        this.costType = costType;
        return this;
    }

    public Integer getCpc() {
        return cpc;
    }

    public Ad setCpc(Integer cpc) {
        this.cpc = cpc;
        return this;
    }

    public Integer getCpm() {
        return cpm;
    }

    public Ad setCpm(Integer cpm) {
        this.cpm = cpm;
        return this;
    }

    public boolean isDisclaimerMedical() {
        return disclaimerMedical == BoolInt.YES;
    }

    public BoolInt getDisclaimerMedical() {
        return disclaimerMedical;
    }

    public boolean isDisclaimerSpecialist() {
        return disclaimerSpecialist == BoolInt.YES;
    }

    public BoolInt getDisclaimerSpecialist() {
        return disclaimerSpecialist;
    }

    public boolean isDisclaimerSupplements() {
        return disclaimerSupplements == BoolInt.YES;
    }

    public BoolInt getDisclaimerSupplements() {
        return disclaimerSupplements;
    }

    public Integer getId() {
        return id;
    }

    public Ad setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getImpressionsLimit() {
        return impressionsLimit;
    }

    public Ad setImpressionsLimit(Integer impressionsLimit) {
        this.impressionsLimit = impressionsLimit;
        return this;
    }

    public boolean isImpressionsLimited() {
        return impressionsLimited == BoolInt.YES;
    }

    public BoolInt getImpressionsLimited() {
        return impressionsLimited;
    }

    public String getName() {
        return name;
    }

    public Ad setName(String name) {
        this.name = name;
        return this;
    }

    public AdStatus getStatus() {
        return status;
    }

    public Ad setStatus(AdStatus status) {
        this.status = status;
        return this;
    }

    public boolean isVideo() {
        return video == BoolInt.YES;
    }

    public BoolInt getVideo() {
        return video;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpm, impressionsLimit, allLimit, campaignId, category2Id, disclaimerMedical, category1Id, disclaimerSpecialist, video, disclaimerSupplements, adPlatform, approved, impressionsLimited, costType, cpc, name, id, adFormat, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return Objects.equals(adFormat, ad.adFormat) &&
                Objects.equals(cpm, ad.cpm) &&
                Objects.equals(costType, ad.costType) &&
                Objects.equals(video, ad.video) &&
                Objects.equals(allLimit, ad.allLimit) &&
                Objects.equals(category1Id, ad.category1Id) &&
                Objects.equals(impressionsLimited, ad.impressionsLimited) &&
                Objects.equals(approved, ad.approved) &&
                Objects.equals(disclaimerSpecialist, ad.disclaimerSpecialist) &&
                Objects.equals(adPlatform, ad.adPlatform) &&
                Objects.equals(cpc, ad.cpc) &&
                Objects.equals(impressionsLimit, ad.impressionsLimit) &&
                Objects.equals(name, ad.name) &&
                Objects.equals(id, ad.id) &&
                Objects.equals(disclaimerSupplements, ad.disclaimerSupplements) &&
                Objects.equals(campaignId, ad.campaignId) &&
                Objects.equals(category2Id, ad.category2Id) &&
                Objects.equals(disclaimerMedical, ad.disclaimerMedical) &&
                Objects.equals(status, ad.status);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        final StringBuilder sb = new StringBuilder("Ad{");
        sb.append("adFormat=").append(adFormat);
        sb.append(", cpm=").append(cpm);
        sb.append(", costType=").append(costType);
        sb.append(", video=").append(video);
        sb.append(", allLimit=").append(allLimit);
        sb.append(", category1Id=").append(category1Id);
        sb.append(", impressionsLimited=").append(impressionsLimited);
        sb.append(", approved=").append(approved);
        sb.append(", disclaimerSpecialist=").append(disclaimerSpecialist);
        sb.append(", adPlatform=").append(adPlatform);
        sb.append(", cpc=").append(cpc);
        sb.append(", impressionsLimit=").append(impressionsLimit);
        sb.append(", name='").append(name).append("'");
        sb.append(", id=").append(id);
        sb.append(", disclaimerSupplements=").append(disclaimerSupplements);
        sb.append(", campaignId=").append(campaignId);
        sb.append(", category2Id=").append(category2Id);
        sb.append(", disclaimerMedical=").append(disclaimerMedical);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
