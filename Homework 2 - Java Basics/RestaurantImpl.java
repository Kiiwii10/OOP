package OOP.Solution;

import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;
import java.util.stream.Collectors;

public class RestaurantImpl implements Restaurant, Comparable<Restaurant> {
    private int id;
    private String name;
    private int distFromTech;
    private Set<String> menu;
    private HashMap<HungryStudent, Integer> ratings;


    // Assuming we get legal params
    // TODO: check if it passes a pointer to the set
    public RestaurantImpl(int id, String name, int distFromTech, Set<String> menu){
        this.id = id;
        this.name = name;
        this.distFromTech = distFromTech;
        this.menu = (menu == null) ? new HashSet<>() : new HashSet<>(menu);
        this.ratings = new HashMap<HungryStudent, Integer>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDistFromTech() { return distFromTech;    }
    public void setDistFromTech(int distFromTech) { this.distFromTech = distFromTech; }
    public Set<String> getMenu() { return (new HashSet<>(menu)); }
    public void setMenu(Set<String> menu) { this.menu = menu; }
    public HashMap<HungryStudent, Integer> getRatings() { return new HashMap<>(ratings); }
    public void setRatings(HashMap<HungryStudent, Integer> ratings) { this.ratings = ratings; }

    @Override
    public int distance() { return distFromTech; }

    @Override
    public Restaurant rate(HungryStudent s, int r) throws RateRangeException {
        if (s == null) { return this; }
        if ((r > 5) || (r < 0)){
            throw new RateRangeException();
        }
        ratings.put(s, r);
        return this;
    }

    @Override
    public int numberOfRates() {
        return ratings.size();
    }

    @Override
    public double averageRating() {
        if(ratings.isEmpty()) { return 0; }

        int summ = 0;
        for (int rating : ratings.values()) {
            summ += rating; // Sum up all the ratings
        }

        return  ( summ / (double) ratings.size());
    }

    @Override
    public String toString() {
        List<String> sortedMenu = this.menu.stream()
                .sorted()
                .collect(Collectors.toList());

        String strMenu = String.join(", ", sortedMenu);

        return String.format("Restaurant: %s.\nId: %d.\nDistance: %d.\nMenu: %s.",
                this.name, this.id, this.distFromTech, strMenu);
    }

    @Override
    public int compareTo(Restaurant o) {
        if(o == null) { return -1; } // TODO return something else??
        return this.id - ((RestaurantImpl)o).getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || o.getClass() != this.getClass())
            return false;
        return this.id == ((RestaurantImpl)o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId()); //, getName(), getMenu(), getDistFromTech());
    }

    public int studentRating(HungryStudentImpl s){
        if (ratings.containsKey(s)) { return ratings.get(s); }
        return -1;
    }

}
