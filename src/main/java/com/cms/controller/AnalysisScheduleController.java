package com.cms.controller;

import com.cms.model.AnalysisSchedule;
import com.cms.model.ReactionTrackedModel;
import com.cms.service.AnalysisSchedulerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analysis-schedules")
@AllArgsConstructor
public class AnalysisScheduleController {

    private final AnalysisSchedulerService analysisSchedulerService;

    @GetMapping
    public ResponseEntity<List<AnalysisSchedule>> getAllSchedules() {
        List<AnalysisSchedule> schedules = analysisSchedulerService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisSchedule> getScheduleById(@PathVariable String id) {
        AnalysisSchedule schedule = analysisSchedulerService.getScheduleById(id);
        if (schedule != null) {
            return ResponseEntity.ok(schedule);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<AnalysisSchedule> createSchedule(@RequestBody AnalysisSchedule schedule) {
        AnalysisSchedule createdSchedule = analysisSchedulerService.createSchedule(schedule);
        return ResponseEntity.ok(createdSchedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnalysisSchedule> updateSchedule(@PathVariable String id, @RequestBody AnalysisSchedule schedule) {
        AnalysisSchedule updatedSchedule = analysisSchedulerService.updateSchedule(id, schedule);
        if (updatedSchedule != null) {
            return ResponseEntity.ok(updatedSchedule);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String id) {
        boolean deleted = analysisSchedulerService.deleteSchedule(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enableSchedule(@PathVariable String id) {
        boolean enabled = analysisSchedulerService.enableSchedule(id);
        if (enabled) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableSchedule(@PathVariable String id) {
        boolean disabled = analysisSchedulerService.disableSchedule(id);
        if (disabled) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/run-now")
    public ResponseEntity<Void> runScheduleNow(@PathVariable String id) {
        analysisSchedulerService.runScheduleNow(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-model-type/{modelType}")
    public ResponseEntity<List<AnalysisSchedule>> getSchedulesByModelType(@PathVariable ReactionTrackedModel.ModelType modelType) {
        List<AnalysisSchedule> schedules = analysisSchedulerService.getSchedulesByModelType(modelType);
        return ResponseEntity.ok(schedules);
    }
}
