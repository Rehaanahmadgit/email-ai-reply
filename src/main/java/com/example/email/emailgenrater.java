package com.example.email;

import com.example.email.entity.CodeRequest;
import com.example.email.entity.RunResponse;
import com.example.email.entity.Textcontent;
import com.example.email.service.DocumentService;
import com.example.email.service.JavaRunnerService;
import com.example.email.service.serviceemail;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class emailgenrater {

    //
//
//    @PostMapping("/generate")
//    public ResponseEntity<String> generateEmail(@RequestBody Emailrequest email) {
//        String response = service.generateEmail(email);
//        return ResponseEntity.ok(response);
//    }
    @Autowired
    serviceemail service;
    @Autowired
    private final JavaRunnerService runner;
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


    public emailgenrater(JavaRunnerService runner) {
        this.runner = runner;
    }

    @PostMapping(value = "/compile-run", consumes = "application/json", produces = "application/json")
  public ResponseEntity<RunResponse> compileRun(@Valid @RequestBody CodeRequest req) {
        try {
            var result = runner.compileAndRun(req.getCode());
            var resp = new RunResponse(result.success(), result.stage(), result.out(), result.err(), result.timeMs());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            var resp = new RunResponse(false, "error", "", e.getMessage(), 0);
            return ResponseEntity.status(500).body(resp);
        }
    }


}

