/*
 * The MIT License
 *
 * Copyright 2019 Synclab Consulting LLC.
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
package com.conversationkit.builder;

import com.conversationkit.impl.node.DialogTreeNode;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tyreus
 */
public class DialogTreeNodeBuilder implements JsonNodeBuilder<DialogTreeNode> {

    @Override
    public DialogTreeNode nodeFromJson(Integer id, String type, JsonObject metadata) throws IOException {

        List<String> messages = new ArrayList();
        if (metadata.get("message") != null) {
            if (metadata.get("message").isArray()) {
                for (JsonValue node : metadata.get("message").asArray()) {
                    messages.add(node.asString());
                }
            } else {
                messages.add(metadata.get("message").asString());
            }
        } else {
            throw new IOException("No \"message\" metadata for node " + id);
        }

        DialogTreeNode conversationNode = new DialogTreeNode(id, messages);

        return conversationNode;
    }
;

}
