package io.zumely.gateway.resume.application.command.handler;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.result.Result;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends Result> {

    R handle(Principal principal, T command);

    Class<T> supported();
}
