package com.email_sb.controller;

import com.email_sb.service.EmailGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailGeneratorController {

    @Autowired
    private EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    public String generateReply(@RequestBody EmailRequest request) {
        return emailGeneratorService.generateEmailReply(request);
    }
}
