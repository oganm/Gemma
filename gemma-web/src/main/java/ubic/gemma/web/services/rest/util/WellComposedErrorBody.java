/*
 * The Gemma project
 *
 * Copyright (c) 2011 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.web.services.rest.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Object acting as a payload for the ResponseErrorObject.
 *
 * @author tesarst
 */
public class WellComposedErrorBody {
    private final Response.Status status;
    private final int code;

    private final String message;

    /**
     * Exclude this field from serialization if unset.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors = null;

    /**
     * Creates a new well composed error body that can be used as a payload for a GemmaApiException, or ResponseErrorObject.
     *
     * @param status  the response status that caused this error.
     * @param message the message to be displayed as the main cause of the error.
     */
    public WellComposedErrorBody( Response.Status status, String message ) {
        this.status = status;
        this.code = status.getStatusCode();
        this.message = message;
    }

    /**
     * Adds descriptive values from the throwable object to the instance of WellComposedErrorBody.
     *
     * @param body the object to add the throwable description to.
     * @param t    the throwable to read the description from.
     */
    public static void addExceptionFields( WellComposedErrorBody body, Throwable t ) {
        body.addErrorsField( "exceptionName", t.getClass().getName() );
        body.addErrorsField( "exceptionMessage", t.getMessage() );
    }

    /**
     * Adds a new field into the errors array property.
     *
     * @param key   key in the array where the value will be inserted.
     * @param value value of the error that will be inserted.
     */
    public void addErrorsField( String key, String value ) {
        if ( this.errors == null ) {
            this.errors = new HashMap<>();
        }
        this.errors.put( key, value );
    }

    /**
     * @return the Response Status of this error body.
     */
    @JsonIgnore
    public Response.Status getStatus() {
        return status;
    }

    /**
     * Used by JSON Serializer.
     *
     * @return the code of the Response Status of this error body.
     */
    @SuppressWarnings("unused") // Used by JSON Serializer
    @JsonProperty
    public int getCode() {
        return code;
    }

    /**
     * Used by JSON Serializer.
     *
     * @return the message of this error body.
     */
    @JsonProperty
    public String getMessage() {
        return message;
    }

    /**
     * Used by JSON Serializer.
     *
     * @return the errors array of this error body.
     */
    @SuppressWarnings("unused") // Used by JSON Serializer
    @JsonProperty
    public Map<String, String> getErrors() {
        return errors;
    }
}
