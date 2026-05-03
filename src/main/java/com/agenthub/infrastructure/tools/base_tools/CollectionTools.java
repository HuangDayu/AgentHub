package com.agenthub.infrastructure.tools.base_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.*;
import java.util.stream.Collectors;

@AgentTools(defaultEnable = false)
public class CollectionTools {

    @Tool(name = "collection_sort_numbers", description = "Sort numbers ascending")
    public String sortNumbers(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .sorted()
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Tool(name = "collection_sort_desc", description = "Sort numbers descending")
    public String sortDesc(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Tool(name = "collection_unique", description = "Remove duplicates from list")
    public String unique(String items) {
        return Arrays.stream(items.split(","))
                .distinct()
                .collect(Collectors.joining(","));
    }

    @Tool(name = "collection_reverse", description = "Reverse list order")
    public String reverse(String items) {
        List<String> list = new ArrayList<>(Arrays.asList(items.split(",")));
        Collections.reverse(list);
        return String.join(",", list);
    }

    @Tool(name = "collection_count", description = "Count items in list")
    public int count(String items) {
        return items.split(",").length;
    }

    @Tool(name = "collection_min", description = "Find minimum value")
    public double min(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .min()
                .orElse(0);
    }

    @Tool(name = "collection_max", description = "Find maximum value")
    public double max(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .max()
                .orElse(0);
    }

    @Tool(name = "collection_sum", description = "Sum all numbers")
    public double sum(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .sum();
    }

    @Tool(name = "collection_average", description = "Calculate average")
    public double average(String numbers) {
        return Arrays.stream(numbers.split(","))
                .mapToDouble(Double::parseDouble)
                .average()
                .orElse(0);
    }

    @Tool(name = "collection_contains", description = "Check if list contains item")
    public boolean contains(String items, String item) {
        return Arrays.asList(items.split(",")).contains(item);
    }

    @Tool(name = "collection_index_of", description = "Find index of item")
    public int indexOf(String items, String item) {
        String[] arr = items.split(",");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(item)) return i;
        }
        return -1;
    }

    @Tool(name = "collection_get", description = "Get item at index")
    public String get(String items, int index) {
        return items.split(",")[index];
    }

    @Tool(name = "collection_sublist", description = "Get sublist from start to end")
    public String sublist(String items, int start, int end) {
        return Arrays.stream(items.split(","))
                .skip(start)
                .limit(end - start)
                .collect(Collectors.joining(","));
    }

    @Tool(name = "collection_first", description = "Get first item")
    public String first(String items) {
        return items.split(",")[0];
    }

    @Tool(name = "collection_last", description = "Get last item")
    public String last(String items) {
        String[] arr = items.split(",");
        return arr[arr.length - 1];
    }

    @Tool(name = "collection_join", description = "Join two lists")
    public String join(String list1, String list2) {
        return list1 + "," + list2;
    }

    @Tool(name = "collection_intersect", description = "Intersection of two lists")
    public String intersect(String list1, String list2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(list1.split(",")));
        Set<String> set2 = new HashSet<>(Arrays.asList(list2.split(",")));
        set1.retainAll(set2);
        return String.join(",", set1);
    }

    @Tool(name = "collection_difference", description = "Difference of two lists")
    public String difference(String list1, String list2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(list1.split(",")));
        Set<String> set2 = new HashSet<>(Arrays.asList(list2.split(",")));
        set1.removeAll(set2);
        return String.join(",", set1);
    }

    @Tool(name = "collection_frequency", description = "Count occurrences of item")
    public int frequency(String items, String item) {
        return (int) Arrays.stream(items.split(","))
                .filter(s -> s.equals(item))
                .count();
    }
}
