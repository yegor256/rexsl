package com.rexsl.foo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class FrontEnd {
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Home home() {
    return new Home();
  }
}
