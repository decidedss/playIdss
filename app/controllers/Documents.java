package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.File;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.util.ISO9075;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.*;

public class Documents extends Controller {

    /**
     * Check whether the agency has edit permission
     * @return boolean
     * @throws IOException
     */
    public static boolean hasEdit(String username) throws IOException{
        ArrayList<String> groups = User.getPerson(username).getGroups();

        if (groups.contains("GROUP_idss_edit") || groups.contains("GROUP_idss_admin")) {
            return true;
        }
        return false;
    }

    public static Result addtag() throws IOException
    {
        String name = Form.form().bindFromRequest().get("tagname");
        String tagsURL =  Messages.get("ALFRSCO_REST_API_URL")+ "/tag/workspace/SpacesStore"+"?alf_ticket=" + session().get("alf_ticket");
        HttpPost post = new HttpPost(tagsURL);
        StringEntity params = new StringEntity("{\"name\":\""+name+"\"}", "UTF-8");

        try {
            post.setEntity(params);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(post);
            StringBuilder sb = new StringBuilder();
            DataInputStream in = new DataInputStream(response.getEntity().getContent());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            in.close();
            br.close();
            String json = sb.toString();
            JsonNode result = Json.parse(json);

            return ok(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("error adding tag");
    }

    /**
     * Method that uploads the file-document with the assorted properties, tags, categories on the alfresco repository folder
     * @return Renders the result (uploaded document) to the user
     * @throws IOException
     * @maxLength is set here to 100MB (which means that the uploaded document cannot exceed this limit)
     */
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 1000 * 1024 * 1024)
    public static Result savefile() throws IOException {

        if (session().get("userName") != null && session().get("agency") != null) {
            Http.MultipartFormData.FilePart doc = request().body().asMultipartFormData().getFile("document");
            DynamicForm requestData = Form.form().bindFromRequest();

            if (doc != null) {

                String fileName = doc.getFilename();
                String contentType = doc.getContentType();
                java.io.File file = doc.getFile();

                // -------- Get folder where the file will be saved
                Folder folder;
                if (User.isAdmin(session().get("userName"))) {
                    folder = (Folder) Application.ses.getObjectByPath("/Sites/idss/documentLibrary/CommonDocuments");
                } else {
                    folder = (Folder) Application.ses.getObjectByPath("/Sites/idss/documentLibrary/" + session().get("agency").replaceAll(" ", ""));
                }

                // -------- Create OpenCMIS file stream
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                properties.put(PropertyIds.NAME, fileName);
                FileInputStream fin = new FileInputStream(file);
                byte fileContent[] = new byte[(int) file.length()];
                fin.read(fileContent);
                ContentStream contentStream = new ContentStreamImpl(fileName, BigInteger.valueOf(fileContent.length), contentType, new ByteArrayInputStream(fileContent));

                try {
                    Document newDoc = folder.createDocument(properties, contentStream, VersioningState.MAJOR);

                    List<Object> aspects = newDoc.getProperty("cmis:secondaryObjectTypeIds").getValues();
                    Map<String, Object> props = new HashMap<String, Object>();

                    // -------- Tag list
                    ArrayList<String> addedTags = new ArrayList<String>();
                    for (Map.Entry<String, String> entry : requestData.data().entrySet()) {
                        if (entry.getValue().startsWith("workspace://SpacesStore")) {
                            addedTags.add(entry.getValue());
                        }
                    }

                    // All tags in a hashmap
                    HashMap<String, String> tagsMap = new HashMap<>();
                    ItemIterable<QueryResult> tagsQuery = Application.ses.query("SELECT * FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:taggable/*\"')", false);
                    if (tagsQuery != null) {
                        for (QueryResult tag : tagsQuery) {
                            tagsMap.put(tag.getPropertyByQueryName("cmis:name").getFirstValue().toString(), tag.getPropertyValueById("alfcmis:nodeRef").toString());
                        }
                    }

                    // -------- Create OpenCMIS file aspects - tags
                    if (!aspects.contains("P:cm:taggable")) {
                        aspects.add("P:cm:taggable");
                        aspects.add("P:cm:titled");
                        props.put("cmis:secondaryObjectTypeIds", aspects);
                        newDoc.updateProperties(props, false);
                    }

                    // --------- Create OpenCMIS file aspects - categories
                    if (!aspects.contains("P:cm:generalclassifiable")) {
                        aspects.add("P:cm:generalclassifiable");
                        props.put("cmis:secondaryObjectTypeIds", aspects);
                        newDoc.updateProperties(props, false);
                    }

                    // always want the latest version for this
                    if (!newDoc.isLatestVersion()) {
                        newDoc = newDoc.getObjectOfLatestVersion(false);
                    }

                    List<String> catIds = new ArrayList<>();
                    List<String> tagIds = new ArrayList<>();
                    if (Form.form().bindFromRequest().get("categories") != "") {

                        List<String> catList = Arrays.asList(Form.form().bindFromRequest().get("categories").split(","));

                        if (catList.size() > 0 && !catList.get(0).isEmpty()) {
                            for (String c : Arrays.asList(Form.form().bindFromRequest().get("categories").split(","))) {

                                ItemIterable<QueryResult> query = Application.ses.query("select alfcmis:nodeRef from cm:category where cmis:name = '" + c + "'", false);
                                String catId = null;

                                if (query.getTotalNumItems() > 0) {
                                    for (QueryResult q : query) {
                                        catId = q.getPropertyValueById("alfcmis:nodeRef");
                                    }
                                }
                                catIds.add(catId);
                                // At this point add a tag with the same category name
                                if (tagsMap.get(c.toLowerCase()) != null) {
                                    tagIds.add(tagsMap.get(c.toLowerCase()));
                                }
                            }
                            props.put("cm:categories", catIds);
                            props.put("cm:taggable", tagIds);
                        }
                    }

                    // -------- Create OpenCMIS file aspects - dublincore
                    if (!aspects.contains("P:cm:dublincore")) {
                        aspects.add("P:cm:dublincore");
                        props.put("cmis:secondaryObjectTypeIds", aspects);
                        newDoc.updateProperties(props, false);
                    }

                    // -------- OpenCMIS file update properties (dublin core fields)
                    // props.put("cm:taggable", addedTags);
                    props.put("cm:title", Form.form().bindFromRequest().get("title")); // Get title from form
                    props.put("cm:description", Form.form().bindFromRequest().get("description"));
                    props.put("cm:publisher", Form.form().bindFromRequest().get("publisher"));
                    props.put("cm:identifier", Form.form().bindFromRequest().get("number"));
                    props.put("cm:coverage", Form.form().bindFromRequest().get("issuing"));
                    newDoc.updateProperties(props, false);

                    flash("indexing", "The file you just uploaded is being processed, please come back in ~1 minute");
                } catch (CmisContentAlreadyExistsException exists) {
                    flash("contentAlreadyExists", "ERROR! The file already exists!");
                }
                return redirect("/documents");
            } else {
                return ok("No file found!");
            }
        } else {
            return redirect("/login");
        }
    }

    /**
     * Update document details
     */
    public static Result updatefile()  {
        if (session().get("userName") != null && session().get("agency") != null) {
            DynamicForm requestData = Form.form().bindFromRequest();

            OperationContext context = Application.ses.createOperationContext();
            context.setRenditionFilterString("*");
            Document doc = (Document) Application.ses.getObject(Application.ses.createObjectId(requestData.get("id2edit")), context);

            if (doc != null) {

                List<Object> aspects = doc.getProperty("cmis:secondaryObjectTypeIds").getValues();
                if (!aspects.contains("P:cm:taggable") || !aspects.contains("P:cm:titled")) {
                    aspects.add("P:cm:taggable");
                    aspects.add("P:cm:titled");
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put("cmis:secondaryObjectTypeIds", aspects);
                    doc.updateProperties(props, false);
                }

                ArrayList<String> addedTags = new ArrayList<String>();
                for (Map.Entry<String, String> entry : requestData.data().entrySet()) {
                    if (entry.getValue().startsWith("workspace://SpacesStore")) {
                        addedTags.add(entry.getValue());
                    }
                }

                // Add Categories
                if (!aspects.contains("P:cm:generalclassifiable")) {
                    aspects.add("P:cm:generalclassifiable");
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put("cmis:secondaryObjectTypeIds", aspects);
                    doc.updateProperties(props, false);
                }

                // always want the latest version for this
                if (!doc.isLatestVersion()) {
                    doc = doc.getObjectOfLatestVersion(false);
                }

                // All tags in a hashmap
                HashMap<String, String> tagsMap = new HashMap<>();
                ItemIterable<QueryResult> tagsQuery = Application.ses.query("SELECT * FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:taggable/*\"')", false);
                if (tagsQuery != null) {
                    for (QueryResult tag : tagsQuery) {
                        tagsMap.put(tag.getPropertyByQueryName("cmis:name").getFirstValue().toString(), tag.getPropertyValueById("alfcmis:nodeRef").toString());
                    }
                }

                List<String> catIds = new ArrayList<>();
                List<String> tagIds = new ArrayList<>();
                if (Form.form().bindFromRequest().get("categories") != "") {

                    for (String c : Arrays.asList(Form.form().bindFromRequest().get("categories").split(","))) {
                        ItemIterable<QueryResult> query = Application.ses.query("select alfcmis:nodeRef from cm:category where cmis:name = '" + c + "'", false);
                        String catId = null;

                        if (query.getTotalNumItems() > 0) {
                            for (QueryResult q : query) {
                                catId = q.getPropertyValueById("alfcmis:nodeRef");
                            }
                        }
                        catIds.add(catId);
                        // At this point add a tag with the same category name
                        if (tagsMap.get(c.toLowerCase()) != null) {
                            tagIds.add(tagsMap.get(c.toLowerCase()));
                        }
                    }
                }

                Map<String, Object> props = new HashMap<String, Object>();
                props.put("cm:categories", catIds);
                props.put("cm:taggable", tagIds);
                doc.updateProperties(props, false);
                // -------------------------------------------------


                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("cm:title", requestData.get("title"));
                properties.put("cmis:description", requestData.get("description"));
                // properties.put("cm:taggable", addedTags);
                properties.put("cm:publisher", requestData.get("publisher"));
                properties.put("cm:identifier", requestData.get("number"));
                properties.put("cm:coverage", requestData.get("issuing"));

                doc.updateProperties(properties, false);

                return redirect("/documents");
            } else {
                return ok("Error modifying file");
            }
        } else {
            return redirect("/login");
        }
    }

    /**
     * Delete document
     * @return success message
     */
    public static Result delete (String id)  {
        if (session().get("userName") != null && session().get("agency") != null) {
            OperationContext context = Application.ses.createOperationContext();
            context.setRenditionFilterString("*");
            Document doc = (Document) Application.ses.getObject(Application.ses.createObjectId(id), context);
            doc.delete(true);
            return ok("deleted");
        } else {
            return redirect("/login");
        }
    }

    /**
     * Download document
     * @return url to download
     */
    public static Result download(String id)  {
        if (session().get("userName") != null && session().get("agency") != null) {
            OperationContext context = Application.ses.createOperationContext();
            context.setRenditionFilterString("*");
            Document doc = (Document) Application.ses.getObject(Application.ses.createObjectId(id), context);
            return ok(doc.getContentUrl().replace("http://", "http://admin:admin@"));
        } else {
            return redirect("/login");
        }
    }

        public static Result search() throws IOException {
            if (session().get("userName") != null && session().get("agency") != null) {
                String keyword = Form.form().bindFromRequest().get("keyword");
                return redirect("/documents?q=" + URLEncoder.encode(keyword, "UTF-8"));
            } else {
                return redirect("/login");
            }
        }


    /**
     * Listing of all documents related to the user (agency folder & common documents folder)
     * @return list of thumbnails
     * @throws IOException
     */
    public static Result list() throws IOException {

            String thumbnailId = "/assets/images/very-basic-document-icon.png";

            if (Application.ses == null)
                return redirect("/login");

            if (session().get("userName") != null && session().get("agency") != null) {

                Folder agencyFolder = null;
                Folder commonFolder = (Folder) Application.ses.getObjectByPath("/Sites/idss/documentLibrary/CommonDocuments");
                if (!User.isAdmin(session().get("userName"))) {
                    agencyFolder = (Folder) Application.ses.getObjectByPath("/Sites/idss/documentLibrary/" + session().get("agency"));
                }

                if (agencyFolder!=null) {

                    // ------- Get keyword from request
                    String keyword = "";
                    if (request().getQueryString("q") != null) {
                        keyword = request().getQueryString("q");
                    }
                    // ------- Get tag from requestg
                    String selectedTag = "";
                    if (request().getQueryString("tag") != null) {
                        selectedTag = request().getQueryString("tag");
                    }

                    // ------- Get category from request
                    String selectedCategory = "";
                    if (request().getQueryString("category") != null) {
                        selectedCategory = request().getQueryString("category");
                    }

                    ItemIterable<QueryResult> commonQuery = Application.ses.query("SELECT * FROM cmis:document WHERE  IN_TREE('" + commonFolder.getId() + "')", false);
                    ItemIterable<QueryResult> agencyQuery = null;
                    if (!User.isAdmin(session().get("userName"))) {
                        agencyQuery = Application.ses.query("SELECT * FROM cmis:document WHERE  IN_TREE('" + agencyFolder.getId() + "')", false);
                    }

                    if (!keyword.isEmpty()) {
                        commonQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('" + keyword + "') AND IN_TREE('" + commonFolder.getId() + "') ", false);
                        if (!User.isAdmin(session().get("userName"))) {
                            agencyQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('" + keyword + "') AND IN_TREE('" + agencyFolder.getId() + "') ", false);
                        }
                    }

                    if (!selectedTag.isEmpty()) {
                        commonQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('TAG:\"" + selectedTag + "\"') AND IN_TREE('" + commonFolder.getId() + "')", false);
                        if (!User.isAdmin(session().get("userName"))) {
                            agencyQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('TAG:\"" + selectedTag + "\"') AND IN_TREE('" + agencyFolder.getId() + "')", false);
                        }
                    }

                    if (!selectedCategory.isEmpty()) {
                        commonQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('TAG:\"" + selectedCategory + "\"') AND IN_TREE('" + commonFolder.getId() + "')", false);
                        if (!User.isAdmin(session().get("userName"))) {
                            agencyQuery = Application.ses.query("SELECT * FROM cmis:document WHERE CONTAINS ('TAG:\"" + selectedCategory + "\"') AND IN_TREE('" + agencyFolder.getId() + "')", false);
                        }
                    }

                    // ------ Get all Tags
                    ArrayList<QueryResult> tags = new ArrayList<>();
                    ItemIterable<QueryResult> tagsQuery = Application.ses.query("SELECT * FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:taggable/*\"')", false);
                    if (tagsQuery != null) {
                        for (QueryResult tag : tagsQuery) {
                            tags.add(tag);
                        }
                    }

                    // ------ Render list based on user role
                    if (User.isAdmin(session().get("userName"))) { // admin/admin can view only common files
                        return ok(views.html.documents.render(getAlfrescoCategories(), new ArrayList<>(), getFiles(commonQuery, thumbnailId), tags, keyword, User.isAdmin(session().get("userName")), hasEdit(session().get("userName"))));
                    } else {
                        return ok(views.html.documents.render(getAlfrescoCategories(), getFiles(agencyQuery, thumbnailId), getFiles(commonQuery, thumbnailId), tags, keyword, User.isAdmin(session().get("userName")), hasEdit(session().get("userName"))));
                    }
                }
                else {
                    flash("folder", "notfound");
                    return ok(views.html.documents.render(getAlfrescoCategories(), new ArrayList<>(), new ArrayList<>(),  new ArrayList<>(),  "", User.isAdmin(session().get("userName")), hasEdit(session().get("userName"))));
                }
            } else {
                return redirect("/login");
            }

    }

    /**
     * Parse categories in Alfresco
     * @return hashmap of categories
     */
    public static Map<String, ArrayList<String>> getAlfrescoCategories() {
        Map<String, ArrayList<String>> categories = new HashMap<String, ArrayList<String>>();

        if (Application.ses != null) {
            ItemIterable<QueryResult> queryLang = Application.ses.query("SELECT cmis:name FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:generalclassifiable/*\"')", false);

            // Get language category ******************
            if (queryLang.getTotalNumItems() > 0) {
                for (QueryResult ql : queryLang) {
                    if (session().get("lang").equals(ql.getPropertyByQueryName("cmis:name").getFirstValue().toString())) {
                        ItemIterable<QueryResult> queryCategories = Application.ses.query("SELECT cmis:name FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:generalclassifiable/cm:" + session().get("lang") + "/*\"')", false);

                        // Get categories ******************
                        if (queryCategories.getTotalNumItems() > 0) {
                            for (QueryResult qc : queryCategories) {

                                //  Encode path elements with ISO9075.encode method
                                String category = ISO9075.encode(qc.getPropertyByQueryName("cmis:name").getFirstValue().toString());

                                ItemIterable<QueryResult> queryChildren = Application.ses.query("SELECT cmis:name FROM cm:category WHERE CONTAINS('PATH:\"//cm:categoryRoot/cm:generalclassifiable/cm:" + session().get("lang") + "/" + "cm:" + category + "//*\"')", false);

                                // Get categories children ******************
                                ArrayList<String> children = new ArrayList<>();
                                if (queryChildren.getTotalNumItems() > 0) {
                                    for (QueryResult ch : queryChildren) {
                                        children.add(ch.getPropertyByQueryName("cmis:name").getFirstValue().toString());
                                    }
                                }
                                categories.put(qc.getPropertyByQueryName("cmis:name").getFirstValue().toString(), children);
                            }
                        }
                    }
                }
            }
        }

        if (!categories.isEmpty()){
            return categories;
        }
        return null;
    }

    private static ArrayList<File> getFiles(ItemIterable<QueryResult> query, String thumbnailId){
        ArrayList<File> res = new ArrayList<>();

        if (query!=null) {
            for (QueryResult item : query) {

                try {
                String objectId = item.getPropertyValueByQueryName("cmis:objectId");

                    OperationContext context = Application.ses.createOperationContext();
                    context.setRenditionFilterString("*");
                    Document d = (Document) Application.ses.getObject(Application.ses.createObjectId(objectId), context);
                    File file = new File();

                    // Set file TITLE
                    List<Property<?>> props = d.getProperties();
                    List<String> tags = new ArrayList<String>();
                    List<String> cats = new ArrayList<String>();
                    for (Property<?> p : props) {
                        if (p.getDefinition().getDisplayName().startsWith("Title")) {
                            String title = p.getValuesAsString();
                            title = title.replaceAll("\\]", "");
                            title = title.replaceAll("\\[", "");
                            file.setTitle(title);
                        }

                        if (p.getDefinition().getDisplayName().startsWith("Tags")) {
                            for (Object tag : p.getValues()) {
                                tags.add(tag.toString());
                            }
                        }

                        if (p.getDefinition().getDisplayName().startsWith("Categories")) {
                            for (Object cat : p.getValues()) {
                                cats.add(cat.toString());
                            }
                        }

                        if (p.getDefinition().getDisplayName().startsWith("Publisher")) {
                            String publisher = p.getValuesAsString();
                            publisher = publisher.replaceAll("\\]", "");
                            publisher = publisher.replaceAll("\\[", "");
                            file.setPublisher(publisher);
                        }

                        if (p.getDefinition().getDisplayName().startsWith("Identifier")) {
                            String identifier = p.getValuesAsString();
                            identifier = identifier.replaceAll("\\]", "");
                            identifier = identifier.replaceAll("\\[", "");
                            file.setNumber(identifier);
                        }

                        if (p.getDefinition().getDisplayName().startsWith("Coverage")) {
                            String issuingDate = p.getValuesAsString();
                            issuingDate = issuingDate.replaceAll("\\]", "");
                            issuingDate = issuingDate.replaceAll("\\[", "");
                            file.setIssuingDate(issuingDate);
                        }
                    }

                    String tagStr = "";
                    for (String tag : tags) {
                        tagStr += tag + "\t";
                    }
                    file.setTags(tagStr);

                    String catStr = "";
                    for (String cat : cats) {
                        //catStr += cat + "\t";

                        ArrayList<QueryResult> categories = new ArrayList<>();
                        ItemIterable<QueryResult> catQuery = Application.ses.query("SELECT * FROM cm:category WHERE cmis:objectId='" + cat + "'", false);
                        if (catQuery != null) {
                            for (QueryResult c : catQuery) {
                                catStr = catStr + c.getPropertyByQueryName("cmis:name").getFirstValue().toString().trim() + "#";
                            }
                        }
                    }
                    if (catStr.contains("#")) { // Trim last character
                        catStr = catStr.substring(0, catStr.length() - 1);
                    }
                    file.setCategories(catStr);

                    List<Rendition> renditions = d.getRenditions();
                    for (Rendition rendition : renditions) {
                        thumbnailId = rendition.getStreamId(); // rendition.getContentUrl();
                    }

                    file.setName(item.getPropertyByQueryName("cmis:name").getFirstValue().toString());

                    if (item.getPropertyByQueryName("cmis:description").getFirstValue() != null)
                        file.setDescription(item.getPropertyByQueryName("cmis:description").getFirstValue().toString());

                    file.setOwner(item.getPropertyByQueryName("cmis:createdBy").getFirstValue().toString());
                    if (renditions.size() > 0) {
                        file.setThumbnail(Messages.get("ALFRSCO_REST_API_URL") + "/node/content/workspace/SpacesStore/" + thumbnailId + "?alf_ticket=" + session().get("alf_ticket"));
                    } else {
                        file.setThumbnail("/assets/images/very-basic-document-icon.png");
                    }
                    file.setUrl(d.getContentUrl(item.getPropertyByQueryName("cmis:name").getFirstValue().toString()));
                    file.setId(d.getId());

                    res.add(file);
                } catch (NullPointerException e){
                    break;
                }
            }
        }
        return res;
    }
}