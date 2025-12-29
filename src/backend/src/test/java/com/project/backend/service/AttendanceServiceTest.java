package com.project.backend.service;

import com.project.backend.exception.DuplicateCheckInException;
import com.project.backend.exception.ExpiredSessionException;
import com.project.backend.exception.StudentNotEnrolledException;
import com.project.backend.model.Attendance;
import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private SessionRepository sessionRepository;
    private StudentCourseRepository studentCourseRepository;
    private AttendanceRepository attendanceRepository;

    private AttendanceService attendanceService;

    private User student;
    private Course course;
    private SessionEntity session;

    @BeforeEach
    void setUp() {
        sessionRepository = Mockito.mock(SessionRepository.class);
        studentCourseRepository = Mockito.mock(StudentCourseRepository.class);
        attendanceRepository = Mockito.mock(AttendanceRepository.class);

        attendanceService = new AttendanceService(sessionRepository, studentCourseRepository, attendanceRepository);
        // 配置早退阈值 = 5 分钟
        ReflectionTestUtils.setField(attendanceService, "earlyLeaveMinutes", 5);

        student = new User();
        student.setId(100L);
        student.setUsername("student_wang");

        course = new Course();
        course.setId(1L);

        session = new SessionEntity();
        session.setId(2L);
        session.setCourse(course);
    }

    @Test
    void checkIn_onTime_shouldSetStatusOnTime() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(5));
        session.setEndTime(now.plusMinutes(30));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.of(new StudentCourse()));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.checkIn(student, 2L);

        assertNotNull(result.getCheckInTime());
        assertEquals("ON_TIME", result.getStatus());
        assertEquals(student, result.getStudent());
        assertEquals(session, result.getSession());
    }

    @Test
    void checkIn_late_shouldSetStatusLate() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(20)); // 开始时间比现在早 20 分钟
        session.setEndTime(now.plusMinutes(30));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.of(new StudentCourse()));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.checkIn(student, 2L);

        assertEquals("LATE", result.getStatus());
    }

    @Test
    void checkIn_whenNotEnrolled_shouldThrowStudentNotEnrolledException() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(5));
        session.setEndTime(now.plusMinutes(30));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.empty());

        assertThrows(StudentNotEnrolledException.class,
                () -> attendanceService.checkIn(student, 2L));
    }

    @Test
    void checkIn_whenOutsideSessionTime_shouldThrowExpiredSessionException() {
        LocalDateTime now = LocalDateTime.now();
        // 课程还没开始：start 比 now 晚 10 分钟
        session.setStartTime(now.plusMinutes(10));
        session.setEndTime(now.plusMinutes(60));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));

        assertThrows(ExpiredSessionException.class,
                () -> attendanceService.checkIn(student, 2L));
    }

    @Test
    void checkIn_whenDuplicateCheckIn_shouldThrowDuplicateCheckInException() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(5));
        session.setEndTime(now.plusMinutes(30));

        Attendance existing = new Attendance();
        existing.setStudent(student);
        existing.setSession(session);
        existing.setCheckInTime(now.minusMinutes(1)); // 已经签过到

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(studentCourseRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.of(new StudentCourse()));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.of(existing));

        assertThrows(DuplicateCheckInException.class,
                () -> attendanceService.checkIn(student, 2L));
    }

    @Test
    void checkOut_whenEarlyLeave_shouldSetStatusEarlyLeave() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(30));
        session.setEndTime(now.plusMinutes(30)); // 距离下课还有 30 分钟 > earlyLeaveMinutes(5)

        Attendance existing = new Attendance();
        existing.setId(200L);
        existing.setStudent(student);
        existing.setSession(session);
        existing.setCheckInTime(now.minusMinutes(10));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Attendance result = attendanceService.checkOut(student, 2L);

        assertNotNull(result.getCheckOutTime());
        assertEquals("EARLY_LEAVE", result.getStatus());
    }

    @Test
    void checkOut_whenAlreadyCheckedOut_shouldThrowRuntimeException() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(30));
        session.setEndTime(now.plusMinutes(30));

        Attendance existing = new Attendance();
        existing.setStudent(student);
        existing.setSession(session);
        existing.setCheckInTime(now.minusMinutes(20));
        existing.setCheckOutTime(now.minusMinutes(10)); // 已经签退

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> attendanceService.checkOut(student, 2L));
    }

    @Test
    void checkOut_whenNoExistingAttendance_shouldThrowRuntimeException() {
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now.minusMinutes(30));
        session.setEndTime(now.plusMinutes(30));

        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByStudentAndSession(student, session))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> attendanceService.checkOut(student, 2L));
    }
}
