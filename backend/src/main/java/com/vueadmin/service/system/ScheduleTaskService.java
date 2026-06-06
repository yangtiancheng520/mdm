package com.vueadmin.service.system;

import com.alibaba.fastjson2.JSON;
import com.vueadmin.dto.PageResult;
import com.vueadmin.dto.ScheduleTaskDto;
import com.vueadmin.entity.system.ScheduleTask;
import com.vueadmin.repository.ScheduleTaskRepository;
import com.vueadmin.scheduler.DynamicSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleTaskService {

    private final ScheduleTaskRepository scheduleTaskRepository;
    private final DynamicSchedulerService dynamicSchedulerService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 任务类型映射
    private static final java.util.Map<String, String> TASK_TYPE_MAP = new java.util.HashMap<>() {{
        put("quality_check", "质量检测");
        put("distribution", "数据分发");
        put("data_sync", "数据同步");
        put("data_clean", "数据清理");
        put("custom", "自定义任务");
    }};

    /**
     * 搜索定时任务
     */
    public PageResult<ScheduleTaskDto> search(String taskCode, String taskName, String taskType, String status) {
        List<ScheduleTask> tasks = scheduleTaskRepository.searchTasks(taskCode, taskName, taskType, status);
        List<ScheduleTaskDto> list = tasks.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取所有启用的任务
     */
    public List<ScheduleTaskDto> getActiveTasks() {
        return scheduleTaskRepository.findByStatus("active").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务详情
     */
    public ScheduleTaskDto getById(Long id) {
        return scheduleTaskRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 创建定时任务
     */
    @Transactional
    public ScheduleTask create(ScheduleTaskDto dto) {
        if (scheduleTaskRepository.existsByTaskCode(dto.getTaskCode())) {
            throw new RuntimeException("任务编码已存在: " + dto.getTaskCode());
        }

        ScheduleTask task = new ScheduleTask();
        task.setTaskCode(dto.getTaskCode());
        task.setTaskName(dto.getTaskName());
        task.setTaskType(dto.getTaskType());
        task.setTaskParams(dto.getTaskParams());
        task.setCronExpression(dto.getCronExpression());
        task.setTaskClass(dto.getTaskClass());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "paused");
        task.setCreatedBy(dto.getCreatedBy());
        task.setDescription(dto.getDescription());

        ScheduleTask saved = scheduleTaskRepository.save(task);

        // 如果状态为启用，则注册到调度器
        if ("active".equals(saved.getStatus())) {
            try {
                dynamicSchedulerService.scheduleTask(saved);
            } catch (Exception e) {
                log.error("注册定时任务失败", e);
            }
        }

        return saved;
    }

    /**
     * 更新定时任务
     */
    @Transactional
    public void update(Long id, ScheduleTaskDto dto) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task == null) return;

        String oldStatus = task.getStatus();

        task.setTaskName(dto.getTaskName());
        task.setTaskType(dto.getTaskType());
        task.setTaskParams(dto.getTaskParams());
        task.setCronExpression(dto.getCronExpression());
        task.setTaskClass(dto.getTaskClass());
        task.setDescription(dto.getDescription());
        task.setUpdatedBy(dto.getUpdatedBy());

        scheduleTaskRepository.save(task);

        // 如果任务状态为启用，更新调度器
        if ("active".equals(oldStatus)) {
            dynamicSchedulerService.unscheduleTask(task.getTaskCode());
        }
        if ("active".equals(dto.getStatus())) {
            task.setStatus("active");
            scheduleTaskRepository.save(task);
            try {
                dynamicSchedulerService.scheduleTask(task);
            } catch (Exception e) {
                log.error("更新定时任务调度失败", e);
            }
        }
    }

    /**
     * 删除定时任务
     */
    @Transactional
    public void delete(Long id) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task != null) {
            // 先取消调度
            dynamicSchedulerService.unscheduleTask(task.getTaskCode());
            scheduleTaskRepository.deleteById(id);
        }
    }

    /**
     * 启动任务
     */
    @Transactional
    public void start(Long id) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task == null) return;

        task.setStatus("active");
        scheduleTaskRepository.save(task);

        try {
            dynamicSchedulerService.scheduleTask(task);
        } catch (Exception e) {
            log.error("启动定时任务失败", e);
            throw new RuntimeException("启动任务失败: " + e.getMessage());
        }
    }

    /**
     * 暂停任务
     */
    @Transactional
    public void pause(Long id) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task == null) return;

        task.setStatus("paused");
        scheduleTaskRepository.save(task);

        dynamicSchedulerService.unscheduleTask(task.getTaskCode());
    }

    /**
     * 立即执行一次
     */
    public void executeNow(Long id) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task == null) return;

        try {
            dynamicSchedulerService.executeNow(task);
        } catch (Exception e) {
            log.error("立即执行任务失败", e);
            throw new RuntimeException("执行失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务执行信息
     */
    @Transactional
    public void updateExecuteInfo(Long id, LocalDateTime lastExecuteTime) {
        ScheduleTask task = scheduleTaskRepository.findById(id).orElse(null);
        if (task == null) return;

        task.setLastExecuteTime(lastExecuteTime);
        task.setExecuteCount(task.getExecuteCount() + 1);
        scheduleTaskRepository.save(task);
    }

    /**
     * 转换为DTO
     */
    private ScheduleTaskDto toDto(ScheduleTask task) {
        ScheduleTaskDto dto = new ScheduleTaskDto();
        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        dto.setTaskName(task.getTaskName());
        dto.setTaskType(task.getTaskType());
        dto.setTaskParams(task.getTaskParams());
        dto.setCronExpression(task.getCronExpression());
        dto.setTaskClass(task.getTaskClass());
        dto.setStatus(task.getStatus());
        dto.setExecuteCount(task.getExecuteCount());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setUpdatedBy(task.getUpdatedBy());
        dto.setDescription(task.getDescription());

        // 设置类型名称
        dto.setTaskTypeName(TASK_TYPE_MAP.getOrDefault(task.getTaskType(), task.getTaskType()));

        // 格式化时间
        if (task.getLastExecuteTime() != null) {
            dto.setLastExecuteTime(task.getLastExecuteTime().format(formatter));
        }
        if (task.getNextExecuteTime() != null) {
            dto.setNextExecuteTime(task.getNextExecuteTime().format(formatter));
        }
        if (task.getCreatedAt() != null) {
            dto.setCreatedAt(task.getCreatedAt().format(formatter));
        }
        if (task.getUpdatedAt() != null) {
            dto.setUpdatedAt(task.getUpdatedAt().format(formatter));
        }

        // 解析Cron表达式说明
        try {
            dto.setCronDescription(parseCronDescription(task.getCronExpression()));
        } catch (Exception e) {
            dto.setCronDescription("无效的Cron表达式");
        }

        return dto;
    }

    /**
     * 解析Cron表达式说明
     */
    private String parseCronDescription(String cron) {
        if (cron == null || cron.isEmpty()) return "";

        String[] parts = cron.split(" ");
        if (parts.length != 5) return cron;

        String second = parts[0];
        String minute = parts[1];
        String hour = parts[2];
        String day = parts[3];
        String month = parts[4];

        // 简单解析常见模式
        if ("0".equals(minute) && "0".equals(hour) && "*".equals(day) && "*".equals(month)) {
            return "每天 00:00 执行";
        }
        if ("0".equals(minute) && "*".equals(hour) && "*".equals(day) && "*".equals(month)) {
            return "每小时执行一次";
        }
        if ("*/5".equals(minute) && "*".equals(hour) && "*".equals(day) && "*".equals(month)) {
            return "每5分钟执行一次";
        }
        if ("*/30".equals(minute) && "*".equals(hour) && "*".equals(day) && "*".equals(month)) {
            return "每30分钟执行一次";
        }

        return cron;
    }
}
