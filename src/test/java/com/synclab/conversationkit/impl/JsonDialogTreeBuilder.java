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

import com.synclab.conversationkit.impl.edge.StatementEdge;
import com.synclab.conversationkit.impl.node.ConversationNode;
import com.synclab.conversationkit.model.SnippetType;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import com.synclab.conversationkit.impl.edge.DialogTreeEdge;
import com.synclab.conversationkit.model.IConversationEdge;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationState;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author tyreus
 */
public class JsonDialogTreeBuilder {

    private static final Logger logger = Logger.getLogger(JsonDialogTreeBuilder.class.getName());

    public DirectedConversation<UserDialogTreeState> fromJson(Reader reader) throws IOException {
        return fromJsonWithTemplates(reader);
    }

    public DirectedConversation<UserDialogTreeState> fromJsonWithTemplates(Reader reader) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject().get("graph").asObject();

        Map<Integer, TemplatedDialogTreeNode<UserDialogTreeState>> nodeMap = new HashMap();

        //run through once to create nodes
        for (JsonValue member : keyTree.get("nodes").asArray()) {
            JsonObject node = member.asObject();
            Integer id = Integer.parseInt(node.get("id").asString());
            //make the node into something
            String type = node.get("type").asString();
            String content = node.get("label").asString();
            SnippetType snippetType = SnippetType.valueOf(type);
            TemplatedDialogTreeNode dtNode = new TemplatedDialogTreeNode(id, snippetType, content);
            nodeMap.put(id, dtNode);
        }

        logger.info(MessageFormat.format("Created {0} named nodes", nodeMap.keySet().size()));

        //connect the nodes
        for (JsonValue member : keyTree.get("edges").asArray()) {
            JsonObject edge = member.asObject();

            Integer sourceId = Integer.parseInt(edge.get("source").asString());
            Integer targetId = Integer.parseInt(edge.get("target").asString());

            TemplatedDialogTreeNode source = nodeMap.get(sourceId);
            TemplatedDialogTreeNode target = nodeMap.get(targetId);

            SnippetType snippetType = source.getType();
            switch (snippetType) {
                case STATEMENT:
                    StatementEdge e = new StatementEdge(target);
                    source.addEdge(e);
                    break;
                case QUESTION:
                    final String answerContent = edge.get("label").asString();
                    String stateKey = null;
                    if ((edge.get("metadata") != null) && (edge.get("metadata").asObject().get("stateKey") != null)) {
                        stateKey = edge.get("metadata").asObject().get("stateKey").asString();
                    }
                    DialogTreeEdge de = new DialogTreeEdge(answerContent, stateKey, target);
                    source.addEdge(de);

                    break;
            }

        }

        MapBackedNodeIndex<UserDialogTreeState> index = new MapBackedNodeIndex();

        index.buildIndexFromStartNode(nodeMap.get(1));

        return new DirectedConversation(index);

    }

}
