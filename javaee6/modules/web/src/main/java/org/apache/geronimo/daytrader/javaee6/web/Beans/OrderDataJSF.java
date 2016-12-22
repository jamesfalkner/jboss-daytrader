/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.daytrader.javaee6.web.Beans;


import java.math.BigDecimal;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.daytrader.javaee6.core.direct.FinancialUtils;
import org.apache.geronimo.daytrader.javaee6.entities.AccountDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.AccountProfileDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.HoldingDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;

import org.apache.geronimo.daytrader.javaee6.utils.TradeConfig;
import org.apache.geronimo.daytrader.javaee6.web.TradeAction;

@ManagedBean(name="orderdata")
public class OrderDataJSF {    
    
    private OrderData[] allOrders;
    private int size;
    private OrderData orderData;
    
    public OrderDataJSF(){
        /*getAllOrder();
        setSize(allOrders.length);*/
        getOrder();
    }
    
    public void getAllOrder(){
        TradeAction tAction = new TradeAction();
        try {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);    
        String userID = (String)session.getAttribute("uidBean");        
        AccountDataBean accountData = tAction.getAccountData(userID);
        //AccountProfileDataBean accountProfileData = tAction .getAccountProfileData(userID); 
        ArrayList orderDataBeans = (TradeConfig.getLongRun() ? new ArrayList() : (ArrayList) tAction.getOrders(userID));
        OrderData[] orders = new OrderData[orderDataBeans.size()];
        int count = 0;
        for (Object order : orderDataBeans){
            OrderData r = new OrderData(((OrderDataBean)order).getOrderID(),((OrderDataBean)order).getOrderStatus(), ((OrderDataBean)order).getOpenDate(), ((OrderDataBean)order).getCompletionDate(), ((OrderDataBean)order).getOrderFee(), ((OrderDataBean)order).getOrderType(),((OrderDataBean)order).getQuantity(), ((OrderDataBean)order).getSymbol());
            r.setPrice(((OrderDataBean)order).getPrice());
            r.setTotal(r.getPrice().multiply(new BigDecimal(r.getQuantity())));
            orders[count] = r;
            count ++;
        }    
        setAllOrders(orders);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public void getOrder(){
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);    
        OrderData order = (OrderData)session.getAttribute("orderData");
        if(order != null){
        	setOrderData(order);
        }
    }
    
    public void setAllOrders(OrderData[] allOrders) {
        this.allOrders = allOrders;
    }

    public OrderData[] getAllOrders() {
        return allOrders;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setOrderData(OrderData orderData) {
        this.orderData = orderData;
    }

    public OrderData getOrderData() {
        return orderData;
    }
}
