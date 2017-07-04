package parse;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by jiangyuan5 on 2017/6/29.
 */
public class Result {
    private int availablePos = 0;
    private int unreadStatus = 0;
    private int feedsNum = 0;
    private long requestTime = 0;

    private String serviceName = "";

    private String dateTime = "";



    public  Result() {

    }

    public String getKey() {
        return serviceName + dateTime;
    }

    public Result add(Result origin) {
        availablePos += origin.getAvailPos();
        unreadStatus += origin.getUnreadStatus();
        feedsNum     += origin.getFeedsNum();

        return this;
    }

    public Result setDateTime() {
        Date date = new Date(requestTime * 1000 );
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        dateTime = formatter.format(date);

        return this;
    }

    public Result setAvailPos(int a) {
        availablePos = a;
        return this;
    }

    public Result setUnreadStatus(int r) {
        unreadStatus = r;
        return this;
    }

    public Result setFeedsNum(int f) {
        feedsNum = f;
        return this;
    }

    public Result setServiceName(String s) {
        serviceName = s;
        return this;
    }

    public Result setReqtime(long time) {
        requestTime = time;
        return this;
    }

    public int getAvailPos() {
        return availablePos;
    }

    public int getUnreadStatus() {
        return unreadStatus;
    }

    public int getFeedsNum() {
        return feedsNum;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getReqtime() {
        return requestTime;
    }

    public String getDateTime() {
        return dateTime;
    }

}
