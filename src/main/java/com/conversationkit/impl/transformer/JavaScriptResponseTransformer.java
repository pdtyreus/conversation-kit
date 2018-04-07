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

import com.conversationkit.model.IConversationResponseTransformer;
import java.util.Optional;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author tyreus
 */
public class JavaScriptResponseTransformer<R>  implements IConversationResponseTransformer<R> {

    private final String transformerExpression;
    
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");
    
    private static final Logger logger = Logger.getLogger(JavaScriptResponseTransformer.class.getName());

    public JavaScriptResponseTransformer(String transformerExpression) {
        this.transformerExpression = transformerExpression;
    }
    
    @Override
    public Optional<R> transformResponse(Optional<String> response) {
        Invocable inv = (Invocable) engine;
        String template = "function transformResponse(response) {";
        template += transformerExpression;
        template += "};";
        try {
            engine.eval(template);
            return Optional.ofNullable((R)inv.invokeFunction("transformResponse", (response.isPresent() ? response.get() : null)));
        } catch (ScriptException e) {
            logger.warning(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.severe(e.toString());
        }
        return Optional.empty();
    }
    
}
