package com.example.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class emailgenrater {
    @Autowired
    serviceemail service;
//
//
//    @PostMapping("/generate")
//    public ResponseEntity<String> generateEmail(@RequestBody Emailrequest email) {
//        String response = service.generateEmail(email);
//        return ResponseEntity.ok(response);
//    }
//

    @Autowired
    private DocumentService documentService;

    // Upload file and return extracted text
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) throws Exception {
        String extractedText = documentService.extractText(file);

        if (extractedText == null || extractedText.isEmpty()) {
            return "No text found in the document!";
        }

        // Convert string -> EmailRequest
        Textcontent textcontent = new Textcontent();

        textcontent.setContent(extractedText);
        textcontent.setQuestion(question);
        // Default tone or modify as needed

        return service.getnrateanswer(textcontent);
    }
}

