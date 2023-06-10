package com.example.terraformdemoproject.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyWsRequest {
    private String workspaceId;
    private String message;

}
