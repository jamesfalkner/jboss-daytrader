package org.jboss.demo.ejbrest.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Order {
	private int limit;
	private String ticker;
	private int quantity;
	private String action;
	private String expiration;

	public Order() {

	}

	public Order(int limit, String ticker, int quantity, String action, String expiration) {
		this.limit = limit;
		this.ticker = ticker;
		this.quantity = quantity;
		this.action = action;
		this.expiration = expiration;
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