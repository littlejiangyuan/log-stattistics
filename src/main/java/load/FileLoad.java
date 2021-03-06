package load;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import parse.StringParse;
import parse.Result;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.Arrays;
import java.util.Date;

import output.OutputLog;



/**
 * Created by jiangyuan5 on 2017/6/26.
 */
public class FileLoad {
    private static Logger log = Logger.getLogger(FileLoad.class);
    //private static OutputLog log = new OutputLog();

    private final Map<AsynchronousFileChannel, FileNode> map = new HashMap<AsynchronousFileChannel, FileNode>();

    private final static long HOUR = 3600 * 1000;
    private static final int FILE_NUM = 1;
    private static final int DELIMITER_ASCII = 10; //换行符

    private long runSec = 0; //运行时的时间戳，用来每隔一段时间将数据刷新到mysql

    private MemoryStorage memory =  new MemoryStorage();

    private int lineNum = 0;
    private static int INT_SEC = 10;

    public FileLoad() {
        //log.info("this is first!");
        //System.exit(0);
    }

    /*
     *文件加载
     */
    public  boolean load ( boolean first) {
        DateTime dateTime = new DateTime();
        runSec = new Date().getTime()/1000;

        int hour = dateTime.getHourOfDay();
        String date = dateTime.toString("yyyy-MM-dd");
        Path startingDir = Paths.get( "/data0/nginx/logs/uve_core/stats/" + date + "/" + String.format("%02d", hour));
        boolean exist = false;

        //判断目录是否存在
        while (!exist) {
            exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
            if (!exist) {
                log.info("目录不存在:" + startingDir.toAbsolutePath());
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    log.info("recv interrunpt");
                    return false;
                }
            }
        }

        exist = false;
        while (!exist) {
            List<Path> result = new LinkedList<Path>();
            try {
                Files.walkFileTree(startingDir, new FindJavaVisitor(result, "-stats.log"));

                for (Path p : result) {
                    AsynchronousFileChannel channel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
                    FileNode node = new FileNode();
                    node.setBf(ByteBuffer.allocate(50000));
                    node.getBf().clear();
                    node.setCnt(null);
                    node.setOffset(0);

                    /*
                    if (!first && true) {
                        long size = channel.size();//获取通道文件大小
                        if (size > 1000) {
                            size = size - 1000;
                        }
                        node.setOffset(size);
                    }
                    */

                    node.setCurTime(dateTime.getMillis());
                    this.map.put(channel, node);
                    log.info("文件加载成功:" + p.toAbsolutePath() + " with offset:" + node.getOffset());
                }

            }catch (IOException ioe) {

            }

            if (result.size() >= FILE_NUM) {
                exist = true;
            } else {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("目录初始化成功!");
        return true;
    }

    public void process() throws IOException, ExecutionException{
        while (true) {
            int count = 0; //用来标识当前目录已经处理完的文件数量
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }

                for (Entry<AsynchronousFileChannel, FileNode> entry : this.map.entrySet()) {
                    //log.info(entry.getValue().getFileName() + "文件偏移量" + entry.getValue().getOffset());
                    //log.info("文件大小" + entry.getKey().size());
                    Future<Integer> f = entry.getKey().read(entry.getValue().getBf(), entry.getValue().getOffset());
                    entry.getValue().setCnt(f);
                }

                for (FileNode node : this.map.values()) {
                    try {
                        Integer cnt = 0;
                        cnt = node.getCnt().get();
                        //log.info(cnt);
                        if (cnt > 0) {
                            handle(node);
                        } else if (!node.isHasReadEOF()) { //设置为文件已经读完
                            if (check(node.getCurTime())) {
                                count = count + 1;
                                node.setHasReadEOF(true);
                                log.trace("文件" + node.getFileName() + "已经读完！");
                            }
                        }

                        if(memory.getItemsCount() > 0) {
                            long now = new Date().getTime()/1000;
                            if( now - runSec > INT_SEC ) { //每十秒刷新到数据库
                                memory.flushToDb();
                                runSec = now;
                            }
                        }
                    } catch (InterruptedException e) {
                        log.error("目录处理异常");
                        Thread.currentThread().interrupt();
                    }
                }

                if (count >= FILE_NUM) { //所有文件都读
                    for (AsynchronousFileChannel channel : this.map.keySet()) {
                        channel.close();
                    }
                    break;
                }
            }

            this.map.clear();

            if (!loadNext()) {
                log.error("加载下个目录出错!");
                break;
            }
        }
        log.info("退出流程");
    }

    private boolean loadNext() {
        long curTime = 0;
        for (FileNode node : map.values()) {
            curTime = node.getCurTime();
            break;
        }
        curTime = curTime + HOUR;
        load( false);
        return true;
    }


    public void handle(FileNode node) {
        ByteBuffer bf = node.getBf();
        bf.flip(); //写模式切换到读模式
        int limit = bf.limit();
        int index = 0;
        for (int i = 0; i < limit; i++) {
            byte c = bf.get(i);
            if (c == DELIMITER_ASCII) {
                //log.info("处理完一行" + lineNum++);
                int length = (i + 1) - index;
                if (length != 0) {
                    byte[] dst = new byte[length - 1];
                    bf.get(dst, 0, length - 1);
                    bf.get();

                    //String t = new String(dst);
                    StringParse sp = new StringParse(new String(dst));
                    Result rs = sp.parsing();
                    memory.add( rs);

                    long now = new Date().getTime()/1000;
                    if( now - runSec > INT_SEC ) { //每十秒刷新到数据库
                        memory.flushToDb();
                        runSec = now;
                    }

                    dst = null;
                }
                index = i + 1;
            }
        }

        int finallength = limit - index;
        byte[] fdst = new byte[finallength];
        bf.get(fdst, 0, finallength);
        bf.clear();
        bf.put(fdst);
        node.setOffset(node.getOffset() + limit - node.getLastFinalLength());
        node.setLastFinalLength(finallength);
    }

    public boolean check(long curTime) throws InterruptedException {
        DateTime now = new DateTime();
        DateTime cur = new DateTime(curTime);
        if (now.getHourOfDay() == cur.getHourOfDay()) {
            Thread.sleep(2000);
            return false;
        } else {
            return checkFile(curTime);
        }

    }

    //如果下个目录文件都已经产生，返回true， 否则返回失败
    public boolean checkFile(long l) throws InterruptedException {
        l = l + HOUR;
        DateTime dateTime = new DateTime(l);
        int hour = dateTime.getHourOfDay();
        String date = dateTime.toString("yyyy-MM-dd");
        Path startingDir = Paths.get("/data0/nginx/logs/uve_core/stats/" + date + "/" + String.format("%02d", hour));
        boolean exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
        if (exist) {
            try {
                List<Path> result = new LinkedList<Path>();
                Files.walkFileTree(startingDir, new FindJavaVisitor(result, "-stats.log"));
                if (result.size() >= FILE_NUM) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("试图加载下个目录失败!");
            Thread.sleep(1000L);
        }
        return false;
    }

}
