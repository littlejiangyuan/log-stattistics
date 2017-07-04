package models;

import parse.Result;
import sloadmonitor.SloadMonitor;


/**
 * Created by jiangyuan5 on 2017/6/28.
 */
public class SloadModel {
    public int id           = 0;
    public int feedsNum     = 0;
    public int availablePos = 0;
    public int unreadStatus = 0;

    public String serviceName = "";
    public String serverIp    = "";

    public String datetime = "";

    public SloadModel(Result r) {
        feedsNum = r.getFeedsNum();
        availablePos = r.getAvailPos();
        unreadStatus = r.getUnreadStatus();

        serviceName = r.getServiceName();

        datetime = r.getDateTime() + ":00";
        serverIp = SloadMonitor.serverIp;
    }




}
