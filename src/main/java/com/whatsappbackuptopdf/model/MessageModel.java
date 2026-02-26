package com.whatsappbackuptopdf.model;

public class MessageModel {
    private String date;
    private String time;
    private String sender;
    private String content;

    public MessageModel() {
    }

    public MessageModel(String date, String time, String sender, String content) {
        this.date = date;
        this.time = time;
        this.sender = sender;
        this.content = content;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender){
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageModel{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

}
