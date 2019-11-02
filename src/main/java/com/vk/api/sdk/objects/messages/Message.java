package com.vk.api.sdk.objects.messages;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.Validable;
import com.vk.api.sdk.objects.annotations.Required;
import com.vk.api.sdk.objects.base.BoolInt;
import com.vk.api.sdk.objects.base.Geo;
import java.util.List;
import java.util.Objects;

public class Message implements Validable {
    @SerializedName("action")
    private MessageAction action;
    @SerializedName("admin_author_id")
    private Integer adminAuthorId;
    @SerializedName("attachments")
    private List<MessageAttachment> attachments;
    @SerializedName("conversation_message_id")
    private Integer conversationMessageId;
    @SerializedName("date")
    @Required
    private Integer date;
    @SerializedName("deleted")
    private BoolInt deleted;
    @SerializedName("from_id")
    private Integer fromId;
    @SerializedName("fwd_messages")
    private List<ForeignMessage> fwdMessages;
    @SerializedName("geo")
    private Geo geo;
    @SerializedName("id")
    @Required
    private Integer id;
    @SerializedName("important")
    private Boolean important;
    @SerializedName("is_hidden")
    private Boolean isHidden;
    @SerializedName("keyboard")
    private Keyboard keyboard;
    @SerializedName("members_count")
    private Integer membersCount;
    @SerializedName("out")
    @Required
    private BoolInt out;
    @SerializedName("payload")
    private String payload;
    @SerializedName("peer_id")
    private Integer peerId;
    @SerializedName("random_id")
    private Integer randomId;
    @SerializedName("ref")
    private String ref;
    @SerializedName("ref_source")
    private String refSource;
    @SerializedName("reply_message")
    private ForeignMessage replyMessage;
    @SerializedName("text")
    @Required
    private String text;
    @SerializedName("update_time")
    private Integer updateTime;

    public Message() {
    }

    public MessageAction getAction() {
        return this.action;
    }

    public Message setAction(MessageAction action) {
        this.action = action;
        return this;
    }

    public Integer getAdminAuthorId() {
        return this.adminAuthorId;
    }

    public Message setAdminAuthorId(Integer adminAuthorId) {
        this.adminAuthorId = adminAuthorId;
        return this;
    }

    public List<MessageAttachment> getAttachments() {
        return this.attachments;
    }

    public Message setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public Boolean fromConversation(){
        return this.peerId >= 2000000000;
    }

    public Integer getConversationMessageId() {
        return this.conversationMessageId;
    }

    public Message setConversationMessageId(Integer conversationMessageId) {
        this.conversationMessageId = conversationMessageId;
        return this;
    }

    public Integer getDate() {
        return this.date;
    }

    public Message setDate(Integer date) {
        this.date = date;
        return this;
    }

    public boolean isDeleted() {
        return this.deleted == BoolInt.YES;
    }

    public BoolInt getDeleted() {
        return this.deleted;
    }

    public Integer getFromId() {
        return this.fromId;
    }

    public Message setFromId(Integer fromId) {
        this.fromId = fromId;
        return this;
    }

    public List<ForeignMessage> getFwdMessages() {
        return this.fwdMessages;
    }

    public Message setFwdMessages(List<ForeignMessage> fwdMessages) {
        this.fwdMessages = fwdMessages;
        return this;
    }

    public Geo getGeo() {
        return this.geo;
    }

    public Message setGeo(Geo geo) {
        this.geo = geo;
        return this;
    }

    public Integer getId() {
        return this.id;
    }

    public Message setId(Integer id) {
        this.id = id;
        return this;
    }

    public Boolean getImportant() {
        return this.important;
    }

    public Message setImportant(Boolean important) {
        this.important = important;
        return this;
    }

    public Boolean getIsHidden() {
        return this.isHidden;
    }

    public Message setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }

    public Keyboard getKeyboard() {
        return this.keyboard;
    }

    public Message setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public Integer getMembersCount() {
        return this.membersCount;
    }

    public Message setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
        return this;
    }

    public boolean isOut() {
        return this.out == BoolInt.YES;
    }

    public BoolInt getOut() {
        return this.out;
    }

    public String getPayload() {
        return this.payload;
    }

    public Message setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public Integer getPeerId() {
        return this.peerId;
    }

    public Message setPeerId(Integer peerId) {
        this.peerId = peerId;
        return this;
    }

    public Integer getRandomId() {
        return this.randomId;
    }

    public Message setRandomId(Integer randomId) {
        this.randomId = randomId;
        return this;
    }

    public String getRef() {
        return this.ref;
    }

    public Message setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public String getRefSource() {
        return this.refSource;
    }

    public Message setRefSource(String refSource) {
        this.refSource = refSource;
        return this;
    }

    public ForeignMessage getReplyMessage() {
        return this.replyMessage;
    }

    public Message setReplyMessage(ForeignMessage replyMessage) {
        this.replyMessage = replyMessage;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public Integer getUpdateTime() {
        return this.updateTime;
    }

    public Message setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.date, this.peerId, this.keyboard, this.membersCount, this.attachments, this.adminAuthorId, this.updateTime, this.fromId, this.isHidden, this.refSource, this.out, this.geo, this.important, this.ref, this.fwdMessages, this.randomId, this.deleted, this.conversationMessageId, this.payload, this.replyMessage, this.action, this.id, this.text});
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Message message = (Message)o;
            return Objects.equals(this.date, message.date) && Objects.equals(this.keyboard, message.keyboard) && Objects.equals(this.attachments, message.attachments) && Objects.equals(this.fromId, message.fromId) && Objects.equals(this.isHidden, message.isHidden) && Objects.equals(this.refSource, message.refSource) && Objects.equals(this.conversationMessageId, message.conversationMessageId) && Objects.equals(this.out, message.out) && Objects.equals(this.peerId, message.peerId) && Objects.equals(this.geo, message.geo) && Objects.equals(this.important, message.important) && Objects.equals(this.ref, message.ref) && Objects.equals(this.updateTime, message.updateTime) && Objects.equals(this.deleted, message.deleted) && Objects.equals(this.payload, message.payload) && Objects.equals(this.action, message.action) && Objects.equals(this.adminAuthorId, message.adminAuthorId) && Objects.equals(this.fwdMessages, message.fwdMessages) && Objects.equals(this.membersCount, message.membersCount) && Objects.equals(this.id, message.id) && Objects.equals(this.randomId, message.randomId) && Objects.equals(this.text, message.text) && Objects.equals(this.replyMessage, message.replyMessage);
        } else {
            return false;
        }
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder("Message{");
        sb.append("date=").append(this.date);
        sb.append(", keyboard=").append(this.keyboard);
        sb.append(", attachments=").append(this.attachments);
        sb.append(", fromId=").append(this.fromId);
        sb.append(", isHidden=").append(this.isHidden);
        sb.append(", refSource='").append(this.refSource).append("'");
        sb.append(", conversationMessageId=").append(this.conversationMessageId);
        sb.append(", out=").append(this.out);
        sb.append(", peerId=").append(this.peerId);
        sb.append(", geo=").append(this.geo);
        sb.append(", important=").append(this.important);
        sb.append(", ref='").append(this.ref).append("'");
        sb.append(", updateTime=").append(this.updateTime);
        sb.append(", deleted=").append(this.deleted);
        sb.append(", payload='").append(this.payload).append("'");
        sb.append(", action=").append(this.action);
        sb.append(", adminAuthorId=").append(this.adminAuthorId);
        sb.append(", fwdMessages=").append(this.fwdMessages);
        sb.append(", membersCount=").append(this.membersCount);
        sb.append(", id=").append(this.id);
        sb.append(", randomId=").append(this.randomId);
        sb.append(", text='").append(this.text).append("'");
        sb.append(", replyMessage=").append(this.replyMessage);
        sb.append('}');
        return sb.toString();
    }
}
