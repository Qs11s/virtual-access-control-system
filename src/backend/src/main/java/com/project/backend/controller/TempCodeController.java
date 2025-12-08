package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.model.TempCode;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.TempCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/access/temp-code")
public class TempCodeController {

    @Autowired
    private TempCodeRepository tempCodeRepository;

    @Autowired
    private AccessEventRepository accessEventRepository;

    private final Random random = new Random();

    @PostMapping
    public ResponseEntity<Map<String, String>> generateTempCode(@RequestBody TempCodeGenerateRequest request) {
        String tempCode = String.format("%06d", random.nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(request.getValidMinutes());
        
        TempCode codeEntity = new TempCode(
                tempCode,
                request.getLocationId(),
                request.getOwnerId(),
                expiresAt,
                request.getRemainingUses()
        );
        
        tempCodeRepository.save(codeEntity);
        
        Map<String, String> response = new HashMap<>();
        response.put("code", tempCode);
        response.put("expiresAt", expiresAt.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyTempCode(@RequestBody TempCodeVerifyRequest request) {
        Map<String, String> response = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        TempCode tempCode = tempCodeRepository.findByCodeAndLocationIdAndExpiresAtAfterAndRemainingUsesGreaterThan(
                request.getCode(),
                request.getLocationId(),
                now,
                0
        ).orElse(null);

        if (tempCode == null) {
            response.put("result", "deny");
            response.put("reason", "密码无效/已过期/次数用尽");
            return ResponseEntity.ok(response);
        }

        tempCode.setRemainingUses(tempCode.getRemainingUses() - 1);
        if (tempCode.getRemainingUses() == 0) {
            tempCode.setUsedAt(now);
        }
        tempCodeRepository.save(tempCode);

        AccessEvent accessEvent = new AccessEvent();
        accessEvent.setLocationId(request.getLocationId());
        accessEvent.setAccessTime(now);
        accessEvent.setAccessMethod("TEMP_CODE");
        accessEvent.setStatus("ALLOWED");
        accessEventRepository.save(accessEvent);

        response.put("result", "allow");
        response.put("reason", "验证成功，允许开门");
        return ResponseEntity.ok(response);
    }

    public static class TempCodeGenerateRequest {
        private Long locationId;
        private Long ownerId;
        private Integer validMinutes;
        private Integer remainingUses;

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
        public Integer getValidMinutes() { return validMinutes; }
        public void setValidMinutes(Integer validMinutes) { this.validMinutes = validMinutes; }
        public Integer getRemainingUses() { return remainingUses; }
        public void setRemainingUses(Integer remainingUses) { this.remainingUses = remainingUses; }
    }

    public static class TempCodeVerifyRequest {
        private Long locationId;
        private String code;

        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}