package eu.h2020.symbiote.enablerlogic.messaging;


import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import eu.h2020.symbiote.enablerlogic.messaging.WrongRequestException;

public class WrongRequestExceptionTest {

    @Test
    public void messageGeneration_shouldContainSpecificJson() {
        //given
        WrongRequestException exception = new WrongRequestException("message",
            "request object",
            String.class.getName());

        // when
        String exceptionMessage = exception.getMessage();

        //then
        assertThat(exceptionMessage).isEqualTo("message {\"requestClassName\":\"java.lang.String\",\"request\":\"\\\"request object\\\"\"}");
    }
}
