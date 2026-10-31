/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StateMachine<T extends Enum<T>, Context> {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private T currentState;
    private List<Transition<T, Context>> allTransitions;
    private Set<T> allStates = new HashSet<T>();
    private Map<T, Action<T, Context>> onSetStateActions;
    private Action<T, Context> onChangeStateAction;
    private StateHistory<T> stateHistory;
    private Map<T, List<Transition<T, Context>>> transitionsByFromState;
    private Map<T, List<Transition<T, Context>>> transitionsByToState;

    private StateMachine(T initialState, List<Transition<T, Context>> allowedTransitions, Map<T, Action<T, Context>> onSetStateActions, Action<T, Context> onChangeStateAction) {
        this.currentState = initialState;
        this.allTransitions = Collections.unmodifiableList(allowedTransitions);
        this.onChangeStateAction = onChangeStateAction;
        this.transitionsByFromState = new HashMap<T, List<Transition<T, Context>>>();
        this.transitionsByToState = new HashMap<T, List<Transition<T, Context>>>();
        for (Transition<T, Context> transition : allowedTransitions) {
            this.allStates.add(transition.fromState);
            this.allStates.add(transition.toState);
            this.transitionsByFromState.computeIfAbsent(transition.fromState, k -> new ArrayList()).add(transition);
            this.transitionsByToState.computeIfAbsent(transition.toState, k -> new ArrayList()).add(transition);
        }
        this.onSetStateActions = Collections.unmodifiableMap(onSetStateActions);
        this.stateHistory = new StateHistory(this.allStates.size() + 1);
        this.stateHistory.add(initialState);
    }

    public T getCurrentState() {
        return this.currentState;
    }

    public void resetToState(T newState) {
        if (newState != this.currentState) {
            this.currentState = newState;
        }
    }

    public T setState(Context context, T newState) {
        boolean result = false;
        List<Transition<T, Context>> possibleTransitions = this.transitionsByToState.get(newState);
        if (possibleTransitions == null) {
            throw new IllegalArgumentException("Unknown state: " + String.valueOf(newState));
        }
        for (Transition<T, Context> transition : possibleTransitions) {
            if (!Objects.equals(this.currentState, transition.fromState) || !Objects.equals(newState, transition.toState) || !transition.p.test(context)) continue;
            this.setState(context, transition);
            result = true;
            break;
        }
        if (result) {
            this.update(context);
        }
        return result ? (T)this.currentState : null;
    }

    public T setStateToAnyOf(Context context, Collection<T> toStates) {
        Enum toState;
        Enum result = null;
        Iterator<T> iterator = toStates.iterator();
        while (iterator.hasNext() && (result = this.setState(context, toState = (Enum)iterator.next())) == null) {
        }
        return (T)result;
    }

    public T update(Context context) {
        Transition<T, Context> transition;
        int maxAllowedTransitions = this.allStates.size();
        for (int i = 0; i < maxAllowedTransitions && (transition = this.next(context, TransitionMode.AUTO)) != null; ++i) {
            this.setState(context, transition);
        }
        return this.currentState;
    }

    private void setState(Context context, Transition<T, Context> transition) {
        Action onSetStateAction;
        if (transition.preAction != null) {
            transition.preAction.execute(context, transition.fromState, transition.toState);
        }
        this.currentState = transition.toState;
        this.stateHistory.add(this.currentState);
        if (transition.postAction != null) {
            transition.postAction.execute(context, transition.fromState, transition.toState);
        }
        if ((onSetStateAction = this.onSetStateActions.get(this.currentState)) != null) {
            onSetStateAction.execute(context, transition.fromState, transition.toState);
        }
        if (this.onChangeStateAction != null) {
            this.onChangeStateAction.execute(context, transition.fromState, transition.toState);
        }
        LOGGER.debug("Transitioned: {} -> {}", transition.fromState, transition.toState);
    }

    private Transition<T, Context> next(Context context, TransitionMode transitionMode) {
        List<Transition<T, Context>> possibleTransitions = this.transitionsByFromState.get(this.currentState);
        for (Transition<T, Context> transition : possibleTransitions) {
            if (!Objects.equals(this.currentState, transition.fromState) || !transition.p.test(context) || transition.mode != transitionMode) continue;
            return transition;
        }
        return null;
    }

    public List<Transition<T, Context>> getAllTransitions() {
        return this.allTransitions;
    }

    public Collection<T> getStateHistory() {
        return Collections.unmodifiableCollection(this.stateHistory.deque);
    }

    public static interface Action<T extends Enum<T>, Context> {
        public void execute(Context var1, T var2, T var3);
    }

    private static class Transition<T extends Enum<T>, Context> {
        T fromState;
        T toState;
        Predicate<Context> p;
        Action<T, Context> preAction;
        Action<T, Context> postAction;
        TransitionMode mode;

        public Transition(T fromState, T toState, Predicate<Context> p, Action<T, Context> preAction, Action<T, Context> postAction, TransitionMode transitionMode) {
            this.fromState = fromState;
            this.toState = toState;
            this.p = p;
            this.preAction = preAction;
            this.postAction = postAction;
            this.mode = transitionMode;
        }

        public String toString() {
            return String.format("Tr[%s->%s, %s, pre: %s, post: %s]", new Object[]{this.fromState, this.toState, this.mode, this.preAction, this.postAction});
        }
    }

    public static class StateHistory<E> {
        private final ArrayDeque<E> deque;
        private final int maxSize;

        public StateHistory(int size) {
            this.deque = new ArrayDeque(size);
            this.maxSize = size;
        }

        public void add(E element) {
            if (this.deque.size() == this.maxSize) {
                this.deque.poll();
            }
            this.deque.offer(element);
        }

        public E remove() {
            return this.deque.poll();
        }

        public int size() {
            return this.deque.size();
        }

        public boolean contains(E element) {
            return this.deque.contains(element);
        }
    }

    public static enum TransitionMode {
        AUTO,
        EVENT_OR_AUTO,
        EVENT;

    }

    public static class Builder<T extends Enum<T>, Context> {
        private List<Transition<T, Context>> allowedTransitions = new ArrayList<Transition<T, Context>>();
        private final Set<T> allStates = new HashSet<T>();
        private Map<T, Action<T, Context>> onSetStateActions = new HashMap<T, Action<T, Context>>();
        private Action<T, Context> onChangeStateAction;

        public Builder<T, Context> withOnSetStateAction(T toState, Action<T, Context> action) {
            this.onSetStateActions.put(toState, action);
            return this;
        }

        public Builder<T, Context> withOnChangeStateAction(Action<T, Context> onChangeStateAction) {
            this.onChangeStateAction = onChangeStateAction;
            return this;
        }

        public Builder<T, Context> withTransition(List<T> fromStates, T toState, Predicate<Context> predicate, TransitionMode transitionMode, Action<T, Context> preAction, Action<T, Context> postAction) {
            if (predicate == null) {
                predicate = context -> true;
            }
            for (Enum fromState : fromStates) {
                this.withTransition(fromState, toState, predicate, transitionMode, preAction, postAction);
            }
            return this;
        }

        public Builder<T, Context> withTransition(T fromState, T toState, Predicate<Context> predicate, TransitionMode transitionMode, Action<T, Context> preAction, Action<T, Context> postAction) {
            if (predicate == null) {
                predicate = context -> true;
            }
            this.allowedTransitions.add(new Transition<T, Context>(fromState, toState, predicate, preAction, postAction, transitionMode));
            this.allStates.add(fromState);
            this.allStates.add(toState);
            return this;
        }

        public Builder<T, Context> withTransition(T fromState, T toState, Predicate<Context> predicate, TransitionMode transitionMode) {
            return this.withTransition(fromState, toState, predicate, transitionMode, null, null);
        }

        public Builder<T, Context> withTransition(T fromState, T toState) {
            return this.withTransition(fromState, toState, null, TransitionMode.EVENT, null, null);
        }

        public Builder<T, Context> withTransition(T fromState, T toState, Predicate<Context> predicate) {
            return this.withTransition(fromState, toState, predicate, TransitionMode.AUTO, null, null);
        }

        private void validate() {
            HashSet statesWithOutgoingTransitions = new HashSet();
            for (Transition<T, Context> transition : this.allowedTransitions) {
                statesWithOutgoingTransitions.add(transition.fromState);
            }
            for (Enum state : this.allStates) {
                if (statesWithOutgoingTransitions.contains(state)) continue;
                throw new IllegalArgumentException("No outgoing transitions found for state " + String.valueOf(state));
            }
            for (Enum state : this.onSetStateActions.keySet()) {
                if (this.allStates.contains(state)) continue;
                throw new IllegalArgumentException("No transitions defined for state used in action: " + String.valueOf(state));
            }
        }

        public StateMachine<T, Context> build(T initialState) {
            this.validate();
            return new StateMachine<T, Context>(initialState, this.allowedTransitions, this.onSetStateActions, this.onChangeStateAction);
        }
    }
}

