package OOP.Solution;

import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;
import java.util.stream.Collectors;

public class HungryStudentImpl implements HungryStudent, Comparable<HungryStudent> {
    private int id;
    private String name;
    private HashSet<Restaurant> favorites;
    private Set<HungryStudent> friends;

    public HungryStudentImpl(int id, String name) {
        this.id = id;
        this.name = name;
        this.favorites = new HashSet<>();
        this.friends = new HashSet<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
//    public HashSet<Restaurant> getFavorites() { return new HashSet<>(favorites); }
    public void setFavorites(HashSet<Restaurant> favorites) { this.favorites = favorites; }
    public void setFriends(Set<HungryStudent> friends) { this.friends = friends; }

    @Override
    public HungryStudent favorite(Restaurant r) throws UnratedFavoriteRestaurantException {
        if (r == null) { return this; }
        int rating = ((RestaurantImpl)r).studentRating(this);
        if(rating == -1)
            throw new UnratedFavoriteRestaurantException();

        // according to FAQ - if two restaurants with same ID were rated by student, and we try to add both to the favourite collection
        // the second call to favorite with the parameter r2 should NOT override r1,
        // and r2 will not be added to the favorite restaurants collection
        for (Restaurant restaurant : this.favorites){
            if(((RestaurantImpl)restaurant).getId() == ((RestaurantImpl)r).getId())
                return this;
        }

        this.favorites.add(r);
        return this;
    }

    @Override
    public Collection<Restaurant> favorites() {
        return new HashSet<>(favorites);
    }// one level copy

    @Override
    public HungryStudent addFriend(HungryStudent s) throws SameStudentException, ConnectionAlreadyExistsException {
        if(s == null)
            return this;
        if(this.equals(s))
            throw new SameStudentException();
        if(friends.contains(s))
            throw new ConnectionAlreadyExistsException();
        friends.add(s);
        return this;
    }

    @Override
    public Set<HungryStudent> getFriends() { return new HashSet<>(friends); }// one level copy

    @Override
    public Collection<Restaurant> favoritesByRating(int rLimit) {
        return favorites.stream()
                // filter favorite restaurants by rLimit
                .filter(r -> r.averageRating() >= rLimit)
                // for all the restaurants that "survived" filter - sort in 3 levels using comparator (as instructed in PDF)
                .sorted((Comparator.comparingDouble(Restaurant::averageRating).reversed())
                    .thenComparingInt(Restaurant::distance)
                    .thenComparing(r -> ((RestaurantImpl) r).getId()))
                // must return sorted collection
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Restaurant> favoritesByDist(int dLimit) {
        return favorites.stream()
                // filter favorite restaurants by dLimit
                .filter(r -> r.distance() <= dLimit)
                // for all the restaurants that "survived" filter - sort in 3 levels using comparator (as instructed in PDF)
                .sorted(Comparator.comparingInt(Restaurant::distance)
                        .thenComparing(Comparator.comparingDouble(Restaurant::averageRating).reversed())
                        .thenComparing(r -> ((RestaurantImpl) r).getId()))
                // must return sorted collection
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(HungryStudent o) {
        if(o == null) { return -1; } // TODO return something else??
        return this.id - ((HungryStudentImpl)o).getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (this.getClass() != o.getClass())
            return false;
        return this.id == ((HungryStudentImpl) o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        List<String> sortedFavorites = this.favorites.stream()
                .map(restaurant -> (RestaurantImpl) restaurant) // Cast to RestaurantImpl
                .map(RestaurantImpl::getName)                   // Can access getName
                .sorted()
                .collect(Collectors.toList());

        String favoritesString = String.join(", ", sortedFavorites);

        return String.format("Hungry student: %s.\nId: %d.\nFavorites: %s.",
                this.name, this.id, favoritesString);
    }
}
