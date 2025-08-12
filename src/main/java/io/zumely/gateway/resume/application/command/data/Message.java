package io.zumely.gateway.resume.application.command.data;

public record Message<T>(String id, RoleType role, T content) {

}