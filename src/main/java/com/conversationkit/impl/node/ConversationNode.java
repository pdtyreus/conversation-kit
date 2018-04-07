/*
 * The MIT License
 *
 * Copyright 2016 Synclab Consulting LLC.
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
package com.conversationkit.impl.node;

import com.conversationkit.model.IConversationEdge;
import com.conversationkit.model.IConversationNode;
import com.conversationkit.model.IConversationResponseTransformer;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.IConversationStateTransformer;
import com.conversationkit.model.SnippetType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Convenience base class for creating nodes.
 *
 * @author pdtyreus
 * @param <R>
 */
public abstract class ConversationNode<R, S extends IConversationState> implements IConversationNode<R, S> {

    protected final List<IConversationEdge<R, S>> edges;
    private final SnippetType type;
    private final int id;
    private final IConversationResponseTransformer<R> responseTransformer;
    private final IConversationStateTransformer<R, S> stateTransformer;

    public ConversationNode(int id, SnippetType type) {
        this.id = id;
        this.type = type;
        this.edges = new ArrayList();
        this.responseTransformer = null;
        this.stateTransformer = null;
    }

    public ConversationNode(int id, SnippetType type, IConversationResponseTransformer<R> responseTransformer, IConversationStateTransformer<R, S> stateTransformer) {
        this.id = id;
        this.type = type;
        this.edges = new ArrayList();
        this.responseTransformer = responseTransformer;
        this.stateTransformer = stateTransformer;
    }

    @Override
    public Iterable<IConversationEdge<R, S>> getEdges() {
        return edges;
    }

    @Override
    public void addEdge(IConversationEdge<R, S> edge) {
        edges.add(edge);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public SnippetType getType() {
        return type;
    }

    @Override
    /**
     * Transforms the user's response by delegating to an
     * {@link com.conversationkit.model.IConversationResponseTransformer}
     * instance.
     */
    public Optional<R> transformResponse(Optional<String> response) {
        if (responseTransformer == null) {
            return Optional.empty();
        } else {
            return responseTransformer.transformResponse(response);
        }
    }

    @Override
    public Optional<Map<String, Object>> transformState(Optional<R> response, S currentState) {
        if (stateTransformer == null) {
            return Optional.empty();
        } else {
            return stateTransformer.transformState(response, currentState);
        }

    }

}
