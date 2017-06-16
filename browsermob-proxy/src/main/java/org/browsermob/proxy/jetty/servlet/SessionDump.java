// ========================================================================
// $Id: SessionDump.java,v 1.15 2005/08/13 00:01:28 gregwilkins Exp $
// Copyright 1996-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.browsermob.proxy.jetty.servlet;

import org.apache.commons.logging.Log;
import org.browsermob.proxy.jetty.html.Page;
import org.browsermob.proxy.jetty.html.TableForm;
import org.browsermob.proxy.jetty.log.LogFactory;
import org.browsermob.proxy.jetty.util.LogSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;

// TODO: Auto-generated Javadoc
/* ------------------------------------------------------------ */
/**
 * Test Servlet Sessions.
 * 
 * @version $Revision: 1.15 $
 * @author Greg Wilkins (gregw)
 */
public class SessionDump extends HttpServlet {

	/** The log. */
	private static Log log = LogFactory.getLog(SessionDump.class);

	/** The redirect count. */
	int redirectCount = 0;
	/* ------------------------------------------------------------ */
	/** The page type. */
	String pageType;

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String action = request.getParameter("Action");
		String name = request.getParameter("Name");
		String value = request.getParameter("Value");
		String age = request.getParameter("MaxAge");

		String nextUrl = getURI(request) + "?R=" + redirectCount++;
		if (action.equals("New Session")) {
			session = request.getSession(true);
		} else if (session != null) {
			if (action.equals("Invalidate"))
				session.invalidate();
			else if (action.equals("Set")) {
				session.setAttribute(name, value);
				try {
					int m = Integer.parseInt(age);
					session.setMaxInactiveInterval(m);
				} catch (Exception e) {
					LogSupport.ignore(log, e);
				}
			} else if (action.equals("Remove"))
				session.removeAttribute(name);
		}

		String encodedUrl = response.encodeRedirectURL(nextUrl);
		response.sendRedirect(encodedUrl);

	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		Page page = new Page();

		HttpSession session = request
				.getSession(getURI(request).indexOf("new") > 0);

		page.title("Session Dump Servlet: ");

		TableForm tf = new TableForm(response.encodeURL(getURI(request)));
		tf.method("POST");

		if (session == null) {
			page.add("<H1>No Session</H1>");
			tf.addButton("Action", "New Session");
		} else {
			try {
				tf.addText("ID", session.getId());
				tf.addText("State", session.isNew() ? "NEW" : "Valid");
				tf.addText("Creation",
						new Date(session.getCreationTime()).toString());
				tf.addText("Last Access",
						new Date(session.getLastAccessedTime()).toString());
				tf.addText("Max Inactive",
						"" + session.getMaxInactiveInterval());

				tf.addText("Context", "" + session.getServletContext());

				Enumeration keys = session.getAttributeNames();
				while (keys.hasMoreElements()) {
					String name = (String) keys.nextElement();
					String value = session.getAttribute(name).toString();
					tf.addText(name, value);
				}

				tf.addTextField("Name", "Property Name", 20, "name");
				tf.addTextField("Value", "Property Value", 20, "value");
				tf.addTextField("MaxAge", "MaxAge(s)", 5, "");
				tf.addButtonArea();
				tf.addButton("Action", "Set");
				tf.addButton("Action", "Remove");
				tf.addButton("Action", "Invalidate");

				page.add(tf);
				tf = null;
				if (request.isRequestedSessionIdFromCookie())
					page.add("<P>Turn off cookies in your browser to try url encoding<BR>");

				if (request.isRequestedSessionIdFromURL())
					page.add("<P>Turn on cookies in your browser to try cookie encoding<BR>");

			} catch (IllegalStateException e) {
				log.debug(LogSupport.EXCEPTION, e);
				page.add("<H1>INVALID Session</H1>");
				tf = new TableForm(getURI(request));
				tf.addButton("Action", "New Session");
			}
		}

		if (tf != null)
			page.add(tf);

		Writer writer = response.getWriter();
		page.write(writer);
		writer.flush();
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#getServletInfo()
	 */
	public String getServletInfo() {
		return "Session Dump Servlet";
	}

	/* ------------------------------------------------------------ */
	/**
	 * Gets the uri.
	 * 
	 * @param request
	 *            the request
	 * @return the uri
	 */
	private String getURI(HttpServletRequest request) {
		String uri = (String) request
				.getAttribute("javax.servlet.forward.request_uri");
		if (uri == null)
			uri = request.getRequestURI();
		return uri;
	}

}
