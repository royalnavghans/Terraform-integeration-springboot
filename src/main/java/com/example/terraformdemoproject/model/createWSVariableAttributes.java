package com.example.terraformdemoproject.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createWSVariableAttributes {
    private String key;
    private String value;
    private String description;
    private String category;
    private boolean hcl;
    private boolean sensitive;
}
