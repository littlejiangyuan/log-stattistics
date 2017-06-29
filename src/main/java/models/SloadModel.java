package models;

import parse.Result;

/**
 * Created by jiangyuan5 on 2017/6/28.
 */
public class SloadModel {
    public int id           = 0;
    public int feedsNum     = 0;
    public int availablePos = 0;
    public int unreadStatus = 0;
    public int adRatio      = 0;

    public String serviceName = "";
    public int year           = 0;
    public int month          = 0;
    public int day            = 0;
    public int hour           = 0;
    public int min            = 0;


    public SloadModel(Result r) {
        feedsNum = r.getFeedsNum();
        availablePos = r.getAvailPos();
        unreadStatus = r.getUnreadStatus();

        serviceName = r.getServiceName();

        String s[] = r.getDateTime().split("-");

        year   = Integer.parseInt(s[0]);
        month  = Integer.parseInt(s[1]);
        day    = Integer.parseInt(s[2]);
        hour   = Integer.parseInt(s[3]);
        min    = Integer.parseInt(s[4]);
    }




}
