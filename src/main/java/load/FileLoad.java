package load;

import org.joda.time.DateTime;

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




/**
 * Created by jiangyuan5 on 2017/6/26.
 */
public class FileLoad {
    private final Map<AsynchronousFileChannel, FileNode> map = new HashMap<AsynchronousFileChannel, FileNode>();

    private final static long HOUR = 3600 * 1000;
    private static final int FILE_NUM = 1;
    private static final int DELIMITER_ASCII = 10; //换行符

    public void Fileload() {

    }

    /*
     *文件加载
     */
    public  boolean load ( boolean first) {
        DateTime dateTime = new DateTime();
        int hour = dateTime.getHourOfDay();
        String date = dateTime.toString("yyyy-MM-dd");
        Path startingDir = Paths.get( "/data0/nginx/logs/uve_core/stats/" + date + "/" + String.format("%02d", hour));
        boolean exist = false;

        while (!exist) {
            exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
            if (!exist) {
                System.out.println("dir not exist(sleeping):" + startingDir.toAbsolutePath());
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    System.out.println("recv interrunpt");
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
                    node.setBf(ByteBuffer.allocate(100000));
                    node.getBf().clear();
                    node.setCnt(null);
                    node.setOffset(0);
                    if (first && true) {
                        long size = channel.size();//获取通道文件大小
                        if (size > 1000) {
                            size = size - 1000;
                        }
                        node.setOffset(size);
                    }
                    node.setCurTime(dateTime.getMillis());
                    this.map.put(channel, node);
                    System.out.println("load file successs:" + p.toAbsolutePath() + " with offset:" + node.getOffset());
                }

            }catch (IOException ioe) {

            }

            if (result.size() == FILE_NUM) {
                exist = true;
            } else {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    System.out.println("recv interrunpt");
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("init successs!");
        return true;
    }

    public void process() throws IOException, ExecutionException{
        while (true) {
            int count = 0;
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }

                for (Entry<AsynchronousFileChannel, FileNode> entry : this.map.entrySet()) {
                    Future<Integer> f = entry.getKey().read(entry.getValue().getBf(), entry.getValue().getOffset());
                    entry.getValue().setCnt(f);
                }

                for (FileNode node : this.map.values()) {
                    try {
                        Integer cnt = 0;
                        cnt = node.getCnt().get();
                        if (cnt > 0) {
                            handle(node);
                        } else if (!node.isHasReadEOF()) {
                            if (check(node.getCurTime())) {
                                count = count + 1;
                                node.setHasReadEOF(true);
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.println("recv interrunpt");
                        Thread.currentThread().interrupt();
                    }
                }

                if (count == FILE_NUM) {
                    for (AsynchronousFileChannel channel : this.map.keySet()) {
                        channel.close();
                    }
                    break;
                }
            }

            if (!loadNext()) {
                System.out.println("load next dir error!");
                break;
            }
        }
        System.out.println("exit the thread!");
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
        bf.flip();
        int limit = bf.limit();
        int index = 0;
        for (int i = 0; i < limit; i++) {
            byte c = bf.get(i);
            if (c == DELIMITER_ASCII) {
                int length = (i + 1) - index;
                if (length != 0) {
                    byte[] dst = new byte[length - 1];
                    bf.get(dst, 0, length - 1);
                    bf.get();

                    String t = new String(dst);
                    System.out.println(t);

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
                if (result.size() == FILE_NUM) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("try next file: not exist!");
            Thread.sleep(1000L);
        }
        return false;
    }

}
