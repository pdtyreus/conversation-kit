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

import com.synclab.conversationkit.model.IConversation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author tyreus
 */
public class RegexDialogTree<T extends DialogTreeNode, V extends DialogTreeState> implements IConversation<T, V> {

    private static Logger logger = Logger.getLogger(RegexDialogTree.class.getName());

    protected final Map<Integer, T> nodeIndex;

    public RegexDialogTree(T rootNode) {
        this.nodeIndex = new HashMap();
        addToIndex(rootNode);
    }

    protected void addToIndex(T startNode) {
        nodeIndex.put(startNode.getId(), startNode);
        for (Object node : startNode.getLeafNodes()) {
            addToIndex((T) node);
        }
    }

    public List<T> startConversationFromState(V state) {
        List<T> nodes = new ArrayList();
        T current = nodeIndex.get(state.getCurrentNodeId());
        nodes.add(current);
        while ((current.getType() == DialogTreeNodeType.STATEMENT) && (!current.getLeafNodes().isEmpty())) {
            current = (T)current.getLeafNodes().get(0);
            nodes.add(current);
            state.setCurrentNodeId(current.getId());
        }
        return nodes;
    }

    public List<T> processResponse(String response, V state) {
        DialogTreeNode currentSnippet = nodeIndex.get(state.getCurrentNodeId());
        logger.fine(String.format("processing response '%s' for node of type %s with %d allowed answers",response,currentSnippet.getType(),currentSnippet.getLeafNodes().size()));
        switch (currentSnippet.getType()) {
            case QUESTION:
                for (DialogTreeNode allowedAnswer : currentSnippet.getLeafNodes()) {
                    logger.fine(String.format("inspecting possible answer %s",allowedAnswer.renderContent(state)));
                    if (allowedAnswer.renderContent(state).equals(response)) {
                        state.setCurrentNodeId(allowedAnswer.getLeafNodes().get(0).getId());
                        logger.info(String.format("response '%s' matches answer %d",response,allowedAnswer.getId()));
                    }
                }
        }
        return this.startConversationFromState(state);
    }

}
