package net.whydah.identity.admin;

import net.whydah.identity.admin.config.AppConfig;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Properties;

/**
 * Created by Leon on 25.04.14.
 */
@RequestMapping("/{apptokenid}/{usertokenid}")
@Controller
public class UserAdminUibController {
    private static final Logger logger = LoggerFactory.getLogger(UserAdminUibController.class);
    private static final String JSON_DATA_KEY = "jsondata";
    //private final String uibUrl;
    private final String userAdminServiceUrl;
    private final HttpClient httpClient;
    //private String utf8query;


    public UserAdminUibController() throws IOException {
        Properties properties = AppConfig.readProperties();
//        uibUrl = properties.getProperty("useridentitybackend");
        userAdminServiceUrl = properties.getProperty("useradminservice");
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/users/find/{query}", method = RequestMethod.GET)
    public String findUsers(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("query") String query, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("findUsers - entry.  applicationtokenid={},  usertokenid={}", apptokenid, usertokenid);
        if (usertokenid == null || usertokenid.length() < 7) {
            usertokenid = CookieManager.getUserTokenIdFromCookie(request);
            logger.trace("findUsers - Override usertokenid={}", usertokenid);
        }
        String utf8query = query;
        try {
            utf8query = new String(query.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException uee) {

        }
        logger.trace("findUsers - Finding users with query: " + utf8query);
        HttpMethod method = new GetMethod();
        String url;
        try {
            url = buildUasUrl(apptokenid, usertokenid, "users/find/" + URIUtil.encodeAll(utf8query));
        } catch (URIException urie) {
            logger.warn("Error in handling URIencoding", urie);
            url = buildUasUrl(apptokenid, usertokenid, "users/find/" + query);
        }
        makeUibRequest(method, url, model, response);
        return "json";
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/", method = RequestMethod.GET)
    public String getUser(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Getting user with uid: " + uid);
        HttpMethod method = new GetMethod();
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid);
        makeUibRequest(method, url, model, response);
        logger.trace("getUser with uid={} returned the following jsondata=\n{}", uid, model.asMap().get(JSON_DATA_KEY));
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/useraggregate/{uid}/", method = RequestMethod.GET)
    public String getUserAggregate(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpMethod method = new GetMethod();
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid);
        makeUibRequest(method, url, model, response);
        logger.trace("getUserAggregate with uid={} returned the following jsondata=\n{}", uid, model.asMap().get(JSON_DATA_KEY));
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/", method = RequestMethod.DELETE)
    public String deleteUser(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Deleting user with uid: " + uid);
        DeleteMethod method = new DeleteMethod();
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid);
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/", method = RequestMethod.PUT)
    public String putUser(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Putting user with uid: " + uid);
        PutMethod method = new PutMethod();
        InputStreamRequestEntity inputStreamRequestEntity = null;
        try {
            inputStreamRequestEntity = new InputStreamRequestEntity(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        method.setRequestEntity(inputStreamRequestEntity);
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid);
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/", method = RequestMethod.POST)
    public String postUser(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Posting new user");
        PostMethod method = new PostMethod();
        InputStreamRequestEntity inputStreamRequestEntity = null;
        try {
            inputStreamRequestEntity = new InputStreamRequestEntity(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        method.setRequestEntity(inputStreamRequestEntity);
        String url = buildUasUrl(apptokenid, usertokenid, "user/");
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }


    // ROLES

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/roles", method = RequestMethod.GET)
    public String getUserRoles(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Getting user roles for user with uid: " + uid);
        HttpMethod method = new GetMethod();
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid + "/roles");
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/role/", method = RequestMethod.POST)
    public String postUserRole(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Posting new role for user with uid: " + uid);
        PostMethod method = new PostMethod();
        InputStreamRequestEntity inputStreamRequestEntity = null;
        try {
            inputStreamRequestEntity = new InputStreamRequestEntity(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        method.setRequestEntity(inputStreamRequestEntity);
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid + "/role/");
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/role/{roleId}", method = RequestMethod.DELETE)
    public String deleteUserRole(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, @PathVariable("roleId") String roleId, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Deleting role with roleId: " + roleId + ", for user with uid: " + uid);
        DeleteMethod method = new DeleteMethod();
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid + "/role/" + roleId);
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{uid}/role/{roleId}", method = RequestMethod.PUT)
    public String putUserRole(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("uid") String uid, @PathVariable("roleId") String roleId, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Putting role with roleId: " + roleId + ", for user with uid: " + uid);
        PutMethod method = new PutMethod();
        InputStreamRequestEntity inputStreamRequestEntity = null;
        try {
            inputStreamRequestEntity = new InputStreamRequestEntity(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        method.setRequestEntity(inputStreamRequestEntity);
        String url = buildUasUrl(apptokenid, usertokenid, "user/" + uid + "/role/" + roleId);
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }


    // PASSWORD

    @POST
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/user/{username}/resetpassword", method = RequestMethod.POST)
    public String resetPassword(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, @PathVariable("username") String username, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("Resetting password for user: " + username);
        PostMethod method = new PostMethod();
        String url = userAdminServiceUrl + "password/" + apptokenid +"/reset/username/" + username;
        makeUibRequest(method, url, model, response);
        response.setContentType("application/json; charset=utf-8");
        return "json";
    }


    // APPLICATIONS

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    @ResponseBody
    public String getApplications(@PathVariable("apptokenid") String apptokenid, @PathVariable("usertokenid") String usertokenid, HttpServletRequest request, HttpServletResponse response, Model model) {
        logger.trace("getApplications - entry.  applicationtokenid={},  usertokenid={}", apptokenid, usertokenid);
        if (usertokenid == null || usertokenid.length() < 7) {
            usertokenid = CookieManager.getUserTokenIdFromCookie(request);
            logger.trace("getApplications - Override usertokenid={}", usertokenid);
        }

        String resourcePath = "applications";
        String applicationsJson = "{no-apps-found}";
        try {
            applicationsJson = makeUibRequest(apptokenid, usertokenid, model, resourcePath);
        } catch (Exception e) {
            logger.warn("getApplications - Could not fetch Applications from UIB.", e);
        }

        response.setContentType("application/json; charset=utf-8");
        return applicationsJson;
    }


    private String buildUasUrl(String apptokenid, String usertokenid, String s) {
        return userAdminServiceUrl + apptokenid + "/" + usertokenid + "/" + s;
    }

    protected String makeUibRequest(String apptokenid, String usertokenid, Model model, String resourcePath) {
        String url = buildUasUrl(apptokenid, usertokenid, resourcePath);
        HttpMethodParams params = new HttpMethodParams();
        params.setHttpElementCharset("UTF-8");
        params.setContentCharset("UTF-8");
        HttpMethod method = new GetMethod();
        method.setParams(params);
        StringBuilder responseBody = new StringBuilder();
        try {
            method.setURI(new URI(url, true));
            int rescode = httpClient.executeMethod(method);
            // TODO: check rescode?
            if (rescode == 204) { // Delete
                // Do something
            } else {
                InputStream responseBodyStream = method.getResponseBodyAsStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(responseBodyStream));

                String line;
                while ((line = in.readLine()) != null) {
                    responseBody.append(line);
                }
                model.addAttribute(JSON_DATA_KEY, responseBody.toString());


            }
        } catch (Exception e) {
            logger.info("Could not find applcations data. Url: " + url + " Response: " + responseBody, e);
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }

        return responseBody.toString();
    }

    /*
    @Deprecated Use makeUibRequest(String apptokenid, String usertokenid, String resourcePath)
     */
    private void makeUibRequest(HttpMethod method, String url, Model model, HttpServletResponse response) {
        HttpMethodParams params = new HttpMethodParams();
        params.setHttpElementCharset("UTF-8");
        params.setContentCharset("UTF-8");
        method.setParams(params);
        try {
            method.setURI(new URI(url, true));
            int rescode = httpClient.executeMethod(method);
            // TODO: check rescode?
            if (rescode == 204) { // Delete
                // Do something
            } else {
                InputStream responseBodyStream = method.getResponseBodyAsStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(responseBodyStream));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = in.readLine()) !=null) {
                    responseBody.append(line);
                }
                model.addAttribute(JSON_DATA_KEY, responseBody.toString());
                response.setContentType("application/json; charset=utf-8");
            }
            response.setStatus(rescode);
        } catch (IOException e) {
            response.setStatus(503);
            logger.error("IOException", e);
        } catch (NullPointerException e) {
            response.setStatus(503);
            logger.error("Nullpointer:", e);
        } finally {
            method.releaseConnection();
        }
        if (!model.containsAttribute(JSON_DATA_KEY)) {
            logger.error("jsondata attribute not set when fetching data from URL: {}", url);
        }
    }
}
