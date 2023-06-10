package com.example.terraformdemoproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createConfigVersionAttributes {
    @JsonProperty("upload-url")
    private String uploadUrl;
}
