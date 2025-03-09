package pleinlib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lists {
    
    /**
     * Create a mutable list with the specified capacity and all the element
     * are initialized to the desired value.
     * @param value for all elements of the list.
     * @param capacity of the list.
     * @throws IllegalArgumentException if capacity is negative.
     * @return List with size {capacity} and all elements with value = {value}.
     */
    public static <T> List<T> sameElementList(T value, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative: " + capacity);
        }
        return new ArrayList<>(Collections.nCopies(capacity, value));
    }
}
