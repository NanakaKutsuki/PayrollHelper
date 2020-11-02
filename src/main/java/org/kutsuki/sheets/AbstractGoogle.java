package org.kutsuki.sheets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;

/**
 * Common methods shared by all Helpers.
 * 
 * Adding a new Employee: Update If EITO, update InvoiceHelper Range. Update
 * BonusHelper
 * 
 * @author MatchaGreen
 */
public abstract class AbstractGoogle {
    private static final int PORT = 8888;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "PayrollHelper";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String OFFLINE = "offline";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String USER = "user";

    private NetHttpTransport transport;

    public AbstractGoogle() {
	try {
	    this.transport = GoogleNetHttpTransport.newTrustedTransport();
	} catch (GeneralSecurityException | IOException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    public abstract void run();

    /**
     * Accesses Google Sheets
     * 
     * @return the Google Credential
     * @throws IOException              Errors from Google API
     * @throws GeneralSecurityException Errors from Google API
     */
    public Credential getCredentials() throws IOException {
	// Load client secrets.
	InputStream in = AbstractGoogle.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	if (in == null) {
	    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	}

	GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	List<String> scopes = new ArrayList<String>();
	scopes.add(DocsScopes.DOCUMENTS);
	scopes.add(DriveScopes.DRIVE);
	scopes.add(SheetsScopes.SPREADSHEETS);

	// Build flow and trigger user authorization request.
	GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(getTransport(), getJsonFactory(),
		clientSecrets, scopes).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
			.setAccessType(OFFLINE).build();
	LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(PORT).build();

	return new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER);
    }

    public JsonFactory getJsonFactory() {
	return JSON_FACTORY;
    }

    public NetHttpTransport getTransport() {
	return transport;
    }

    public String getApplicationName() {
	return APPLICATION_NAME;
    }
}
