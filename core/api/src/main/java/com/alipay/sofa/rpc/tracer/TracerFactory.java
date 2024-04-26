/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.tracer;

import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.ext.ExtensionClass;
import com.alipay.sofa.rpc.ext.ExtensionLoaderFactory;
import com.alipay.sofa.rpc.log.LogCodes;

/**
 * Factory of Tracer
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public final class TracerFactory {

    static {
        // sofaTrace默认会将日志生成在用户目录下，占用C盘内存极大。logging.path在非springboot工程中未找到设置方式，因此通过System.setProperty设置。
        // 原理，在构造ServerConfig时，会触发RpcRuntimeContext的初始化，执行模块加载ModuleFactory.installModules();
        // 在执行sofaTrace模块的module.install()时，debug跟踪会发现，在构造构造RpcSofaTrace实例过程中，会调用此方法LogEnvUtils.processGlobalSystemLogProperties()
        // 就是如果未配置logging.path，会调用System.setProperty添加此属性，默认值即用户目录下
        // 因此，在启动时，构造RpcSofaTracer之前，设置此属性，避免日志生成在C盘。
        // 跟踪思路，发现服务启动前后，System.getProperty("logging.path")为null，在启动之后，有值了，因此断点在System类上，看是在何时被设置的。
        System.setProperty("logging.path", "./logs");
    }

    /**
     * 初始化Tracer实例
     * @return Tracer
     */
    public synchronized static Tracer getTracer(String tracerName) {
        try {
            ExtensionClass<Tracer> ext = ExtensionLoaderFactory.getExtensionLoader(Tracer.class)
                    .getExtensionClass(tracerName);
            if (ext == null) {
                throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_FAIL_LOAD_TRACER_EXT, tracerName));
            }
            return ext.getExtInstance();
        } catch (SofaRpcRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_FAIL_LOAD_TRACER_EXT, tracerName), e);
        }
    }
}
