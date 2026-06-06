package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.ScheduleTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 定时任务控制器
 */
@RestController
@RequestMapping("/api/schedule-task")
@RequiredArgsConstructor
public class ScheduleTaskController {

    private final ScheduleTaskService scheduleTaskService;

    @GetMapping("/list")
    public ApiResponse<PageResult<ScheduleTaskDto>> list(
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(scheduleTaskService.search(taskCode, taskName, taskType, status));
    }

    @GetMapping("/active")
    public ApiResponse<java.util.List<ScheduleTaskDto>> getActiveTasks() {
        return ApiResponse.success(scheduleTaskService.getActiveTasks());
    }

    @GetMapping("/{id}")
    public ApiResponse<ScheduleTaskDto> getById(@PathVariable Long id) {
        return ApiResponse.success(scheduleTaskService.getById(id));
    }

    @PostMapping("/create")
    public ApiResponse<ScheduleTaskDto> create(@RequestBody ScheduleTaskDto dto) {
        com.vueadmin.entity.system.ScheduleTask task = scheduleTaskService.create(dto);
        ScheduleTaskDto result = new ScheduleTaskDto();
        result.setId(task.getId());
        result.setTaskCode(task.getTaskCode());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody ScheduleTaskDto dto) {
        scheduleTaskService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        scheduleTaskService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/start/{id}")
    public ApiResponse<?> start(@PathVariable Long id) {
        scheduleTaskService.start(id);
        return ApiResponse.success("任务已启动");
    }

    @PostMapping("/pause/{id}")
    public ApiResponse<?> pause(@PathVariable Long id) {
        scheduleTaskService.pause(id);
        return ApiResponse.success("任务已暂停");
    }

    @PostMapping("/execute/{id}")
    public ApiResponse<?> executeNow(@PathVariable Long id) {
        scheduleTaskService.executeNow(id);
        return ApiResponse.success("任务已触发执行");
    }
}
