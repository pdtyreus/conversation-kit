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
package com.synclab.conversationkit.impl;

import com.synclab.conversationkit.model.SnippetType;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationSnippetRenderer;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.IResponseEvaluator;
import com.synclab.conversationkit.model.IUnmatchedResponseHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tyreus
 */
public class DialogTreeNode<T extends IConversationState> implements IConversationNode<DialogTreeNode, T> {

    private final List<DialogTreeNode> leafNodes;
    private final String content;
    private final SnippetType type;
    private final int id;
    private IConversationSnippetRenderer<T> renderer = new BasicConversationSnippetRenderer();
    private IResponseEvaluator responseEvaluator = new ExactMatchResponseEvaluator();
    private IUnmatchedResponseHandler unmatchedResponseHandler = new BasicUnmatchedResponseHandler();
    private List<String> suggestedResponses = new ArrayList();

    public DialogTreeNode(SnippetType type, int id, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.leafNodes = new ArrayList();
    }

    public List<DialogTreeNode> getLeafNodes() {
        return leafNodes;
    }

    public void addLeafNode(DialogTreeNode node) {
        leafNodes.add(node);
    }

    public String renderContent(T state) {
        return renderer.renderContentUsingState(content, state);
    }

    public boolean isMatchForResponse(String response) {
        return responseEvaluator.isMatch(content, response);
    }

    /**
     * @param renderer the renderer to set
     */
    public void setRenderer(IConversationSnippetRenderer<T> renderer) {
        this.renderer = renderer;
    }

    /**
     * @param responseEvaluator the responseEvaluator to set
     */
    public void setResponseEvaluator(IResponseEvaluator responseEvaluator) {
        this.responseEvaluator = responseEvaluator;
    }

    public IUnmatchedResponseHandler getUnmatchedResponseHandler() {
        return unmatchedResponseHandler;
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
    
    
}
