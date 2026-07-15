package com.zantrix.orders.web;

import com.zantrix.orders.OrderRequest;
import com.zantrix.orders.OrderSummary;
import com.zantrix.orders.ResultRequest;
import com.zantrix.orders.ResultSummary;
import com.zantrix.orders.internal.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAnyRole('PHYSICIAN','NURSE')")
public class OrderController {
    private final OrderService orders;
    public OrderController(OrderService orders) { this.orders = orders; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OrderSummary place(@Valid @RequestBody OrderRequest request) { return orders.place(request); }

    @GetMapping
    public List<OrderSummary> list(@RequestParam String patientId,
                                   @RequestParam(defaultValue = "false") boolean includeCompleted) {
        return orders.list(patientId, includeCompleted);
    }

    @PostMapping("/{orderId}/results") @ResponseStatus(HttpStatus.CREATED)
    public ResultSummary result(@PathVariable String orderId, @Valid @RequestBody ResultRequest request) {
        return orders.result(orderId, request);
    }

    @GetMapping("/results")
    public List<ResultSummary> results(@RequestParam String patientId) { return orders.results(patientId); }
}
