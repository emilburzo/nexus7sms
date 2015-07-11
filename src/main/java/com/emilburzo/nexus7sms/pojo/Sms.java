package com.emilburzo.nexus7sms.pojo;

import com.emilburzo.nexus7sms.manager.SMSManager;
import com.emilburzo.nexus7sms.model.SmsModel;

import java.util.Date;

public class Sms {

    public String uuid;
    public String body; // SMS content
    public String phone; // phone number from/to
    public Date timestamp; // when it was received/sent
    public String type; // 'in' or 'out'
    public boolean sent; // sent notification for 'out'
    public boolean delivered; // delivery notification for 'out'

    public Sms(SmsModel smsModel) {
        this.uuid = smsModel.getUuid();
        this.body = SMSManager.removeNewlines(smsModel.getBody());
        this.phone = smsModel.getPhone();
        this.timestamp = smsModel.getTimestamp();
        this.type = smsModel.getType();
        this.sent = smsModel.isSent();
        this.delivered = smsModel.isDelivered();
    }
}
