package servlets;

import beans.LiftRecord;
import org.json.simple.JSONObject;
import workers.WorkerManager;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

public class SkiersDetailedServlet extends HttpServlet {
    private static final String ERR_MSG = "BAD GATEWAY";
    private static final String WAIT_MSG = "WAIT";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
            int skiersId = 0;
            int resortId = 0;
            int seasonId = 0;
            int daysId = 0;
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
            boolean first = true;
            for(int i = 0; i < frags.length; i++){
                String s = frags[i];
                if(s.equals("skiers")&&i+1< frags.length){
                    if(first)resortId = Integer.parseInt(frags[i+1]);
                    else skiersId = Integer.parseInt(frags[i+1]);
                    first = false;
                }
                if(s.equals("seasons")&&i+1<frags.length){
                    seasonId = Integer.parseInt(frags[i+1]);
                }
                if(s.equals("days")&&i+1<frags.length){
                    daysId = Integer.parseInt(frags[i+1]);
                }
            }

            //put it into queue
            try {
                LiftRecord record = new LiftRecord(time,liftId,waitTime,resortId,skiersId,seasonId,daysId);
                boolean wait = WorkerManager.getManager().enqueue(record);
                if(wait)writer.write(WAIT_MSG + " time: " + time + " liftId: " + liftId + " waitTime: " + waitTime);
                else writer.write(INSERT_MSG + " time: " + time + " liftId: " + liftId + " waitTime: " + waitTime);
                response.setStatus(HttpServletResponse.SC_CREATED);
                writer.flush();
            } catch (TimeoutException e) {
                e.printStackTrace();
                writer.write(ERR_MSG);
                response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                writer.flush();
            }

        } else if (!dataFound(uri)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
}
