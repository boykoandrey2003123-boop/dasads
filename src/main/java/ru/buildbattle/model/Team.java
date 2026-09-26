package ru.buildbattle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private final List<UUID> members = new ArrayList<>();
    private Plot plot;
    private final List<Integer> receivedRatingPoints = new ArrayList<>();

    public List<UUID> getMembers() {
        return members;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }

    public void addRating(int points) {
        receivedRatingPoints.add(points);
    }

    public void recalculate(java.util.Collection<Integer> freshPoints) {
        receivedRatingPoints.clear();
        receivedRatingPoints.addAll(freshPoints);
    }

    public int getTotalScore() {
        int sum = 0;
        for (int p : receivedRatingPoints) sum += p;
        return sum;
    }

    public double getAverageScore() {
        if (receivedRatingPoints.isEmpty()) return 0;
        return getTotalScore() / (double) receivedRatingPoints.size();
    }

    public int getVoteCount() {
        return receivedRatingPoints.size();
    }
}
