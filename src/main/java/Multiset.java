import java.util.*;

public class Multiset {

    private TreeMap<Double, Integer> map;

    public Multiset(){
        map = new TreeMap<>();
    }

    public double last(){
        if(map.size() == 0) return 1;
        if(map.lastKey() == 0.0){
            System.out.println("Error.. Largest value in multiset is zero");
            return 1;
        }
        return map.lastKey();
    }

    public void remove(double value){
        Double key = map.ceilingKey(value - Constants.EPS);
        if(key == null){
            System.out.println("Error.. Null key: " + value);
            return;
        }
        int count = map.get(key);
        if(count == 1){
            map.remove(key);
        }
        else {
            map.put(key, count - 1);
        }
    }

    public void add(double value){
        int count = map.getOrDefault(value, 0);
        map.put(value, count + 1);
    }
}
