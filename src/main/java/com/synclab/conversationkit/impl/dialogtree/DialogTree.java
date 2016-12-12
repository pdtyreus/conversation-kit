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
package com.synclab.conversationkit.impl.dialogtree;

import com.synclab.conversationkit.impl.MapBackedState;
import com.synclab.conversationkit.model.SnippetType;
import com.synclab.conversationkit.model.IConversation;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationNodeIndex;
import com.synclab.conversationkit.model.IConversationSnippet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author pdtyreus
 */
public class DialogTree<V extends MapBackedState> implements IConversation<V> {
    private static Logger logger = Logger.getLogger(DialogTree.class.getName());
    protected final IConversationNodeIndex<DialogTreeNode> nodeIndex;

    public DialogTree(IConversationNodeIndex<DialogTreeNode> nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public List<IConversationSnippet> startConversationFromState(V state) {
        List<IConversationSnippet> nodes = new ArrayList();
        DialogTreeNode<V> current = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        nodes.add(current);
        while ((current.getType() == SnippetType.STATEMENT) && (!current.getLeafNodes().isEmpty())) {
            current = current.getLeafNodes().get(0);
            nodes.add(current);
            state.setCurrentNodeId(current.getId());
        }
        return nodes;
    }

    public List<IConversationSnippet> processResponse(String response, V state){
        DialogTreeNode<V> currentSnippet = nodeIndex.getNodeAtIndex(state.getCurrentNodeId());
        logger.fine(String.format("processing response '%s' for node of type %s with %d allowed answers",response,currentSnippet.getType(),currentSnippet.getLeafNodes().size()));
        boolean matchFound = false;
        switch (currentSnippet.getType()) {
            case QUESTION:
                for (DialogTreeNode<V> allowedAnswer : currentSnippet.getLeafNodes()) {
                    logger.fine(String.format("inspecting possible answer %s",allowedAnswer.renderContent(state)));
                    
                    if (allowedAnswer.isMatchForResponse(response)) {
                        IConversationNode nextLeaf = allowedAnswer.getLeafNodes().get(0);
                        state.setCurrentNodeId(nextLeaf.getId());
                        logger.info(String.format("response '%s' matches answer %d",response,allowedAnswer.getId()));
                        matchFound = true;
                        if (currentSnippet.getStateKey() != null) {
                            state.put(currentSnippet.getStateKey(), response);
                        }
                    }
                }
        }
        
        List<IConversationSnippet> nodes = new ArrayList();
        
        if (!matchFound) {
            nodes.addAll(currentSnippet.getUnmatchedResponseHandler().handleUnmatchedResponse(response, state));
        }
        
        nodes.addAll(startConversationFromState(state));
        
        return nodes;
    }
}
