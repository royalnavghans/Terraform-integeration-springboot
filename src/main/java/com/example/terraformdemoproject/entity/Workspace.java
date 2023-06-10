package com.example.terraformdemoproject.entity;

import com.example.terraformdemoproject.model.WorkspaceData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "TF-Workspaces")
public class Workspace {
    private WorkspaceData data;
}
