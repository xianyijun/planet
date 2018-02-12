package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Cluster utils.
 * @author xianyijun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClusterUtils {

    /**
     * Merge url url.
     *
     * @param remoteUrl the remote url
     * @param localMap  the local map
     * @return the url
     */
    public static URL mergeUrl(URL remoteUrl, Map<String, String> localMap) {
        Map<String, String> map = new HashMap<>();
        Map<String, String> remoteMap = remoteUrl.getParameters();


        if (!MapUtils.isEmpty(localMap)) {
            map.putAll(remoteMap);

            map.remove(Constants.THREAD_NAME_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREAD_NAME_KEY);

            map.remove(Constants.THREAD_POOL_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREAD_POOL_KEY);

            map.remove(Constants.CORE_THREADS_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.CORE_THREADS_KEY);

            map.remove(Constants.THREADS_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.THREADS_KEY);

            map.remove(Constants.QUEUES_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.QUEUES_KEY);

            map.remove(Constants.ALIVE_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.ALIVE_KEY);

            map.remove(Constants.TRANSPORTER_KEY);
            map.remove(Constants.DEFAULT_KEY_PREFIX + Constants.TRANSPORTER_KEY);
        }

        if (localMap != null && localMap.size() > 0) {
            map.putAll(localMap);
        }
        if (remoteMap != null && remoteMap.size() > 0) {
            String rpc = remoteMap.get(Constants.RPC_VERSION_KEY);
            if (!StringUtils.isBlank(rpc)) {
                map.put(Constants.RPC_VERSION_KEY, rpc);
            }
            String version = remoteMap.get(Constants.VERSION_KEY);
            if (!StringUtils.isBlank(version)) {
                map.put(Constants.VERSION_KEY, version);
            }
            String group = remoteMap.get(Constants.GROUP_KEY);
            if (!StringUtils.isBlank(group)) {
                map.put(Constants.GROUP_KEY, group);
            }
            String methods = remoteMap.get(Constants.METHODS_KEY);
            if (!StringUtils.isBlank(methods)) {
                map.put(Constants.METHODS_KEY, methods);
            }
            // 保留provider的启动timestamp
            String remoteTimestamp = remoteMap.get(Constants.TIMESTAMP_KEY);
            if (remoteTimestamp != null && remoteTimestamp.length() > 0) {
                map.put(Constants.REMOTE_TIMESTAMP_KEY, remoteMap.get(Constants.TIMESTAMP_KEY));
            }
            // 合并filter和listener
            String remoteFilter = remoteMap.get(Constants.REFERENCE_FILTER_KEY);
            String localFilter = localMap.get(Constants.REFERENCE_FILTER_KEY);
            if (remoteFilter != null && remoteFilter.length() > 0
                    && localFilter != null && localFilter.length() > 0) {
                localMap.put(Constants.REFERENCE_FILTER_KEY, remoteFilter + "," + localFilter);
            }
            String remoteListener = remoteMap.get(Constants.INVOKER_LISTENER_KEY);
            String localListener = localMap.get(Constants.INVOKER_LISTENER_KEY);
            if (remoteListener != null && remoteListener.length() > 0
                    && localListener != null && localListener.length() > 0) {
                localMap.put(Constants.INVOKER_LISTENER_KEY, remoteListener + "," + localListener);
            }
        }

        return remoteUrl.clearParameters().addParameters(map);
    }

}
