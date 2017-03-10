package org.jboss.demo.ejbrest.rest;


import org.apache.geronimo.daytrader.javaee6.entities.OrderDataBean;
import org.apache.geronimo.daytrader.javaee6.web.ejb3.DirectSLSBRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;


@Path("/api/daytrader")
public class DayTraderEndpoint {

	/**
	 * Simple REST endpoint that executes a trade by calling a remote EJB.
	 * @param userId DayTrader user ID
	 * @param symbol Stock ticker symbol to buy
	 * @param quantity quantity of shares to buy
	 * @return Completed order object
	 * @throws Exception If things go wrong
	 */
	@POST
	@Path("/buy/{userId}/{symbol}/{quantity}")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderDataBean buy(@PathParam("userId") String userId,
							 @PathParam("symbol") String symbol,
							 @PathParam("quantity") int quantity) throws Exception {

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
		trader.login(userId, "xxx");

		return trader.buy(userId, symbol, quantity, 0);
	}



}