package com.conversationkit.nlp;

import com.conversationkit.model.IConversationIntent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pdtyreus
 */
public class RegexIntentDetectorTest {

    @Test
    public void testDetectIntent() {
        System.out.println("detectIntent");
        String sessionId = "0";
        
        Map<String,String> intentMap = new HashMap();
        intentMap.put("DUCK", "duck");
        intentMap.put("GOOSE", "goose");
        intentMap.put("YES", RegexIntentDetector.YES);
        intentMap.put("NO", RegexIntentDetector.NO);
        
        RegexIntentDetector instance = new RegexIntentDetector(intentMap);
        Optional<IConversationIntent> result = instance.detectIntent("yes, please", "en_US", sessionId);
        assertTrue(result.isPresent());
        assertEquals("YES", result.get().getIntentId());
        
        result = instance.detectIntent("no, thanks", "en_US", sessionId);
        assertTrue(result.isPresent());
        assertEquals("NO", result.get().getIntentId());
        
        result = instance.detectIntent("I see 2 ducks", "en_US", sessionId);
        assertTrue(result.isPresent());
        assertEquals("DUCK", result.get().getIntentId());
    }

    @Test
    public void testDetectIntentWithSlot() {
        System.out.println("detectIntent");
        String sessionId = "0";
        
        Map<String,String> intentMap = new HashMap();
        intentMap.put("FOWL", "(?<number>\\d)\\s(?<type>ducks|geese)(\\s(?<where>on the lake|in the air))?");
        intentMap.put("GOOSE", "goose");
        
        Map<String,List<RegexIntentSlot>> slotMap = new HashMap();
        List<RegexIntentSlot> slots = new ArrayList();
        slots.add(new RegexIntentSlot("number",true));
        slots.add(new RegexIntentSlot("type",true));
        slots.add(new RegexIntentSlot("where",false));
        slotMap.put("FOWL", slots);
        
        RegexIntentDetector instance = new RegexIntentDetector(intentMap, slotMap);
        
        Optional<IConversationIntent> result = instance.detectIntent("I see ducks", "en_US", sessionId);
        assertTrue(!result.isPresent());
        
        result = instance.detectIntent("I see 2 ducks", "en_US", sessionId);
        assertTrue(result.isPresent());
        assertTrue(result.get().getAllRequiredSlotsFilled());
        assertEquals("FOWL", result.get().getIntentId());
        
        result = instance.detectIntent("I see 3 geese in the air", "en_US", sessionId);
        assertTrue(result.isPresent());
        assertTrue(result.get().getAllRequiredSlotsFilled());
        assertEquals("FOWL", result.get().getIntentId());
        assertEquals("3",result.get().getSlots().get("number"));
        assertEquals("geese",result.get().getSlots().get("type"));
        
    }

}
