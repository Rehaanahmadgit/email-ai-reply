package com.example.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
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

    public String genrateemail (Emailrequest email) {
        String promt=buildpromt((email));
        Map<String , Object> requestbody=Map.of(
                "contents",new Object[] {
                        Map.of("parts",new Object[] {
                                        Map.of("text", promt)
        })
                }
        );
        String responce=webClient.post()
                .uri(geminiapiurl+gminiapikey)
                .header("content-type","application/json")
                .bodyValue(requestbody)
                .retrieve()
                .bodyToMono(String.class)
                .block();




        return extractResponseContent(responce);
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


    private String buildpromt(Emailrequest email) {
        StringBuilder promt = new StringBuilder();



        promt.append("Generate a professional email reply for hte following email content. Please don't generate a subject line");
        if (email.getTone()!=null && !email.getTone().equals("")) {
            promt.append("use a").append(email.getTone()).append(" tone");

        }
        promt.append("\n original email :\n").append(email.getEmailContent());
        return promt.toString();
    }

}
