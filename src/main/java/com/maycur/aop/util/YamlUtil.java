package com.maycur.aop.util;

import com.maycur.aop.bean.AopBeanDefinition;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;

public class YamlUtil {

    public static List<AopBeanDefinition> loadYaml(InputStream is) {
        Constructor constructor = new Constructor(List.class);
        Yaml yaml = new Yaml(constructor);
        return (List<AopBeanDefinition>) yaml.load(is);
    }

}
