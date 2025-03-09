package pleinlib.utils.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ListsTest {
    
    @Test
    void sameElementList_whenInteger() {
        List<Integer> list = Lists.sameElementList(10, 5);
        assertEquals(List.of(10, 10, 10, 10, 10), list);
    }

    @Test
    void sameElementList_whenString() {
        List<String> list = Lists.sameElementList("test", 2);
        assertEquals(List.of("test", "test"), list);
    }

    @Test
    void sameElementList_whenNull() {
        List<String> list = Lists.sameElementList(null, 3);
        assertEquals(null, list.get(0));
        assertEquals(null, list.get(1));
        assertEquals(null, list.get(2));
    }

    @Test
    void sameElementList_whenCapacityIsZero() {
        List<String> list = Lists.sameElementList("test", 0);
        assertEquals(List.of(), list);
    }

    @Test
    void sameElementList_whenCapacityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Lists.sameElementList("test", -2));
    }
}
