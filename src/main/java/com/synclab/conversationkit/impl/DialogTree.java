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
 * @author pdtyreus
 */
public class DialogTree implements IConversation<DialogTreeNode,DialogTreeState> {

    //private final DialogTreeNode rootNode;

    private final Map<Integer, DialogTreeNode> nodeIndex;
    private static Logger logger = Logger.getLogger(DialogTree.class.getName());

    public DialogTree(DialogTreeNode rootNode) {
        this.nodeIndex = new HashMap();
        addToIndex(rootNode);
    }

    private void addToIndex(DialogTreeNode startNode) {
        nodeIndex.put(startNode.getId(), startNode);
        logger.info(String.format("indexing node %03d:[%-9s] %s", startNode.getId(),startNode.getType(), startNode.renderContent(null)));
        for (DialogTreeNode node : startNode.getLeafNodes()) {
            addToIndex(node);
        }
    }
    
    public List<DialogTreeNode> startConversationFromState(DialogTreeState state) {
        List<DialogTreeNode> nodes = new ArrayList();
        DialogTreeNode current = nodeIndex.get(state.getCurrentNodeId());
        nodes.add(current);
        while ((current.getType() == DialogTreeNodeType.STATEMENT) && (!current.getLeafNodes().isEmpty())) {
            current = current.getLeafNodes().get(0);
            nodes.add(current);
            state.setCurrentNodeId(current.getId());
        }
        return nodes;
    }


    public List<DialogTreeNode> processResponse(String response, DialogTreeState state) {
        DialogTreeNode currentSnippet = nodeIndex.get(state.getCurrentNodeId());
        logger.info(String.format("processing response '%s' for node of type %s with %d allowed answers",response,currentSnippet.getType(),currentSnippet.getLeafNodes().size()));
        switch (currentSnippet.getType()) {
            case QUESTION:
                for (DialogTreeNode allowedAnswer : currentSnippet.getLeafNodes()) {
                    logger.info(String.format("inspecting possible answer %s",allowedAnswer.renderContent(state)));
                    if (allowedAnswer.renderContent(state).equals(response)) {
                        state.setCurrentNodeId(allowedAnswer.getLeafNodes().get(0).getId());
                        logger.info(String.format("response '%s' matches answer %d",response,allowedAnswer.getId()));
                    }
                }
        }
        return this.startConversationFromState(state);
    }
}
