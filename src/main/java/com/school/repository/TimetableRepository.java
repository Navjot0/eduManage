package com.school.repository;

import com.school.entity.TimetableSlot;
import com.school.enums.WeekdayEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableSlot, UUID> {
    List<TimetableSlot> findByClassNameAndSectionAndAcademicYear(String className, String section, String academicYear);
    List<TimetableSlot> findByClassNameAndSectionAndDayAndAcademicYear(String className, String section, WeekdayEnum day, String academicYear);
    List<TimetableSlot> findByTeacherId(UUID teacherId);
    List<TimetableSlot> findByTeacherIdAndAcademicYear(UUID teacherId, String academicYear);
    List<TimetableSlot> findByTeacherIdAndDay(UUID teacherId, WeekdayEnum day);
}
