package controllers.api;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Helper methods for writing REST API routines
 * @author mattwigway
 *
 */
public class JsonManager<T> {
    private ObjectWriter ow;
    private ObjectMapper om;
    
    public JsonManager (Class view) {
        this.om = new ObjectMapper();
        this.ow = om.writerWithView(view);
    }
    
    private Class<T> theClass;
    
    /**
     * Convert an object to its JSON representation
     * @param o the object to convert
     * @return the JSON string
     * @throws JsonProcessingException 
     */
    public String write (T o) throws JsonProcessingException {
        return ow.writeValueAsString(o);
    }
    
    /**
     * Convert a collection of objects to their JSON representation.
     * @param c the collection
     * @return A JsonNode representing the collection
     * @throws JsonProcessingException 
     */
    public String write (Collection<T> c) throws JsonProcessingException {
        return ow.writeValueAsString(c);
    }
    
    public T read (String s) throws JsonParseException, JsonMappingException, IOException {
        return om.readValue(s, theClass);
    }
    
    public T read (JsonParser p) throws JsonParseException, JsonMappingException, IOException {
        return om.readValue(p, theClass);
    }
}
