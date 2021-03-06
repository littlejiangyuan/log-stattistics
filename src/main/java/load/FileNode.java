package load;


import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


/**
 * Created by jiangyuan5 on 2017/6/27.
 */


public class FileNode {
    private String fileName;
    private ByteBuffer bf;
    private long offset;
    private Future<Integer> cnt;
    private long lastFinalLength;
    private boolean hasReadEOF = false;
    private long curTime;

    public ByteBuffer getBf() {
        return bf;
    }

    public void setBf(ByteBuffer bf) {
        this.bf = bf;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Future<Integer> getCnt() {
        return cnt;
    }

    public void setCnt(Future<Integer> cnt) {
        this.cnt = cnt;
    }


    public long getLastFinalLength() {
        return lastFinalLength;
    }

    public void setLastFinalLength(long lastFinalLength) {
        this.lastFinalLength = lastFinalLength;
    }

    public boolean isHasReadEOF() {
        return hasReadEOF;
    }

    public void setHasReadEOF(boolean hasReadEOF) {
        this.hasReadEOF = hasReadEOF;
    }

    public long getCurTime() {
        return curTime;
    }

    public void setCurTime(long curTime) {
        this.curTime = curTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
