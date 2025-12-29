package com.project.backend.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TempCodeServiceTest {

    @Mock
    private TempCodeRepository tempCodeRepository;

    @Mock
    private AccessEventRepository accessEventRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TempCodeService tempCodeService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void verifyTempCode_whenCodeNotFound_shouldDeny() {
        TempCodeVerifyRequest request = new TempCodeVerifyRequest();
        request.setLocationId(1L);
        request.setCode("123456");

        when(tempCodeRepository.findByCodeAndLocationId("123456", 1L))
                .thenReturn(Optional.empty());

        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);

        assertEquals("deny", response.getResult());
        assertEquals("密码无效", response.getReason());
        verify(tempCodeRepository, never()).save(any());
        verify(accessEventRepository, never()).save(any());
    }

    @Test
    void verifyTempCode_whenExpired_shouldDeny() {
        TempCode tempCode = new TempCode();
        tempCode.setId(1L);
        tempCode.setCode("123456");
        tempCode.setLocationId(1L);
        tempCode.setOwnerId(46L);
        tempCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tempCode.setRemainingUses(1);

        TempCodeVerifyRequest request = new TempCodeVerifyRequest();
        request.setLocationId(1L);
        request.setCode("123456");

        when(tempCodeRepository.findByCodeAndLocationId("123456", 1L))
                .thenReturn(Optional.of(tempCode));

        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);

        assertEquals("deny", response.getResult());
        assertEquals("临时码已过期", response.getReason());
    }

    @Test
    void verifyTempCode_whenNoRemainingUses_shouldDeny() {
        TempCode tempCode = new TempCode();
        tempCode.setId(1L);
        tempCode.setCode("123456");
        tempCode.setLocationId(1L);
        tempCode.setOwnerId(46L);
        tempCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        tempCode.setRemainingUses(0);

        TempCodeVerifyRequest request = new TempCodeVerifyRequest();
        request.setLocationId(1L);
        request.setCode("123456");

        when(tempCodeRepository.findByCodeAndLocationId("123456", 1L))
                .thenReturn(Optional.of(tempCode));

        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);

        assertEquals("deny", response.getResult());
        assertEquals("临时码次数已用尽", response.getReason());
    }

    @Test
    void verifyTempCode_whenValid_shouldAllowAndDecreaseRemainingUses() {
        LocalDateTime now = LocalDateTime.now();

        TempCode tempCode = new TempCode();
        tempCode.setId(1L);
        tempCode.setCode("123456");
        tempCode.setLocationId(1L);
        tempCode.setOwnerId(46L);
        tempCode.setExpiresAt(now.plusMinutes(10));
        tempCode.setRemainingUses(2);

        TempCodeVerifyRequest request = new TempCodeVerifyRequest();
        request.setLocationId(1L);
        request.setCode("123456");

        Location location = new Location();
        location.setId(1L);
        location.setName("Main Gate");

        User owner = new User();
        owner.setId(46L);
        owner.setUsername("owner_user");

        when(tempCodeRepository.findByCodeAndLocationId("123456", 1L))
                .thenReturn(Optional.of(tempCode));
        when(locationRepository.findById(1L))
                .thenReturn(Optional.of(location));
        when(userRepository.findById(46L))
                .thenReturn(Optional.of(owner));

        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);

        assertEquals("allow", response.getResult());
        assertEquals("验证成功，允许开门", response.getReason());
        assertEquals(1, tempCode.getRemainingUses());
        assertNotNull(tempCode.getUsedAt());

        verify(tempCodeRepository).save(tempCode);
        verify(accessEventRepository).save(any(AccessEvent.class));
    }
}
