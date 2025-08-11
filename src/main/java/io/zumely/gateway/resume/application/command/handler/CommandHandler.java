package io.zumely.gateway.resume.application.command.handler;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.data.CommandResult;

import java.security.Principal;

public interface CommandHandler<T extends Command, R extends CommandResult> {

    R handle(Principal actor, T command);

    Class<T> supported();
}
