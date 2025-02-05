package it.gov.pagopa.pu.send.exception;

import it.gov.pagopa.pu.send.dto.generated.SendNotificationErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MongoTooManyRequestsExceptionHandler {
    private static final Pattern RETRY_AFTER_MS_PATTERN = Pattern.compile("RetryAfterMs=(\\d+)");

    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<SendNotificationErrorDTO> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        if (isRequestRateTooLargeException(ex)) {
            Long retryAfterMs = getRetryAfterMs(ex);
            return handleRequestRateTooLargeException(ex, request, retryAfterMs);
        } else {
            return SendNotificationExceptionHandler.handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, SendNotificationErrorDTO.CodeEnum.GENERIC_ERROR);
        }
    }

    private ResponseEntity<SendNotificationErrorDTO> handleRequestRateTooLargeException(Exception ex, HttpServletRequest request, Long retryAfterMs) {
        String message = ex.getMessage();

        log.info(
                "A MongoQueryException (RequestRateTooLarge) occurred handling request {}: HttpStatus 429 - {}",
                SendNotificationExceptionHandler.getRequestDetails(request), message);

        final ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON);

        if (retryAfterMs != null) {
            long retryAfter = (long) Math.ceil((double) retryAfterMs / 1000);
            responseBuilder.header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
                    .header("Retry-After-Ms", String.valueOf(retryAfterMs));
        }

        return responseBuilder.build();
    }

    public static Long getRetryAfterMs(DataAccessException ex) {
        Matcher matcher = RETRY_AFTER_MS_PATTERN.matcher(ex.getMessage());
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    public static boolean isRequestRateTooLargeException(DataAccessException ex) {
        return ex.getMessage().contains("TooManyRequests") || ex.getMessage().contains("Error=16500,");
    }
}
