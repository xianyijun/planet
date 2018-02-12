package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.common.ReSetable;
import cn.xianyijun.planet.exception.RemotingException;

/**
 * Created by xianyijun on 2017/10/28.
 */
public interface Client extends Endpoint,Channel ,ReSetable {

    /**
     * Re connect.
     *
     * @throws RemotingException the remoting exception
     */
    void reConnect() throws RemotingException;
}
