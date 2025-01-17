package ubic.gemma.web.services.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate endpoints that will have their payload compressed with {@link java.util.zip.GZIPOutputStream}
 * unconditionally.
 *
 * Note that using this annotation will disregard any form of content encoding negotiation for the endpoint. This should
 * only be used on endpoints that produce significant payloads.
 *
 * @see ubic.gemma.web.services.rest.providers.GzipWriterInterceptor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface GZIP {

}
