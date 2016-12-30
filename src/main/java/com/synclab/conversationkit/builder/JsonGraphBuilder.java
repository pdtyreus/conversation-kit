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
package com.synclab.conversationkit.builder;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.synclab.conversationkit.impl.DirectedConversationEngine;
import com.synclab.conversationkit.impl.MapBackedNodeIndex;
import com.synclab.conversationkit.impl.edge.DialogTreeEdge;
import com.synclab.conversationkit.impl.edge.JavaScriptEdge;
import com.synclab.conversationkit.impl.edge.RegexEdge;
import com.synclab.conversationkit.impl.edge.StatementEdge;
import com.synclab.conversationkit.impl.node.DialogTreeNode;
import com.synclab.conversationkit.impl.node.ResponseSuggestingNode;
import com.synclab.conversationkit.impl.node.StringReplacingNode;
import com.synclab.conversationkit.model.IConversationNode;
import com.synclab.conversationkit.model.IConversationState;
import com.synclab.conversationkit.model.SnippetContentType;
import com.synclab.conversationkit.model.SnippetType;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * I/O class for loading a directed conversation from a JSON file.
 *
 * @author pdtyreus
 * @see <a href="http://jsongraphformat.info/">JSON Graph Format</a>
 */
public class JsonGraphBuilder<S extends IConversationState> {

    private static final Logger logger = Logger.getLogger(JsonGraphBuilder.class.getName());

    protected enum NodeType {

        StringReplacing, ResponseSuggesting, DialogTree
    }

    protected enum EdgeType {

        DialogTree, JavaScript, Regex, Statement
    }

    public DirectedConversationEngine<S> readJsonGraph(Reader reader) throws IOException {

        JsonValue value = Json.parse(reader);

        JsonObject keyTree = value.asObject().get("graph").asObject();

        MapBackedNodeIndex<S> index = new MapBackedNodeIndex();

        int i = 0;
        //run through once to create nodes
        for (JsonValue member : keyTree.get("nodes").asArray()) {
            JsonObject node = member.asObject();
            Integer id;
            try {
                id = Integer.parseInt(node.get("id").asString());
            } catch (Exception e) {
                throw new IOException("Missing or Invalid \"id\" for node: " + node.toString());
            }
            //validate
            if (node.get("label") == null) {
                throw new IOException("Missing \"label\" for node " + id);
            }
            if (node.get("type") == null) {
                throw new IOException("Missing \"type\" for node " + id);
            }
            String type = node.get("type").asString();
            String content = node.get("label").asString();
            JsonValue metadataValue = node.get("metadata");
            JsonObject metadata = null;
            if (metadataValue == null) {
               throw new IOException("Missing \"metadata\" for node " + id);
            } else {
                 metadata = metadataValue.asObject();
            }
            SnippetType snippetType = SnippetType.valueOf(metadata.get("snippetType").asString());
            NodeType nodeType = NodeType.valueOf(type);

            //make the node into something
            IConversationNode<S> conversationNode;

            switch (nodeType) {
                case DialogTree:
                    DialogTreeNode dtNode = new DialogTreeNode(id, snippetType, content);
                    conversationNode = dtNode;
                    break;
                case StringReplacing:
                    StringReplacingNode srNode = new StringReplacingNode(id, snippetType, content);
                    if (metadata.get("suggestedResponses") != null) {
                        JsonArray suggestions = node.get("metadata").asObject().get("suggestedResponses").asArray();
                        for (JsonValue suggestion : suggestions) {
                            srNode.addSuggestedResponse(suggestion.asString());
                        }
                    }
                    conversationNode = srNode;
                    break;
                case ResponseSuggesting:
                    SnippetContentType contentType = SnippetContentType.TEXT;
                    if (metadata.get("contentType") != null) {
                        contentType = SnippetContentType.valueOf(metadata.get("contentType").asString());
                    }
                    ResponseSuggestingNode rsNode = new ResponseSuggestingNode(id, snippetType, content, contentType);
                    if (metadata.get("suggestedResponses") != null) {
                        JsonArray suggestions = node.get("metadata").asObject().get("suggestedResponses").asArray();
                        for (JsonValue suggestion : suggestions) {
                            rsNode.addSuggestedResponse(suggestion.asString());
                        }
                    }
                    conversationNode = rsNode;
                    break;
                default:
                    throw new IOException("Unhandled node type " + nodeType);
            }

            index.addNodeToIndex(id, conversationNode);
            i++;
        }

        logger.info(MessageFormat.format("Created {0} nodes", i));

        i = 0;

        //connect the nodes
        for (JsonValue member : keyTree.get("edges").asArray()) {
            JsonObject edge = member.asObject();

            if (edge.get("label") == null) {
                throw new IOException("Missing \"label\" for edge: " + edge.toString());
            }
            if (edge.get("type") == null) {
                throw new IOException("Missing \"type\" for edge: " + edge.toString());
            }
            if (edge.get("source") == null) {
                throw new IOException("Missing \"source\" for edge: " + edge.toString());
            }
            if (edge.get("target") == null) {
                throw new IOException("Missing \"target\" for edge: " + edge.toString());
            }
            String type = edge.get("type").asString();
            String content = edge.get("label").asString();
            JsonValue metadataValue = edge.get("metadata");
            JsonObject metadata = null;
            if (metadataValue != null) {
                metadata = metadataValue.asObject();
            }

            Integer sourceId = Integer.parseInt(edge.get("source").asString());
            Integer targetId = Integer.parseInt(edge.get("target").asString());

            IConversationNode<S> source = index.getNodeAtIndex(sourceId);
            IConversationNode<S> target = index.getNodeAtIndex(targetId);

            EdgeType edgeType = EdgeType.valueOf(type);
            String stateKey = null;
            if ((metadata != null) && (metadata.get("stateKey") != null)) {
                stateKey = metadata.get("stateKey").asString();
            }
            switch (edgeType) {
                case DialogTree:
                    if ((metadata == null) || (metadata.get("answer") == null)) {
                        throw new IOException("DialogTreeEdge missing \"answer\" metadata key: " + edge);
                    }
                    DialogTreeEdge dte = new DialogTreeEdge(metadata.get("answer").asString(), stateKey, target);
                    source.addEdge(dte);
                    break;
                case JavaScript:

                    if ((metadata == null) || (metadata.get("isMatchForState") == null)) {
                        throw new IOException("JavaScriptEdge missing \"isMatchForState\" metadata key: " + edge);
                    }
                    String isMatch = metadata.get("isMatchForState").asString();
                    if (metadata.get("onMatch") != null) {
                        String onMatch = metadata.get("onMatch").asString();
                        JavaScriptEdge de = new JavaScriptEdge(isMatch, onMatch, target);
                        source.addEdge(de);
                    } else {
                        JavaScriptEdge de = new JavaScriptEdge(isMatch, target);
                        source.addEdge(de);
                    }
                    break;
                case Regex:
                    if ((metadata == null) || (metadata.get("pattern") == null)) {
                        throw new IOException("RegexEdge missing \"pattern\" metadata key: " + edge);
                    }
                    String pattern = metadata.get("pattern").asString();
                    RegexEdge de = new RegexEdge(pattern, stateKey, target);
                    source.addEdge(de);

                    break;
                case Statement:
                    StatementEdge e = new StatementEdge(target);
                    source.addEdge(e);
                    break;
                default:
                    throw new IOException("Unhandled node edge " + edgeType);
            }
            i++;
        }

        logger.info(MessageFormat.format("Created {0} edges", i));
        return new DirectedConversationEngine(index);

    }
}
