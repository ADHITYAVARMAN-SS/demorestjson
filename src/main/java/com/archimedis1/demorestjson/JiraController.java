package com.archimedis1.demorestjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jira")
public class JiraController {

    @Autowired
    private JiraService jiraService;

    // add comment with an attachement
    @PostMapping("/comment-attachment")
    public String addCommentAndAttachment(@RequestParam String issueKey, @RequestParam String comment, @RequestParam String filePath) {
        try {
            jiraService.addCommentAndAttachment(issueKey, comment, filePath);
            return "Comment and attachement added successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to add comment: " + e.getMessage();
        }
    }
}
