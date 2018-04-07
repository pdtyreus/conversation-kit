/*
 * The MIT License
 *
 * Copyright 2017 Synclab Consulting LLC.
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

import com.conversationkit.model.IConversationResponseTransformer;
import com.conversationkit.model.IConversationSnippetButton;
import com.conversationkit.model.IConversationState;
import com.conversationkit.model.IConversationStateTransformer;
import com.conversationkit.model.SnippetContentType;
import com.conversationkit.model.SnippetType;

/**
 * An implementation of <code>IConversationNode</code> that serves simply as a
 * pass-though node that renders no content.
 *
 * @author pdtyreus
 */
public class HiddenNode<R, S extends IConversationState> extends ConversationNode<R, S> {

    public HiddenNode(int id, SnippetType type) {
        super(id, type);
    }

    public HiddenNode(int id, SnippetType type, IConversationResponseTransformer<R> responseTransformer, IConversationStateTransformer<R, S> stateTransformer) {
        super(id, type, responseTransformer, stateTransformer);
    }

    @Override
    public String renderContent(S state) {
        return null;
    }

    @Override
    public SnippetContentType getContentType() {
        return SnippetContentType.NOTHING;
    }

    @Override
    public Iterable<String> getSuggestedResponses(S state) {
        return null;
    }

    @Override
    public Iterable<IConversationSnippetButton> getButtons() {
        return null;
    }

}
