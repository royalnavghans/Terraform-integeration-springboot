package com.example.terraformdemoproject.service;

import com.example.terraformdemoproject.entity.TFOrganization;
import com.example.terraformdemoproject.entity.Workspace;
import com.example.terraformdemoproject.model.*;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;

@Service
public class WorkspaceServiceImpl {

    private final RestTemplate restTemplate=new RestTemplate();
@Autowired
    private MongoTemplate mongoTemplate;
    String uploadUrl= "https://archivist.terraform.io/v1/"; //this to be coming from db

    //Create Organization in Terraform
    public TFOrganization registerOrganization(TFOrganization tfOrganization){
        mongoTemplate.save(tfOrganization);
        return tfOrganization;
    }
    //get TF organization
    public TFOrganization getTFOrganization(){
        return getOrganization();
    }

    public TFOrganization updateTFOrganization(TFOrganization tfOrganization) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgName").is(tfOrganization.getOrgName()));
        TFOrganization oldOrg = mongoTemplate.findOne(query, TFOrganization.class);
        if (oldOrg != null) {
            Update update = new Update();
            update.set("orgName", tfOrganization.getOrgName());
            update.set("accessToken", tfOrganization.getAccessToken());
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TFOrganization.class);
            return updateResult.getModifiedCount() > 0 ? tfOrganization : null;
        }
        return null;
    }
    private TFOrganization getOrganization(){
        return mongoTemplate.findAll(TFOrganization.class).get(0);
    }

//creating a terraform workspace
public Workspace createWorkspace(CreateWSRequest createWorkspace){
    TFOrganization tfOrganization=getOrganization();
    ResponseEntity<Workspace>response=restTemplate.postForEntity(String.format(TerraformAPIs.WORKSPACE_CREATE,tfOrganization.getOrgName()),getEntity(buildWorkspaceRequest(createWorkspace),tfOrganization.getAccessToken()), Workspace.class);
    return response.getBody()!=null ? mongoTemplate.save(response.getBody()):null;
}
private createWorkspaceRequestBuilder buildWorkspaceRequest(CreateWSRequest createWSRequest){
        createWorkspaceRequestBuilder requestBuilder = new createWorkspaceRequestBuilder();
    WorkspaceData data=new WorkspaceData();
    data.setType("workspaces");
    WorkspaceDataAttributes attributes =new WorkspaceDataAttributes();
    attributes.setName(createWSRequest.getWorkspaceName());
    attributes.setResourceCount(0);
    data.setAttributes(attributes);
    requestBuilder.setData(data);
    return requestBuilder;
}


public Object createVariable(createWsVaraibleRequest createWsVaraibleRequest){
        ResponseEntity<Object> response=restTemplate.postForEntity(TerraformAPIs.WORKSPACE_VARIABLE_CREATE,getEntity(variableWorkspaceBuilder(createWsVaraibleRequest), getOrganization().getAccessToken()), Object.class);
    return response.getBody() !=null ? mongoTemplate.save(response.getBody(),"variables") : null;
    }
    private createWorkspaceRequestBuilder variableWorkspaceBuilder(createWsVaraibleRequest createWsVaraibleRequest){
        createWorkspaceRequestBuilder requestBuilder=new createWorkspaceRequestBuilder();
        WorkspaceData data=new WorkspaceData();
        createWSVariableAttributes attributes=new createWSVariableAttributes();
        WorkspaceDataRelationships relationships=new WorkspaceDataRelationships();
        WorkspaceDataRelationshipsWorkspace workspace=new WorkspaceDataRelationshipsWorkspace();
        WorkspaceDataRelationshipsWorkspaceData workspaceData = new WorkspaceDataRelationshipsWorkspaceData();
        workspaceData.setType("workspaces");
        workspaceData.setId(createWsVaraibleRequest.getWorkspaceId());
        workspace.setWorkspace(workspaceData);

        relationships.setWorkspace(workspace);
        attributes.setKey(createWsVaraibleRequest.getKey());
        attributes.setValue(createWsVaraibleRequest.getValue());
        attributes.setHcl(false);
        attributes.setSensitive(createWsVaraibleRequest.isSensitive());
        attributes.setDescription(createWsVaraibleRequest.getDescription());
        attributes.setCategory("terraform");

        data.setType("vars");
        data.setAttributes(attributes);
        data.setRelationship(relationships);
        requestBuilder.setData(data);
        return requestBuilder;

    }

public Object CreateConfigurationVersion(createConfigVersionReq configVersionReq){
        createWorkspaceRequestBuilder request=new createWorkspaceRequestBuilder();
        WorkspaceData data=new WorkspaceData();
        data.setType("configuration-versions");
        request.setData(data);
        ResponseEntity<Workspace> response=restTemplate.postForEntity(String.format(TerraformAPIs.WORKSPACE_CREATE_CONFIGURATION_VERSION,configVersionReq.getWorkspaceId()),getEntity(request,getOrganization().getAccessToken()), Workspace.class);
return  response.getBody() !=null;
    }
    private Workspace configurationVersionResponseBuilder(Workspace workspace){
        createConfigVersionAttributes attributes=(createConfigVersionAttributes) workspace.getData().getAttributes();
   workspace.getData().setAttributes(attributes);
   return workspace;
    }

    public Object createConfiguration(){
        HttpHeaders headers =new HttpHeaders();
        headers.add("Content-Type","application/octet-stream");
        headers.set("Content-Disposition","attachment; filename=\"terraform.tar.gz\"");
        ResponseEntity<Object>response=restTemplate.exchange(uploadUrl, HttpMethod.PUT,new HttpEntity<>(generateTerraformZip(),headers), Object.class);
        return response.getBody();
    }
    public byte[] generateTerraformZip(){
        File mainTfFile=new File("main.tf");
        String terraformContent = "terraform {\n" +
                "  required_providers {\n" +
                "    aws = {\n" +
                "      source  = \"hashicorp/aws\"\n" +
                "      version = \"3.58.0\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "variable \"aws_access_key\" {\n" +
                "    description = \"AWS access key ID\"\n" +
                "}\n" +
                "variable \"aws_secret_access_key\" {\n" +
                "    description = \"AWS secret access key\"\n" +
                "}\n" +
                "provider \"aws\" {\n" +
                "    region          = \"us-east-1\"\n" +
                "    access_key      = var.aws_access_key\n" +
                "    secret_key      = var.aws_secret_access_key\n" +
                "}\n" +
                "resource \"aws_instance\" \"example\" {\n" +
                "    ami           = \"ami-0261755bbcb8c4a84\" \n" +
                "    instance_type = \"t2.micro\"\n" +
                "}\n" +
                "output \"instance_id\" {\n" +
                "    value = aws_instance.example.id\n" +
                "}";

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mainTfFile);
            fileOutputStream.write(terraformContent.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            return null; // Error writing the file
        }

        // Create the zip file and add the config directory
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(byteArrayOutputStream))) {
            addDirectoryToZip(mainTfFile, tarOutputStream);
        } catch (IOException e) {
            return null; // Error creating the zip file
        }

        // Return the zip file
        return byteArrayOutputStream.toByteArray();
    }
    private void addDirectoryToZip(File directory, TarArchiveOutputStream tarOutputStream) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(directory);
        tarOutputStream.putArchiveEntry(new TarArchiveEntry(directory));

        byte[] buffer = new byte[1024];
        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
            tarOutputStream.write(buffer, 0, length);
        }

        fileInputStream.close();
        tarOutputStream.closeArchiveEntry();
    }
public Object destroyWorkSpace(DestroyWsRequest destroyWsRequest){
        ResponseEntity<Object>response=restTemplate.postForEntity(TerraformAPIs.WORKSPACE_DESTROY,getEntity(buildDestroyRequest(destroyWsRequest), getOrganization().getAccessToken()), Object.class);
        return response.getBody();
}
public createWorkspaceRequestBuilder buildDestroyRequest(DestroyWsRequest destroyWsRequest){
        createWorkspaceRequestBuilder requestBuilder = new createWorkspaceRequestBuilder();
        WorkspaceData data=new WorkspaceData();
        DestroyWsAttributes attributes=new DestroyWsAttributes();
        WorkspaceDataRelationships relationships=new WorkspaceDataRelationships();
        WorkspaceDataRelationshipsWorkspace workspace=new WorkspaceDataRelationshipsWorkspace();
        WorkspaceDataRelationshipsWorkspaceData workspaceData=new WorkspaceDataRelationshipsWorkspaceData();
        workspaceData.setId(destroyWsRequest.getWorkspaceId());
        workspaceData.setType("workspaces");
        relationships.setWorkspace(workspace);
        attributes.setDestroy(true);
        attributes.setMessage(destroyWsRequest.getMessage());
        data.setType("runs");
        data.setRelationship(relationships);
        data.setAttributes(attributes);
        requestBuilder.setData(data);
        return requestBuilder;
}
    public HttpEntity<createWorkspaceRequestBuilder>getEntity(createWorkspaceRequestBuilder createWorkspaceRequest,String token){
    HttpHeaders headers=new HttpHeaders();
    headers.add("Content-Type","application/vnd.api+json");
    headers.add("Authorization","Bearer" + token);
    return new HttpEntity<>(createWorkspaceRequest,headers);
}

}
