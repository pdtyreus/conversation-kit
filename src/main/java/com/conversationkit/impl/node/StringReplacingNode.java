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

import com.conversationkit.impl.MapBackedState;
import com.conversationkit.model.IConversationResponseTransformer;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.IConversationStateTransformer;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;

/**
 *
 * @author pdtyreus
 */
public class StringReplacingNode<R, S extends IConversationState> extends ResponseSuggestingNode<R, S> {

    public StringReplacingNode(int id, SnippetType type, String content, SnippetContentType contentType) {
        super(id, type, content, contentType);
    }
    
    public StringReplacingNode(int id, SnippetType type, IConversationResponseTransformer<R> responseTransformer, IConversationStateTransformer<R, S> stateTransformer, String content, SnippetContentType contentType) {
        super(id, type, responseTransformer, stateTransformer, content, contentType);
    }
    
    @Override
    public String renderContent(S state) {
        if (state == null) {
            return content;
        }

        if (state instanceof MapBackedState) {
            String renderedContent = content;
            MapBackedState map = (MapBackedState) state;
            for (Object okey : map.keySet()) {
                String key = (String) okey;
                renderedContent = renderedContent.replace("{{" + key + "}}", state.get(key).toString());
            }

            return renderedContent;
        } else {
            return content;
        }
    }
}
