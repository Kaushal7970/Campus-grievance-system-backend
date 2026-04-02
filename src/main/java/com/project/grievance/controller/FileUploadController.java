package com.project.grievance.controller;

import java.io.File;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/files")
public class FileUploadController {

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {

        String path = "uploads/" + file.getOriginalFilename();
        file.transferTo(new File(path));

        return "Uploaded Successfully";
    }
}