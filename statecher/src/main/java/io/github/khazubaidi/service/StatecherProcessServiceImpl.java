package io.github.khazubaidi.service;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.commands.StatecherProcessCommand;
import io.github.khazubaidi.contracts.*;
import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.resolvers.PermissionValidatorResolver;
import io.github.khazubaidi.exceptions.StatecherStateNotFoundException;
import io.github.khazubaidi.Statechable;
import io.github.khazubaidi.models.State;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import io.github.khazubaidi.objects.OneTimeTokeMetadata;
import io.github.khazubaidi.utils.BeanUtils;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import io.github.khazubaidi.utils.TypesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatecherProcessServiceImpl<T> implements StatecherProcessService<T> {

    private final StatecherRegistry statecherRegistry;
    private final PermissionValidatorResolver permissionValidator;
    private final BeanUtils beanUtils;
    private final OneTimeTokenService oneTimeTokenService;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void process(StatecherProcessCommand command){

        //todo validation

        OneTimeTokeMetadata metadata = oneTimeTokenService.consume(command.getInitiator(), command.getToken());
        boolean doseStatecherExists = statecherRegistry.exists(metadata.getName());
        if(!doseStatecherExists)
            throw new RuntimeException();

        Statecher stateacher = statecherRegistry.get(metadata.getName());
        Statechable entity = getEntity(metadata.getId(), stateacher.getEntity());
        boolean hasState = hasState(stateacher, entity.getState());

        if(!hasState)
            throw new StatecherStateNotFoundException("State (" + entity.getState() + ") not found within statecher (" + metadata.getName() + ").");

        State currentState = findState(stateacher, entity.getState());

        boolean doseLoginUserHasPermission = permissionValidator.hasAny(currentState.getPermissions());
        if(!doseLoginUserHasPermission)
            return;

        boolean canAccess = runValidators(currentState, entity);
        if(!canAccess)
            throw new RuntimeException("You cannot operate on this");

        List<Transition> transitions = getTransitions(stateacher.getTransitions(), currentState.getTransitions());
        Transition oldState = stateacher.getTransitions().get(entity.getState());
        Transition newState = transitions.stream()
                .filter(t -> t.getId().equals(command.getTransitionId()))
                .findFirst()
                .orElseThrow();

        State nextState = findState(stateacher, newState.getValue());
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(status -> {

            var rollbackables = new ArrayList<Rollbackable>();
            try {

                runOnStart(oldState, currentState, nextState, entity, rollbackables);
                runOnExit(currentState, entity, rollbackables);
                runOnEnd(newState, nextState, currentState, entity, rollbackables);
                runOnEnter(nextState, entity, rollbackables);
                runFormProcessor(newState.getForm(), entity, command.getData());
                setState(metadata.getId(), stateacher.getEntity(), newState.getValue());
            } catch (Exception e) {

                runRollbacks(rollbackables, currentState, nextState, entity);
                status.setRollbackOnly();
                throw e;
            }

            return null;
        });
    }

    public void runFormProcessor(String form, Statechable statechable, Map<String, Object> data){

        if(StringUtils.isBlank(form))
            return;

        if(Objects.isNull(statechable))
            return;

        if(CollectionUtils.isEmpty(data))
            return;

        var formProcessor = beanUtils.findByName(form, FormProcessor.class);
        formProcessor.process(statechable, data);
    }

    public boolean runValidators(State state, Statechable statechable){

        return state.getValidators()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherValidator.class))
                .allMatch(validator -> validator.isValid(statechable, state));
    }

    public void runRollbacks(List<Rollbackable> rollbackables, State currentState, State previousState, Statechable statechable){

        if(rollbackables == null || rollbackables.isEmpty())
            return;

        rollbackables
                .forEach(t -> {
                    try {
                        t.onRollback(statechable, currentState, previousState);
                    } catch (Exception e) {

                        log.error(e.getMessage(), e);
                    }
                });
    }

    public void runOnExit(
            State state,
            Statechable statechable,
            List<Rollbackable> rollbackables){

        if(state.getOnExit() == null || state.getOnExit().isEmpty())
            return;

        var onExists = state.getOnExit()
                .stream()
                .map(t -> beanUtils.findByName(t, StateExit.class))
                .collect(Collectors.toList());

        for (StateExit onExit : onExists) {

            onExit.onExit(statechable, state);

            if(onExit instanceof Rollbackable)
                rollbackables.add((Rollbackable)onExit);
        }
    }


    public void runOnEnter(
            State state,
            Statechable statechable,
            List<Rollbackable> rollbackables){

        if(state.getOnExit() == null || state.getOnExit().isEmpty())
            return;

        var onEnters = state.getOnExit()
                .stream()
                .map(t -> beanUtils.findByName(t, StateEnter.class))
                .collect(Collectors.toList());

        for (StateEnter onEnter : onEnters) {

            onEnter.onEnter(statechable, state);

            if(onEnter instanceof Rollbackable)
                rollbackables.add((Rollbackable)onEnter);
        }
    }

    public void runOnStart(
            Transition transition,
            State currentState,
            State previousState,
            Statechable statechable,
            List<Rollbackable> rollbackables){

        if(transition.getOnStart() == null || transition.getOnStart().isEmpty())
            return;

        var onStarts =  transition.getOnStart()
                .stream().map(t -> beanUtils.findByName(t, TransitionStart.class))
                .collect(Collectors.toList());

        for (TransitionStart onStart : onStarts) {

            onStart.onStart(statechable, currentState, previousState);

            if(onStart instanceof Rollbackable)
                rollbackables.add((Rollbackable)onStart);
        }
    }

    public void runOnEnd(
            Transition transition,
            State currentState,
            State previousState,
            Statechable statechable,
            List<Rollbackable> rollbackables){

        if(transition.getOnEnd() == null || transition.getOnEnd().isEmpty())
            return;

        var onEnds = transition.getOnEnd()
                .stream().map(t -> beanUtils.findByName(t, TransitionEnd.class))
                .collect(Collectors.toList());

        for (TransitionEnd transitionEnd : onEnds) {

            transitionEnd.onEnd(statechable, currentState, previousState);

            if(transitionEnd instanceof Rollbackable)
                rollbackables.add((Rollbackable)transitionEnd);
        }
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

            if(allowedTransition.contains(key)){

                transition.setValue(key);
                transitions.add(transition);
            }

        });

        return transitions;
    }

    @Transactional
    public Statechable getEntity(Object id, String entityClass) {

        try {

            Class<?> klass = Class.forName(entityClass);
            Class<?> idType = getIdType(klass);
            Object entity = entityManager.find(klass, TypesUtils.convertId(id, idType));

            if (!(entity instanceof Statechable))
                throw new IllegalStateException("Entity does not implement Stateched");

            return (Statechable) entity;
        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }
    }

    public Class<?> getIdType(Class<?> entityClass) {

        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<?> entityType = metamodel.entity(entityClass);

        return entityType.getIdType().getJavaType();
    }

    public void setState(Object id, String entityClass, Object value) {

        try {

            Class<?> klass = Class.forName(entityClass);
            Class<?> idType = getIdType(klass);
            Object entity = entityManager.find(klass, TypesUtils.convertId(id, idType));

            if (!(entity instanceof Statechable))
                throw new IllegalStateException("Entity does not implement Stateched");

            ((Statechable) entity).setState(value);
        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }
    }
}
