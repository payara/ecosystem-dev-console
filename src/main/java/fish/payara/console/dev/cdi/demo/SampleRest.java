/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.cdi.demo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Random;

/**
 *
 * @author Gaurav Gupta
 */

@Path("/dev")
@Produces(MediaType.APPLICATION_JSON)
public class SampleRest {
    
     Random random = new Random();
    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRestMethods() {
                // Define possible HTTP status codes
        int[] possibleStatuses = {200, 400, 401, 403, 404, 500};
        int randomStatus = possibleStatuses[random.nextInt(possibleStatuses.length)];

        // Only return entity if status is 200
        if (randomStatus == 200) {
            return Response.status(randomStatus).entity("Hello world").build();
        } else {
            // Return empty or message for error statuses
            String errorMessage = "Random error occurred with status: " + randomStatus;
            return Response.status(randomStatus).entity(errorMessage).build();
        }
//        return registry.getRestMethodInfoMap().values().stream().collect(Collectors.toList());
    }
}
