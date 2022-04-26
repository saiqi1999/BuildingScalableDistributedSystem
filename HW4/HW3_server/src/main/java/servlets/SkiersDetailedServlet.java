package servlets;

import beans.LiftRecord;
import org.json.simple.JSONObject;
import workers.WorkerManager;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * This servlet handles 3 of the functions:
 *
 * GET + /skiers/{resortID}/seasons/{SeasonID}/days/{dayID}/skiers/{skierID}
 * => get ski day vertical for a skier
 *
 * GET + /skiers/{skierID}/vertical?{paras}
 * => get the total vertical for the skier the specified resort. If no season is specified, return all seasons
 * => this means the parameters are in the request like /././.?resortID=xx&seasonID=yy, which we should extract
 *
 * POST + /skiers/{skierID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}?time={time}&liftId={liftID}&waitTime={waited}
 * => add such a record to the database (actually queueing first) with all information in url and request
 *
 *
 * INDEXES DEFINED ON REDIS:
 *
 * skier Redis server:
 * 'SkierSearch', built on skierID, get set of unique key to record set of skier
 * '{resortID}_{seasonID}_{daysID}', built on skierID, get set of unique key to resort/season/day/skier
 * '{resortID}_{seasonID}', built on skierID, get set of unique key to resort/season/skier
 * '{resortID}', built on skierID, get set of unique key to resort/skier
 *
 * resort Redis server:
 * 'ResortSearch', built on resortID, get set of unique key to record in that resort
 * '{seasonID}_{daysID}', built on resortID, get set of unique key to record in that resort/season/day
 */
public class SkiersDetailedServlet extends HttpServlet {

    private static final String ERR_MSG = "BAD GATEWAY";
    private static final String BAD_REQUEST = "request is bad";
    private static final String NOT_FOUND_MSG = "page not found";
    private static final String GET_SKIER_FAILURE = "unrecognized parameters";
    private static final String SKIER_NOT_FOUND = "skier id not found";
    private static final String INSERT_MSG = "insert succeed, data: ";
    private static final String PARA_NOT_FOUND = "some parameter is null in request";
    private static final String skierDbIP = "54.213.162.5";
    private boolean ipComplete = false;

    /**
     * we get data specific to the data structure we stored in, how to index to get max availability
     * @param request req
     * @param response resp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String[] frags =requestURI.split("/");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        if(!ipComplete){
            try {
                WorkerManager.getManager().setSkierIP(skierDbIP);
                ipComplete = true;
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        if(frags.length == 5){
            //mapping to /skiers/{skierID}/vertical + possible param
            try{
                int skierID = Integer.parseInt(frags[3]);
                String paraResortID = request.getParameter("resortID");
                String paraSeasonID = request.getParameter("seasonID");
                Set<LiftRecord> set = WorkerManager.getManager().query(paraResortID,paraSeasonID,null, skierID);
                int totalVerticals = calcVerticals(set);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write("total vertices: "+totalVerticals);
                writer.flush();
            }catch(Exception e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
            }
        }
        else if(frags.length == 10){
            //mapping to /skiers/{resortID}/seasons/{SeasonID}/days/{dayID}/skiers/{skierID}
            try {
                String resortID = frags[3];
                String seasonID = frags[5];
                String dayID = frags[7];
                int skierID = Integer.parseInt(frags[9]);
                Set<LiftRecord> set = WorkerManager.getManager().query(resortID,seasonID,dayID,skierID);
                int totalVerticals = calcVerticals(set);
                response.setStatus(HttpServletResponse.SC_OK);
                writer.write("total vertices: "+totalVerticals);
                writer.flush();
            }catch (Exception e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
            }
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(NOT_FOUND_MSG);
            writer.flush();
        }
    }

    private int calcVerticals(Set<LiftRecord> set) {
        int sum = 0;
        for(LiftRecord record : set){
            sum += Integer.parseInt(record.getLiftId()) * 10;
        }
        return sum;
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
                WorkerManager.getManager().enqueue(record);
                writer.write(INSERT_MSG + " time: " + time + " liftId: " + liftId + " waitTime: " + waitTime);
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
