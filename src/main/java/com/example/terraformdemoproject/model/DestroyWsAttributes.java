package com.example.terraformdemoproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyWsAttributes {
    @JsonProperty("is-destroy")
    private boolean isDestroy;
    private String message;
}
