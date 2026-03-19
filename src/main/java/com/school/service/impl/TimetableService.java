package com.school.service.impl;

import com.school.dto.request.CreateTimetableSlotRequest;
import com.school.dto.response.TimetableSlotResponse;
import com.school.entity.Teacher;
import com.school.entity.TimetableSlot;
import com.school.enums.WeekdayEnum;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.TeacherRepository;
import com.school.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final TeacherRepository teacherRepository;

    public List<TimetableSlotResponse> getTimetableByClass(String className, String section, String academicYear) {
        return timetableRepository.findByClassNameAndSectionAndAcademicYear(className, section, academicYear)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TimetableSlotResponse> getTimetableByClassAndDay(String className, String section, WeekdayEnum day, String academicYear) {
        return timetableRepository.findByClassNameAndSectionAndDayAndAcademicYear(className, section, day, academicYear)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TimetableSlotResponse> getTeacherTimetable(UUID teacherId) {
        return timetableRepository.findByTeacherId(teacherId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TimetableSlotResponse> getTeacherTimetableByDay(UUID teacherId, WeekdayEnum day) {
        return timetableRepository.findByTeacherIdAndDay(teacherId, day)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TimetableSlotResponse createSlot(CreateTimetableSlotRequest request) {
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getTeacherId()));

        // Check for class conflict
        boolean classConflict = timetableRepository
                .findByClassNameAndSectionAndDayAndAcademicYear(request.getClassName(), request.getSection(), request.getDay(), request.getAcademicYear())
                .stream().anyMatch(s -> s.getIsActive() &&
                        !(request.getEndTime().compareTo(s.getStartTime()) <= 0 ||
                          request.getStartTime().compareTo(s.getEndTime()) >= 0));
        if (classConflict) throw new ConflictException("Time slot conflicts with existing class schedule");

        TimetableSlot slot = TimetableSlot.builder()
                .className(request.getClassName()).section(request.getSection())
                .day(request.getDay()).startTime(request.getStartTime()).endTime(request.getEndTime())
                .subject(request.getSubject()).teacher(teacher).room(request.getRoom())
                .academicYear(request.getAcademicYear()).isActive(true)
                .build();
        return mapToResponse(timetableRepository.save(slot));
    }

    @Transactional
    public void deleteSlot(UUID id) {
        TimetableSlot slot = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));
        slot.setIsActive(false);
        timetableRepository.save(slot);
    }

    private TimetableSlotResponse mapToResponse(TimetableSlot s) {
        return TimetableSlotResponse.builder()
                .id(s.getId()).className(s.getClassName()).section(s.getSection())
                .day(s.getDay()).startTime(s.getStartTime()).endTime(s.getEndTime())
                .subject(s.getSubject()).teacherId(s.getTeacher().getId())
                .teacherName(s.getTeacher().getUser().getName())
                .room(s.getRoom()).academicYear(s.getAcademicYear()).isActive(s.getIsActive())
                .build();
    }
}
