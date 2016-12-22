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

package org.apache.geronimo.daytrader.javaee6.web.ejb3;

import org.apache.geronimo.daytrader.javaee6.entities.*;
import org.apache.geronimo.daytrader.javaee6.core.beans.RunStatsDataBean;
import org.apache.geronimo.daytrader.javaee6.core.beans.MarketSummaryDataBean;
import org.apache.geronimo.daytrader.javaee6.core.direct.TradeJEEDirect;

import java.math.BigDecimal;
import java.util.Collection;
import javax.ejb.*;


@Stateless
@EJB(name="java:global/DirectSLSBBean", beanInterface=DirectSLSBRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class DirectSLSBBean implements DirectSLSBRemote, DirectSLSBLocal {

    public DirectSLSBBean() {
    }

    public MarketSummaryDataBean getMarketSummary() throws Exception {
        return (new TradeJEEDirect(true)).getMarketSummary();
    }


    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        return (new TradeJEEDirect(true)).buy(userID, symbol, quantity, orderProcessingMode);
    }

    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        return (new TradeJEEDirect(true)).sell(userID, holdingID, orderProcessingMode);
    }

    public void queueOrder(Integer orderID, boolean twoPhase) throws Exception {
        (new TradeJEEDirect(true)).queueOrder(orderID, twoPhase);
    }

    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        return (new TradeJEEDirect(true)).completeOrder(orderID, twoPhase);
    }

    public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception {
        (new TradeJEEDirect(true)).cancelOrder(orderID, twoPhase);
    }

    public void orderCompleted(String userID, Integer orderID) throws Exception {
        (new TradeJEEDirect(true)).orderCompleted(userID, orderID);
    }

    public Collection getOrders(String userID) throws Exception {
        return (new TradeJEEDirect(true)).getOrders(userID);
    }

    public Collection getClosedOrders(String userID) throws Exception {
        return (new TradeJEEDirect(true)).getClosedOrders(userID);
    }

    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        return (new TradeJEEDirect(true)).createQuote(symbol, companyName, price);
    }

    public QuoteDataBean getQuote(String symbol) throws Exception {
        return (new TradeJEEDirect(true)).getQuote(symbol);
    }

    public Collection getAllQuotes() throws Exception {
        return (new TradeJEEDirect(true)).getAllQuotes();
    }

    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) throws Exception {
        return (new TradeJEEDirect(true)).updateQuotePriceVolume(symbol, newPrice, sharesTraded);
    }

    public Collection getHoldings(String userID) throws Exception {
        return (new TradeJEEDirect(true)).getHoldings(userID);
    }

    public HoldingDataBean getHolding(Integer holdingID) throws Exception {
        return (new TradeJEEDirect(true)).getHolding(holdingID);
    }

    public AccountDataBean getAccountData(String userID) throws Exception {
        return (new TradeJEEDirect(true)).getAccountData(userID);
    }

    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        return (new TradeJEEDirect(true)).getAccountProfileData(userID);
    }

    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        return (new TradeJEEDirect(true)).updateAccountProfile(profileData);
    }

    public AccountDataBean login(String userID, String password) throws Exception {
        return (new TradeJEEDirect(true)).login(userID, password);
    }

    public void logout(String userID) throws Exception {
        (new TradeJEEDirect(true)).logout(userID);
    }

    public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard, BigDecimal openBalance) throws Exception {
        return (new TradeJEEDirect(true)).register(userID, password, fullname, address, email, creditcard, openBalance);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        return (new TradeJEEDirect(false)).resetTrade(deleteAll);
    }
}
