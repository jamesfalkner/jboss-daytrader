package org.jboss.demo.ejbrest.rest;


import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;
import org.apache.geronimo.daytrader.javaee6.web.ejb3.DirectSLSBRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.Properties;


@Path("/api")
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
	@Path("/daytrader")
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
	
	@GET
	@Path("/health")
	public String health() {
		return "ok";
	}

}

