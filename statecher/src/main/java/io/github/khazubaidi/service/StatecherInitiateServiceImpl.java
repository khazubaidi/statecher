package io.github.khazubaidi.service;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.configurations.IdGenerator;
import io.github.khazubaidi.configurations.PermissionValidator;
import io.github.khazubaidi.markers.Statechable;
import io.github.khazubaidi.extendables.StatecherValidator;
import io.github.khazubaidi.models.State;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import io.github.khazubaidi.objects.OneTimeTokeMetadata;
import io.github.khazubaidi.objects.StatecherObject;
import io.github.khazubaidi.utils.BeanUtils;

import javax.persistence.EntityManagerFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class StatecherInitiateServiceImpl<T> implements StatecherInitiateService<T> {

    private final IdGenerator idGenerator;
    private final StatecherRegistry statecherRegistry;
    private final PermissionValidator permissionValidator;
    private final BeanUtils beanUtils;
    private final EntityManagerFactory entityManagerFactory;
    private final OneTimeTokenService oneTimeTokenService;

    @Override
    public StatecherObject initiate(String name, Object id, String initiator){

        boolean doseStatecherExists = statecherRegistry.exists(name);
        if(!doseStatecherExists)
            throw new RuntimeException();

        Statecher stateacher = statecherRegistry.get(name);
        Statechable entity = getEntity(id, stateacher.getEntity());
        boolean hasState = hasState(stateacher, entity.getState());

        if(!hasState){

            return new StatecherObject(
                    idGenerator.generate(),
                    Collections.emptyList()) ;
        }

        State state = findState(stateacher, entity.getState());

        boolean doseLoginUserHasPermission = permissionValidator.hasAny(state.getPermissions());
        if(!doseLoginUserHasPermission)
            return new StatecherObject(
                    idGenerator.generate(),
                    Collections.emptyList()) ;

        boolean canAccess = runValidators(state, entity, initiator);
        if(!canAccess)
            throw new RuntimeException("You cannot operate on this");

        List<Transition> transitions = getTransitions(stateacher.getTransitions(), state.getTransitions());

        OneTimeTokeMetadata metadata = new OneTimeTokeMetadata(name, id);
        String token = oneTimeTokenService.<OneTimeTokeMetadata>create(initiator, metadata, Duration.ofMinutes(10));
        return new StatecherObject(
                token,
                transitions);
    }

    public boolean runValidators(State state, Statechable statechable, String initiator){

        return state.getValidators()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherValidator.class))
                .allMatch(validator -> validator.isValid(statechable, state, initiator));
    }

    public boolean hasState(Statecher stateacher, String currentState){

        if(StringUtils.isBlank(currentState))
            throw new RuntimeException("Current state is null");

        return stateacher.getStates().get(currentState) != null;
    }

    public State findState(Statecher stateacher, String currentState){

        if(StringUtils.isBlank(currentState))
            throw new RuntimeException("Current state is null");

        return stateacher.getStates().get(currentState);
    }

    public List<Transition> getTransitions(Map<String, Transition> availableTransitions, List<String> allowedTransition){

        if(allowedTransition.isEmpty())
            return Collections.emptyList();

        List<Transition> transitions = new ArrayList<>();
        availableTransitions.forEach((key, transition) -> {

            if(allowedTransition.contains(key))
                transitions.add(transition);
        });

        return transitions;
    }

    @Transactional
    public Statechable getEntity(Object id, String entityClass) {

        try {

            Class<?> klass = Class.forName(entityClass);
            Object entity = entityManagerFactory.createEntityManager().find(klass, id);

            if (!(entity instanceof Statechable))
                throw new IllegalStateException("Entity does not implement Stateched");

            return (Statechable) entity;
        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }
    }
}
