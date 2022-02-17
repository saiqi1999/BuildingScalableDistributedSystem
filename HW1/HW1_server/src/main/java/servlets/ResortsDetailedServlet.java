package servlets;

import beans.SeasonData;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import static strings.ServletStrings.BAD_REQUEST;

/**
 * GET: get a list of seasons of specified resort
 * POST: post a new season to the specified resort
 */
@WebServlet(name = "ResortsDetailedServlet", value = "/ResortsDetailedServlet")
public class ResortsDetailedServlet extends HttpServlet {
    private static final String NOT_FOUND_MSG = "page not found";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        response.setContentType("application/json");
        String[] frags = requestURI.trim().split("/");
        PrintWriter writer = response.getWriter();
        int len = frags.length;
        if (len == 5) {
            //resort/*/seasons
            //validation
            try {
                Integer.parseInt(frags[3]);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
                return;
            }
            //dummy data
            SeasonData seasonData = new SeasonData();
            seasonData.addSeason("spring");
            seasonData.addSeason("summer");
            seasonData.addSeason("autumn");
            seasonData.addSeason("winter");
            /*Gson gson = new Gson();*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg",String.valueOf(seasonData));
            writer.write(String.valueOf(jsonObject));
            writer.flush();
        } else if (len == 9) {
            ///resorts/*/seasons/*/day/*/skiers
            //validation
            try {
                Integer.parseInt(frags[3]);
                Integer.parseInt(frags[5]);
                Integer.parseInt(frags[7]);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                writer.flush();
                return;
            }
            //dummy data by random
            response.setStatus(HttpServletResponse.SC_OK);
            Random random = new Random(System.currentTimeMillis());
            writer.write("# skiers : " + Math.abs(random.nextInt() % 1000));
            writer.flush();
        }
        else{
            //unrecognized
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(NOT_FOUND_MSG);
            writer.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String[] frags = requestURI.split("/");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        String para = request.getParameter("season");
        if(frags.length==5){
            System.out.println("insert season: " + para);

            //validation
            try {
                Integer.parseInt(frags[3]);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.write(BAD_REQUEST);
                return;
            }

            //without database, we just skip and success
            response.setStatus(HttpServletResponse.SC_CREATED);
            writer.write("season added: " + para);
            writer.flush();
        }
    }
}
