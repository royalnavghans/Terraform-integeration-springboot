package com.example.terraformdemoproject.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createWsVaraibleRequest {
    private String key;
    private String value;
    private String description;
    private boolean sensitive;
    private String workspaceId;
}
