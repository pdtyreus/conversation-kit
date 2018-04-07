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
package com.conversationkit.impl.transformer;

import com.conversationkit.model.IConversationState;
import com.conversationkit.model.IConversationStateTransformer;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author pdtyreus
 */
public class JavaScriptStateTransformer<R, S extends IConversationState> implements IConversationStateTransformer<R, S> {

    private final String transformerExpression;
    
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");
    
    private static final Logger logger = Logger.getLogger(JavaScriptStateTransformer.class.getName());

    public JavaScriptStateTransformer(String transformerExpression) {
        this.transformerExpression = transformerExpression;
    }
    
    @Override
    public Optional<Map<String,Object>> transformState(Optional<R> response, S state) {
        Invocable inv = (Invocable) engine;
        String template = "function transformState(response, state) {";
        template += transformerExpression;
        template += "};";
        try {
            engine.eval(template);
            return Optional.ofNullable((Map<String,Object>)inv.invokeFunction("transformState", (response.isPresent() ? response.get() : null), state));
        } catch (ScriptException e) {
            logger.warning(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.severe(e.toString());
        }
        return Optional.empty();
    }
    
}
