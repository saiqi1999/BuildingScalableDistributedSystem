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

@WebServlet(name = "StatisticsServlet", value = "/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        //skip validation cause it's fixed by web.xml

        response.setStatus(HttpServletResponse.SC_OK);
        /*HashMap<String,Integer> vertMap = new HashMap<>();
        vertMap.put("#threads",100);
        vertMap.put("avg wall time",100);
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree("");
        jsonElement.getAsJsonObject().addProperty("resort", String.valueOf(vertMap));*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("#threads",100);
        jsonObject.put("avg wall time",100);
        writer.write(String.valueOf(jsonObject));
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
