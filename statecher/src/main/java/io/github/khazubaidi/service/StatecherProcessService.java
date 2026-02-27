package io.github.khazubaidi.service;

import io.github.khazubaidi.commands.StatecherProcessCommand;

public interface StatecherProcessService<T> {

    void process(StatecherProcessCommand command);
}
