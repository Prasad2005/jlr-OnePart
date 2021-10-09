package com.exalead.customers.jlr.onepart.access.writers;

import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.exalead.access.feedapi.Entry;
import com.exalead.access.feedapi.ResultFeed;
import com.exalead.cv360.customcomponents.CustomComponent;
import com.exalead.cv360.service.api.ServiceResponseSerializer;

/**
 * Custom ResultFeed serializer (can be used to override stantard serializers
 * (atom and JSON) and plug other systems on top of Access API)
 * 
 * @author sguillom
 * 
 */
@CustomComponent(displayName = "jlr-onepart Custom Serializer", description = "jlr-onepart Custom result feed serializer (not atom)")
public class CustomResponseSerializer implements ServiceResponseSerializer {
	/** LOGGER */
	private static final Logger LOGGER = Logger.getLogger(CustomResponseSerializer.class);

	/**
	 * Constructor
	 */
	public CustomResponseSerializer() {
		// Nothing
	}

	/**
	 * Get supported formats of this serializer
	 * 
	 * @return Supported formats table
	 */
	@Override
	public String[] getSupportedFormat() {
		return new String[] { "jlr-onepart" };
	}

	/**
	 * Serialize result feed into output stream
	 * 
	 * @param pOutputStream
	 *            Output stream
	 * @param pResultFeed
	 *            Result feed
	 * @throws Exception
	 *             Exception
	 */
	public void serializeFeed(OutputStream pOutputStream, ResultFeed pResultFeed) throws Exception {
		Entry rootEntry = pResultFeed.getEntries().get(0);

		StringBuffer sb = new StringBuffer();

		sb.append("================ PAGE ");
		sb.append(rootEntry.getId());
		sb.append(" ================\n");

		if (rootEntry.getInfos() != null) {
			for (String key : rootEntry.getInfos().keySet()) {
				sb.append("INFO --> ");
				sb.append(key);
				sb.append(" = ");
				sb.append(rootEntry.getInfos().get(key));
				sb.append("\n");
			}
		}

		sb.append("\n\n");

		for (ResultFeed subfeed : rootEntry.getSubfeeds()) {
			sb.append("\n================ FEED ");
			sb.append(subfeed.getId());
			sb.append(" ================\n");

			for (Entry entry : subfeed.getEntries()) {
				sb.append("================ ENTRY ");
				sb.append(entry.getEntryUri());
				sb.append(" ================\n");

				for (String metaName : entry.getMetas().keySet()) {
					sb.append("META --> ");
					sb.append(metaName);
					sb.append(" = ");
					sb.append(entry.getMetas().get(metaName).stringValue());
					sb.append("\n");
				}
			}
		}

		pOutputStream.write(sb.toString().getBytes(Charset.forName("UTF-8")));
		pOutputStream.flush();
	}

	/**
	 * Method called to serialize Access API response into HTTP response, so it
	 * is possible to access directly the output stream and write XML, JSON,
	 * text ... in it
	 * 
	 * @param <T>
	 *            Result object
	 * @param format
	 *            Format
	 * @param request
	 *            HTTP request
	 * @param response
	 *            HTTP response
	 * @param message
	 *            Result feed object
	 * @throws Exception
	 *             Exception
	 */
	@Override
	public <T> void serialize(String format, HttpServletRequest request, HttpServletResponse response, T message)
			throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		if (message != null) {
			LOGGER.debug("Message class [" + message.getClass().getName() + "]");
			if (message instanceof ResultFeed) {
				serializeFeed(response.getOutputStream(), (ResultFeed) message);
			} else {
				throw new Exception("Troubleshooting Response Serializer only supports type ResultFeed (current class ["
						+ message.getClass().getName() + "])");
			}
		} else {
			throw new Exception("Result feed is null !!");
		}
	}
}
