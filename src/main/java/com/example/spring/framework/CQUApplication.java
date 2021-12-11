package com.example.spring.framework;

import com.example.spring.annotation.*;
import com.example.spring.util.StringUtils;
import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

public class CQUApplication {
    private static final String SERVER_PORT_KEY = "server.port";
    private static int serverPort = 8080;
    private static String packagePath = "/";
    private static String configFilePath = "/application.properties";
    public static Map<String, Object> beanFactory = new ConcurrentHashMap<String, Object>();
    public static Map<String, Method> methodMapperFactory = new ConcurrentHashMap<String, Method>();
    public static void run(Class clazz, String[] args) throws Exception {
        //检查SpringBootApplication 注解及 扫描包路径
        doLoadRunAnnotation(clazz);

        //加载配置文件
        doLoadConfigures();

        //扫描包
        doScannerPackage();

        //依赖注入
        doAutowired();

        //启动容器
        doStartTomcat();
    }

    /**
     * 获取注解，加载包路径及配置文件路径
     *
     * @param clazz
     */
    private static void doLoadRunAnnotation(Class clazz) throws Exception {
        //启动类无注解
        if (!clazz.isAnnotationPresent(CQUSpringBootApplication.class)) {
            throw new Exception("Unable to start ServletWebServerApplicationContext due to missing ServletWebServerFactory bean");
        }
        //获取注解对象
        CQUSpringBootApplication annotation = (CQUSpringBootApplication) clazz.getAnnotation(CQUSpringBootApplication.class);
        //获取扫描包路径
        String basePackages = annotation.scanBasePackages();
        //basePackages 为null 获取当前启动类所在包路径
        if (!StringUtils.isEmpty(basePackages)) {
            packagePath = basePackages;
        } else {
            packagePath = StringUtils.getPackagePath(clazz.getName(), clazz.getSimpleName());
        }
        //获取配置文件路径
        String configPath = annotation.configPath();

        if (!StringUtils.isEmpty(configPath)) {
            configFilePath = configPath;
        }
    }

    /**
     * 解析加载properties config
     *
     * @throws Exception
     */
    private static void doLoadConfigures() throws Exception {
        Properties properties = new Properties();
        //加载配置文件
        properties.load(new FileInputStream((CQUApplication.class.getResource("/").getPath()
                + configFilePath).replaceAll("/+", "/")));
        if (properties.isEmpty()) {
            return;
        }
        //启动端口 暂时只写了这一个配置项
        String port = properties.getProperty(SERVER_PORT_KEY);
        //不为空 替换默认
        if (!StringUtils.isEmpty(port)) {
            serverPort = Integer.valueOf(port);
        }
    }

    /**
     * 扫描包路径，register bean
     *
     * @throws Exception
     */
    private static void doScannerPackage() throws Exception {
        //Reflections 扫描包路径 ，需要配置待会需要的Scanner
        Reflections relation = new Reflections(packagePath, new SubTypesScanner(), new TypeAnnotationsScanner());
        //存在Controller注解的 类集合
        Set<Class<?>> typesAnnotatedWithController = relation.getTypesAnnotatedWith(CQUController.class);
        //注册进 methodMapperFactory
        registerControllerBean(typesAnnotatedWithController);
        //注册进 beanFactory
        registerBean(typesAnnotatedWithController);
        //存在Service注解的 类集合
        Set<Class<?>> typesAnnotatedWithBean = relation.getTypesAnnotatedWith(CQUService.class);
        registerBean(typesAnnotatedWithBean);
    }

    /**
     * controller 注册，mapper地址 -> 方法实例
     *
     * @param typesAnnotatedWith
     */
    private static void registerControllerBean(Set<Class<?>> typesAnnotatedWith) {
        for (Class<?> aClass : typesAnnotatedWith) {
            if (!aClass.isAnnotationPresent(CQUController.class)) {//验证
                return;
            }
            String controllerMapper = "/";
            //controller 存在 requestMapper 注解
            if (aClass.isAnnotationPresent(CQURequestMapper.class)) {
                controllerMapper += aClass.getAnnotation(CQURequestMapper.class).value();
            }
            //获取方法列表
            for (Method method : aClass.getMethods()) {
                //不处理不带requestMapper 注解方法
                if (!method.isAnnotationPresent(CQURequestMapper.class)) {
                    continue;
                }
                CQURequestMapper requestAnnotation = method.getAnnotation(CQURequestMapper.class);
                //获取注解内容，拼接映射路径
                String requestMapper = (controllerMapper + "/" + requestAnnotation.value()).replaceAll("/+", "/");
                methodMapperFactory.put(requestMapper, method);
            }
        }
    }

    /**
     * 注册bean 进入factory beanName & alisaName -> bean 实例
     *
     * @param typesAnnotatedWithBean
     * @throws Exception
     */
    private static void registerBean(Set<Class<?>> typesAnnotatedWithBean) throws Exception {
        for (Class<?> aClass : typesAnnotatedWithBean) {
            CQUService annotation = aClass.getAnnotation(CQUService.class);
            //默认名称：类名首字母小写
            String className = StringUtils.toFirstByteLowerCase(getClassName(aClass));
            //别名：注解自定义名称
            String alisaName = annotation != null ? annotation.value() : null;
            //存放对象实例
            Object obj = aClass.newInstance();

            if (!StringUtils.isEmpty(alisaName)) {
                beanFactory.put(alisaName, obj);
            }
            beanFactory.put(className, obj);
        }
    }

    /**
     * 实现类获取接口名称，普通类返回自己名称
     *
     * @param clazz
     * @return
     */
    private static String getClassName(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();

        if (interfaces != null && interfaces.length > 0) {
            //这边做了偷懒处理，获取的第一个接口的名称
            return interfaces[0].getSimpleName();
        }
        return clazz.getSimpleName();
    }

    /**
     * 依赖注入
     *
     * @throws Exception
     */
    private static void doAutowired() throws Exception {
        //扫描包路径 选择器注册为 field
        Reflections relation = new Reflections(packagePath, new FieldAnnotationsScanner());
        // 包路径下带 AutoWired 注解属性字段 集合
        Set<Field> fields = relation.getFieldsAnnotatedWith(CQUAutowired.class);

        for (Field field : fields) {
            field.setAccessible(true);//暴力访问 所有修饰符
            //持有此fields的类
            String declaringClassName = StringUtils.toFirstByteLowerCase(getClassName(field.getDeclaringClass()));
            //注入类型
            String className = StringUtils.toFirstByteLowerCase(getClassName(field.getType()));
            //set
            field.set(beanFactory.get(declaringClassName), beanFactory.get(className));
        }
    }

    /**
     * 启动容器
     */
    private static void doStartTomcat() {
        try {
            Tomcat tomcat = new Tomcat();// 内置tomcat

            tomcat.setHostname("localhost");// 设置地址端口
            tomcat.setPort(serverPort);
            tomcat.setBaseDir(".");// 设置文件夹
            tomcat.getConnector().setURIEncoding("utf-8");

            Context ctx = tomcat.addContext("/", null);// 网络访问路径
            tomcat.addServlet(ctx, "servlet", CQUDispatcherServlet.class.getCanonicalName()); // 配置servlet
            ctx.addServletMappingDecoded("/*", "servlet");

            tomcat.getServer().addLifecycleListener(new AprLifecycleListener());// 监听器
            tomcat.start();
            tomcat.getServer().await();// 启动监听
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}