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
package com.conversationkit.model;

import java.util.Map;

/**
 * An abstraction of an action button that may be rendered as part of a message
 * by clients that support buttons. Facebook Messenger and Slack messages can
 * both have appended action buttons.
 * 
 * @author pdtyreus
 */
public interface IConversationSnippetButton {
    /**
     * The type of button to render, which will be implementation specific. Examples
     * might be "action", "url", "call", or "share".
     * @return 
     */
    public String getType();
    
    /**
     * The text to display on the button.
     * @return 
     */
    public String getText();
    
    /**
     * The value associated with pressing the button. How the value is used may
     * depend on the button type.
     * @return 
     */
    public String getValue();
    
    /**
     * Additional attributes needed for the implementation to defined the button.
     * 
     * @return 
     */
    public Map<String,Object> getAttributes();
}
