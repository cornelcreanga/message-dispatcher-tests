package com.ccreanga.history.gateway;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class TopicStorageMemory implements TopicStorage {

    private ListMultimap<Long, String> map = ArrayListMultimap.create();

    @Override
    public List<String> getTopics(Long match) {
        return map.get(match);
    }

    @Override
    public List<String> getAllTopics() {
        return Arrays.asList("topic1","topic2","topic3","topic4","topic5","topic6");
    }

    @PostConstruct
    private void init(){
        map.put(1L,"topic1");
        map.put(2L,"topic1");

        map.put(1L,"topic2");
        map.put(2L,"topic2");
        map.put(3L,"topic2");
        map.put(4L,"topic2");
        map.put(5L,"topic2");
        map.put(6L,"topic2");
        map.put(7L,"topic2");
        map.put(8L,"topic2");

        map.put(1L,"topic3");

        map.put(1L,"topic4");
        map.put(2L,"topic4");
        map.put(3L,"topic4");

        map.put(1L,"topic5");
        map.put(2L,"topic5");
        map.put(3L,"topic5");
        map.put(4L,"topic5");
        map.put(5L,"topic5");

        map.put(9L,"topic6");
    }
}
