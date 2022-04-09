
import java.util.HashMap;
import java.util.Objects;

public class LiftRecord {
    private String time;
    private String liftId;
    private String waitTime;
    private int resortId;
    private int skiersId;
    private int seasonId;
    private int daysId;

    public LiftRecord(String time, String liftId, String waitTime, int resortId, int skiersId, int seasonId, int daysId) {
        this.time = time;
        this.liftId = liftId;
        this.waitTime = waitTime;
        this.resortId = resortId;
        this.skiersId = skiersId;
        this.seasonId = seasonId;
        this.daysId = daysId;
    }

    public LiftRecord() {
    }

    public HashMap<String,String> toHashMap(){
        HashMap<String,String> map = new HashMap<>();
        map.put("time",time);
        map.put("liftId",liftId);
        map.put("waitTime",waitTime);
        map.put("resortId", String.valueOf(resortId));
        map.put("skiersId", String.valueOf(skiersId));
        map.put("seasonId", String.valueOf(seasonId));
        map.put("daysId", String.valueOf(daysId));
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiftRecord)) return false;
        LiftRecord that = (LiftRecord) o;
        return getResortId() == that.getResortId() && getSkiersId() == that.getSkiersId() && getSeasonId() == that.getSeasonId() && getDaysId() == that.getDaysId() && Objects.equals(getTime(), that.getTime()) && Objects.equals(liftId, that.liftId) && Objects.equals(waitTime, that.waitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), liftId, waitTime, getResortId(), getSkiersId(), getSeasonId(), getDaysId());
    }

    @Override
    public String toString() {
        return "LiftRecord" +
                " " + time +
                " " + liftId +
                " " + waitTime +
                " " + resortId +
                " " + skiersId +
                " " + seasonId +
                " " + daysId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getResortId() {
        return resortId;
    }

    public void setResortId(int resortId) {
        this.resortId = resortId;
    }

    public int getSkiersId() {
        return skiersId;
    }

    public void setSkiersId(int skiersId) {
        this.skiersId = skiersId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getDaysId() {
        return daysId;
    }

    public void setDaysId(int daysId) {
        this.daysId = daysId;
    }
}
