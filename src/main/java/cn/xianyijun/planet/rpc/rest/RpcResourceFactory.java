package cn.xianyijun.planet.rpc.rest;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import lombok.AllArgsConstructor;

/**
 * @author xianyijun
 */
@AllArgsConstructor
public class RpcResourceFactory implements ResourceFactory {
    private Object resourceInstance;
    private Class scannableClass;

    @Override
    public Object createResource(HttpRequest request, HttpResponse response,
                                 ResteasyProviderFactory factory) {
        return resourceInstance;
    }

    @Override
    public Class<?> getScannableClass() {
        return scannableClass;
    }

    @Override
    public void registered(ResteasyProviderFactory factory) {
//        this.propertyInjector = factory.getInjectorFactory().createPropertyInjector(getScannableClass(), factory);
    }

    @Override
    public void requestFinished(HttpRequest request, HttpResponse response,
                                Object resource) {
    }

    @Override
    public void unregistered() {
    }
}
