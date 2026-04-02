package com.project.grievance.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {

        String path = "uploads/" + file.getOriginalFilename();
        file.transferTo(new File(path));

        return "Uploaded Successfully";
    }
}