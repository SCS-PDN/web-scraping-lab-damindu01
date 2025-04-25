import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/scrape")
public class ScrapeServlet extends HttpServlet {

    static class ScrapeResult {
        String type;
        String content;

        ScrapeResult(String type, String content) {
            this.type = type;
            this.content = content;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Session tracking
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 0;
        session.setAttribute("visitCount", ++visitCount);

        String url = request.getParameter("url");
        String[] options = request.getParameterValues("options");
        List<ScrapeResult> results = new ArrayList<>();

        if (options != null && url != null && !url.isEmpty()) {
            Document doc = Jsoup.connect(url).get();

            for (String option : options) {
                switch (option) {
                    case "title":
                        results.add(new ScrapeResult("Title", doc.title()));
                        break;
                    case "links":
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            results.add(new ScrapeResult("Link", link.attr("abs:href")));
                        }
                        break;
                    case "images":
                        Elements images = doc.select("img[src]");
                        for (Element img : images) {
                            results.add(new ScrapeResult("Image", img.attr("abs:src")));
                        }
                        break;
                }
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(results);
        session.setAttribute("scrapedData", results);  // Store in session for CSV download

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Scrape Results</title></head><body>");
        out.println("<h2>Scraped Results</h2>");
        out.println("<p>You have visited this page " + visitCount + " times.</p>");
        out.println("<table border='1'><tr><th>Type</th><th>Content</th></tr>");
        for (ScrapeResult result : results) {
            out.println("<tr><td>" + result.type + "</td><td>" + result.content + "</td></tr>");
        }
        out.println("</table>");
        out.println("<br><form action='download' method='get'><button type='submit'>Download Results as CSV</button></form>");
        out.println("</body></html>");
    }
}\
