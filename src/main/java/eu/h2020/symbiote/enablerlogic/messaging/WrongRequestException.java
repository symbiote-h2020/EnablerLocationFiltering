package eu.h2020.symbiote.enablerlogic.messaging;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

public class WrongRequestException extends RuntimeException {
    private static final long serialVersionUID = 1738499233264896039L;

    @Getter
    private Object request;

    @Getter
    private String requestClassName;

    public WrongRequestException(Object request, String requestClassName) {
        this(null, request, requestClassName);
    }

    public WrongRequestException(String message, Object request, String requestClassName) {
        super(generateMessage(message, request, requestClassName));
        this.request = request;
        this.requestClassName = requestClassName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((request == null) ? 0 : request.hashCode());
        result = prime * result + ((requestClassName == null) ? 0 : requestClassName.hashCode());
        result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WrongRequestException other = (WrongRequestException) obj;
        if (request == null) {
            if (other.request != null)
                return false;
        } else if (!request.equals(other.request))
            return false;
        if (requestClassName == null) {
            if (other.requestClassName != null)
                return false;
        } else if (!requestClassName.equals(other.requestClassName))
            return false;
        if (getMessage() == null) {
            if (other.getMessage() != null)
                return false;
        } else if (!getMessage().equals(other.getMessage()))
            return false;
        return true;
    }

    private static String generateMessage(String message, Object request, String requestClassName) {
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonFactory jsonFactory = jsonMapper.getFactory();
        StringWriter sw = new StringWriter();
        sw.write(message);
        sw.write(" ");

        try {
            JsonGenerator generator = jsonFactory.createGenerator(sw);
            generator.writeStartObject();
            generator.writeStringField("requestClassName", requestClassName);
            generator.writeObjectField("request", LoggingTrimHelper.logToJson(request));
            generator.writeEndObject();
            generator.close();
        } catch (IOException e) {
            sw.write("|| Can not write JSON! ||");
        }

        return sw.toString();
    }
}
