package servlets;

import beans.Resort;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * GET: get a list of the resorts
 */
@WebServlet(name = "ResortsServlet", value = "/ResortsServlet")
public class ResortsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        //dummy data
        ArrayList<Resort> resorts = new ArrayList<>();
        resorts.add(new Resort("dummy1", 100));
        resorts.add(new Resort("dummy2", 101));
        resorts.add(new Resort("dummy3", 102));
        System.out.println(1);

        //response
        /*Gson gson = new Gson();
        System.out.println(1);
        String msg = gson.toJson(resorts);*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resorts",resorts);
        System.out.println(jsonObject);
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        writer.write(String.valueOf(jsonObject));
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

    }
}
