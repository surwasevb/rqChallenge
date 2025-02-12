package com.reliaquest.api.config;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ServerErrorException;
import com.reliaquest.api.exception.TooManyRequestsException;
import java.io.IOException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return httpResponse.getStatusCode().is5xxServerError()
                || httpResponse.getStatusCode().is4xxClientError();
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().is5xxServerError()) {
            throw new ServerErrorException("Internal Server Error");
        } else if (httpResponse.getStatusCode().is4xxClientError()) {
            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EmployeeNotFoundException("Employee not found");
            }
            if (httpResponse.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new TooManyRequestsException("rate limit exceeded");
            }
            if (httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException("Bad Request");
            }
        }
    }
}
