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
package com.conversationkit.model;

import java.util.Map;

/**
 * A general representation of a user's intent from a statement or question in a conversation.
 * <p>
 * Intents in your map user input to responses. The implementation will depend on the
 * vender you choose for natural language understanding. 
 * @author pdtyreus
 */
public interface IConversationIntent {
    /**
     * @return unique ID for the intent.
     */
    public String getIntentId();
    /**
     * Slots are parameters collected from the user's input while detecting intent. As an example, a
     * a slot might be a color. So in the user input, "Press the red button" the intent
     * might be <code>PRESS_BUTTON</code> with a slot of <code>color</code> set to <code>red</code>.
     * @return map of slot keys to slot values
     */
    public Map<String,Object> getSlots();
    
    public boolean getAllRequiredSlotsFilled();
}
