package io.github.khazubaidi.service;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.resolvers.PermissionValidatorResolver;
import io.github.khazubaidi.exceptions.StatecherStateNotFoundException;
import io.github.khazubaidi.markers.Statechable;
import io.github.khazubaidi.extendables.StatecherAfterTransition;
import io.github.khazubaidi.extendables.StatecherBeforeTransition;
import io.github.khazubaidi.extendables.StatecherValidator;
import io.github.khazubaidi.models.State;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import io.github.khazubaidi.objects.OneTimeTokeMetadata;
import io.github.khazubaidi.utils.BeanUtils;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import io.github.khazubaidi.utils.TypesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

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
    public void process(String token, String initiator, String transitionId){

        OneTimeTokeMetadata metadata = oneTimeTokenService.consume(initiator, token);
        boolean doseStatecherExists = statecherRegistry.exists(metadata.getName());
        if(!doseStatecherExists)
            throw new RuntimeException();

        Statecher stateacher = statecherRegistry.get(metadata.getName());
        Statechable entity = getEntity(metadata.getId(), stateacher.getEntity());
        boolean hasState = hasState(stateacher, entity.getState());

        if(!hasState)
            throw new StatecherStateNotFoundException("State (" + entity.getState() + ") not found within statecher (" + metadata.getName() + ").");

        State state = findState(stateacher, entity.getState());

        boolean doseLoginUserHasPermission = permissionValidator.hasAny(state.getPermissions());
        if(!doseLoginUserHasPermission)
            return;

        boolean canAccess = runValidators(state, entity);
        if(!canAccess)
            throw new RuntimeException("You cannot operate on this");

        List<Transition> transitions = getTransitions(stateacher.getTransitions(), state.getTransitions());
        Transition transition = transitions.stream().filter(t -> t.getId().equals(transitionId))
                .findFirst()
                .orElseThrow();

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(status -> {

            try {

                runBefore(state, entity);
                setState(metadata.getId(), stateacher.getEntity(), transition.getValue());
                runAfter(state, entity);
            } catch (Exception e) {

                status.setRollbackOnly();
                throw e;
            }

            return null;
        });
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

            if(allowedTransition.contains(key))
                transitions.add(transition);
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
