package com.zantrix.mobile.controller;

import com.zantrix.tasks.domain.TaskEntity;
import com.zantrix.tasks.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mobile-bff")
public class MobileBffController {

    private final TaskService taskService;

    public MobileBffController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getMobileDashboardData(@PathVariable UUID userId) {
        // Aggregates data for the mobile dashboard (BFF pattern)
        List<TaskEntity> myTasks = taskService.getTasksForUser(userId);
        
        // Count uncompleted tasks
        long activeTasks = myTasks.stream().filter(t -> t.getStatus() != com.zantrix.tasks.domain.TaskStatus.DONE).count();

        // In a full implementation, we might also fetch recent alerts, messages, etc.
        return ResponseEntity.ok(Map.of(
            "activeTasksCount", activeTasks,
            "tasks", myTasks
        ));
    }
}
