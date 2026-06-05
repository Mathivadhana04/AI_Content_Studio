// seo/service/SeoService.java
package seo.service;

import seo.dto.SeoRequest;
import seo.dto.SeoResponse;

public interface SeoService {
    SeoResponse analyzeSeo(SeoRequest request);
}