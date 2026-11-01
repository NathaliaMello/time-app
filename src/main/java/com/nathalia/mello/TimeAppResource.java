package com.nathalia.mello;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/time-app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TimeAppResource {

    @RestClient
    TimeApiService timeApiService;

    @GET
    public String getTime() {
        return "API: " + timeApiService.getTime();
    }
}
