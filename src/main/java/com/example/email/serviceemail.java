package com.example.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Service
public class serviceemail {
    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiapiurl;

    @Value("${gemini.api.key}")
    private String  gminiapikey;

    public serviceemail(WebClient.Builder webclintbuilder){

        this.webClient=webclintbuilder.build();
    }

    public String generateEmail(Emailrequest email) {
        String prompt = buildPrompt(email);
        Map<String , Object> requestbody=Map.of(
                "contents",new Object[] {
                        Map.of("parts",new Object[] {
                                        Map.of("text", prompt)
        })
                }
        );
        String response = webClient.post()
                .uri(geminiapiurl + gminiapikey)
                .header("content-type","application/json")
                .bodyValue(requestbody)
                .retrieve()
                .bodyToMono(String.class)
                .block();




        return extractResponseContent(response);
    }
    private String extractResponseContent(String responce) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootnood=mapper.readTree(responce);
            return rootnood.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        }catch (Exception e){
            return e.getMessage();
        }
    }


    private String buildPrompt(Emailrequest email) {
        StringBuilder prompt = new StringBuilder();



        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line.");
//        prompt.append(" i give you  document content so give summarize answer = ");
        if (email.getTone() != null && !email.getTone().isEmpty()) {
            prompt.append(" Use a ").append(email.getTone()).append(" tone.");

        }
        prompt.append("\nOriginal email:\n").append(email.getEmailContent());
        return prompt.toString();
    }

}
