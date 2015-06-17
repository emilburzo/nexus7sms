package com.emilburzo.nexus7sms.model;

import io.realm.RealmObject;

import java.util.Date;

public class SmsModel extends RealmObject {

    private String uuid;
    private String body; // SMS content
    private String phone; // phone number to
    private Date timestamp; // when it was received/sent
    private String type; // 'in' or 'out'
    private boolean sent; // delivery notification for 'out'

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

}
