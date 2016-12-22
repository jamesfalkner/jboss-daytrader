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

import org.apache.geronimo.daytrader.javaee6.core.direct.TradeJEEDirect;
import org.apache.geronimo.daytrader.javaee6.core.direct.FinancialUtils;
import org.apache.geronimo.daytrader.javaee6.core.beans.MarketSummaryDataBean;
import org.apache.geronimo.daytrader.javaee6.core.beans.RunStatsDataBean;

import org.apache.geronimo.daytrader.javaee6.entities.AccountDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.AccountProfileDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.HoldingDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.QuoteDataBean;
import org.apache.geronimo.daytrader.javaee6.utils.Log;

import org.apache.geronimo.daytrader.javaee6.utils.TradeConfig;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.transaction.RollbackException;


@Stateless
@EJB(name="java:global/TradeSLSBBean",beanInterface=TradeSLSBRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TradeSLSBBean implements TradeSLSBRemote, TradeSLSBLocal {

    @Resource(name = "jms/QueueConnectionFactory")
    private QueueConnectionFactory queueConnectionFactory;

    @Resource(name = "jms/TopicConnectionFactory")
    private TopicConnectionFactory topicConnectionFactory;

    @Resource(name = "jms/TradeStreamerTopic")
    private Topic tradeStreamerTopic;

    @Resource(name = "jms/TradeBrokerQueue")
    private Queue tradeBrokerQueue;

    @PersistenceContext(unitName = "daytrader")
    private EntityManager entityManager;

    /** Creates a new instance of TradeSLSBBean */
    public TradeSLSBBean() {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:ejbCreate  -- JNDI lookups of EJB and JMS resources");
    }

    public MarketSummaryDataBean getMarketSummary() {
        MarketSummaryDataBean marketSummaryData;
        
        try {
            if (Log.doTrace())
                Log.trace("TradeSLSBBean:getMarketSummary -- getting market summary");

            // Find Trade Stock Index Quotes (Top 100 quotes)
            // ordered by their change in value
            Collection<QuoteDataBean> quotes;

            Query query = entityManager.createNamedQuery("quoteejb.quotesByChange");
            quotes = query.getResultList();

            QuoteDataBean[] quoteArray = (QuoteDataBean[]) quotes.toArray(new QuoteDataBean[quotes.size()]);
            ArrayList<QuoteDataBean> topGainers = new ArrayList<QuoteDataBean>(5);
            ArrayList<QuoteDataBean> topLosers = new ArrayList<QuoteDataBean>(5);
            BigDecimal TSIA = FinancialUtils.ZERO;
            BigDecimal openTSIA = FinancialUtils.ZERO;
            double totalVolume = 0.0;

            if (quoteArray.length > 5) {
                for (int i = 0; i < 5; i++)
                    topGainers.add(quoteArray[i]);
                for (int i = quoteArray.length - 1; i >= quoteArray.length - 5; i--)
                    topLosers.add(quoteArray[i]);

                for (QuoteDataBean quote : quoteArray) {
                    BigDecimal price = quote.getPrice();
                    BigDecimal open = quote.getOpen();
                    double volume = quote.getVolume();
                    TSIA = TSIA.add(price);
                    openTSIA = openTSIA.add(open);
                    totalVolume += volume;
                }
                TSIA = TSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
                openTSIA = openTSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
            }

            marketSummaryData = new MarketSummaryDataBean(TSIA, openTSIA, totalVolume, topGainers, topLosers);
        } catch (Exception e) {
            Log.error("TradeSLSBBean:getMarketSummary", e);
            throw new EJBException("TradeSLSBBean:getMarketSummary -- error ", e);
        }
        return marketSummaryData;
    }

    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        OrderDataBean order;
        BigDecimal total;
        try {
            if (Log.doTrace())
                Log.trace("TradeSLSBBean:buy", userID, symbol, quantity, orderProcessingMode);

            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
            AccountDataBean account = profile.getAccount();
            QuoteDataBean quote = entityManager.find(QuoteDataBean.class, symbol);
            HoldingDataBean holding = null; // The holding will be created by this buy order

            order = createOrder(account, quote, holding, "buy", quantity);

            // UPDATE - account should be credited during completeOrder
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(balance.subtract(total));

            if (orderProcessingMode == TradeConfig.SYNCH)
                completeOrder(order.getOrderID(), false);
            else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE)
                queueOrder(order.getOrderID(), true);
        } catch (Exception e) {
            Log.error("TradeSLSBBean:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
            /* On exception - cancel the order */
            // TODO figure out how to do this with JPA
            // if (order != null) order.cancel();
            throw new EJBException(e);
        }
        return order;
    }

    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) {
        OrderDataBean order;
        BigDecimal total;
        try {
            if (Log.doTrace())
                Log.trace("TradeSLSBBean:sell", userID, holdingID, orderProcessingMode);

            AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
            AccountDataBean account = profile.getAccount();
            HoldingDataBean holding = entityManager.find(HoldingDataBean.class, holdingID);
            
            if (holding == null) {
                Log.error("TradeSLSBBean:sell User " + userID + " attempted to sell holding " + holdingID + " which has already been sold");
                
                OrderDataBean orderData = new OrderDataBean();
                orderData.setOrderStatus("cancelled");
                entityManager.persist(orderData);
                
                return orderData;
            }            

            QuoteDataBean quote = holding.getQuote();
            double quantity = holding.getQuantity();
            order = createOrder(account, quote, holding, "sell", quantity);

            // UPDATE the holding purchase data to signify this holding is "inflight" to be sold
            // -- could add a new holdingStatus attribute to holdingEJB
            holding.setPurchaseDate(new java.sql.Timestamp(0));

            // UPDATE - account should be credited during completeOrder
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
            account.setBalance(balance.add(total));

            if (orderProcessingMode == TradeConfig.SYNCH)
                completeOrder(order.getOrderID(), false);
            else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE)
                queueOrder(order.getOrderID(), true);

        } catch (Exception e) {
            Log.error("TradeSLSBBean:sell(" + userID + "," + holdingID + ") --> failed", e);
            // TODO figure out JPA cancel
            // if (order != null) order.cancel();
            // UPDATE - handle all exceptions like:
            throw new EJBException("TradeSLSBBean:sell(" + userID + "," + holdingID + ")", e);
        }
        return order;
    }

    public void queueOrder(Integer orderID, boolean twoPhase) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:queueOrder", orderID);

        Connection conn = null;
        Session sess = null;
        try {
            conn = queueConnectionFactory.createConnection();
            sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer msgProducer = sess.createProducer(tradeBrokerQueue);
            TextMessage message = sess.createTextMessage();

            message.setStringProperty("command", "neworder");
            message.setIntProperty("orderID", orderID);
            message.setBooleanProperty("twoPhase", twoPhase);
            message.setText("neworder: orderID=" + orderID + " runtimeMode=EJB twoPhase=" + twoPhase);
            message.setLongProperty("publishTime", System.currentTimeMillis());

            if (Log.doTrace())
                Log.trace("TradeSLSBBean:queueOrder Sending message: " + message.getText());
            msgProducer.send(message);
        } catch (javax.jms.JMSException e) {
            throw new EJBException(e.getMessage(), e); // pass the exception back
        } finally {
            try {
                if (conn != null)
                    conn.close();
                if (sess != null)
                    sess.close();
            } catch (javax.jms.JMSException e) {
                throw new EJBException(e.getMessage(), e); // pass the exception back
            }
        }
    }

    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase)
            throws Exception {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:completeOrder", orderID + " twoPhase=" + twoPhase);

        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);
        order.getQuote();

        if (order == null) {
            Log.error("TradeSLSBBean:completeOrder -- Unable to find Order " + orderID + " FBPK returned " + order);
            return null;
        }

        if (order.isCompleted())
            throw new EJBException("Error: attempt to complete Order that is already completed\n" + order);

        AccountDataBean account = order.getAccount();
        QuoteDataBean quote = order.getQuote();
        HoldingDataBean holding = order.getHolding();
        BigDecimal price = order.getPrice();
        double quantity = order.getQuantity();

        String userID = account.getProfile().getUserID();

        if (Log.doTrace())
            Log.trace("TradeSLSBBeanInternal:completeOrder--> Completing Order " + order.getOrderID() 
                    + "\n\t Order info: " + order
                    + "\n\t Account info: " + account 
                    + "\n\t Quote info: " + quote 
                    + "\n\t Holding info: " + holding);

        if (order.isBuy()) {
            /*
             * Complete a Buy operation 
             * - create a new Holding for the Account 
             * - deduct the Order cost from the Account balance
             */

            HoldingDataBean newHolding = createHolding(account, quote, quantity, price);
            order.setHolding(newHolding);
        }

        if (order.isSell()) {
            /*
             * Complete a Sell operation 
             * - remove the Holding from the Account 
             * - deposit the Order proceeds to the Account balance
             */
            if (holding == null) {
                Log.error("TradeSLSBBean:completeOrder -- Unable to sell order " + order.getOrderID() + " holding already sold");
                order.cancel();
                return order;
            } else {
                entityManager.remove(holding);
                order.setHolding(null);
            }
        }
        order.setOrderStatus("closed");

        order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));

        if (Log.doTrace())
            Log.trace("TradeSLSBBean:completeOrder--> Completed Order " + order.getOrderID() 
                    + "\n\t Order info: " + order
                    + "\n\t Account info: " + account 
                    + "\n\t Quote info: " + quote 
                    + "\n\t Holding info: " + holding);

        //if (Log.doTrace())
        //    Log.trace("Calling TradeAction:orderCompleted from Session EJB using Session Object");
        // FUTURE All getEJBObjects could be local -- need to add local I/F

        // commented out following call
        // - orderCompleted doesn't really do anything (think it was a hook for old Trade caching code)
        
        /*TradeAction tradeAction = new TradeAction();
        tradeAction.orderCompleted(userID, orderID);*/

        return order;
    }

    public void cancelOrder(Integer orderID, boolean twoPhase) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:cancelOrder", orderID + " twoPhase=" + twoPhase);

        OrderDataBean order = entityManager.find(OrderDataBean.class, orderID);
        order.cancel();
    }

    public void orderCompleted(String userID, Integer orderID) {
        throw new UnsupportedOperationException("TradeSLSBBean:orderCompleted method not supported");
    }

    public Collection<OrderDataBean> getOrders(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getOrders", userID);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();
        return account.getOrders();
    }

    public Collection<OrderDataBean> getClosedOrders(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getClosedOrders", userID);

        try {

            // Get the primary keys for all the closed Orders for this
            // account.
            Query query = entityManager.createNamedQuery("orderejb.closedOrders");
            query.setParameter("userID", userID);
            Collection results = query.getResultList();
            Iterator itr = results.iterator();
            
            // Spin through the orders to populate the lazy quote fields
            while (itr.hasNext()){
                OrderDataBean thisOrder = (OrderDataBean)itr.next();
                thisOrder.getQuote();
            }
            
            /* Add logic to do update orders operation, because JBoss5' Hibernate 3.3.1GA DB2Dialect 
             * and MySQL5Dialect do not work with annotated query "orderejb.completeClosedOrders"
             * defined in OrderDatabean 
             */
            if (TradeConfig.jpaLayer == TradeConfig.OPENJPA) {
                Query updateStatus = entityManager.createNamedQuery("orderejb.completeClosedOrders");
                updateStatus.setParameter("userID", userID);
                updateStatus.executeUpdate();
                }
                
            if (TradeConfig.jpaLayer == TradeConfig.HIBERNATE) {
                Query findaccountid = entityManager.createNativeQuery("select "+
                                                                          "a.ACCOUNTID, "+
                                                                          "a.LOGINCOUNT, "+
                                                                          "a.LOGOUTCOUNT, "+
                                                                          "a.LASTLOGIN, "+
                                                                          "a.CREATIONDATE, "+
                                                                          "a.BALANCE, "+
                                                                          "a.OPENBALANCE, "+
                                                                          "a.PROFILE_USERID "+
                                                                          "from accountejb a where a.profile_userid = ?", org.apache.geronimo.daytrader.javaee6.entities.AccountDataBean.class);
                findaccountid.setParameter(1, userID);
                AccountDataBean account = (AccountDataBean)findaccountid.getSingleResult();                
                Integer accountid = account.getAccountID();
                Query updateStatus = entityManager.createNativeQuery("UPDATE orderejb o SET o.orderStatus = 'completed' WHERE o.orderStatus = 'closed' AND o.ACCOUNT_ACCOUNTID  = ?");
                updateStatus.setParameter(1, accountid.intValue());
                updateStatus.executeUpdate();
                }
                
            return results;
        } catch (Exception e) {
            Log.error("TradeSLSBBean.getClosedOrders", e);
            throw new EJBException("TradeSLSBBean.getClosedOrders - error", e);
        }
    }

    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) {
        try {
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            entityManager.persist(quote);
            if (Log.doTrace())
                Log.trace("TradeSLSBBean:createQuote-->" + quote);
            return quote;
        } catch (Exception e) {
            Log.error("TradeSLSBBean:createQuote -- exception creating Quote", e);
            throw new EJBException(e);
        }
    }

    public QuoteDataBean getQuote(String symbol) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getQuote", symbol);

        return entityManager.find(QuoteDataBean.class, symbol);
    }

    public Collection<QuoteDataBean> getAllQuotes() {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getAllQuotes");

        Query query = entityManager.createNamedQuery("quoteejb.allQuotes");
        return query.getResultList();
    }

    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getUpdateQuotePrices())
            return new QuoteDataBean();

        if (Log.doTrace())
            Log.trace("TradeSLSBBean:updateQuote", symbol, changeFactor);

        /* Add logic to determine JPA layer, because JBoss5' Hibernate 3.3.1GA DB2Dialect 
         * and MySQL5Dialect do not work with annotated query "quoteejb.quoteForUpdate"
         * defined in QuoteDatabean 
          */    
        QuoteDataBean quote = new QuoteDataBean();
        if (TradeConfig.jpaLayer == TradeConfig.HIBERNATE) {
            quote = entityManager.find(QuoteDataBean.class, symbol);
           }
           
        if (TradeConfig.jpaLayer == TradeConfig.OPENJPA) {
            Query q = entityManager.createNamedQuery("quoteejb.quoteForUpdate");
            q.setParameter(1, symbol);
            quote = (QuoteDataBean) q.getSingleResult();
           }       

        BigDecimal oldPrice = quote.getPrice();

        if (quote.getPrice().equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

        quote.setPrice(newPrice);
        quote.setVolume(quote.getVolume() + sharesTraded);
        entityManager.merge(quote);

        // TODO find out if requires new here is really intended -- it is backwards,
        // change can get published w/o it occurring.
        // ((Trade) context.getEJBObject()).publishQuotePriceChange(quote,oldPrice, changeFactor, sharesTraded);
        this.publishQuotePriceChange(quote, oldPrice, changeFactor, sharesTraded);

        return quote;
    }

    public Collection<HoldingDataBean> getHoldings(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getHoldings", userID);

        Query query = entityManager.createNamedQuery("holdingejb.holdingsByUserID");
        query.setParameter("userID", userID);
        Collection<HoldingDataBean> holdings = query.getResultList();
        /*
         * Inflate the lazy data memebers
        */
        Iterator itr = holdings.iterator();
        while (itr.hasNext()) {
            ((HoldingDataBean) itr.next()).getQuote();
        }

        return holdings;
    }

    public HoldingDataBean getHolding(Integer holdingID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getHolding", holdingID);
        return entityManager.find(HoldingDataBean.class, holdingID);
    }

    public AccountDataBean getAccountData(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getAccountData", userID);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        /*
         * Inflate the lazy data memebers
         */
        AccountDataBean account = profile.getAccount();
        account.getProfile();
        
        // Added to populate transient field for account
        account.setProfileID(profile.getUserID());
        return account;
    }

    public AccountProfileDataBean getAccountProfileData(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:getProfileData", userID);

        return entityManager.find(AccountProfileDataBean.class, userID);
    }

    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:updateAccountProfileData", profileData);
        /*
         * // Retreive the pevious account profile in order to get account
         * data... hook it into new object AccountProfileDataBean temp =
         * entityManager.find(AccountProfileDataBean.class,
         * profileData.getUserID()); // In order for the object to merge
         * correctly, the account has to be hooked into the temp object... // -
         * may need to reverse this and obtain the full object first
         * 
         * profileData.setAccount(temp.getAccount());
         * 
         * //TODO this might not be correct temp =
         * entityManager.merge(profileData); //System.out.println(temp);
         */

        AccountProfileDataBean temp = entityManager.find(AccountProfileDataBean.class, profileData.getUserID());
        temp.setAddress(profileData.getAddress());
        temp.setPassword(profileData.getPassword());
        temp.setFullName(profileData.getFullName());
        temp.setCreditCard(profileData.getCreditCard());
        temp.setEmail(profileData.getEmail());

        entityManager.merge(temp);

        return temp;
    }

    public AccountDataBean login(String userID, String password) throws RollbackException {
        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);

        if (profile == null) {
            throw new EJBException("No such user: " + userID);
        }
        entityManager.merge(profile);
        AccountDataBean account = profile.getAccount();

        if (Log.doTrace())
            Log.trace("TradeSLSBBean:login", userID, password);
        account.login(password);
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:login(" + userID + "," + password + ") success" + account);

        return account;
    }

    public void logout(String userID) {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:logout", userID);

        AccountProfileDataBean profile = entityManager.find(AccountProfileDataBean.class, userID);
        AccountDataBean account = profile.getAccount();

        account.logout();
        
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:logout(" + userID + ") success");
    }

    public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard, BigDecimal openBalance) {
        AccountDataBean account = null;
        AccountProfileDataBean profile = null;
        
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:register", userID, password, fullname, address, email, creditcard, openBalance);

        // Check to see if a profile with the desired userID already exists
        profile = entityManager.find(AccountProfileDataBean.class, userID);

        if (profile != null) {
            Log.error("Failed to register new Account - AccountProfile with userID(" + userID + ") already exists");
            return null;
        } else {
            profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
            account = new AccountDataBean(0, 0, null, new Timestamp(System.currentTimeMillis()), openBalance, openBalance, userID);

            profile.setAccount(account);
            account.setProfile(profile);

            entityManager.persist(profile); 
            entityManager.persist(account);
        }
        
        return account;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public RunStatsDataBean resetTrade(boolean deleteAll) throws Exception {
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:resetTrade", deleteAll);

        return (new TradeJEEDirect(false)).resetTrade(deleteAll);
    }

    private void publishQuotePriceChange(QuoteDataBean quote, BigDecimal oldPrice, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getPublishQuotePriceChange())
            return;
        if (Log.doTrace())
            Log.trace("TradeSLSBBean:publishQuotePricePublishing -- quoteData = " + quote);

        Connection conn = null;
        Session sess = null;

        try {
            conn = topicConnectionFactory.createConnection();
            sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer msgProducer = sess.createProducer(tradeStreamerTopic);
            TextMessage message = sess.createTextMessage();

            String command = "updateQuote";
            message.setStringProperty("command", command);
            message.setStringProperty("symbol", quote.getSymbol());
            message.setStringProperty("company", quote.getCompanyName());
            message.setStringProperty("price", quote.getPrice().toString());
            message.setStringProperty("oldPrice", oldPrice.toString());
            message.setStringProperty("open", quote.getOpen().toString());
            message.setStringProperty("low", quote.getLow().toString());
            message.setStringProperty("high", quote.getHigh().toString());
            message.setDoubleProperty("volume", quote.getVolume());

            message.setStringProperty("changeFactor", changeFactor.toString());
            message.setDoubleProperty("sharesTraded", sharesTraded);
            message.setLongProperty("publishTime", System.currentTimeMillis());
            message.setText("Update Stock price for " + quote.getSymbol() + " old price = " + oldPrice + " new price = " + quote.getPrice());

            msgProducer.send(message);
        } catch (Exception e) {
            throw new EJBException(e.getMessage(), e); // pass the exception back
        } finally {
            try {
                if (conn != null)
                    conn.close();
                if (sess != null)
                    sess.close();
            } catch (javax.jms.JMSException e) {
                throw new EJBException(e.getMessage(), e); // pass the exception back
            }
        }
    }

    private OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding, String orderType, double quantity) {

        OrderDataBean order;

        if (Log.doTrace())
            Log.trace("TradeSLSBBean:createOrder(orderID=" 
                    + " account=" + ((account == null) ? null : account.getAccountID()) 
                    + " quote=" + ((quote == null) ? null : quote.getSymbol()) 
                    + " orderType=" + orderType 
                    + " quantity=" + quantity);
        try {
            order = new OrderDataBean(orderType, "open", new Timestamp(System.currentTimeMillis()), null, quantity, quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND), 
                    TradeConfig.getOrderFee(orderType), account, quote, holding);
            entityManager.persist(order);
        } catch (Exception e) {
            Log.error("TradeSLSBBean:createOrder -- failed to create Order", e);
            throw new EJBException("TradeSLSBBean:createOrder -- failed to create Order", e);
        }
        return order;
    }

    private HoldingDataBean createHolding(AccountDataBean account, QuoteDataBean quote, double quantity, BigDecimal purchasePrice) throws Exception {
        HoldingDataBean newHolding = new HoldingDataBean(quantity, purchasePrice, new Timestamp(System.currentTimeMillis()), account, quote);
        entityManager.persist(newHolding);
        return newHolding;
    }
    
    public double investmentReturn(double investment, double NetValue) throws Exception {
        if (Log.doTrace()) Log.trace("TradeSLSBBean:investmentReturn");
    
        double diff = NetValue - investment;
        double ir = diff / investment;
        return ir;
    }
    
    public QuoteDataBean pingTwoPhase(String symbol) throws Exception {
        try{
            if (Log.doTrace()) Log.trace("TradeSLSBBean:pingTwoPhase", symbol);
            QuoteDataBean quoteData=null;
            Connection conn = null;
            Session sess = null;        
            
            try {

                //Get a Quote and send a JMS message in a 2-phase commit
                quoteData = entityManager.find(QuoteDataBean.class, symbol);

                conn = queueConnectionFactory.createConnection();                        
                sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer msgProducer = sess.createProducer(tradeBrokerQueue);
                TextMessage message = sess.createTextMessage();

                String command= "ping";
                message.setStringProperty("command", command);
                message.setLongProperty("publishTime", System.currentTimeMillis());         
                message.setText("Ping message for queue java:comp/env/jms/TradeBrokerQueue sent from TradeSLSBBean:pingTwoPhase at " + new java.util.Date());

                msgProducer.send(message);  
            } 
            catch (Exception e) {
                Log.error("TradeSLSBBean:pingTwoPhase -- exception caught",e);
            }

            finally {
                if (conn != null)
                    conn.close();   
                if (sess != null)
                    sess.close();
            }           

            return quoteData;
        } catch (Exception e){
            throw new Exception(e.getMessage(),e);
        }
    }

    class quotePriceComparator implements java.util.Comparator {
        public int compare(Object quote1, Object quote2) {
            double change1 = ((QuoteDataBean) quote1).getChange();
            double change2 = ((QuoteDataBean) quote2).getChange();
            return new Double(change2).compareTo(change1);
        }
    }

    @PostConstruct
    public void postConstruct() {
        Log.trace("POST CONSTRUCT");
        Log.trace("updateQuotePrices: " + TradeConfig.getUpdateQuotePrices());
        Log.trace("publishQuotePriceChange: " + TradeConfig.getPublishQuotePriceChange());
    }
}
