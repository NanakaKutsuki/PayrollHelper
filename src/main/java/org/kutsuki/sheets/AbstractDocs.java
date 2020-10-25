package org.kutsuki.sheets;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public abstract class AbstractDocs extends AbstractGoogle {
    private static final String FOLDER = "application/vnd.google-apps.folder";
    private static final String ID = "id";
    private static final String ID_PARENTS = "id, parents";
    private static final String PARENTS = "parents";
    private static final String PARENT_ID = "1IRxkLu-flTPe3UZuYuKEnnWLllHS1zUG";

    private Docs docs;
    private Drive drive;

    public AbstractDocs() {
	try {
	    this.docs = new Docs.Builder(getTransport(), getJsonFactory(), getCredentials())
		    .setApplicationName(getApplicationName()).build();
	    this.drive = new Drive.Builder(getTransport(), getJsonFactory(), getCredentials())
		    .setApplicationName(getApplicationName()).build();

	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    public String createFolder(int month) {
	File file = null;

	try {
	    File fileMetadata = new File();
	    fileMetadata.setName(Integer.toString(month));
	    fileMetadata.setParents(Collections.singletonList(PARENT_ID));
	    fileMetadata.setMimeType(FOLDER);

	    file = drive.files().create(fileMetadata).setFields(ID).execute();
	    System.out.println(month + " created!");
	} catch (IOException e) {
	    throw new IllegalArgumentException(e);
	}

	return file.getId();
    }

    public String createDoc(String title) {
	String id = null;

	try {
	    Document doc = new Document().setTitle(title);
	    doc = docs.documents().create(doc).execute();
	    id = doc.getDocumentId();
	    System.out.println(title + " created!");
	} catch (IOException e) {
	    throw new IllegalArgumentException("Failed to create doc: " + title, e);
	}

	return id;
    }

    public void batchUpdate(String id, List<Request> requestList) {
	try {
	    BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requestList);
	    docs.documents().batchUpdate(id, body).execute();
	} catch (IOException e) {
	    throw new IllegalArgumentException("Unable to batch update.", e);
	}
    }

    public void moveFile(String id, String folderId) {
	try {
	    File file = drive.files().get(id).setFields(PARENTS).execute();
	    StringBuilder sb = new StringBuilder();
	    for (String parent : file.getParents()) {
		sb.append(parent);
		sb.append(',');
	    }

	    file = drive.files().update(id, null).setAddParents(folderId).setRemoveParents(sb.toString())
		    .setFields(ID_PARENTS).execute();
	} catch (IOException e) {
	    throw new IllegalArgumentException("Failed move file!", e);
	}
    }
}
