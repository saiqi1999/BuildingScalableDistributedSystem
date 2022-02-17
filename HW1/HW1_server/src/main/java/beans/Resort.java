package beans;

import java.util.Objects;

public class Resort {
    private String resortName;
    private int resortId;

    public Resort(String name, int id) {
        this.resortName = name;
        this.resortId = id;
    }

    @Override
    public String toString() {
        return "Resort{" +
                "resortName='" + resortName + '\'' +
                ", id=" + resortId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resort resort = (Resort) o;
        return resortId == resort.resortId && Objects.equals(resortName, resort.resortName);
    }

    public String getResortName() {
        return resortName;
    }

    public void setResortName(String resortName) {
        this.resortName = resortName;
    }

    public int getResortId() {
        return resortId;
    }

    public void setResortId(int resortId) {
        this.resortId = resortId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resortName, resortId);
    }
}
