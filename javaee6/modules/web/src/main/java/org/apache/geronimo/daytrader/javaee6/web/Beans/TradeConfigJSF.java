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

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.daytrader.javaee6.core.beans.RunStatsDataBean;
import org.apache.geronimo.daytrader.javaee6.utils.Log;
import org.apache.geronimo.daytrader.javaee6.utils.TradeConfig;
import org.apache.geronimo.daytrader.javaee6.web.TradeAction;
import org.apache.geronimo.daytrader.javaee6.web.TradeBuildDB;

@ManagedBean(name = "tradeconfig")
public class TradeConfigJSF {
	private String runtimeMode = "Direct (JDBC)";
	private String JPALayer = "OpenJPA";
	private String orderProcessingMode = "Synchronous";
	private int maxUsers = 500;
	private int maxQuotes = 1000;
	private int marketSummaryInterval = 20;
	private String webInterface = "JSP";
	private int primIterations = 1;
	private String workloadMix = "Standard";
	private boolean publishQuotePriceChange = false;
	private boolean longRun = true;
	private boolean actionTrace = false;
	private boolean trace = false;
	private String[] runtimeModeList = TradeConfig.runTimeModeNames;
	private String[] JPALayerList = TradeConfig.jpaLayerNames;
	private String[] orderProcessingModeList = TradeConfig.orderProcessingModeNames;
	private String[] workloadMixNamesList = TradeConfig.workloadMixNames;
	private String[] webInterfaceList = TradeConfig.webInterfaceNames;
	private String result;

	public void updateConfig() {		
		String currentConfigStr = "\n\n########## Trade configuration update. Current config:\n\n";
		String runTimeModeStr = this.runtimeMode;
		if (runTimeModeStr != null) {
			try {
				for (int i = 0; i < runtimeModeList.length; i++) {
				   if(runTimeModeStr.equals(runtimeModeList[i])){
					   TradeConfig.setRunTimeMode(i);
				   }
				}
			} catch (Exception e) {

				Log.error(
						e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set runtimemode to " + runTimeModeStr,
						"reverting to current value");

			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tRunTimeMode:\t\t"
				+ TradeConfig.runTimeModeNames[TradeConfig.getRunTimeMode()]
				+ "\n";

		/* Add JPA layer choice to avoid some ugly Hibernate bugs */
		String jpaLayerStr = JPALayer;
		if (jpaLayerStr != null) {
			try {
				for (int i = 0; i < JPALayerList.length; i++) {
					   if(jpaLayerStr.equals(JPALayerList[i])){
						   TradeConfig.jpaLayer = i;
					   }
					}
					
			} catch (Exception e) {
				Log.error(e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set JPALayer to " + jpaLayerStr,
						"reverting to current value");

			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tJPALayer:\t\t"
				+ TradeConfig.jpaLayerNames[TradeConfig.jpaLayer] + "\n";

		String orderProcessingModeStr = this.orderProcessingMode;
		if (orderProcessingModeStr != null) {
			try {
				for (int i = 0; i < orderProcessingModeList.length; i++) {
					   if(orderProcessingModeStr.equals(orderProcessingModeList[i])){
						   TradeConfig.orderProcessingMode = i;
					   }
					}					
			} catch (Exception e) {				
				Log.error(
						e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set orderProcessing to "
								+ orderProcessingModeStr,
						"reverting to current value");

			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tOrderProcessingMode:\t"
				+ TradeConfig.orderProcessingModeNames[TradeConfig.orderProcessingMode]
				+ "\n";

		// String accessModeStr = req.getParameter("AcessMode");
		String accessModeStr = "Standard";
		if (accessModeStr != null) {
			try {
				TradeConfig.setAccessMode(0);
			} catch (Exception e) {
				// >>rjm
				Log.error(
						e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set orderProcessing to "
								+ orderProcessingModeStr,
						"reverting to current value");

			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tAcessMode:\t\t"
				+ TradeConfig.accessModeNames[TradeConfig.getAccessMode()]
				+ "\n";

		String workloadMixStr = workloadMix;
		if (workloadMixStr != null) {
			try {
				for (int i = 0; i < workloadMixNamesList.length; i++) {
					   if(workloadMixStr.equals(workloadMixNamesList[i])){
						   TradeConfig.workloadMix = i;
					   }
					}	
					
			} catch (Exception e) {
				Log.error(
						e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set workloadMix to " + workloadMixStr,
						"reverting to current value");
			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tWorkload Mix:\t\t"
				+ TradeConfig.workloadMixNames[TradeConfig.workloadMix] + "\n";

		String webInterfaceStr = webInterface;
		if (webInterfaceStr != null) {
			try {
				for (int i = 0; i < webInterfaceList.length; i++) {
					   if(webInterfaceStr.equals(webInterfaceList[i])){
						   TradeConfig.webInterface = i;
					   }
					}				
			} catch (Exception e) {
				Log.error(
						e,
						"TradeConfigJSF.updateConfig(..): minor exception caught",
						"trying to set WebInterface to " + webInterfaceStr,
						"reverting to current value");

			} // If the value is bad, simply revert to current
		}
		currentConfigStr += "\t\tWeb Interface:\t\t"
				+ TradeConfig.webInterfaceNames[TradeConfig.webInterface]
				+ "\n";

		TradeConfig.setMAX_USERS(maxUsers);			
		TradeConfig.setMAX_QUOTES(maxQuotes);
		
		currentConfigStr += "\t\t#Trade  Users:\t\t"
				+ TradeConfig.getMAX_USERS() + "\n";
		currentConfigStr += "\t\t#Trade Quotes:\t\t"
				+ TradeConfig.getMAX_QUOTES() + "\n";

		
		TradeConfig.setMarketSummaryInterval(marketSummaryInterval);
		
		currentConfigStr += "\t\tMarket Summary Interval:\t\t"
				+ TradeConfig.getMarketSummaryInterval() + "\n";

		TradeConfig.setPrimIterations(primIterations);

		currentConfigStr += "\t\tPrimitive Iterations:\t\t"
				+ TradeConfig.getPrimIterations() + "\n";

		TradeConfig.setPublishQuotePriceChange(publishQuotePriceChange);
		currentConfigStr += "\t\tTradeStreamer MDB Enabled:\t"
				+ TradeConfig.getPublishQuotePriceChange() + "\n";

		Log.setTrace(trace);
		Log.setActionTrace(actionTrace);
		TradeConfig.setLongRun(longRun);
		currentConfigStr += "\t\tLong Run Enabled:\t\t"
				+ TradeConfig.getLongRun() + "\n";

		System.out.println(currentConfigStr);
		setResult("DayTrader Configuration Updated");
	}
	
	public String resetTrade(){
		RunStatsDataBean runStatsData = new RunStatsDataBean();
		TradeConfig currentConfig = new TradeConfig(); 
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        try
        {
            runStatsData = new TradeAction().resetTrade(false); 
            session.setAttribute("runStatsData", runStatsData);
            session.setAttribute("tradeConfig", currentConfig);
            result += "Trade Reset completed successfully";
            
            
        }
        catch (Exception e)
        {
            result += "Trade Reset Error  - see log for details";
            session.setAttribute("result", result);
            Log.error(e,     result);            
        }
        //Go to page stats
        /*getServletConfig().getServletContext()
                .getRequestDispatcher(TradeConfig.getPage(TradeConfig.STATS_PAGE))
                .include(req, resp);    */ 
        return "stats";
	}
	
	public String buildDB(){
		try {
			new TradeBuildDB(new java.io.PrintWriter(System.out), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        result = "DayTrader Database Built - " + TradeConfig.getMAX_USERS() + "users created";
        //Got to config.xhtml
        return "config";
        
	}
	
	public String buildDBTables(){
		try {
			new TradeBuildDB(new java.io.PrintWriter(System.out), FacesContext.getCurrentInstance().getExternalContext().getRealPath("/"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Go to config.xhtml
		return "config";
	}

	public void setRuntimeMode(String runtimeMode) {
		this.runtimeMode = runtimeMode;
	}

	public String getRuntimeMode() {
		return runtimeMode;
	}

	public void setJPALayer(String jPALayer) {
		JPALayer = jPALayer;
	}

	public String getJPALayer() {
		return JPALayer;
	}

	public void setOrderProcessingMode(String orderProcessingMode) {
		this.orderProcessingMode = orderProcessingMode;
	}

	public String getOrderProcessingMode() {
		return orderProcessingMode;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setmaxQuotes(int maxQuotes) {
		this.maxQuotes = maxQuotes;
	}

	public int getMaxQuotes() {
		return maxQuotes;
	}

	public void setMarketSummaryInterval(int marketSummaryInterval) {
		this.marketSummaryInterval = marketSummaryInterval;
	}

	public int getMarketSummaryInterval() {
		return marketSummaryInterval;
	}

	public void setPrimIterations(int primIterations) {
		this.primIterations = primIterations;
	}

	public int getPrimIterations() {
		return primIterations;
	}

	public void setWorkloadMix(String workloadMix) {
		this.workloadMix = workloadMix;
	}

	public String getWorkloadMix() {
		return workloadMix;
	}

	public void setPublishQuotePriceChange(boolean publishQuotePriceChange) {
		this.publishQuotePriceChange = publishQuotePriceChange;
	}

	public boolean isPublishQuotePriceChange() {
		return publishQuotePriceChange;
	}

	public void setLongRun(boolean longRun) {
		this.longRun = longRun;
	}

	public boolean isLongRun() {
		return longRun;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	public boolean isTrace() {
		return trace;
	}

	public void setRuntimeModeList(String[] runtimeModeList) {
		this.runtimeModeList = runtimeModeList;
	}

	public String[] getRuntimeModeList() {
		return runtimeModeList;
	}

	public void setJPALayerList(String[] jPALayerList) {
		JPALayerList = jPALayerList;
	}

	public String[] getJPALayerList() {
		return JPALayerList;
	}

	public void setOrderProcessingModeList(String[] orderProcessingModeList) {
		this.orderProcessingModeList = orderProcessingModeList;
	}

	public String[] getOrderProcessingModeList() {
		return orderProcessingModeList;
	}

	public void setWorkloadMixNamesList(String[] workloadMixNamesList) {
		this.workloadMixNamesList = workloadMixNamesList;
	}

	public String[] getWorkloadMixNamesList() {
		return workloadMixNamesList;
	}

	public void setWebInterface(String webInterface) {
		this.webInterface = webInterface;
	}

	public String getWebInterface() {
		return webInterface;
	}

	public void setWebInterfaceList(String[] webInterfaceList) {
		this.webInterfaceList = webInterfaceList;
	}

	public String[] getWebInterfaceList() {
		return webInterfaceList;
	}

	public void setActionTrace(boolean actionTrace) {
		this.actionTrace = actionTrace;
	}

	public boolean isActionTrace() {
		return actionTrace;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

}
