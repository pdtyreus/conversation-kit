/*
 * The MIT License
 *
 * Copyright 2019 Synclab Consulting LLC.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.conversationkit.redux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public final class Store<S> implements Dispatcher {

    private static final Logger logger = Logger.getLogger(Store.class.getName());

    private Map currentState;

    private final Reducer reducer;
    private ActionDispatcher dispatcher;
    private final Function<Map,S> typedStateBuilder;
    private final Map<UUID, Consumer<Map<String, Object>>> consumers = new HashMap<>();

    @FunctionalInterface
    public interface ActionDispatcher {

        public void dispatch(Object action);

    }

    protected Store(Reducer reducer, Map initialState, Function<Map,S> stateBuilder, Middleware... middlewares) {
        this.reducer = reducer;
        this.currentState = initialState;
        this.typedStateBuilder = stateBuilder;

        List<Middleware> allMiddlewares = new ArrayList();
        //native middleware, last middleware in chain

        for (Middleware mw : middlewares) {
            allMiddlewares.add(mw);
        }
        allMiddlewares.add((store, action, next) -> {
            Map nextState;
            synchronized (Store.this) {
                logger.fine(String.format("[REDUX] reducing action: %s", action.toString()));
                if (!(action instanceof Action)) {
                    throw new RuntimeException("The action must be an instance of Action by the time it is received by the reducer. Action is " + action.getClass().getName());
                }
                Action a = (Action) action;
                nextState = store.reducer.reduce(a, currentState);
            }
            if (!nextState.equals(currentState)) {
                logger.fine(String.format("[REDUX] state has changed after %s", action.toString()));
                currentState = nextState;
                consumers.values().parallelStream().forEach(e -> e.accept(currentState));
            } else {
                logger.fine(String.format("[REDUX] state has not changed after %s", action.toString()));
            }
        });

        logger.info(String.format("initializing redux store with %d middleware(s).", (allMiddlewares.size() - 1)));

        for (int i = allMiddlewares.size() - 1; i >= 0; i--) {
            final Middleware mw = allMiddlewares.get(i);
            logger.fine(String.format("chaining middleware (%d)", i));
            //this will be null for the native middleware only, which is last
            final Middleware next = (i == allMiddlewares.size() - 1 ? null : allMiddlewares.get(i + 1));
            this.dispatcher = (action) -> {
                mw.dispatch(Store.this, action, next);
            };

        }
    }

    @Override
    public S dispatch(Object action) {
        logger.fine(String.format("[REDUX] dispatching action: %s", action.toString()));

        dispatcher.dispatch(action);
        logger.finer(String.format("[REDUX] reduced state: %s", getState().toString()));
        return getState();
    }

    public S getState() {
        return typedStateBuilder.apply(currentState);
    }

    public UUID subscribe(Consumer<Map<String, Object>> subscriber) {
        UUID uuid = UUID.randomUUID();
        this.consumers.put(uuid, subscriber);

        return uuid;
    }

    public void unsubscribe(UUID uuid) {

        this.consumers.remove(uuid);
    }
}
