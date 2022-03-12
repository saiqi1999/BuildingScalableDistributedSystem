package beans;

import java.util.ArrayList;
import java.util.Objects;

public class SeasonData {
    private ArrayList<String> seasons;

    public SeasonData() {
        seasons = new ArrayList<>();
    }

    public void addSeason(String season) {
        seasons.add(season);
    }

    public void clear() {
        seasons = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SeasonData{" +
                "seasons=" + seasons +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeasonData that = (SeasonData) o;
        return Objects.equals(seasons, that.seasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seasons);
    }

    public ArrayList<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(ArrayList<String> seasons) {
        this.seasons = seasons;
    }
}
