package org.scale_a_bull;

import java.util.List;
import java.util.stream.Collectors;

public class ExampleUtil {

    public static List<Character> toCharList(String s) {
        return s.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    }

}