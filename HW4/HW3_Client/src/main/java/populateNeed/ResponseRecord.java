package populateNeed;

public class ResponseRecord {
    private long startTime;
    private String reqType;
    private long latency;
    private int responseCode;

    public ResponseRecord(long startTime, String reqType, long latency, int responseCode) {
        this.startTime = startTime;
        this.reqType = reqType;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return reqType +
                ", " + responseCode +
                ", " + startTime +
                ", " + latency +
                '\n';
    }
}
