package com.printpass.job.client;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Component
public class GotenbergClient {

    private final RestClient restClient;

        public GotenbergClient(RestClient.Builder builder, 
                           @Value("${GOTENBERG_URL:http://localhost:3000}") String gotenbergUrl) {
        this.restClient = builder.baseUrl(gotenbergUrl).build(); 
    }

    public byte[] convertToPdf(byte[] fileBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        return restClient.post()
                .uri("/forms/libreoffice/convert")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(byte[].class);
    }
}