package servlets;

import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;


/**
 * GET: get a list of seasons of specified resort
 * POST: post a new season to the specified resort
 */
@WebServlet(name = "ResortsDetailedServlet", value = "/ResortsDetailedServlet")
public class ResortsDetailedServlet extends HttpServlet {
    private static final String NOT_FOUND_MSG = "page not found";
    public static final String BAD_REQUEST = "request is bad";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI =request.getRequestURI();
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
            /*SeasonData seasonData = new SeasonData();
            seasonData.addSeason("spring");
            seasonData.addSeason("summer");
            seasonData.addSeason("autumn");
            seasonData.addSeason("winter");*/
            /*Gson gson = new Gson();*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "String.valueOf(seasonData)");
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
        } else {
            //unrecognized
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write(NOT_FOUND_MSG);
            writer.flush();
        }
    }
}