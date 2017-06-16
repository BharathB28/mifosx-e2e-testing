// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * An implementation of Resolver that sends one query to one server.
 * SimpleResolver handles TCP retries, transaction security (TSIG), and EDNS 0.
 * 
 * @see Resolver
 * @see TSIG
 * @see OPTRecord
 * 
 * @author Brian Wellington
 */

public class SimpleResolver implements Resolver {

	/** The default port to send queries to. */
	public static final int DEFAULT_PORT = 53;

	/** The default EDNS payload size. */
	public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;

	/** The address. */
	private InetSocketAddress address;

	/** The local address. */
	private InetSocketAddress localAddress;

	/** The ignore truncation. */
	private boolean useTCP, ignoreTruncation;

	/** The query opt. */
	private OPTRecord queryOPT;

	/** The tsig. */
	private TSIG tsig;

	/** The timeout value. */
	private long timeoutValue = 10 * 1000;

	/** The Constant DEFAULT_UDPSIZE. */
	private static final short DEFAULT_UDPSIZE = 512;

	/** The default resolver. */
	private static String defaultResolver = "localhost";

	/** The unique id. */
	private static int uniqueID = 0;

	/**
	 * Creates a SimpleResolver that will query the specified host.
	 * 
	 * @param hostname
	 *            the hostname
	 * @throws UnknownHostException
	 *             Failure occurred while finding the host
	 */
	public SimpleResolver(String hostname) throws UnknownHostException {
		if (hostname == null) {
			hostname = ResolverConfig.getCurrentConfig().server();
			if (hostname == null)
				hostname = defaultResolver;
		}
		InetAddress addr;
		if (hostname.equals("0"))
			addr = InetAddress.getLocalHost();
		else
			addr = InetAddress.getByName(hostname);
		address = new InetSocketAddress(addr, DEFAULT_PORT);
	}

	/**
	 * Creates a SimpleResolver. The host to query is either found by using
	 * ResolverConfig, or the default host is used.
	 * 
	 * @throws UnknownHostException
	 *             Failure occurred while finding the host
	 * @see ResolverConfig
	 */
	public SimpleResolver() throws UnknownHostException {
		this(null);
	}

	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	InetSocketAddress getAddress() {
		return address;
	}

	/**
	 * Sets the default host (initially localhost) to query.
	 * 
	 * @param hostname
	 *            the new default resolver
	 */
	public static void setDefaultResolver(String hostname) {
		defaultResolver = hostname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setPort(int)
	 */
	public void setPort(int port) {
		address = new InetSocketAddress(address.getAddress(), port);
	}

	/**
	 * Sets the address of the server to communicate with.
	 * 
	 * @param addr
	 *            The address of the DNS server
	 */
	public void setAddress(InetSocketAddress addr) {
		address = addr;
	}

	/**
	 * Sets the address of the server to communicate with (on the default DNS
	 * port).
	 * 
	 * @param addr
	 *            The address of the DNS server
	 */
	public void setAddress(InetAddress addr) {
		address = new InetSocketAddress(addr, address.getPort());
	}

	/**
	 * Sets the local address to bind to when sending messages.
	 * 
	 * @param addr
	 *            The local address to send messages from.
	 */
	public void setLocalAddress(InetSocketAddress addr) {
		localAddress = addr;
	}

	/**
	 * Sets the local address to bind to when sending messages. A random port
	 * will be used.
	 * 
	 * @param addr
	 *            The local address to send messages from.
	 */
	public void setLocalAddress(InetAddress addr) {
		localAddress = new InetSocketAddress(addr, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setTCP(boolean)
	 */
	public void setTCP(boolean flag) {
		this.useTCP = flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setIgnoreTruncation(boolean)
	 */
	public void setIgnoreTruncation(boolean flag) {
		this.ignoreTruncation = flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setEDNS(int, int, int, java.util.List)
	 */
	public void setEDNS(int level, int payloadSize, int flags, List options) {
		if (level != 0 && level != -1)
			throw new IllegalArgumentException("invalid EDNS level - "
					+ "must be 0 or -1");
		if (payloadSize == 0)
			payloadSize = DEFAULT_EDNS_PAYLOADSIZE;
		queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setEDNS(int)
	 */
	public void setEDNS(int level) {
		setEDNS(level, 0, 0, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setTSIGKey(org.xbill.DNS.TSIG)
	 */
	public void setTSIGKey(TSIG key) {
		tsig = key;
	}

	/**
	 * Gets the tSIG key.
	 * 
	 * @return the tSIG key
	 */
	TSIG getTSIGKey() {
		return tsig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setTimeout(int, int)
	 */
	public void setTimeout(int secs, int msecs) {
		timeoutValue = (long) secs * 1000 + msecs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xbill.DNS.Resolver#setTimeout(int)
	 */
	public void setTimeout(int secs) {
		setTimeout(secs, 0);
	}

	/**
	 * Gets the timeout.
	 * 
	 * @return the timeout
	 */
	long getTimeout() {
		return timeoutValue;
	}

	/**
	 * Parses the message.
	 * 
	 * @param b
	 *            the b
	 * @return the message
	 * @throws WireParseException
	 *             the wire parse exception
	 */
	private Message parseMessage(byte[] b) throws WireParseException {
		try {
			return (new Message(b));
		} catch (IOException e) {
			if (Options.check("verbose"))
				e.printStackTrace();
			if (!(e instanceof WireParseException))
				e = new WireParseException("Error parsing message");
			throw (WireParseException) e;
		}
	}

	/**
	 * Verify tsig.
	 * 
	 * @param query
	 *            the query
	 * @param response
	 *            the response
	 * @param b
	 *            the b
	 * @param tsig
	 *            the tsig
	 */
	private void verifyTSIG(Message query, Message response, byte[] b, TSIG tsig) {
		if (tsig == null)
			return;
		int error = tsig.verify(response, b, query.getTSIG());
		if (Options.check("verbose"))
			System.err.println("TSIG verify: " + Rcode.string(error));
	}

	/**
	 * Apply edns.
	 * 
	 * @param query
	 *            the query
	 */
	private void applyEDNS(Message query) {
		if (queryOPT == null || query.getOPT() != null)
			return;
		query.addRecord(queryOPT, Section.ADDITIONAL);
	}

	/**
	 * Max udp size.
	 * 
	 * @param query
	 *            the query
	 * @return the int
	 */
	private int maxUDPSize(Message query) {
		OPTRecord opt = query.getOPT();
		if (opt == null)
			return DEFAULT_UDPSIZE;
		else
			return opt.getPayloadSize();
	}

	/**
	 * Sends a message to a single server and waits for a response. No checking
	 * is done to ensure that the response is associated with the query.
	 * 
	 * @param query
	 *            The query to send.
	 * @return The response.
	 * @throws IOException
	 *             An error occurred while sending or receiving.
	 */
	public Message send(Message query) throws IOException {
		if (Options.check("verbose"))
			System.err.println("Sending to "
					+ address.getAddress().getHostAddress() + ":"
					+ address.getPort());

		if (query.getHeader().getOpcode() == Opcode.QUERY) {
			Record question = query.getQuestion();
			if (question != null && question.getType() == Type.AXFR)
				return sendAXFR(query);
		}

		query = (Message) query.clone();
		applyEDNS(query);
		if (tsig != null)
			tsig.apply(query, null);

		byte[] out = query.toWire(Message.MAXLENGTH);
		int udpSize = maxUDPSize(query);
		boolean tcp = false;
		long endTime = System.currentTimeMillis() + timeoutValue;
		do {
			byte[] in;

			if (useTCP || out.length > udpSize)
				tcp = true;
			if (tcp)
				in = TCPClient.sendrecv(localAddress, address, out, endTime);
			else
				in = UDPClient.sendrecv(localAddress, address, out, udpSize,
						endTime);

			/*
			 * Check that the response is long enough.
			 */
			if (in.length < Header.LENGTH) {
				throw new WireParseException("invalid DNS header - "
						+ "too short");
			}
			/*
			 * Check that the response ID matches the query ID. We want to check
			 * this before actually parsing the message, so that if there's a
			 * malformed response that's not ours, it doesn't confuse us.
			 */
			int id = ((in[0] & 0xFF) << 8) + (in[1] & 0xFF);
			int qid = query.getHeader().getID();
			if (id != qid) {
				String error = "invalid message id: expected " + qid
						+ "; got id " + id;
				if (tcp) {
					throw new WireParseException(error);
				} else {
					if (Options.check("verbose")) {
						System.err.println(error);
					}
					continue;
				}
			}
			Message response = parseMessage(in);
			verifyTSIG(query, response, in, tsig);
			if (!tcp && !ignoreTruncation
					&& response.getHeader().getFlag(Flags.TC)) {
				tcp = true;
				continue;
			}
			return response;
		} while (true);
	}

	/**
	 * Asynchronously sends a message to a single server, registering a listener
	 * to receive a callback on success or exception. Multiple asynchronous
	 * lookups can be performed in parallel. Since the callback may be invoked
	 * before the function returns, external synchronization is necessary.
	 * 
	 * @param query
	 *            The query to send
	 * @param listener
	 *            The object containing the callbacks.
	 * @return An identifier, which is also a parameter in the callback
	 */
	public Object sendAsync(final Message query, final ResolverListener listener) {
		final Object id;
		synchronized (this) {
			id = new Integer(uniqueID++);
		}
		Record question = query.getQuestion();
		String qname;
		if (question != null)
			qname = question.getName().toString();
		else
			qname = "(none)";
		String name = this.getClass() + ": " + qname;
		Thread thread = new ResolveThread(this, query, id, listener);
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
		return id;
	}

	/**
	 * Send axfr.
	 * 
	 * @param query
	 *            the query
	 * @return the message
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Message sendAXFR(Message query) throws IOException {
		Name qname = query.getQuestion().getName();
		ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(qname, address, tsig);
		xfrin.setTimeout((int) (getTimeout() / 1000));
		xfrin.setLocalAddress(localAddress);
		try {
			xfrin.run();
		} catch (ZoneTransferException e) {
			throw new WireParseException(e.getMessage());
		}
		List records = xfrin.getAXFR();
		Message response = new Message(query.getHeader().getID());
		response.getHeader().setFlag(Flags.AA);
		response.getHeader().setFlag(Flags.QR);
		response.addRecord(query.getQuestion(), Section.QUESTION);
		Iterator it = records.iterator();
		while (it.hasNext())
			response.addRecord((Record) it.next(), Section.ANSWER);
		return response;
	}

}
