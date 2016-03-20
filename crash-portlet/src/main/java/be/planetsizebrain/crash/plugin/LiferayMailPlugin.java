package be.planetsizebrain.crash.plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.*;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.util.Utils;

import com.google.common.base.Strings;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.util.PropsValues;

@Named
public class LiferayMailPlugin extends CRaSHPlugin<LiferayMailPlugin> {

	public static PropertyDescriptor<String> SMTP_FROM = new PropertyDescriptor<String>(String.class, "mail.smtp.from", null, "The mail sender address") {
		@Override
		protected String doParse(String smtpFrom) throws Exception {
			return smtpFrom;
		}
	};

	@Override
	protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
		return Utils.<PropertyDescriptor<?>>list(SMTP_FROM);
	}

	private String smtpFrom;

	@Override
	public LiferayMailPlugin getImplementation() {
		return this;
	}

	@Override
	public void init() {
		smtpFrom = getContext().getProperty(SMTP_FROM);
		if (Strings.isNullOrEmpty(smtpFrom)) {
			smtpFrom = PropsValues.ADMIN_EMAIL_FROM_ADDRESS;
		}
	}

	public Future<Boolean> send(
			Iterable<String> recipients,
			final String subject,
			final String body,
			final DataSource... attachments) throws MessagingException {
		return send(recipients, subject, body, null, attachments);
	}

	public Future<Boolean> send(
			Iterable<String> recipients,
			final String subject,
			final Object body,
			final String type,
			final DataSource... attachments) throws MessagingException {

		final InternetAddress[] addresses = InternetAddress.parse(Utils.join(recipients, ","));

		Callable<Boolean> f = (new Callable<Boolean>() {
			public Boolean call() throws Exception {
				try {
					Session session = InfrastructureUtil.getMailSession();
					MimeMessage message = new MimeMessage(session);

					if (smtpFrom != null) {
						message.setFrom(new InternetAddress(smtpFrom));
					}

					message.setRecipients(Message.RecipientType.TO, addresses);
					if (subject != null) {
						message.setSubject(subject);
					}

					MimePart bodyPart;
					if (attachments != null && attachments.length > 0) {
						Multipart multipart = new MimeMultipart();
						bodyPart = new MimeBodyPart();
						bodyPart.setContent(body, type);
						for (DataSource attachment : attachments) {
							MimeBodyPart attachmentPart = new MimeBodyPart();
							attachmentPart.setDataHandler(new DataHandler(attachment));
							attachmentPart.setFileName(attachment.getName());
							multipart.addBodyPart(attachmentPart);
						}
						message.setContent(multipart);
					} else {
						bodyPart = message;
					}

					if (type != null) {
						bodyPart.setContent(body, type);
					} else {
						bodyPart.setText(body.toString());
					}

					try {
						Transport.send(message);
					} catch (AuthenticationFailedException e) {
						return false;
					}
					return true;
				} catch (Exception e) {
					// TODO
					System.out.println("EXCEPTION");
					e.printStackTrace();
					System.out.println("EXCEPTION");
					throw e;
				}
			}
		});

		return getContext().getExecutor().submit(f);
	}
}