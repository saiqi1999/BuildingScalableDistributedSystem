import java.util.Objects;

public class LiftRecord {
    private String time;
    private String liftId;
    private String waitTime;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return time + ' ' +
                 liftId + ' ' +
                 waitTime + ' ';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiftRecord that = (LiftRecord) o;
        return Objects.equals(time, that.time) && Objects.equals(liftId, that.liftId) && Objects.equals(waitTime, that.waitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, liftId, waitTime);
    }

    public String getLiftId() {
        return liftId;
    }

    public void setLiftId(String liftId) {
        this.liftId = liftId;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }

    public LiftRecord(String time, String liftId, String waitTime) {
        this.time = time;
        this.liftId = liftId;
        this.waitTime = waitTime;
    }
}
