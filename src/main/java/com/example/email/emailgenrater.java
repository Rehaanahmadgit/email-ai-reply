package com.example.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/email")
public class emailgenrater {
    @Autowired
    serviceemail service;
    @PostMapping("/genrate")
    public ResponseEntity<String> genrateemail (@RequestBody Emailrequest email) {
        String responce=service.genrateemail(email);
        return ResponseEntity.ok(responce);
    }

}
