package loglens.dto;

public class URITraffic {
    private long byteCount;
    private long requestCount;

    public void add(long byteCount, long requestCount) {
        this.byteCount += byteCount;
        this.requestCount += requestCount;
    }

    public long getByteCount() {
        return byteCount;
    }

    public long getRequestCount() {
        return requestCount;
    }
}
