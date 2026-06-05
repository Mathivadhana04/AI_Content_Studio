// seo/repository/SeoReportRepository.java
package seo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seo.entity.SeoReport;

public interface SeoReportRepository extends JpaRepository<SeoReport, Long> {
}