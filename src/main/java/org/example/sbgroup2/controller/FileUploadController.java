package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.CashbackDetailsDTO;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.services.CashbackService;
import org.example.sbgroup2.services.FileExtractService;
import org.example.sbgroup2.services.FileUploaderService;
import org.example.sbgroup2.services.MasterDataService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file-upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class FileUploadController {
    private final FileUploaderService fileUploaderService;
    private final MasterDataService masterDataService;
    private final FileExtractService fileExtractService;
    private final CashbackService cashbackService;

    @PostMapping("/excel-csv")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("CSV file is empty");
        }

//        List<MasterData> rows = masterDataService.readExcel(file);
        fileUploaderService.importFromExcel(file);

        return ResponseEntity.ok("Master data uploaded successfully");
    }

    @GetMapping("/excel/export")
    public ResponseEntity<InputStreamResource> exportCashbackExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        List<CashbackDetailsDTO> data =
                cashbackService.getCashbacksByNextDueDate(date);

        ByteArrayInputStream excel =
                fileExtractService.generate(data);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition",
                "attachment; filename=cashback_" + date + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(excel));
    }



    /**
     * Main import endpoint - actually saves data to database
     */
    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file uploaded"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only .xlsx files are supported"));
        }

        try {
            List<MasterData> imported = fileUploaderService.importFromExcel(file);

            return ResponseEntity.ok(Map.of(
                    "status", "import_success",
                    "importedCount", imported.size(),
                    "message", "Successfully imported " + imported.size() + " records"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    ));
        }
    }

    // Optional: Get one record (example)
    @GetMapping("/{id}")
    public ResponseEntity<MasterData> getMasterData(@PathVariable Long id) {
        MasterData data = masterDataService.getMasterDataById(id);
        return ResponseEntity.ok(data);
    }
}
