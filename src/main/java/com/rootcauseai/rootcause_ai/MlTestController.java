
package com.rootcauseai.rootcause_ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MlTestController {

    private final MlPredictionService mlPredictionService;

    public MlTestController(MlPredictionService mlPredictionService) {
        this.mlPredictionService = mlPredictionService;
    }

    @GetMapping("/api/ml-test")
    public Map<String, Object> testPrediction(
            @RequestParam(defaultValue = "FATAL Java heap space OutOfMemoryError")
            String message) {

        return mlPredictionService.predict(message);
    }

    @PostMapping(
        value = "/api/ml-test/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadLogFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a non-empty log file.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !(filename.toLowerCase().endsWith(".log") ||
                  filename.toLowerCase().endsWith(".txt"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only .log and .txt files are supported.");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Maximum file size is 2 MB.");
        }

        try {
            Map<String, Object> result =
                    mlPredictionService.predictFile(
                            file.getBytes(),
                            filename);

            return ResponseEntity.ok(result);

        } catch (java.io.IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Could not read uploaded file.");
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not get a prediction from the Python ML service.");
        }
    }
}
