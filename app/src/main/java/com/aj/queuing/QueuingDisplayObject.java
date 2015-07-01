package com.aj.queuing;

/**
 * Created by Joost on 01/07/2015.
 */
public class QueuingDisplayObject {
    String title, time, info;

    public QueuingDisplayObject(String title, String time, String info) {
        this.title = title;
        this.time = time;
        this.info = info;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }
}
