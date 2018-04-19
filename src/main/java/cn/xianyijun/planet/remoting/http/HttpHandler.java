package cn.xianyijun.planet.remoting.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interface Http handler.
 *
 * @author xianyijun
 */
public interface HttpHandler {

    /**
     * Handle.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException      the io exception
     * @throws ServletException the servlet exception
     */
    void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

}
