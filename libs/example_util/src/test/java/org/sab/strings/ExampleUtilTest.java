package org.sab.strings;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExampleUtilTest {

    @Test
    public void shouldDivideHelloIntoItsLetters() {
        final List<Character> chars = ExampleUtil.toCharList("Hello");
        assertEquals(chars, List.of('H', 'e', 'l', 'l', 'o'));
    }
}
