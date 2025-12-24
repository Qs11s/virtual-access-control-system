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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Optional;

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

    /**
     * 创建临时码（适配DTO入参和出参）
     */
    public TempCodeCreateResponse createTempCode(TempCodeCreateRequest request) {
        // 生成6位随机临时码
        String tempCodeStr = String.format("%06d", random.nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(request.getValidMinutes());

        // 构建并保存临时码实体
        TempCode codeEntity = new TempCode();
        codeEntity.setCode(tempCodeStr);
        codeEntity.setLocationId(request.getLocationId());
        codeEntity.setOwnerId(request.getOwnerId());
        codeEntity.setValidMinutes(request.getValidMinutes());
        codeEntity.setExpiresAt(expiresAt);
        codeEntity.setRemainingUses(request.getRemainingUses());
        tempCodeRepository.save(codeEntity);

        // 返回专用响应DTO
        return new TempCodeCreateResponse(tempCodeStr, expiresAt);
    }

    /**
     * 验证临时码（适配DTO入参和出参）
     */
    public TempCodeVerifyResponse verifyTempCode(TempCodeVerifyRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 查询临时码是否存在
        Optional<TempCode> tempCodeOpt = tempCodeRepository.findByCodeAndLocationId(
                request.getCode(), request.getLocationId()
        );
        if (tempCodeOpt.isEmpty()) {
            return new TempCodeVerifyResponse("deny", "密码无效");
        }

        TempCode tempCode = tempCodeOpt.get();
        // 2. 校验是否过期
        if (tempCode.getExpiresAt().isBefore(now)) {
            return new TempCodeVerifyResponse("deny", "临时码已过期");
        }

        // 3. 校验剩余次数
        if (tempCode.getRemainingUses() <= 0) {
            return new TempCodeVerifyResponse("deny", "临时码次数已用尽");
        }

        // 4. 更新临时码状态
        tempCode.setRemainingUses(tempCode.getRemainingUses() - 1);
        tempCode.setUsedAt(now);
        tempCodeRepository.save(tempCode);

        // 5. 查询关联的Location和User
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + request.getLocationId()));
        User owner = userRepository.findById(tempCode.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + tempCode.getOwnerId()));

        // 6. 保存门禁访问记录
        AccessEvent accessEvent = new AccessEvent(owner, location, now, true, "TEMP_CODE", "ALLOWED");
        accessEventRepository.save(accessEvent);

        // 7. 返回成功响应DTO
        return new TempCodeVerifyResponse("allow", "验证成功，允许开门");
    }
}