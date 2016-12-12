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

import com.synclab.conversationkit.impl.dialogtree.DialogTreeNode;
import com.synclab.conversationkit.impl.dialogtree.DialogTree;
import com.synclab.conversationkit.model.SnippetType;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import com.synclab.conversationkit.model.IConversationNodeIndex;
import com.synclab.conversationkit.model.IConversationSnippetRenderer;
import com.synclab.conversationkit.model.IResponseMatcher;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author tyreus
 */
public class JsonDialogTreeBuilder {

    private static final Logger logger = Logger.getLogger(JsonDialogTreeBuilder.class.getName());
    public DialogTree<UserDialogTreeState> fromJson(Reader reader) throws IOException {
        return fromJson(reader,null,null);
    }
    public DialogTree<UserDialogTreeState> fromJson(Reader reader, IConversationSnippetRenderer<UserDialogTreeState> renderer, IResponseMatcher evaluator) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject();

        Map<Integer, DialogTreeNode<UserDialogTreeState>> nodeMap = new HashMap();

        //run through once to create nodes
        for (Member member : keyTree) {
            String name = member.getName();
            Integer id = Integer.parseInt(name);
            JsonObject node = member.getValue().asObject();

            //make the node into something
            String type = node.get("type").asString();
            String content = node.get("content").asString();
            DialogTreeNode dtNode = new DialogTreeNode(id, SnippetType.valueOf(type), content);
            if (renderer != null) {
                dtNode.setRenderer(renderer);
            }
            if (evaluator != null) {
                dtNode.setResponseMatcher(evaluator);
            }
            if (node.get("stateKey") != null) {
                dtNode.setStateKey(node.get("stateKey").asString());
            }
            nodeMap.put(id, dtNode);
        }

        logger.info(MessageFormat.format("Created {0} named nodes", nodeMap.keySet().size()));

        //connect the nodes
        for (Member member : keyTree) {
            String name = member.getName();
            JsonObject node = member.getValue().asObject();
            Integer id = Integer.parseInt(name);

            //make the node into something
            String type = node.get("type").asString();
            SnippetType nodeType = SnippetType.valueOf(type);
            DialogTreeNode dtNode = nodeMap.get(id);
            if (node.get("next") != null) {
                int nextId = node.get("next").asInt();
                DialogTreeNode nextNode = nodeMap.get(nextId);
                dtNode.addLeafNode(nextNode);
                switch (nodeType) {
                    case STATEMENT:
                        break;
                    case ANSWER:
                        int prevId = node.get("question").asInt();
                        DialogTreeNode prevNode = nodeMap.get(prevId);
                        prevNode.addLeafNode(dtNode);
                        prevNode.addSuggestedResponse(dtNode.renderContent(null));
                        break;
                    case QUESTION:
                        
                        break;
                }
            }

        }

        MapBackedNodeIndex<DialogTreeNode> index     = new MapBackedNodeIndex();
        
        index.buildIndexFromStartNode(nodeMap.get(1));
        
        return new DialogTree(index);

    }

}
