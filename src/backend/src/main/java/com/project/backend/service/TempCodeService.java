package com.project.backend.service;

import com.project.backend.dto.TempCodeCreateRequest;
import com.project.backend.dto.TempCodeCreateResponse;
import com.project.backend.dto.TempCodeVerifyRequest;
import com.project.backend.dto.TempCodeVerifyResponse;
import com.project.backend.model.AccessEvent;
import com.project.backend.model.Location;
import com.project.backend.model.TempCode;
import com.project.backend.model.User;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.LocationRepository;
import com.project.backend.repository.TempCodeRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class TempCodeService {

    @Autowired
    private TempCodeRepository tempCodeRepository;

    @Autowired
    private AccessEventRepository accessEventRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    public TempCodeCreateResponse createTempCode(TempCodeCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("未登录用户不能创建临时码");
        }

        String username = authentication.getName();
        User owner = userRepository.findFirstByUsernameOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        String tempCodeStr = String.format("%06d", random.nextInt(999999));
        LocalDateTime now = LocalDateTime.now();
        Integer expiresInMinutes = request.getExpiresInMinutes();
        Integer maxUses = request.getMaxUses();
        LocalDateTime expiresAt = now.plusMinutes(expiresInMinutes);

        TempCode codeEntity = new TempCode();
        codeEntity.setCode(tempCodeStr);
        codeEntity.setLocationId(request.getLocationId());
        codeEntity.setOwnerId(owner.getId());
        codeEntity.setValidMinutes(expiresInMinutes);
        codeEntity.setExpiresAt(expiresAt);
        codeEntity.setRemainingUses(maxUses);
        tempCodeRepository.save(codeEntity);

        return new TempCodeCreateResponse(tempCodeStr, expiresAt);
    }

    public TempCodeVerifyResponse verifyTempCode(TempCodeVerifyRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Optional<TempCode> tempCodeOpt = tempCodeRepository.findByCodeAndLocationId(
                request.getCode(), request.getLocationId()
        );
        if (tempCodeOpt.isEmpty()) {
            return new TempCodeVerifyResponse("deny", "密码无效");
        }

        TempCode tempCode = tempCodeOpt.get();
        if (tempCode.getExpiresAt().isBefore(now)) {
            return new TempCodeVerifyResponse("deny", "临时码已过期");
        }

        if (tempCode.getRemainingUses() <= 0) {
            return new TempCodeVerifyResponse("deny", "临时码次数已用尽");
        }

        tempCode.setRemainingUses(tempCode.getRemainingUses() - 1);
        tempCode.setUsedAt(now);
        tempCodeRepository.save(tempCode);

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + request.getLocationId()));
        User owner = userRepository.findById(tempCode.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + tempCode.getOwnerId()));

        AccessEvent accessEvent = new AccessEvent(owner, location, now, true, "TEMP_CODE", "ALLOWED");
        accessEventRepository.save(accessEvent);

        return new TempCodeVerifyResponse("allow", "验证成功，允许开门");
    }
}
