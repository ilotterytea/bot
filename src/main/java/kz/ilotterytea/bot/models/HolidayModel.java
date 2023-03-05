package kz.ilotterytea.bot.models;

import java.util.ArrayList;

public class HolidayModel {
    private final String name;
    private final ArrayList<Integer> date;

    public HolidayModel(String name, ArrayList<Integer> date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getDate() {
        return date;
    }
}
