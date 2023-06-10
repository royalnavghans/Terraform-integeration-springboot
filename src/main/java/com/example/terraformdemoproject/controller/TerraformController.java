package com.example.terraformdemoproject.controller;

import com.example.terraformdemoproject.entity.TFOrganization;
import com.example.terraformdemoproject.entity.Workspace;
import com.example.terraformdemoproject.model.*;
import com.example.terraformdemoproject.service.WorkspaceServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TerraformController {
    private final WorkspaceServiceImpl workspaceService;

    public TerraformController(WorkspaceServiceImpl workspaceService) {
        this.workspaceService = workspaceService;
    }
    @PostMapping("/org-reg")
    public ResponseEntity<TFOrganization>registerOrg(@RequestBody TFOrganization tfOrganization){
        return new ResponseEntity<>(workspaceService.registerOrganization(tfOrganization), HttpStatus.CREATED);
    }
    @GetMapping("/org")
    public ResponseEntity<TFOrganization>getOrg(){
        return new ResponseEntity<>(workspaceService.getTFOrganization(),HttpStatus.OK);
    }
    @PutMapping("org-update")
    public ResponseEntity<TFOrganization>updateOrganization(@RequestBody TFOrganization tfOrganization){
        return new ResponseEntity<>(workspaceService.updateTFOrganization(tfOrganization),HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public Workspace createWorkspace(@RequestBody CreateWSRequest createWorkspaceRequest) {
        return workspaceService.createWorkspace(createWorkspaceRequest);
    }
    @PostMapping("/var-create")
    public Object createWorkspace(@RequestBody createWsVaraibleRequest createWorkspace){
        return workspaceService.createVariable(createWorkspace);
    }
    @PostMapping("/configurationVersion")
    public Object createWorkspaceVariable (@RequestBody createConfigVersionReq wsVaraibleRequest){
        return workspaceService.CreateConfigurationVersion(wsVaraibleRequest);
    }
    @PostMapping("/configure")
    public Object createConfiguration(){
        return workspaceService.createConfiguration();
    }
    @PostMapping("/destroy")
    public Object destroyWorkspace(@RequestBody DestroyWsRequest destroyWsRequest){
        return workspaceService.destroyWorkSpace(destroyWsRequest);
    }

}
