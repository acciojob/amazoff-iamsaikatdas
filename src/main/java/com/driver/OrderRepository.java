package com.driver;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository {

    private HashMap<String, Order> orderDetails;
    private HashMap<String, DeliveryPartner> deliveryPartnerDetails;
    private HashMap<String, HashSet<String>> partnerOrderDetails;

    private Set<String> orderNotAssigned;

    public OrderRepository() {
        this.orderDetails = new HashMap<>();
        this.deliveryPartnerDetails = new HashMap<>();
        this.orderNotAssigned = new HashSet<>();
        this.partnerOrderDetails = new HashMap<>();
    }

    public void addOrder(Order order){
        orderDetails.put(order.getId(), order);
        orderNotAssigned.add(order.getId());
    }

    public void addPartner(String partnerId) {
        deliveryPartnerDetails.put(partnerId, new DeliveryPartner(partnerId));
    }

    public void addOrderPartnerPair(String orderId, String partnerId) {

        if (orderDetails.containsKey(orderId) && deliveryPartnerDetails.containsKey(partnerId)){

            HashSet<String> currentOrders = new HashSet<>();
            if (partnerOrderDetails.containsKey(partnerId)){
                currentOrders = partnerOrderDetails.get(partnerId);
            }
            currentOrders.add(orderId);
            partnerOrderDetails.put(partnerId, currentOrders);
            DeliveryPartner deliveryPartner = deliveryPartnerDetails.get(partnerId);
            deliveryPartner.setNumberOfOrders(currentOrders.size());
        }
    }

    public Order getOrderById(String orderId) {
        return orderDetails.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        return deliveryPartnerDetails.get(partnerId);
    }

    public int getOrderCountByPartnerId(String partnerId) {
       int orderCount = 0;
       if (deliveryPartnerDetails.containsKey(partnerId)){
           orderCount = deliveryPartnerDetails.get(partnerId).getNumberOfOrders();
       }
       return orderCount;
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        HashSet<String> orderList = new HashSet<>();
        if(partnerOrderDetails.containsKey(partnerId))
            orderList = partnerOrderDetails.get(partnerId);

        return new ArrayList<>(orderList);
    }

    public List<String> getAllOrders() {
        return new ArrayList<>(orderDetails.keySet());
    }

    public int getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(3));
        int times = hour*60 + minutes;

        int countOfOrders = 0;
        if(partnerOrderDetails.containsKey(partnerId)){
            HashSet<String> orders = partnerOrderDetails.get(partnerId);
            for(String order: orders){
                if(orderDetails.containsKey(order)){
                    Order currOrder = orderDetails.get(order);
                    if(times < currOrder.getDeliveryTime()){
                        countOfOrders += 1;
                    }
                }
            }
        }
        return countOfOrders;
    }

    public String  getLastDeliveryTimeByPartnerId(String partnerId) {
        int time = 0;

        if(partnerOrderDetails.containsKey(partnerId)){
            HashSet<String> orders = partnerOrderDetails.get(partnerId);
            for(String order: orders){
                if(orderDetails.containsKey(order)){
                    Order currOrder = orderDetails.get(order);
                    time = Math.max(time, currOrder.getDeliveryTime());
                }
            }
        }

        int hour = time/60;
        int minutes = time%60;

        String hourInString = String.valueOf(hour);
        String minInString = String.valueOf(minutes);
        if(hourInString.length() == 1){
            hourInString = "0" + hourInString;
        }
        if(minInString.length() == 1){
            minInString = "0" + minInString;
        }

        return hourInString + ":" + minInString;
    }

    public void deleteOrderById(String orderId) {
        if(partnerOrderDetails.containsKey(orderId)){
            String partnerId = String.valueOf(partnerOrderDetails.get(orderId));
            HashSet<String> orders = partnerOrderDetails.get(partnerId);
            orders.remove(orderId);
            partnerOrderDetails.put(partnerId, orders);

            //change order count of partner
            DeliveryPartner partner = deliveryPartnerDetails.get(partnerId);
            partner.setNumberOfOrders(orders.size());
        }

        if(orderDetails.containsKey(orderId)){
            orderDetails.remove(orderId);
        }
    }

    public void deletePartnerById(String partnerId) {
        if(!partnerOrderDetails.isEmpty()){
            orderNotAssigned.addAll(partnerOrderDetails.get(partnerId));
        }
        partnerOrderDetails.remove(partnerId);
        deliveryPartnerDetails.remove(partnerId);

    }

    public int getCountOfUnassignedOrders() {
        int countOfOrders = 0;
        List<String> orders =  new ArrayList<>(orderDetails.keySet());
        for(String orderId: orders){
            if(!partnerOrderDetails.containsKey(orderId)){
                countOfOrders += 1;
            }
        }
        return countOfOrders;
    }
}
