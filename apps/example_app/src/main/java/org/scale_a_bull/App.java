package org.scale_a_bull;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        final List<Character> chars = ExampleUtil.toCharList("Hello World!");
        System.out.println(chars);
    }
}
