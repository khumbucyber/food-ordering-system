package com.food.ordering.system.order.service.domain.ports.input.service;

import javax.validation.Valid;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderComman;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;

public interface OrderApplicationService {

        CreateOrderResponse createOrder(@Valid CreateOrderComman createOrderCommand);

        TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
