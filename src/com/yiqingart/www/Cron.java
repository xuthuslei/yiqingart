package com.yiqingart.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Cron extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -1892829616879239087L;
    private Logger logger = Logger.getLogger("cron");
    public enum Method {
        DOMIN, DOHOUR, DODAY, DOWEEK, NOVALUE;
        public static Method toMethod(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String requestURL = req.getRequestURI().substring(req.getContextPath().length());
        String[] inputParams = requestURL.toString().split("/");
        String method = inputParams[2];
        
        Method m = Method.toMethod(method.toUpperCase());
        switch (m) {
        case DOMIN:
            
            break;
        case DOHOUR:
            
            break;  
        case DODAY:
            ;
            break;
        case DOWEEK:
            ;
            break;        
        default:
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            break;
        }
        
        PrintWriter pw = resp.getWriter();
        pw.write(method + " ok");
        pw.flush();
        pw.close(); 
    }
}
