package eu.h2020.symbiote.enablerlogic.messaging;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

//import eu.h2020.symbiote.smeur.StreetSegment;
//import eu.h2020.symbiote.smeur.StreetSegmentList;
//import eu.h2020.symbiote.smeur.messages.PushInterpolatedStreetSegmentList;

public class MessageConversionExploratoryTest {

//    @Test
//    public void testConversion() {
//        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
//        
//        PushInterpolatedStreetSegmentList isl=new PushInterpolatedStreetSegmentList();
//        isl.regionID="WhatID";
//        isl.theList=new StreetSegmentList();
//        StreetSegmentList theList = isl.theList;
//        StreetSegment ss = new StreetSegment();
//        ss.comment="ss comment";
//        
//        Message msg = converter.toMessage(isl, new MessageProperties());
//        
//        Object converted = converter.fromMessage(msg);
//        
//        assertThat(converted).isInstanceOf(PushInterpolatedStreetSegmentList.class);
//    }
    
//    @Test
//    public void deserialization() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        
//        String json = "{\n" + 
//                "  \"regionID\" : \"zagreb\",\n" + 
//                "  \"theList\" : {\n" + 
//                "    \"247082160\" : {\n" + 
//                "      \"id\" : \"247082160\",\n" + 
//                "      \"exposure\" : {\n" + 
//                "        \"temperature\" : {\n" + 
//                "          \"value\" : \"20.908051570560087\",\n" + 
//                "          \"obsProperty\" : {\n" + 
//                "            \"name\" : \"temperature\"\n" + 
//                "          },\n" + 
//                "          \"uom\" : {\n" + 
//                "            \"symbol\" : \"C\"\n" + 
//                "          }\n" + 
//                "        }\n" + 
//                "      }\n" + 
//                "    },\n" + 
//                "    \"247082161\" : {\n" + 
//                "      \"id\" : \"247082161\",\n" + 
//                "      \"exposure\" : {\n" + 
//                "        \"temperature\" : {\n" + 
//                "          \"value\" : \"20.906255863758275\",\n" + 
//                "          \"obsProperty\" : {\n" + 
//                "            \"name\" : \"temperature\"\n" + 
//                "          },\n" + 
//                "          \"uom\" : {\n" + 
//                "            \"symbol\" : \"C\"\n" + 
//                "          }\n" + 
//                "        }\n" + 
//                "      }\n" + 
//                "    }\n" +
//                "  }\n" +
//                "}";
//        Object o = mapper.readValue(json, mapper.constructType(PushInterpolatedStreetSegmentList.class));
//        System.out.println(o);
//    }
}
