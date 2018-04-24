package cn.xianyijun.planet.test.demo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import cn.xianyijun.planet.rpc.rest.support.ContentType;

/**
 * Created by xianyijun on 2017/10/22.
 */
@Path("process")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public interface ProcessService {
    /**
     * Test object.
     *
     * @return the object
     */
    @GET
    @Path("/test")
    public Object test();
}
