package ubic.gemma.web.logging;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.Attachment;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.util.Collections;

public class SlackAppender extends AppenderSkeleton implements Appender {

    private final Slack slackInstance;

    private String token;
    private String channel;

    public SlackAppender() {
        slackInstance = new Slack();
    }

    @Override
    protected void append( LoggingEvent loggingEvent ) {
        if ( !isAsSevereAsThreshold( loggingEvent.getLevel() ) ) {
            return;
        }
        try {
            ChatPostMessageRequest.ChatPostMessageRequestBuilder request = ChatPostMessageRequest.builder()
                    .channel( channel )
                    .text( this.layout.format( loggingEvent ) );

            // attach a stacktrace if available
            if ( loggingEvent.getThrowableInformation() != null )
                request.attachments( Collections.singletonList( stacktraceAsAttachment( loggingEvent ) ) );

            slackInstance.methods( token ).chatPostMessage( request.build() );
        } catch ( IOException | SlackApiException e ) {
            errorHandler.error( String.format( "Failed to send logging event to Slack channel %s.", channel ), e, 0, loggingEvent );
        }
    }

    @Override
    @SneakyThrows
    public void close() {
        slackInstance.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public void setChannel( String channel ) {
        this.channel = channel;
    }

    private static Attachment stacktraceAsAttachment( LoggingEvent loggingEvent ) {
        return Attachment.builder()
                .title( ExceptionUtils.getMessage( loggingEvent.getThrowableInformation().getThrowable() ) )
                .text( ExceptionUtils.getStackTrace( loggingEvent.getThrowableInformation().getThrowable() ) )
                .fallback( "This attachment normally contains an error stacktrace." )
                .build();
    }
}
