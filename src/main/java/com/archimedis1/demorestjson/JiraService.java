package com.archimedis1.demorestjson;

import org.springframework.stereotype.Service;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.io.File;

@Service
public class JiraService {

    private static final String JIRA_BASE_URL = "http://localhost:8080"; 
    private static final String USERNAME = "username";
    private static final String PASSWORD = "pass"; 

    public void addCommentAndAttachment(String issueKey, String comment, String filePath) throws Exception {
        String commentId = addComment(issueKey, comment);
        
        String attachmentInfo = uploadAttachment(issueKey, filePath);
        
        updateCommentWithAttachment(issueKey, commentId, comment, attachmentInfo);
    }

    public String  addComment(String issueKey, String comment) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("body", comment);



        HttpResponse<JsonNode> response = Unirest.post(JIRA_BASE_URL + "/rest/api/2/issue/{issueIdOrKey}/comment")
            .basicAuth(USERNAME, PASSWORD)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("X-Atlassian-Token", "no-check")
            .routeParam("issueIdOrKey", issueKey)
            .body(requestBody.toString())
            .asJson();

        System.out.println(response.getBody());
        if (response.getStatus() != 201) { // 201 Created
            throw new Exception("Failed to add comment. Status code: " + response.getStatus());
        }

        return response.getBody().getObject().getString("id");
    }

    public String uploadAttachment(String issueKey, String filePath) throws Exception {
        File fileToUpload = new File(filePath);
    
        HttpResponse<JsonNode> response = Unirest.post(JIRA_BASE_URL + "/rest/api/2/issue/{issueIdOrKey}/attachments")
            .basicAuth(USERNAME, PASSWORD)
            .header("X-Atlassian-Token", "no-check")
            .routeParam("issueIdOrKey", issueKey)
            .field("file", fileToUpload)
            .asJson();
    
        System.out.println("Attachment upload response: " + response.getBody());
    
        if (response.getStatus() != 200) {
            throw new Exception("Failed to upload attachment. Status code: " + response.getStatus());
        }
    
        JSONArray responseBody = response.getBody().getArray();
        if (responseBody == null || responseBody.length() == 0) {
            throw new Exception("Invalid response structure for attachments.");
        }
    
        String attachmentUrl = responseBody.getJSONObject(0).getString("filename");
    
    
        return  attachmentUrl; 
    }

    public void updateCommentWithAttachment(String issueKey, String commentId, String comment, String attachmentInfo) throws Exception {
        String attachmentUrl = attachmentInfo;
    
        String updatedComment = comment + "\n\n[^"  + attachmentUrl + "]";
    
        JSONObject requestBody = new JSONObject();
        requestBody.put("body", updatedComment);
    
        HttpResponse<JsonNode> response = Unirest.put(JIRA_BASE_URL + "/rest/api/2/issue/{issueIdOrKey}/comment/{commentId}")
            .basicAuth(USERNAME, PASSWORD)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .routeParam("issueIdOrKey", issueKey)
            .routeParam("commentId", commentId)
            .body(requestBody.toString())
            .asJson();
    
        if (response.getStatus() != 200) { // 200 OK
            throw new Exception("Failed to update comment with attachment. Status code: " + response.getStatus());
        }
    }
}