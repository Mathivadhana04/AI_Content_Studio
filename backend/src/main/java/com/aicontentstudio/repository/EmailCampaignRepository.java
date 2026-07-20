package com.aicontentstudio.repository;

import com.aicontentstudio.entity.EmailCampaign;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {

    Page<EmailCampaign> findByAuthor(User author, Pageable pageable);

    Page<EmailCampaign> findByAuthorAndEmailType(User author, EmailType emailType, Pageable pageable);
}
