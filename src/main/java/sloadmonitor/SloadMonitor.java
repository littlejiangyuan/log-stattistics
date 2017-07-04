package sloadmonitor;

import load.FileLoad;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SloadMonitor {
    public static String serverIp = "";


    public static void main(String[] args) {
        String ip = args[args.length - 1];
        if(args.length < 1) {
            System.out.println("运行方式：[./sloadmonitor 当前运行服务器ip]");
            System.exit(0);
        }
        if(!isIP(ip)) {
            System.out.println("请正确输入当前服务器的ip!");
            System.exit(0);
        }

        serverIp = ip;
        FileLoad sload = new FileLoad();
        sload.load(true);

        try {
            sload.process();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static boolean isIP(String addr) {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        /**
         * 判断IP格式和范围
         */
        String rexp = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        return  mat.find();
    }


}



