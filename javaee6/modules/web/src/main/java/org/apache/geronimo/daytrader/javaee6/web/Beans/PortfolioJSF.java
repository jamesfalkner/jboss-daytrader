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
//import java.util.Collection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.daytrader.javaee6.core.direct.FinancialUtils;
import org.apache.geronimo.daytrader.javaee6.entities.HoldingDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;
import org.apache.geronimo.daytrader.javaee6.entities.QuoteDataBean;

import org.apache.geronimo.daytrader.javaee6.utils.TradeConfig;
import org.apache.geronimo.daytrader.javaee6.web.TradeAction;

@ManagedBean(name="portfolio")
public class PortfolioJSF {        
    private BigDecimal balance;
    private BigDecimal openBalance;    
    private Integer numberHoldings;
    private BigDecimal holdingsTotal;
    private BigDecimal sumOfCashHoldings;
    private BigDecimal totalGain = new BigDecimal(0.0);
    private BigDecimal totalValue = new BigDecimal(0.0);
    private BigDecimal totalBasis = new BigDecimal(0.0);
    private BigDecimal totalGainPercent = new BigDecimal(0.0);    
    private ArrayList<HoldingData> holdingDatas;
    private HtmlDataTable dataTable;
   
    
    
    
    public PortfolioJSF(){
        getPortfolio();
    }    
   
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }    

    public void setHoldingsTotal(BigDecimal holdingsTotal) {
        this.holdingsTotal = holdingsTotal;
    }

    public BigDecimal getHoldingsTotal() {
        return holdingsTotal;
    }

    public void setSumOfCashHoldings(BigDecimal sumOfCashHoldings) {
        this.sumOfCashHoldings = sumOfCashHoldings;
    }

    public BigDecimal getSumOfCashHoldings() {
        return sumOfCashHoldings;
    }

    
    public void setNumberHoldings(Integer numberHoldings) {
        this.numberHoldings = numberHoldings;
    }

    public Integer getNumberHoldings() {
        return numberHoldings;
    }
    
    public void getPortfolio(){
        TradeAction tAction = new TradeAction();
        try {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);    
        String userID = (String)session.getAttribute("uidBean");        
        Collection quoteDataBeans = new ArrayList();
        Collection holdingDataBeans = tAction.getHoldings(userID);
        
        numberHoldings = holdingDataBeans.size();
        // Walk through the collection of user holdings and creating a list of quotes
        if (holdingDataBeans.size() > 0) {
            Iterator it = holdingDataBeans.iterator();  
            holdingDatas = new ArrayList<HoldingData>(holdingDataBeans.size());
            //int count = 0;
            while (it.hasNext()) {
                HoldingDataBean holdingData = (HoldingDataBean) it.next();
                QuoteDataBean quoteData = tAction.getQuote(holdingData.getQuoteID());               
               
                BigDecimal basis = holdingData.getPurchasePrice().multiply(new BigDecimal(holdingData.getQuantity()));
                BigDecimal marketValue = quoteData.getPrice().multiply(new BigDecimal(holdingData.getQuantity()));
                totalBasis = totalBasis.add(basis);    
                totalValue = totalValue.add(marketValue);    
                BigDecimal gain = marketValue.subtract(basis);
                totalGain = totalGain.add(gain);
                BigDecimal gainPercent = null;
                if (basis.doubleValue() == 0.0)
                {
                    gainPercent = new BigDecimal(0.0);
                    //Log.error("portfolio.jsp: Holding with zero basis. holdingID="+holdingData.getHoldingID() + " symbol=" + holdingData.getQuoteID() + " purchasePrice=" + holdingData.getPurchasePrice());
                }
                else {
                    gainPercent = marketValue.divide(basis, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(1.0)).multiply(new BigDecimal(100.0)); 
                }
                HoldingData h = new HoldingData();
                h.setHoldingID(holdingData.getHoldingID());
                h.setPurchaseDate(holdingData.getPurchaseDate());
                h.setQuoteID(holdingData.getQuoteID());
                h.setQuantity(holdingData.getQuantity());
                h.setPurchasePrice(holdingData.getPurchasePrice());
                h.setBasis(basis);
                h.setGain(gain);
                h.setMarketValue(marketValue);
                h.setPrice(quoteData.getPrice());               
                holdingDatas.add(h);
                //count++;
            }
                //dataTable
                setTotalGainPercent(FinancialUtils.computeGainPercent(totalValue, totalBasis));
            
        } else {
            //results = results + ".  Your portfolio is empty.";
        }
            } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
        }    

    public void setTotalGain(BigDecimal totalGain) {
        this.totalGain = totalGain;
    }

    public BigDecimal getTotalGain() {
        return totalGain;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalBasis(BigDecimal totalBasis) {
        this.totalBasis = totalBasis;
    }

    public BigDecimal getTotalBasis() {
        return totalBasis;
    }

    public void setHoldingDatas(ArrayList<HoldingData> holdingDatas) {
        this.holdingDatas = holdingDatas;
    }

    public ArrayList<HoldingData> getHoldingDatas() {
        return holdingDatas;
    }    

    public void setTotalGainPercent(BigDecimal totalGainPercent) {
        this.totalGainPercent = totalGainPercent;
    }

    public BigDecimal getTotalGainPercent() {
        return totalGainPercent;
    } 
    public String sell(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);    
        String userID = (String)session.getAttribute("uidBean");    
        TradeAction tAction = new TradeAction();
        OrderDataBean orderDataBean = null;
        HoldingData holdingData = (HoldingData)dataTable.getRowData();
        try {
        	orderDataBean = tAction.sell(userID, holdingData.getHoldingID(), TradeConfig.orderProcessingMode);
            holdingDatas.remove(holdingData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           
        OrderData orderData = new OrderData(orderDataBean.getOrderID(),
				orderDataBean.getOrderStatus(), orderDataBean.getOpenDate(),
				orderDataBean.getCompletionDate(), orderDataBean.getOrderFee(),
				orderDataBean.getOrderType(), orderDataBean.getQuantity(),
				orderDataBean.getSymbol());
        session.setAttribute("orderData", orderData);
        return "sell";
    }

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public HtmlDataTable getDataTable() {
		return dataTable;
	}
}
