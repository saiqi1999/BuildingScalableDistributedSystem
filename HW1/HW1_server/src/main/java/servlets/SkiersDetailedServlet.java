package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.json.simple.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;


/**
 * the servlet skiers use to upload
 * POST: write a new lift ride for the skiers
 * GET: get ski day vertical for skiers
 */
@WebServlet(name = "SkiersDetailedServlet", value = "/SkiersDetailedServlet")
public class SkiersDetailedServlet extends HttpServlet {
    private static final String GET_SKIER_FAILURE = "unrecognized parameters";
    private static final String SKIER_NOT_FOUND = "skier id not found";
    private static final String NOT_FOUND_MSG = "page not found";
    private static final String INSERT_MSG = "insert succeed, data: ";
    private static final String PARA_NOT_FOUND = "some parameter is null in request";

    private String generateJSON(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", msg);
        return String.valueOf(jsonObject);
    }

    private boolean dataFound(String info) {
        //find if the user data in database, suppose true
        return true;
    }

    private boolean isValid(String[] frags) {
        //validation
        if (frags.length == 5) {
            // /skiers/*/vertical
            try {
                Integer.parseInt(frags[3]);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else if (frags.length == 10) {
            // /skiers/*/seasons/*/day/*/skiers/*
            try {
                Integer.parseInt(frags[3]);
                Integer.parseInt(frags[5]);
                Integer.parseInt(frags[7]);
                Integer.parseInt(frags[9]);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String[] frags = uri.split("/");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        if (!isValid(frags)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
            /*Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("");
            jsonElement.getAsJsonObject().addProperty("msg", GET_SKIER_FAILURE);*/
            writer.write(generateJSON(GET_SKIER_FAILURE));
            writer.flush();
        } else {
            if (frags.length == 10) {
                if (dataFound(uri)) {
                    response.setStatus(HttpServletResponse.SC_OK);//200
                    writer.write("# : " + 32045);
                    writer.flush();
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);//404
            /*Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("");
            jsonElement.getAsJsonObject().addProperty("msg", SKIER_NOT_FOUND);*/
                    writer.write(generateJSON(SKIER_NOT_FOUND));
                    writer.flush();
                }
            } else if (frags.length == 5) {
                if (!dataFound(uri)) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.write(String.valueOf(generateJSON(SKIER_NOT_FOUND)));
                    writer.flush();
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    HashMap<String, Integer> vertMap = new HashMap<>();
                    vertMap.put("spring", 100);
                    vertMap.put("summer", 100);
                    vertMap.put("autumn", 100);
                    vertMap.put("winter", 200);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("seasons", vertMap);
            /*Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("");
            jsonElement.getAsJsonObject().addProperty("resort", String.valueOf(vertMap));*/
                    writer.write(String.valueOf(jsonObject));
                    writer.flush();
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String[] frags = uri.split("/");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        if (isValid(frags)) {
            String time = request.getParameter("time");
            String liftId = request.getParameter("liftId");
            String waitTime = request.getParameter("waitTime");
            if (frags.length != 10) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.write(NOT_FOUND_MSG);
                writer.flush();
                return;
            }
            //integer check
            if (time == null || time.equals("")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(PARA_NOT_FOUND);
                writer.flush();
                return;
            }
            if (liftId == null || liftId.equals("")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(PARA_NOT_FOUND);
                writer.flush();
                return;
            }
            if (waitTime == null || waitTime.equals("")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
                writer.write(PARA_NOT_FOUND);
                writer.flush();
                return;
            }
            writer.write(INSERT_MSG + " time: " + time + " liftId: " + liftId + " waitTime: " + waitTime);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writer.flush();
        } else if (!dataFound(uri)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            /*Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("");
            jsonElement.getAsJsonObject().addProperty("msg", SKIER_NOT_FOUND);*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg",generateJSON(SKIER_NOT_FOUND));
            writer.write(String.valueOf(jsonObject));
            writer.flush();
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write(PARA_NOT_FOUND);
            writer.flush();
        }

    }
}
