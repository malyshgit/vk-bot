package com.vk.api.sdk.objects.base;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Objects;

public class AudioMessage {

    @SerializedName("id")
    private Integer id;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("waveform")
    private Integer[] waveform;
    @SerializedName("link_ogg")
    private String linkOgg;
    @SerializedName("link_mp3")
    private String linkMp3;
    @SerializedName("access_key")
    private String accessKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer[] getWaveform() {
        return waveform;
    }

    public void setWaveform(Integer[] waveform) {
        this.waveform = waveform;
    }

    public String getLinkOgg() {
        return linkOgg;
    }

    public void setLinkOgg(String linkOgg) {
        this.linkOgg = linkOgg;
    }

    public String getLinkMp3() {
        return linkMp3;
    }

    public void setLinkMp3(String linkMp3) {
        this.linkMp3 = linkMp3;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioMessage)) return false;
        AudioMessage that = (AudioMessage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(duration, that.duration) &&
                Arrays.equals(waveform, that.waveform) &&
                Objects.equals(linkOgg, that.linkOgg) &&
                Objects.equals(linkMp3, that.linkMp3) &&
                Objects.equals(accessKey, that.accessKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, ownerId, duration, linkOgg, linkMp3, accessKey);
        result = 31 * result + Arrays.hashCode(waveform);
        return result;
    }

    @Override
    public String toString() {
        return "AudioMessage{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", duration=" + duration +
                ", waveform=" + Arrays.toString(waveform) +
                ", linkOgg='" + linkOgg + '\'' +
                ", linkMp3='" + linkMp3 + '\'' +
                ", accessKey='" + accessKey + '\'' +
                '}';
    }
}
