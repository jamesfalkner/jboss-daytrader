package org.jboss.demo.ejbrest.rest;


import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;
import org.apache.geronimo.daytrader.javaee6.web.ejb3.DirectSLSBRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;


@Path("/api/daytrader")
public class DayTraderEndpoint {

	/**
	 * Simple REST endpoint that executes a trade by calling a remote EJB.
	 * @param order The details of the action to take (buy/sell, quantity, etc)
	 * @return Completed order object
	 * @throws Exception If things go wrong
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public OrderDataBean trade(Order order) throws Exception {

		Properties prop = new Properties();
		prop.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		prop.put(Context.PROVIDER_URL, "http-remoting://web:8080");
		prop.put(Context.SECURITY_PRINCIPAL, "ejbuser");
		prop.put(Context.SECURITY_CREDENTIALS, "ejbuser1!");
		prop.put("jboss.naming.client.ejb.context", true);

		Context context = new InitialContext(prop);

		final DirectSLSBRemote trader = (DirectSLSBRemote) context.lookup("/daytrader-ear-3.0-SNAPSHOT/web//DirectSLSBBean!"
				+ DirectSLSBRemote.class.getName());

		// invoke on the remote service
		System.out.println("logging in and buying shares");
		trader.login("uid:0", "xxx");

		if ("buy".equalsIgnoreCase(order.getAction())) {
			return trader.buy("uid:0", order.getTicker(), order.getQuantity(), 0);
		} else if ("sell".equalsIgnoreCase(order.getAction())) {
			return trader.sell("uid:0", 2000 , 0);
		} else {
			throw new UnsupportedOperationException("Invalid action: " + order.getAction());
		}
	}


	@XmlRootElement
	class Order {
		private int limit;
		private String ticker;
		private int quantity;
		private String action;
		private String expiration;

		public Order() {

		}

		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}

		public String getTicker() {
			return ticker;
		}

		public void setTicker(String ticker) {
			this.ticker = ticker;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getExpiration() {
			return expiration;
		}

		public void setExpiration(String expiration) {
			this.expiration = expiration;
		}
	}
}

