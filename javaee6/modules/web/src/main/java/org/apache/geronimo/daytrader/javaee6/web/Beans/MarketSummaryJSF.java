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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import org.apache.geronimo.daytrader.javaee6.core.beans.MarketSummaryDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.QuoteDataBean;

import org.apache.geronimo.daytrader.javaee6.web.TradeAction;

@ManagedBean(name="marketdata")
public class MarketSummaryJSF {    
    private BigDecimal     TSIA; 
    private BigDecimal     openTSIA; 
    private double      volume;  
    private QuoteData[]     topGainers; 
    private QuoteData[]     topLosers;   
    private Date            summaryDate; 
   
    //cache the gainPercent once computed for this bean
    private BigDecimal  gainPercent = null;

    public MarketSummaryJSF(){
        getMarketSummary();
    }
    
    public void getMarketSummary(){
        TradeAction tAction = new TradeAction();
        try {
        /*FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);    
        String userID = (String)session.getAttribute("uidBean");    */    
        MarketSummaryDataBean marketSummaryData = tAction.getMarketSummary();
        setSummaryDate(marketSummaryData.getSummaryDate());
        setTSIA(marketSummaryData.getTSIA());
        setVolume(marketSummaryData.getVolume());
        setGainPercent(marketSummaryData.getGainPercent());
        
        Collection topGainers = marketSummaryData.getTopGainers();    
        System.out.println("Top gainers" + topGainers.size());
        Iterator gainers = topGainers.iterator();
        int count=0;
        QuoteData[] gainerjsfs = new QuoteData[6];
        while (gainers.hasNext() && (count++ < 5))
        {
            QuoteDataBean quote = (QuoteDataBean) gainers.next();
            QuoteData r = new QuoteData(quote.getPrice(), quote.getOpen(), quote.getSymbol());
            gainerjsfs[count] = r;
        }
               
        setTopGainers(gainerjsfs);
        
        Collection topLosers = marketSummaryData.getTopLosers();
        System.out.println("Top losers" + topLosers.size());
        
        QuoteData[] loserjsfs = new QuoteData[6];
        count = 0;
        Iterator losers = topLosers.iterator();
        while (losers.hasNext() && (count++ < 5))
        {
            QuoteDataBean quote = (QuoteDataBean) losers.next();
            QuoteData r = new QuoteData(quote.getPrice(), quote.getOpen(), quote.getSymbol());
            loserjsfs[count] = r;
        }
        setTopLosers(loserjsfs);
        
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setTSIA(BigDecimal tSIA) {
        TSIA = tSIA;
    }

    public BigDecimal getTSIA() {
        return TSIA;
    }

    public void setOpenTSIA(BigDecimal openTSIA) {
        this.openTSIA = openTSIA;
    }

    public BigDecimal getOpenTSIA() {
        return openTSIA;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getVolume() {
        return volume;
    }

    public void setTopGainers(QuoteData[] topGainers) {
        this.topGainers = topGainers;
    }

    public QuoteData[] getTopGainers() {
        return topGainers;
    }

    public void setTopLosers(QuoteData[] topLosers) {
        this.topLosers = topLosers;
    }

    public QuoteData[] getTopLosers() {
        return topLosers;
    }

    public void setSummaryDate(Date summaryDate) {
        this.summaryDate = summaryDate;
    }

    public Date getSummaryDate() {
        return summaryDate;
    }
    
    public void setGainPercent(BigDecimal gainPercent) {
        this.gainPercent = gainPercent;
    }

    public BigDecimal getGainPercent() {
        return gainPercent;
    }
}
