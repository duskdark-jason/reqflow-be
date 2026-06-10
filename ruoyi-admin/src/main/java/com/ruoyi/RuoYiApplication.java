package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("统一需求流转平台启动成功\n" +
                "  ____            _____ _                 \n" +
                " |  _ \\ ___  __ _|  ___| | _____      __ \n" +
                " | |_) / _ \\/ _` | |_  | |/ _ \\ \\ /\\ / / \n" +
                " |  _ <  __/ (_| |  _| | | (_) \\ V  V /  \n" +
                " |_| \\_\\___|\\__, |_|   |_|\\___/ \\_/\\_/   \n" +
                "            |___/                         ");
    }
}
