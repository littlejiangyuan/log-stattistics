package webserver;


/**
 * Created by jiangyuan5 on 2017/7/5.
 */
public class WebThread implements Runnable{
    private String listenHost = "";

    public WebThread(String host) {
        listenHost = host;
    }

    public void run(){
        Process s = new Process();
        s.loop(listenHost);
    }
}