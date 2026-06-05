// seo/controller/SeoController.java
package seo.controller;

import org.springframework.web.bind.annotation.*;
import seo.dto.SeoRequest;
import seo.dto.SeoResponse;
import seo.service.SeoService;

@RestController
@RequestMapping("/api/seo")
@CrossOrigin
public class SeoController {

    private final SeoService seoService;

    public SeoController(SeoService seoService) {
        this.seoService = seoService;
    }

    @PostMapping("/analyze")
    public SeoResponse analyze(@RequestBody SeoRequest request) {
        return seoService.analyzeSeo(request);
    }
}