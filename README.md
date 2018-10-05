## 简单 AOP 的实现

### 什么是 AOP

AOP，即 aspect-oriented programming 的缩写，面向切面的程序设计。通过预编译方式和运行期动态代理实现程序功能统一维护的一种技术。

### 为什么要使用 AOP

面向切面编程，实际上就是通过预编译或者动态代理技术在不修改源代码的情况下给原来的程序统一添加功能的一种技术。我们看几个关键词，第一个是“动态代理技术”，这个就是 Spring AOP 实现底层技术；第二个“不修改源代码”，这个就是 AOP 最关键的地方，也就是我们平时所说的非入侵性；第三个“添加功能”，不改变原有的源代码，为程序添加功能。

举个例子：你需要统计若干方法的执行时间，如果不是用 AOP 技术，你要做的就是为每一个方法开始的时候获取一个开始时间，在方法结束的时候获取结束时间，二者之差就是方法的执行时间。如果对每一个需要统计的方法都做如上的操作，那代码简直就是灾难。而如果我们使用 AOP 技术，在不修改代码的情况下，添加一个统计方法执行时间的切面。代码就变得十分优雅。

### Spring AOP 的基本思想

Spring 的 AOP 实现主要以下几个步骤：

1. 初始化 AOP 容器
2. 读取配置文件
3. 将配置文件装换为 AOP 能够识别的数据结构 `Advisor`。这里展开讲一讲这个 advisor。Advisor 对象中包又含了两个重要的数据结构，一个是 `Advice`，一个是 `Pointcut`。`Advice`的作用就是描述一个切面的行为，`pointcut`描述的是切面的位置。两个数据结的组合就是”在哪里，干什么“。这样 `Advisor` 就包含了”在哪里干什么“的信息，就能够全面的描述切面了。
4. Spring 将这个 Advisor 转换成自己能够识别的数据结构 – `AdvicedSupport`。Spring 动态的将这些方法拦截器织入到对应的方法。
5. 生成动态代理代理
6. 提供调用，在使用的时候，调用方调用的就是代理方法，也就是已经织入了增强方法的方法

### 简单 AOP 框架的具体实现

#### Advisor 相关

##### Advisor

Advisor主要定义了切点 pointcut 和 通知内容 advice

```java
@Data
@ToString
public class Advisor {
    private Advice advice;
    private Pointcut pointcut;
}
```

##### BeforeMethodAdvice

BeforeMethodAdvice 为前置通知的接口，里面就定义了一个方法 before()

```java
public interface BeforeMethodAdvice extends Advice {
    void before(Method method, Object[] args, Object target);
}
```

##### AfterRunningAdvice

AfterRunningAdvice 为后置通知的接口，里面就定义了一个方法 after()

```java
public interface AfterRunningAdvice extends Advice {
    Object after(Object returnVal, Method method, Object[] args, Object target);
}
```

##### AdvisedSupport

AdvisedSupport 定义了通知的目标对象和所有的拦截器

```java
@Data
@ToString
public class AdvisedSupport extends Advisor {
    //目标对象
    private TargetSource targetSource;
    //拦截器列表
    private List<AopMethodInterceptor> list = new LinkedList<>();

    public void addAopMethodInterceptor(AopMethodInterceptor interceptor) {
        list.add(interceptor);
    }

}
```

#### Bean 相关

##### BeanDefinition

IoC 容器的默认 bean 定义

```java
@Data
@ToString
public class BeanDefinition {
    private String beanName;
    private String className;
}
```

##### AopBeanDefinition

AoP 容器的默认 bean 定义，扩展了 BeanDefinition，加上了目标类和拦截器列表

```java
@Data
@ToString
public class AopBeanDefinition extends BeanDefinition {
    private String target;
    private List<String> interceptorNames;
}
```

##### ProxyFactoryBean

空的类定义，对于 className 是这种类型的 bean，需要用 AOP 框架去解析而非普通的 IoC 框架

#### Invocation 相关

Invocation 是动态代理后，真正执行拦截器的代码，描述的就是一个方法的调用。

##### MethodInvocation

MethodInvocation 基本接口，主要定义了 proceed() 方法，以及获取方法名的 getMethod() 方法和 获取方法参数的 getArguments() 方法

```java
public interface MethodInvocation {
    Method getMethod();
    Object[] getArguments();
    Object proceed() throws Throwable;
}
```

##### ProxyMethodInvocation

ProxyMethodInvocation 接口继承 MethodInvocation，增加了获取代理对象的 getProxy() 方法

```java
public interface ProxyMethodInvocation extends MethodInvocation {
    Object getProxy();
}
```

##### ReflectiveMethodInvocation

ReflectiveMethodInvocation 实现了 ProxyMethodInvocation 接口，其中最重要的 proceed() 方法的实现：根据拦截器列表，一个个执行

```java
public class ReflectiveMethodInvocation implements ProxyMethodInvocation {

    public ReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
        List<AopMethodInterceptor> interceptorList) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.interceptorList = interceptorList;
    }

    protected final Object proxy;
    protected final Object target;
    protected final Method method;
    protected Object[] arguments = new Object[0];
    //存储所有的拦截器
    protected final List<AopMethodInterceptor> interceptorList;
    private int currentInterceptorIndex = -1;

    @Override
    public Object getProxy() {
        return proxy;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {

        //执行完所有的拦截器后，执行目标方法
        if (currentInterceptorIndex == this.interceptorList.size() - 1) {
            return invokeOriginal();
        }

        //迭代的执行拦截器。回顾上面的讲解，我们实现的拦击都会执行 im.proceed() 实际上又会调用这个方法。实现了一个递归的调用，直到执行完所有的拦截器。
        AopMethodInterceptor interceptor = interceptorList.get(++currentInterceptorIndex);
        return interceptor.invoke(this);

    }

    protected Object invokeOriginal() throws Throwable {
        return ReflectionUtil.invokeMethodUseReflection(target, method, arguments);
    }
}
```

##### CglibMethodInvocation

CglibMethodInvocation 继承 ReflectiveMethodInvocation，主要覆盖了 invokeOriginal() 方法，这个方法负责执行真正的核心业务，即代理前的方法实现。其中的 MethodProxy 为 Cglib 提供的功能

```java
public class CglibMethodInvocation extends ReflectiveMethodInvocation {

    private MethodProxy methodProxy;

    public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, List<AopMethodInterceptor> interceptorList, MethodProxy methodProxy) {
        super(proxy, target, method, arguments, interceptorList);
        this.methodProxy = methodProxy;
    }

    @Override
    protected Object invokeOriginal() throws Throwable {
        return methodProxy.invoke(target, arguments);
    }
}
```

#### Interceptor 相关

拦截器拦截的目标就是 `invcation` 包里面的调用

##### AopMethodInterceptor

AopMethodInterceptor 接口只有一个方法 invoke(MethodInvocation mi)，可以看到入参为 MethodInvocation 类型

##### BeforeMethodAdviceInterceptor

BeforeMethodAdviceInterceptor 为前置通知的拦截器，在这个拦截器里，执行前置通知方法，并继续执行 MethodInvocation 的 proceed() 方法

```
public class BeforeMethodAdviceInterceptor implements AopMethodInterceptor {

    private BeforeMethodAdvice advice;

    public BeforeMethodAdviceInterceptor(BeforeMethodAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        advice.before(mi.getMethod(), mi.getArguments(), mi);
        return mi.proceed();
    }
}
```

##### AopMethodInterceptor

AopMethodInterceptor 为后置通知的拦截器，在这个拦截器里，先继续执行 MethodInvocation 的 proceed() 方法，最后执行后置通知方法

```java
public class AfterRunningAdviceInterceptor implements AopMethodInterceptor {

    private AfterRunningAdvice advice;

    public AfterRunningAdviceInterceptor(AfterRunningAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object returnVal = mi.proceed();
        advice.after(returnVal, mi.getMethod(), mi.getArguments(), mi);
        return returnVal;
    }
}
```

#### Factory 相关

##### AopBeanFactory

继承 BeanFactory，对于需要使用 AOP 的 bean，执行 Cglib 相关代码来进行动态织入；对于不需要使用 AOP 的 bean，执行 BeanFactory 里的方法来实例化 bean

```java
public class AopBeanFactory extends BeanFactory {
    private static final ConcurrentHashMap<String, AopBeanDefinition> aopBeanDefinitionMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Object> aopBeanMap = new ConcurrentHashMap<>();

    @Override
    public Object getBean(String name) throws Exception {
        Object aopBean = aopBeanMap.get(name);

        if (aopBean != null) {
            return aopBean;
        }
        if (aopBeanDefinitionMap.containsKey(name)) {
            AopBeanDefinition aopBeanDefinition = aopBeanDefinitionMap.get(name);
            AdvisedSupport advisedSupport = getAdvisedSupport(aopBeanDefinition);
            aopBean = new CglibAopProxy(advisedSupport).getProxy();
            aopBeanMap.put(name, aopBean);

        } else {
            aopBean = super.getBean(name);

        }
        return aopBean;
    }

    protected void registerBean(String name, AopBeanDefinition aopBeanDefinition) {
        aopBeanDefinitionMap.put(name, aopBeanDefinition);
    }

    private AdvisedSupport getAdvisedSupport(AopBeanDefinition aopBeanDefinition) throws Exception {

        AdvisedSupport advisedSupport = new AdvisedSupport();
        List<String> interceptorNames = aopBeanDefinition.getInterceptorNames();
        if (interceptorNames != null && !interceptorNames.isEmpty()) {
            for (String interceptorName : interceptorNames) {

                Advice advice = (Advice) getBean(interceptorName);
                Advisor advisor = new Advisor();
                advisor.setAdvice(advice);

                if (advice instanceof BeforeMethodAdvice) {
                    AopMethodInterceptor interceptor = new BeforeMethodAdviceInterceptor((BeforeMethodAdvice) advice);          
                    advisedSupport.addAopMethodInterceptor(interceptor);
                }

                if (advice instanceof AfterRunningAdvice) {
                    AopMethodInterceptor interceptor = AfterMethodAdviceInterceptor((BeforeMethodAdvice) advice);   
                    advisedSupport.addAopMethodInterceptor(interceptor);
                }

            }
        }

        TargetSource targetSource = new TargetSource();
        Object object = getBean(aopBeanDefinition.getTarget());
        targetSource.setTargetClass(object.getClass());
        targetSource.setTargetObject(object);
        advisedSupport.setTargetSource(targetSource);
        return advisedSupport;

    }
}
```

#### 核心相关

##### AopProxy

AopProxy 接口定义了获取代理对象的方法

```java
public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
```

##### CglibAopProxy

CglibAopProxy 实现了 AopProxy 接口，调用 Cglib 相关代码实现代理 bean 的创建。Cglib 先给 Enchancer 类设置一个需要被代理的类，然后设置 Callback。Callback 可以理解成生成的代理类的方法被调用时，会执行的真正逻辑。即当某个方法被代理时，真正执行的是定义在 callback 里的逻辑，而非这个方法里的逻辑

```java
@Data
public class CglibAopProxy implements AopProxy {
    private static final Logger logger = LoggerFactory.getLogger(CglibAopProxy.class);
    private AdvisedSupport advised;
    private Object[] constructorArgs;
    private Class<?>[] constructorArgTypes;

    public CglibAopProxy(AdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(null);
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Class<?> rootClass = advised.getTargetSource().getTargetClass();
        if (classLoader == null) {
            classLoader = ClassUtil.getDefaultClassLoader();
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(rootClass.getSuperclass());
        //增加拦截器的核心方法
        Callback callbacks = getCallBack(advised);
        enhancer.setCallback(callbacks);
        enhancer.setClassLoader(classLoader);
        if (constructorArgs != null && constructorArgs.length > 0) {
            return enhancer.create(constructorArgTypes, constructorArgs);
        }
        return enhancer.create();
    }

    private Callback getCallBack(AdvisedSupport advised) {
        return new DynamicAdvisedInterceptor(advised.getList(), advised.getTargetSource());
    }
}
```

##### DynamicAdvisedInterceptor

DynamicAdvisedInterceptor 实现了 Cglib 的 MethodInterceptor 接口，这就是 callback 里的具体实现了，即被代理的方法具体逻辑

```java
public class DynamicAdvisedInterceptor implements MethodInterceptor {
    protected final List<AopMethodInterceptor> interceptorList;
    protected final TargetSource targetSource;

    public DynamicAdvisedInterceptor(List<AopMethodInterceptor> interceptorList, TargetSource targetSource) {
        this.interceptorList = interceptorList;
        this.targetSource = targetSource;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        MethodInvocation invocation =
            new CglibMethodInvocation(obj, targetSource.getTargetObject(), method, args, interceptorList, proxy);
        return invocation.proceed();
    }
}
```

##### ApplicationContext

加载 yaml 配置文件，注册 bean 配置

```java
public class ApplicationContext extends AopBeanFactory {
    private String fileName;

    public ApplicationContext(String fileName) {
        this.fileName = fileName;
    }

    public void init() {
        loadFile();
    }

    private void loadFile() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        List<AopBeanDefinition> beanDefinitions = YamlUtil.loadYaml(is);
        if (beanDefinitions != null && !beanDefinitions.isEmpty()) {
            for (AopBeanDefinition beanDefinition : beanDefinitions) {
                Class<?> clz = ClassUtil.loadClass(beanDefinition.getClassName());
                if (clz == ProxyFactoryBean.class) {
                    registerBean(beanDefinition.getBeanName(), beanDefinition);
                } else {
                    registerBean(beanDefinition.getBeanName(), (BeanDefinition) beanDefinition);
                }
            }
        }
    }
}
```

#### 配置文件

```yaml
- !!com.maycur.aop.bean.AopBeanDefinition
  beanName: beforeMethod
  className: com.maycur.aop.test.StartTimeBeforeMethod
- !!com.maycur.aop.bean.AopBeanDefinition
  beanName: afterMethod
  className: com.maycur.aop.test.EndTimeAfterMethod
- !!com.maycur.aop.bean.AopBeanDefinition
  beanName: testService
  className: com.maycur.aop.test.TestService
- !!com.maycur.aop.bean.AopBeanDefinition
  beanName: testServiceProxy
  className: com.maycur.aop.bean.ProxyFactoryBean
  target: testService
  interceptorNames: [beforeMethod, afterMethod]
```

#### 测试相关

##### StartTimeBeforeMethod

```java
public class StartTimeBeforeMethod implements BeforeMethodAdvice {
    private static final Logger logger = LoggerFactory.getLogger(StartTimeBeforeMethod.class);

    @Override
    public void before(Method method, Object[] args, Object target) {
        long startTime = System.currentTimeMillis();
        logger.info("开始计时");
        ThreadLocalUtil.set(startTime);
    }
}
```

##### EndTimeAfterMethod

```java
public class EndTimeAfterMethod implements AfterRunningAdvice {
    private static final Logger logger = LoggerFactory.getLogger(EndTimeAfterMethod.class);

    @Override
    public Object after(Object returnVal, Method method, Object[] args, Object target){
        long endTime = System.currentTimeMillis();
        long startTime = ThreadLocalUtil.get();
        ThreadLocalUtil.remove();
        logger.info("方法耗时：" + (endTime - startTime) + "ms");
        return returnVal;
    }
}
```

##### TestService

```java
public class TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    public void testMethod() throws InterruptedException {
        logger.info("this is a test method");
        Thread.sleep(1000);
    }
}
```

##### MainTest

```java
public class MainTest {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ApplicationContext("applicationBean.yaml");
        applicationContext.init();
        TestService testService = (TestService) applicationContext.getBean("testServiceProxy");
        testService.testMethod();
    }
}
```

### 调用分析

1. 加载配置文件，转换成 AopBeanDefinition 对象

2. 注册 bean，如果 bean class 为 ProxyFactoryBean，则使用 aop bean 的方式注册；否则使用普通 bean 的方式注册

3. 获取 testServiceProxy bean，发现这个 bean 属于 aop bean，初始化 aop bean：

   * 根据 aop bean 的定义，获取拦截器列表 interceptorNames
   * 对于每一个拦截器，初始化拦截器对应的 bean。如果是前置通知拦截器，则创建 BeforeMethodAdviceInterceptor；否则创建 AfterMethodAdviceInterceptor
   * 将所有 AdviceInterceptor 对象放入 AdvisedSupport 中

4. 调用 Cglib 相关方法创建 proxy bean，并返回代理对象

5. 调用代理对象的 testMethod() 方法

6. 因为是代理对象，故 TestService 的testMethod() 方法并不会执行，而是执行 Cglib 里定义的 callback() 方法

7. callback 里执行的是 CglibMethodInvocation 的 proceed() 方法

8. CglibMethodInvocation 的 proceed() 方法会遍历所有的 interceptor：

   * 对于每一个 interceptor 调用对应的 invoke()
   * 而每一个 interceptor 都会递归调用 CglibMethodInvocation 的 proceed() 方法，递归出口即当所有的 interceptor 的 invoke() 方法都被执行了
   * 最后执行 methodProxy.invoke(target, arguments)，即被代理前的方法
   * 这里会形成一个调用链。比如 test() 被代理，有 before1 和 before2 前置通知和after1 和 after2 后置通知，当 CglibMethodInvocation 的 proceed() 方法被执行时：
     * 首先调用 before1的 invoke() 方法，执行完后继续调用 CglibMethodInvocation 的 proceed() 方法
     * 回到 CglibMethodInvocation 的 proceed() 方法，调用 before2的 invoke() 方法，执行完后继续调用 CglibMethodInvocation 的 proceed() 方法
     * 回到 CglibMethodInvocation 的 proceed() 方法，调用 after1的 invoke() 方法，因为是后置通知，所以不执行对应的通知，而是继续调用 CglibMethodInvocation 的 proceed() 方法
     * 回到 CglibMethodInvocation 的 proceed() 方法，调用 after2的 invoke() 方法，因为是后置通知，所以不执行对应的通知，而是继续调用 CglibMethodInvocation 的 proceed() 方法
     * 回到 CglibMethodInvocation 的 proceed() 方法，所有的 interceptor 都已遍历执行完成，所以 invokeOriginal() 方法，即 methodProxy.invoke(target, arguments) 执行被代理前的方法逻辑
     * 执行完成后，回到 after2的 invoke() 方法，执行对应的通知逻辑
     *  after2的 invoke() 方法执行完成后，回到 after2的 invoke() 方法，执行对应的通知逻辑




