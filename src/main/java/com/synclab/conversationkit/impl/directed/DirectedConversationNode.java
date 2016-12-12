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
package com.synclab.conversationkit.impl.directed;

import com.synclab.conversationkit.impl.BasicConversationSnippetRenderer;
import com.synclab.conversationkit.impl.ConstantValueResponseEvaluator;
import com.synclab.conversationkit.impl.ExactMatchResponseMatcher;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationSnippetRenderer;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.IResponseEvaluator;
import com.synclab.conversationkit.model.IResponseMatcher;
import com.synclab.conversationkit.model.SnippetType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pdtyreus
 */
public class DirectedConversationNode <T extends IConversationState> implements IConversationNode<DirectedConversationNode, T> {

    private final List<DirectedConversationNode> leafNodes;
    private final String matcherString;
    private final String evaluatorString;
    private final String displayedValue;
    private final SnippetType type;
    private final int id;
    private String stateKey;
    private IConversationSnippetRenderer<T> renderer = new BasicConversationSnippetRenderer();
    private IResponseEvaluator responseEvaluator = new ConstantValueResponseEvaluator();
    private IResponseMatcher responseMatcher = new ExactMatchResponseMatcher();
    private final List<String> suggestedResponses = new ArrayList();

    public DirectedConversationNode(int id, SnippetType type, String displayedValue, String matcherString, String evaluatorString) {
        this.id = id;
        this.type = type;
        this.leafNodes = new ArrayList();
        this.matcherString = matcherString;
        this.evaluatorString = evaluatorString;
        this.displayedValue = displayedValue;
    }

    public List<DirectedConversationNode> getLeafNodes() {
        return leafNodes;
    }

    public void addLeafNode(DirectedConversationNode node) {
        leafNodes.add(node);
    }

    public String renderContent(T state) {
        return renderer.renderContentUsingState(displayedValue, state);
    }

    public boolean isMatchForResponse(String response) {
        return responseMatcher.isMatch(matcherString, response);
    }
    
    public void setResponseMatcher(IResponseMatcher responseMatcher) {
        this.responseMatcher = responseMatcher;
    }
    
    public Object evaluateMatchForResponse(String response) {
        return responseEvaluator.evaluateMatch(evaluatorString, response);
    }

    public void setRenderer(IConversationSnippetRenderer<T> renderer) {
        this.renderer = renderer;
    }

    public void setResponseEvaluator(IResponseEvaluator responseEvaluator) {
        this.responseEvaluator = responseEvaluator;
    }

    public int getId() {
        return id;
    }

    public SnippetType getType() {
        return type;
    }

    public List<String> getSuggestedResponses() {
        return suggestedResponses;
    }

    public void addSuggestedResponse(String suggestedResponse) {
        this.suggestedResponses.add(suggestedResponse);
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }
    
}
