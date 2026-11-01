package com.arham.notificationservice.service;

import com.arham.notificationservice.dto.OrderResponse;
import com.arham.notificationservice.dto.ProductResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {


    public void handleOrderCreated(OrderResponse event) {

        System.out.println("response" + event.toString());

    }
}
