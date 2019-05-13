package com.ccreanga.history.gateway;

import java.util.List;

public interface TopicStorage {

    List<String> getTopics(Long match);

    List<String> getAllTopics();

}
