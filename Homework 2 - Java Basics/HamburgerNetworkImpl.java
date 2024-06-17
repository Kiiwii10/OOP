package OOP.Solution;

import OOP.Provided.HamburgerNetwork;
import OOP.Provided.HungryStudent;
import OOP.Provided.Restaurant;

import java.util.*;
import java.util.stream.Collectors;

public class HamburgerNetworkImpl implements HamburgerNetwork {

    private HashSet<HungryStudent> students;
    private Set<Restaurant> restaurants;

    public HamburgerNetworkImpl() {
        this.students = new HashSet<>();
        this.restaurants = new HashSet<>();
    }

//    public HashSet<HungryStudent> getStudents() { return new HashSet<>(this.students); }

    public void setStudents(HashSet<HungryStudent> students) {
        this.students = students;
    }

//    public HashSet<Restaurant> getRestaurants() { return new HashSet<>(this.restaurants); }

    public void setRestaurants(HashSet<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    public HungryStudent joinNetwork(int id, String name) throws HungryStudent.StudentAlreadyInSystemException {
        HungryStudent s = new HungryStudentImpl(id, name);
        // handel exceptions
        if(students.contains(s))
            throw new HungryStudent.StudentAlreadyInSystemException();
        this.students.add(s);
        return s;
    }

    @Override
    public Restaurant addRestaurant(int id, String name, int dist, Set<String> menu) throws Restaurant.RestaurantAlreadyInSystemException {
        Restaurant r = new RestaurantImpl(id, name, dist, menu);
        // handel exceptions
        if (restaurants.contains(r)){
            throw new Restaurant.RestaurantAlreadyInSystemException();
        }
        this.restaurants.add(r);
        return r;
    }

    @Override
    public Collection<HungryStudent> registeredStudents() {
        return new HashSet<>(this.students);
    } // one level copy

    @Override
    public Collection<Restaurant> registeredRestaurants() {
        return new HashSet<>(this.restaurants);
    } // one level copy

    @Override
    public HungryStudent getStudent(int id) throws HungryStudent.StudentNotInSystemException {
        // handel exceptions
        for (HungryStudent student : this.students) {
            if (((HungryStudentImpl)student).getId() == id)
                return student;
        }
        throw new HungryStudent.StudentNotInSystemException();
    }

    @Override
    public Restaurant getRestaurant(int id) throws Restaurant.RestaurantNotInSystemException {
        for (Restaurant restaurant : this.restaurants) {
            if (((RestaurantImpl)restaurant).getId() == id)
                return restaurant;
        }
        throw new Restaurant.RestaurantNotInSystemException();
    }

    @Override
    public HamburgerNetwork addConnection(HungryStudent s1, HungryStudent s2) throws
            HungryStudent.StudentNotInSystemException,
            HungryStudent.ConnectionAlreadyExistsException,
            HungryStudent.SameStudentException {
        // handel exceptions
        if((!students.contains(s1)) || (!students.contains(s2)))
            throw new HungryStudent.StudentNotInSystemException();

        try {
            // create symmetrical friendship
            s1.addFriend(s2);
            s2.addFriend(s1);
        }
        // HungryStudent.Add friend throws these exceptions as well - just rethrow them
        catch (HungryStudent.ConnectionAlreadyExistsException |
                 HungryStudent.SameStudentException e) {
            throw e;
        }
        return this;
    }


    @Override
    public Collection<Restaurant> favoritesByRating(HungryStudent s) throws HungryStudent.StudentNotInSystemException {
        // handel exceptions
        if(!this.students.contains(s)){
            throw new HungryStudent.StudentNotInSystemException();
        }
        // get all the students friends sorted by ID in asc order
        List<HungryStudent> sFriends = s.getFriends()
                .stream()
                .sorted(Comparator.comparingInt(s1 -> ((HungryStudentImpl)s1).getId()))
                .toList();

        // the result sorted list
        List<Restaurant> favRests = new ArrayList<>();

        // Iterate over all the students friends
        for(HungryStudent f: sFriends){
            // use the students function to get all the restaurants sorted as requested
            favRests.addAll(f.favoritesByRating(0));
        }

        // distinct ensures no restaurant is repeated in the list
        return favRests.stream().distinct().toList();
    }

    @Override
    public Collection<Restaurant> favoritesByDist(HungryStudent s) throws HungryStudent.StudentNotInSystemException {
        // handel exceptions
        if(!this.students.contains(s)){
            throw new HungryStudent.StudentNotInSystemException();
        }
        // get all the students friends sorted by ID in asc order
        List<HungryStudent> sFriends = s.getFriends()
                .stream()
                .sorted(Comparator.comparingInt(s1 -> ((HungryStudentImpl)s1).getId()))
                .toList();

        // the result sorted list
        List<Restaurant> favRests = new ArrayList<>();

        // Iterate over all the students friends
        for(HungryStudent f: sFriends){
            // use the students function to get all the restaurants sorted as requested
            favRests.addAll(f.favoritesByDist(Integer.MAX_VALUE));
        }

        // distinct ensures no restaurant is repeated inn the list
        return favRests.stream().distinct().toList();
    }

    @Override
    public String toString() {
        String regStudents = "Registered students: " +
                this.students.stream()
                        .map(s -> ((HungryStudentImpl)s).getId())
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

        String regRestaurants = ".\nRegistered restaurants: " +
                this.restaurants.stream()
                        .map(r -> ((RestaurantImpl)r).getId())
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

        String frens = ".\nStudents:\n";
        for (HungryStudent s : this.students){
            frens += ((HungryStudentImpl) s).getId() + " -> [" +
                        s.getFriends()
                        .stream()
                        .map(f -> ((HungryStudentImpl)f).getId())
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ") )+
                        "].\n";
        }
        frens += "End students.";

        return regStudents + regRestaurants + frens;
    }

    @Override
    public boolean getRecommendation(HungryStudent s, Restaurant r, int t) throws
            HungryStudent.StudentNotInSystemException,
            Restaurant.RestaurantNotInSystemException,
            ImpossibleConnectionException {
        // handel exceptions
        // according to FAQ - we can assume that cases of students with the same id will not be checked.
        if(!students.contains(s))
            throw new HungryStudent.StudentNotInSystemException();
        if(t < 0)
            throw new ImpossibleConnectionException();

        // check the restaurant is actually in the network!
        //checking using restaurants.contains(r) isn't good enough because we might have another restaurant in the network with the same ID.
        boolean resInNet = false;
        for(Restaurant restaurant : this.restaurants){
            if (r == restaurant) {
                resInNet = true;
                break;
            }
        }

        if(!resInNet)
            throw new Restaurant.RestaurantNotInSystemException();


        // call recursive function to go over the friends graph
        return getRecRec(s, r, t);

    }

    private boolean getRecRec(HungryStudent s, Restaurant r, int t) {
        // recursion stop conditions:
        // found the restaurant in favorites
        if (s.favorites().contains(r)) {
            return true;
        }
        // went to far in connections
        if (t <= 0)
            return false;

        // go over all the friends and see if they recommend the restaurant - RECURSION
        for (HungryStudent fren : s.getFriends()) {
            if (getRecRec(fren, r, t - 1))
                return true;
        }
        return false;
    }

}
