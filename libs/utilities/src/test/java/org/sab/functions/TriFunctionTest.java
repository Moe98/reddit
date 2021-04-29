package org.sab.functions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TriFunctionTest {

    @Test
    public void shouldAddTwoIntegers() {
        final TriFunction<Integer, Integer, Integer> add = (x, y) -> x + y;

        final Integer x = 3;
        final Integer y = 7;
        final Integer expected = x + y;
        final Integer actual = add.apply(x, y);

        assertEquals(expected, actual);
    }
    
}
