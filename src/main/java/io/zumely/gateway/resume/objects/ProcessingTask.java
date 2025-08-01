package io.zumely.gateway.resume.objects;

import io.zumely.gateway.resume.tasks.Task;

public record ProcessingTask(String message) implements Task {

}