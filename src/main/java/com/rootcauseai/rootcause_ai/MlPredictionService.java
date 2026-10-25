
package com.rootcauseai.rootcause_ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class MlPredictionService {

    private final RestClient restClient;

    public MlPredictionService(
            @Value("${ml.api.url}") String mlApiUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(mlApiUrl)
                .build();
    }

    public Map<String, Object> predict(String message) {
        return restClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "message",
                        message == null ? "" : message))
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> predictFile(
            byte[] fileBytes,
            String filename) {

        String safeFilename = filename == null
                ? "logs.txt"
                : filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        ByteArrayResource fileResource =
                new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return safeFilename;
                    }
                };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", fileResource)
                .filename(safeFilename)
                .contentType(MediaType.TEXT_PLAIN);

        MultiValueMap<String, org.springframework.http.HttpEntity<?>> body =
                builder.build();

        return restClient.post()
                .uri("/predict-file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);
    }
}
