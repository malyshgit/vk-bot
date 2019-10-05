package com.vk.api.sdk.objects.account;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

/**
 * PushParams object
 */
public class PushParams {
    @SerializedName("app_request")
    private List<OnoffOptions> appRequest;

    @SerializedName("birthday")
    private List<OnoffOptions> birthday;

    @SerializedName("chat")
    private List<PushParamsMode> chat;

    @SerializedName("comment")
    private List<PushParamsSettings> comment;

    @SerializedName("event_soon")
    private List<OnoffOptions> eventSoon;

    @SerializedName("friend")
    private List<OnoffOptions> friend;

    @SerializedName("friend_accepted")
    private List<OnoffOptions> friendAccepted;

    @SerializedName("friend_found")
    private List<OnoffOptions> friendFound;

    @SerializedName("group_accepted")
    private List<OnoffOptions> groupAccepted;

    @SerializedName("group_invite")
    private List<OnoffOptions> groupInvite;

    @SerializedName("like")
    private List<PushParamsSettings> like;

    @SerializedName("mention")
    private List<PushParamsSettings> mention;

    @SerializedName("msg")
    private List<PushParamsMode> msg;

    @SerializedName("new_post")
    private List<OnoffOptions> newPost;

    @SerializedName("photos_tag")
    private List<PushParamsSettings> photosTag;

    @SerializedName("reply")
    private List<OnoffOptions> reply;

    @SerializedName("repost")
    private List<PushParamsSettings> repost;

    @SerializedName("sdk_open")
    private List<OnoffOptions> sdkOpen;

    @SerializedName("wall_post")
    private List<OnoffOptions> wallPost;

    @SerializedName("wall_publish")
    private List<OnoffOptions> wallPublish;

    public List<OnoffOptions> getAppRequest() {
        return appRequest;
    }

    public PushParams setAppRequest(List<OnoffOptions> appRequest) {
        this.appRequest = appRequest;
        return this;
    }

    public List<OnoffOptions> getBirthday() {
        return birthday;
    }

    public PushParams setBirthday(List<OnoffOptions> birthday) {
        this.birthday = birthday;
        return this;
    }

    public List<PushParamsMode> getChat() {
        return chat;
    }

    public PushParams setChat(List<PushParamsMode> chat) {
        this.chat = chat;
        return this;
    }

    public List<PushParamsSettings> getComment() {
        return comment;
    }

    public PushParams setComment(List<PushParamsSettings> comment) {
        this.comment = comment;
        return this;
    }

    public List<OnoffOptions> getEventSoon() {
        return eventSoon;
    }

    public PushParams setEventSoon(List<OnoffOptions> eventSoon) {
        this.eventSoon = eventSoon;
        return this;
    }

    public List<OnoffOptions> getFriend() {
        return friend;
    }

    public PushParams setFriend(List<OnoffOptions> friend) {
        this.friend = friend;
        return this;
    }

    public List<OnoffOptions> getFriendAccepted() {
        return friendAccepted;
    }

    public PushParams setFriendAccepted(List<OnoffOptions> friendAccepted) {
        this.friendAccepted = friendAccepted;
        return this;
    }

    public List<OnoffOptions> getFriendFound() {
        return friendFound;
    }

    public PushParams setFriendFound(List<OnoffOptions> friendFound) {
        this.friendFound = friendFound;
        return this;
    }

    public List<OnoffOptions> getGroupAccepted() {
        return groupAccepted;
    }

    public PushParams setGroupAccepted(List<OnoffOptions> groupAccepted) {
        this.groupAccepted = groupAccepted;
        return this;
    }

    public List<OnoffOptions> getGroupInvite() {
        return groupInvite;
    }

    public PushParams setGroupInvite(List<OnoffOptions> groupInvite) {
        this.groupInvite = groupInvite;
        return this;
    }

    public List<PushParamsSettings> getLike() {
        return like;
    }

    public PushParams setLike(List<PushParamsSettings> like) {
        this.like = like;
        return this;
    }

    public List<PushParamsSettings> getMention() {
        return mention;
    }

    public PushParams setMention(List<PushParamsSettings> mention) {
        this.mention = mention;
        return this;
    }

    public List<PushParamsMode> getMsg() {
        return msg;
    }

    public PushParams setMsg(List<PushParamsMode> msg) {
        this.msg = msg;
        return this;
    }

    public List<OnoffOptions> getNewPost() {
        return newPost;
    }

    public PushParams setNewPost(List<OnoffOptions> newPost) {
        this.newPost = newPost;
        return this;
    }

    public List<PushParamsSettings> getPhotosTag() {
        return photosTag;
    }

    public PushParams setPhotosTag(List<PushParamsSettings> photosTag) {
        this.photosTag = photosTag;
        return this;
    }

    public List<OnoffOptions> getReply() {
        return reply;
    }

    public PushParams setReply(List<OnoffOptions> reply) {
        this.reply = reply;
        return this;
    }

    public List<PushParamsSettings> getRepost() {
        return repost;
    }

    public PushParams setRepost(List<PushParamsSettings> repost) {
        this.repost = repost;
        return this;
    }

    public List<OnoffOptions> getSdkOpen() {
        return sdkOpen;
    }

    public PushParams setSdkOpen(List<OnoffOptions> sdkOpen) {
        this.sdkOpen = sdkOpen;
        return this;
    }

    public List<OnoffOptions> getWallPost() {
        return wallPost;
    }

    public PushParams setWallPost(List<OnoffOptions> wallPost) {
        this.wallPost = wallPost;
        return this;
    }

    public List<OnoffOptions> getWallPublish() {
        return wallPublish;
    }

    public PushParams setWallPublish(List<OnoffOptions> wallPublish) {
        this.wallPublish = wallPublish;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(birthday, msg, wallPost, appRequest, groupAccepted, friendAccepted, like, eventSoon, mention, wallPublish, chat, groupInvite, friend, friendFound, newPost, comment, photosTag, reply, sdkOpen, repost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushParams pushParams = (PushParams) o;
        return Objects.equals(birthday, pushParams.birthday) &&
                Objects.equals(msg, pushParams.msg) &&
                Objects.equals(friendAccepted, pushParams.friendAccepted) &&
                Objects.equals(wallPublish, pushParams.wallPublish) &&
                Objects.equals(like, pushParams.like) &&
                Objects.equals(wallPost, pushParams.wallPost) &&
                Objects.equals(appRequest, pushParams.appRequest) &&
                Objects.equals(newPost, pushParams.newPost) &&
                Objects.equals(eventSoon, pushParams.eventSoon) &&
                Objects.equals(mention, pushParams.mention) &&
                Objects.equals(groupAccepted, pushParams.groupAccepted) &&
                Objects.equals(photosTag, pushParams.photosTag) &&
                Objects.equals(sdkOpen, pushParams.sdkOpen) &&
                Objects.equals(chat, pushParams.chat) &&
                Objects.equals(friend, pushParams.friend) &&
                Objects.equals(comment, pushParams.comment) &&
                Objects.equals(friendFound, pushParams.friendFound) &&
                Objects.equals(groupInvite, pushParams.groupInvite) &&
                Objects.equals(reply, pushParams.reply) &&
                Objects.equals(repost, pushParams.repost);
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        final StringBuilder sb = new StringBuilder("PushParams{");
        sb.append("birthday=").append(birthday);
        sb.append(", msg=").append(msg);
        sb.append(", friendAccepted=").append(friendAccepted);
        sb.append(", wallPublish=").append(wallPublish);
        sb.append(", like=").append(like);
        sb.append(", wallPost=").append(wallPost);
        sb.append(", appRequest=").append(appRequest);
        sb.append(", newPost=").append(newPost);
        sb.append(", eventSoon=").append(eventSoon);
        sb.append(", mention=").append(mention);
        sb.append(", groupAccepted=").append(groupAccepted);
        sb.append(", photosTag=").append(photosTag);
        sb.append(", sdkOpen=").append(sdkOpen);
        sb.append(", chat=").append(chat);
        sb.append(", friend=").append(friend);
        sb.append(", comment=").append(comment);
        sb.append(", friendFound=").append(friendFound);
        sb.append(", groupInvite=").append(groupInvite);
        sb.append(", reply=").append(reply);
        sb.append(", repost=").append(repost);
        sb.append('}');
        return sb.toString();
    }
}
