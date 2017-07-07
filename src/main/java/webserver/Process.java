package webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


//import org.apache.commons.httpclient.HttpStatus;

import com.sun.net.httpserver.*;
/**
 * Created by jiangyuan5 on 2017/7/5.
 */
public class Process {

    public void loop(String host) {
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(host, 8765), 0);
            server.createContext("/", new ResponseHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        }catch(Exception e) {
            System.out.println("web服务启动监听失败："+e);
        }

    }

    public static class ResponseHandler implements HttpHandler {
        //取得响应
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Runtime runtime = Runtime.getRuntime();
            long memory = runtime.totalMemory() / (1024*1024);

            String responseString = "<font color='#ff0000'>程序运行情况：</font><br/>";
            responseString += "内存使用：" + memory + "M<br/>";

            String html = readRecentLog("/data0/sload/log/htmlLayout.html", 50000);

            /*
            //设置响应头
            List<String> logList = readLastNLine("/data0/sload/log/htmlLayout.html", 200);
            String html = "";
            for(String s :logList){
                byte[] m = s.getBytes("ISO-8859-1");
                String dest = new String(m , "utf-8");
                html += dest + "<br/>";
            }
            */

            httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + "utf-8");

            httpExchange.sendResponseHeaders(200, responseString.getBytes().length + html.getBytes().length );
            OutputStream os = httpExchange.getResponseBody();
            os.write(responseString.getBytes());
            os.write(html.getBytes());

            os.close();
        }
    }

    //读取最近的一些日志
    public static String readRecentLog(String file, int readNum) {
        Path log = Paths.get( file);
        try {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(log, StandardOpenOption.READ);
            long size = channel.size();
            long offset = size - readNum;
            if(offset < 0) {
                offset = 0;
            }

            ByteBuffer bf = ByteBuffer.allocate(readNum);
            bf.clear();
            Future<Integer> f = channel.read(bf, offset);

            while(!f.isDone());

            bf.flip();
            byte[] data = new byte[bf.limit()];
            bf.get(data);
            String res = new String(data);
            int index = res.indexOf("</tr>");
            res = res.substring(index + 5 );

            String next = res.substring(0, 8);
            if(!next.equals("<!DOCTYPE")) {
                String headerHtml = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<title>Log4J Log Messages</title>\n" +
                        "<style type=\"text/css\">\n" +
                        "<!--\n" +
                        "body, table {font-family: arial,sans-serif; font-size: x-small;}\n" +
                        "th {background: #336699; color: #FFFFFF; text-align: left;}\n" +
                        "-->\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">\n" +

                        "<br>" +
                        "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">\n" +
                        "<tbody><tr>\n" +
                        "<th>Time</th>\n" +
                        "<th>Thread</th>\n" +
                        "<th>Level</th>\n" +
                        "<th>Category</th>\n" +
                        "<th>Message</th>\n" +
                        "</tr>";
                res = headerHtml + res;
            }

            //byte[] m = res.getBytes("ISO-8859-1");
            //res = new String(m , "utf-8");
            //System.out.println(res);
            bf.clear();

            return res;
        } catch(Exception e) {

        }
        return "";

    }

}
