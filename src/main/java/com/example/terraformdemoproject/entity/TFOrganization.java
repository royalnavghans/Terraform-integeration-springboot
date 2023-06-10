package com.example.terraformdemoproject.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "TF-Organization")
public class TFOrganization {
    private String orgName;
    private String accessToken;
}
