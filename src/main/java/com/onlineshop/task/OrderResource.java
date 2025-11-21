package com.onlineshop.task;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderRepository orderRepository;

    @Inject
    CamundaService camundaService;

    @GET
    public Collection<Order> list() {
        return orderRepository.findAll();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        Optional<Order> order = orderRepository.findById(id);
       return order.map(o -> Response.ok(o).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response create(Order order) {
        if (order == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (order.getId() == null || order.getId().isBlank()) {
            order.setId(UUID.randomUUID().toString());
        }
        orderRepository.save(order);

        // start process instance with order id as business key / variable
        camundaService.startProcessInstanceByKey("Process_1milk0d", Map.of("orderId", order.getId()));

        URI location = URI.create("/orders/" + order.getId());
        return Response.created(location).entity(order).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") String id) {
        Optional<Order> removed = orderRepository.delete(id);
        return removed.isPresent() ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("{id}/start")
    public Response startProcess(@PathParam("id") String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        camundaService.startProcessInstanceByKey("Process_1milk0d", Map.of("orderId", id));
        return Response.accepted().build();
    }
}