package io.zumely.gateway.resume.tasks.impl;

import io.zumely.gateway.resume.objects.ProcessingTask;
import io.zumely.gateway.resume.tasks.Task;

public record AsyncTaskResponse<T extends Task>(String taskId, T result) {

    public static AsyncTaskResponse<ProcessingTask> processing(String taskId, String message) {
        return new AsyncTaskResponse<>(taskId, new ProcessingTask(message));
    }
}