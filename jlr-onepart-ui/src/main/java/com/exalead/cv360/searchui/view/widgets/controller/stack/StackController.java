package com.exalead.cv360.searchui.view.widgets.controller.stack;

import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.exalead.access.client.AccessClient;
import com.exalead.access.client.AccessClientFactory;
import com.exalead.cv360.config.CM;
import com.exalead.cv360.config.NotificationListener;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.searchui.config.MashupConfigurationProvider;
import com.exalead.cv360.searchui.configuration.v10.MashupUI;
import com.exalead.cv360.searchui.view.widgets.controller.export.ExportController;
import com.exalead.cv360.service.messages.Notification;
import com.exalead.cv360.service.messages.NotifyConfigurationUpdate;
import java.net.URLClassLoader;

@CustomComponent(displayName = "Stacks controller", description = "Log classLoader information and java stack")
@Controller
public class StackController implements NotificationListener, MessageSourceAware {

	private static final Logger logger = Logger.getLogger(ExportController.class);
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private MashupUI mashupUI = null;
	private final AccessClient accessClient = AccessClientFactory.createAccessClient(MashupConfigurationProvider
			.getInstance().getApplicationId());
	private MessageSource messageSource;

	public StackController() throws Exception {
		this.init();
		CM.registerNotificationListener(this);
	}

	private void init() throws Exception {
		this.lock.writeLock().lock();
		try {
			this.mashupUI = MashupConfigurationProvider.getInstance().loadApplicationConfiguration(MashupUI.class);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void handleNotification(Notification notification) throws Exception {
		if (notification instanceof NotifyConfigurationUpdate) {
			if (((NotifyConfigurationUpdate) notification).isUpdated(MashupConfigurationProvider.getInstance()
					.getApplicationId(), MashupUI.class)) {
				this.init();
			}
		}
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@RequestMapping("/stack")
	public void classLoader(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");

		try {
			Enumeration<URL> e = null;

			ClassLoader applicationClassLoader = null;
			if (request.getParameter("classname") != null) {
				response.getOutputStream().write(("Class " + request.getParameter("classname") + "\n\n").getBytes());
				Class<?> clazz = Class.forName(request.getParameter("classname"));
				applicationClassLoader = clazz.getClassLoader();
			} else {
				applicationClassLoader = this.getClass().getClassLoader();
			}

			if (applicationClassLoader == null) {
				applicationClassLoader = ClassLoader.getSystemClassLoader();
			}
			URL[] urls = ((URLClassLoader) applicationClassLoader).getURLs();
			for (int i = 0; i < urls.length; i++) {
				response.getOutputStream().write(("ClassLoader Resource: " + urls[i].getFile() + "\n").getBytes());
			}

			response.getOutputStream().flush();
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
