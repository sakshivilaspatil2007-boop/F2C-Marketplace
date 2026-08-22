package com.f2c.marketplace.repository;

import com.f2c.marketplace.model.FarmerCopilotChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FarmerCopilotChatRepository extends JpaRepository<FarmerCopilotChat, Long> {
    List<FarmerCopilotChat> findByFarmerIdOrderByCreatedAtAsc(Long farmerId);
    void deleteByFarmerId(Long farmerId);
}
