package servlets;

import beans.LiftRecord;
import org.json.simple.JSONObject;
import workers.WorkerManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;


/**
 * This servlet map to /resorts/* split the uri and determine what to do
 * GET + /resorts/{resortsId}/seasons/{seasonsId}/day/{dayId}/skiers
 * => get the number of unique skiers that day, season, resort
 */
//@WebServlet(name = "ResortsDetailedServlet", value = "/ResortsDetailedServlet")
public class ResortsDetailedServlet extends HttpServlet {
    private static final String NOT_FOUND_MSG = "page not found";
    public static final String BAD_REQUEST = "request is bad";
    private static final String resortDbIP = "54.69.36.225";
    private boolean ipComplete = false;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI =request.getRequestURI();
        response.setContentType("application/json");
        String[] frags = requestURI.trim().split("/");
        PrintWriter writer = response.getWriter();
        int len = frags.length;
        if(!ipComplete){
            try {
                WorkerManager.getManager().setResortIP(resortDbIP);
                ipComplete = true;
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (len == 5) {
            //MAP TO /resorts/?/seasons
            //validation
            try {
                Integer.parseInt(frags[3]);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "String.valueOf(seasonData), TEST SUCCEED");
            writer.write(String.valueOf(jsonObject));
            writer.flush();
        } else if (len == 9) {
            //MAP TO /resorts/?/seasons/?/day/?/skiers
            try {
                int resortID = Integer.parseInt(frags[3]);
                int seasonID = Integer.parseInt(frags[5]);
                int dayID = Integer.parseInt(frags[7]);
                Set<LiftRecord> uniqueIn = WorkerManager.getManager().uniqueIn(resortID,seasonID,dayID);
                Set<Integer> ids = new HashSet<>();
                for(LiftRecord record : uniqueIn){
                    ids.add(record.getSkiersId());
                }
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write("# skiers : " + ids.size());
                writer.flush();
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
            }
        } else {
            //unrecognized
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(NOT_FOUND_MSG);
            writer.flush();
        }
    }
}