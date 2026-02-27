package io.github.khazubaidi.service;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.commands.StatecherProcessCommand;
import io.github.khazubaidi.extendables.*;
import io.github.khazubaidi.resolvers.PermissionValidatorResolver;
import io.github.khazubaidi.exceptions.StatecherStateNotFoundException;
import io.github.khazubaidi.markers.Statechable;
import io.github.khazubaidi.models.State;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import io.github.khazubaidi.objects.OneTimeTokeMetadata;
import io.github.khazubaidi.utils.BeanUtils;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import io.github.khazubaidi.utils.DataUtils;
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
import java.util.regex.Pattern;

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
            runBefore(currentState, entity);

            try {

                runOnExit(oldState, currentState, nextState, entity);
                setState(metadata.getId(), stateacher.getEntity(), newState.getValue());
                runFormProcessor(newState.getForm(), entity, command.getData());
                runOnEnter(newState, nextState, currentState, entity);
            } catch (Exception e) {

                status.setRollbackOnly();
                throw e;
            }

            runAfter(currentState, entity);
            return null;
        });
    }

    public void runFormProcessor(Transition.Form form, Statechable statechable, Map<String, Object> data){

        if(Objects.isNull(form))
            return;

        if(Objects.isNull(statechable))
            return;

        if(CollectionUtils.isEmpty(data))
            return;

        var formProcessor = beanUtils.findByName(form.getProcessor(), FormProcessor.class);
        formProcessor.process(statechable, data);
    }

    public boolean runValidators(State state, Statechable statechable){

        return state.getValidators()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherValidator.class))
                .allMatch(validator -> validator.isValid(statechable, state));
    }

    public void runBefore(State state, Statechable statechable){

        state.getBefore()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherBeforeTransition.class))
                .forEach(validator -> {
                    validator.execute(statechable, state);
                });
    }

    public void runOnExit(Transition transition, State currentState, State nextState, Statechable statechable){

        transition.getOnExist()
                .stream()
                .map(t -> beanUtils.findByName(t, OnExistTransition.class))
                .forEach(t -> {
                    t.onExist(statechable, currentState, nextState);
                });
    }

    public void runOnEnter(Transition transition, State currentState, State previousState, Statechable statechable){

        transition.getOnEnter()
                .stream().map(t -> beanUtils.findByName(t, OnEnterTransition.class))
                .forEach(t -> {
                    t.onEnter(statechable, currentState, previousState);
                });
    }

    public void runAfter(State state, Statechable statechable){

        state.getAfter()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherAfterTransition.class))
                .forEach(validator -> {
                    validator.execute(statechable, state);
                });
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
