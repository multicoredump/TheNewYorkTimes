package com.coremantra.tutorial.thenewyorktimes.models;

import android.util.Log;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radhikak on 3/17/17.
 */

@Parcel
public class SearchFilters {


    public static final String SORT_OLDEST = "oldest";
    public static final String SORT_NEWEST = "newest";

    public static final String NEWS_DESK_FOOD = "\"Food\"";
    public static final String NEWS_DESK_DINING = "\"Dining\"";
    public static final String NEWS_DESK_FASHION = "\"Fashion\"";
    public static final String NEWS_DESK_TRAVEL = "\"Travel\"";
    public static final String NEWS_DESK_TECH = "\"Technology\"";

    private static final String TAG = "SearchFilters";

    private String query;
    private String beginDateString;


    private boolean sortOldest;


    private boolean food;
    private boolean fashion;
    private boolean dining;
    private boolean travel;
    private boolean tech;

    public SearchFilters() { reset(); }

    public void setQuery(String query) {
        this.query = query;
    }

    public void reset() {
        query = null;
        beginDateString = null;
        sortOldest = false;
        food = fashion = dining = travel = tech = true;
    }

    public void resetQuery() {
        query = null;
    }

    public void update(String beginDate, boolean oldest, boolean isfood, boolean isfashion, boolean isdining, boolean istravel, boolean istech) {

        beginDateString = beginDate;
        sortOldest = oldest;

        food = isfood;
        fashion = isfashion;
        dining = isdining;
        travel = istravel;
        tech = istech;

    }

    public String getQuery() {
        return query;
    }

    public String getBeginDateString() {
        return beginDateString;
    }

    public String getSortOrder() {
         return sortOldest ? SORT_OLDEST : SORT_NEWEST;
    }

    public boolean isSortOldest() {
        return sortOldest;
    }

    public boolean isFood() {
        return food;
    }

    public boolean isTravel() {
        return travel;
    }

    public boolean isDining() {
        return dining;
    }

    public boolean isFashion() {
        return fashion;
    }

    public boolean isTech() {
        return tech;
    }

    public String getNewsDesk() {

        List<String> newsDesks = new ArrayList<>();

        if (food) newsDesks.add(NEWS_DESK_FOOD);
        if (fashion) newsDesks.add(NEWS_DESK_FASHION);
        if (dining) newsDesks.add(NEWS_DESK_DINING);
        if (tech) newsDesks.add(NEWS_DESK_TECH);
        if (travel) newsDesks.add(NEWS_DESK_TRAVEL);

        StringBuilder builder = new StringBuilder();
        String joinedString = join(newsDesks, " ");
        if (!joinedString.isEmpty()) {

            builder.append("news_desk:(").append(joinedString).append(")");
        }

        String result = builder.toString();
        return result.isEmpty() ? null : result;

    }

    private static String join(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for(String s : list) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        Log.d(TAG, sb.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("SearchFilter: ")
                .append("Q: ").append(query)
                .append(" sort = ").append(getSortOrder()).append(" ")
                .append(getNewsDesk())
                .append(" begin date: ").append(beginDateString);

        return builder.toString();
    }
}
