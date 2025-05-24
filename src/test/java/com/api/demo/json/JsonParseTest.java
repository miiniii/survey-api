package com.api.demo.json;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JsonParseTest {

    @Test
    public void testParseOptionsJson() throws Exception {
        String jsonOptions = "[\"전혀 명확하지 않았다\", \"명확하지 않았다\", \"보통이다\", \"명확했다\", \"매우 명확했다\"]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> options = objectMapper.readValue(jsonOptions, new TypeReference<List<String>>() {});

        options.forEach(System.out::println);
    }
}
