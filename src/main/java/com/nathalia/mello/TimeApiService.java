package com.nathalia.mello;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.LocalDateTime;

@RegisterRestClient(baseUri = "http://localhost:8081/time-api")
public interface TimeApiService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(3000)
    @Fallback(
            fallbackMethod = "getTimeFallback"
    )
    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.5,
            delay = 5000L,
            successThreshold = 2
    )
    public String getTime();

    default String getTimeFallback() {
        return "Fallback: " + LocalDateTime.now() + "\n";
    }
}
