package org.passwddb.servlet.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Payload {
    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;
}
