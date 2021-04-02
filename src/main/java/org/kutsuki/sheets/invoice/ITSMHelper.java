package org.kutsuki.sheets.invoice;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractDocs;

import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.InsertInlineImageRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.ParagraphStyle;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest;

public class ITSMHelper extends AbstractDocs {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String ALIGNMENT = "alignment";
    private static final String END = "end";
    private static final String INVOICE_FROM = "invoice.from";
    private static final String INVOICE_LOGO = "invoice.logo";
    private static final String INVOICE_NUM = "Invoice #: \n\n";
    private static final String INVOICE_PROPERTIES = "invoice.properties";
    private static final String INVOICE_TO = "invoice.to";
    private static final String PT = "PT";

    private LocalDate date;
    private String from;
    private String logo;
    private String to;

    public ITSMHelper() {
	try {
	    Properties prop = new Properties();
	    prop.load(ITSMHelper.class.getClassLoader().getResourceAsStream(INVOICE_PROPERTIES));

	    this.from = prop.getProperty(INVOICE_FROM);
	    this.logo = prop.getProperty(INVOICE_LOGO);
	    this.to = prop.getProperty(INVOICE_TO);
	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}

	this.date = LocalDate.now();
	if (this.date.getDayOfMonth() > 20) {
	    this.date = this.date.plusMonths(1).withDayOfMonth(1);
	}
    }

    @Override
    public void run() {
	String folderId = createFolder(date.minusMonths(1).getMonthValue());
	createInvoice("TTO-14-2021", folderId);
	createInvoice("TTO-16-2021", folderId);
	createInvoice("TTO-20-2021", folderId);
	createInvoice("TTO-21-2021", folderId);
	createInvoice("TTO-26-2021", folderId);
    }

    public void createInvoice(String title, String folderId) {
	String id = createDoc(title);
	moveFile(id, folderId);

	List<Request> requestList = new ArrayList<Request>();
	requestList.add(new Request().setInsertInlineImage(
		new InsertInlineImageRequest().setUri(logo).setLocation(new Location().setIndex(1))
			.setObjectSize(new Size().setHeight(new Dimension().setMagnitude(80.7).setUnit(PT))
				.setWidth(new Dimension().setMagnitude(187.2).setUnit(PT)))));

	requestList.add(new Request().setInsertText(
		new InsertTextRequest().setText(StringUtils.LF + DTF.format(date).toString() + StringUtils.LF)
			.setLocation(new Location().setIndex(2))));

	requestList.add(new Request()
		.setInsertText(new InsertTextRequest().setText(INVOICE_NUM).setLocation(new Location().setIndex(14))));

	requestList.add(new Request()
		.setInsertText(new InsertTextRequest().setText(from).setLocation(new Location().setIndex(26))));

	requestList.add(new Request()
		.setInsertText(new InsertTextRequest().setText(to).setLocation(new Location().setIndex(95))));

	requestList.add(new Request().setUpdateParagraphStyle(
		new UpdateParagraphStyleRequest().setRange(new Range().setStartIndex(3).setEndIndex(25))
			.setParagraphStyle(new ParagraphStyle().setAlignment(END)).setFields(ALIGNMENT)));

	batchUpdate(id, requestList);
    }

    public static void main(String[] args) {
	ITSMHelper helper = new ITSMHelper();
	helper.run();
    }
}
