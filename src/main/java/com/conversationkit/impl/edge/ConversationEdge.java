/*
 * The MIT License
 *
 * Copyright 2017 Synclab Consulting LLC.
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
package com.conversationkit.impl.edge;

import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationIntent;
import com.conversationkit.model.IConversationState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Convenience base class for creating edges.
 *
 * @author pdtyreus
 * @param <I> type of IConversationIntent
 * @param <S> type of IConversationState
 */
public class ConversationEdge<I extends IConversationIntent, S extends IConversationState> implements IConversationEdge<I,S> {

    private final Integer endNodeId;
    private final String id;
    private final BiFunction<I, S, Boolean> validateFunction;
    private final List<BiFunction<I, S, Object>> sideEffects;

    public ConversationEdge(Integer endNodeId, String intentId) {
        this.endNodeId = endNodeId;
        this.id = intentId;
        this.validateFunction = (intent, state) -> {
            return true;
        };
        this.sideEffects = new ArrayList();
    }

    public ConversationEdge(Integer endNodeId, String intentId, BiFunction<I, S, Boolean> validateFunction, BiFunction<I, S, Object>... sideEffects) {
        this.endNodeId = endNodeId;
        this.id = intentId;
        this.validateFunction = validateFunction;
        this.sideEffects = new ArrayList();
        for (BiFunction<I, S, Object> effect : sideEffects) {
            this.sideEffects.add(effect);
        }
    }
    
    public ConversationEdge(Integer endNodeId, String intentId, BiFunction<I, S, Object> sideEffect) {
        this.endNodeId = endNodeId;
        this.id = intentId;
        this.validateFunction = (intent, state) -> {
            return true;
        };
        this.sideEffects = Arrays.asList(sideEffect);
    }

    @Override
    public Integer getEndNodeId() {
        return endNodeId;
    }

    @Override
    public String getIntentId() {
        return id;
    }

    @Override
    public String toString() {
        return "ConversationEdge {" + getIntentId() + '}';
    }

    @Override
    public boolean validate(I intent, S state) {
        return validateFunction.apply(intent, state);
    }

    @Override
    public List<Object> getSideEffects(I intent, S state) {
        List<Object> effects = new ArrayList();
        for (BiFunction<I, S, Object> effectFunction : sideEffects) {
            effects.add(effectFunction.apply(intent, state));
        }
        return effects;
    }
}
