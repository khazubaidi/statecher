package io.github.khazubaidi.commands;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@AllArgsConstructor
public class StatecherProcessCommand {

    String token;
    String initiator;
    String transitionId;
    Map<String, Object> data;
}
