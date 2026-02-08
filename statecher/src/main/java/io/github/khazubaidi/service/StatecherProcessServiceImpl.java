package io.github.khazubaidi.service;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.configurations.PermissionValidator;
import io.github.khazubaidi.exceptions.StatecherException;
import io.github.khazubaidi.exceptions.StatecherStateNotFoundException;
import io.github.khazubaidi.markers.Statechable;
import io.github.khazubaidi.extendables.StatecherAfterTransition;
import io.github.khazubaidi.extendables.StatecherBeforeTransition;
import io.github.khazubaidi.extendables.StatecherValidator;
import io.github.khazubaidi.models.State;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import io.github.khazubaidi.utils.BeanUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatecherProcessServiceImpl<T> implements StatecherProcessService<T> {

    private final StatecherRegistry statecherRegistry;
    private final PermissionValidator permissionValidator;
    private final BeanUtils beanUtils;
    private final EntityManagerFactory entityManagerFactory;

    @Override
    public void process(String name, Object id, String transitionId){

        //TODO find object from redis
        String initiator = "from-redis";
        boolean doseStatecherExists = statecherRegistry.exists(name);
        if(!doseStatecherExists)
            throw new RuntimeException();

        Statecher stateacher = statecherRegistry.get(name);
        Statechable entity = getEntity(id, stateacher.getEntity());
        boolean hasState = hasState(stateacher, entity.getState());

        if(!hasState)
            throw new StatecherStateNotFoundException("State (" + entity.getState() + ") not found within statecher (" + name + ").");

        State state = findState(stateacher, entity.getState());

        boolean doseLoginUserHasPermission = permissionValidator.hasAny(state.getPermissions());
        if(!doseLoginUserHasPermission)
            return;

        boolean canAccess = runValidators(state, entity, initiator);
        if(!canAccess)
            throw new RuntimeException("You cannot operate on this");

        List<Transition> transitions = getTransitions(stateacher.getTransitions(), state.getTransitions());
        Transition transition = transitions.stream().filter(t -> t.getId().equals(transitionId))
                .findFirst()
                .orElseThrow();

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        runBefore(state, entity, initiator);
        setState(id, stateacher.getEntity(), transition.getValue());
        runAfter(state, entity, initiator);

        transaction.commit();
        entityManager.close();
    }

    public boolean runValidators(State state, Statechable statechable, String initiator){

        return state.getValidators()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherValidator.class))
                .allMatch(validator -> validator.isValid(statechable, state, initiator));
    }

    public void runBefore(State state, Statechable statechable, String initiator){

        state.getBefore()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherBeforeTransition.class))
                .forEach(validator -> {
                    validator.execute(statechable, state, initiator);
                });
    }

    public void runAfter(State state, Statechable statechable, String initiator){

        state.getAfter()
                .stream().map(validator -> beanUtils.findByName(validator, StatecherAfterTransition.class))
                .forEach(validator -> {
                    validator.execute(statechable, state, initiator);
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
            Object entity = entityManagerFactory.createEntityManager().find(klass, id);

            if (!(entity instanceof Statechable))
                throw new IllegalStateException("Entity does not implement Stateched");

            return (Statechable) entity;
        } catch (ClassNotFoundException e) {

            throw new RuntimeException(e);
        }
    }

    public void setState(Object id, String entityClass, Object value) {

        Object entity = getEntity(id, entityClass);
        ((Statechable) entity).setState(value);
    }
}
