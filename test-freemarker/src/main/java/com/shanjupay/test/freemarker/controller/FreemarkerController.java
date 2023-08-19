package com.shanjupay.test.freemarker.controller;


import com.shanjupay.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class FreemarkerController {
//    @Autowired
//    RestTemplate restTemplate;

//    @RequestMapping("/test1")
//    public String freemarker(Map<String, Object> map) {
//        // 设置数据
//        map.put("testName", "world");
//        //返回模板文件名称,在application.yml中配置了freemarker的suffix .ftl
//        return "test1";
//    }

    @RequestMapping("/test1")
    public String freemarker(Map<String, Object> map, HttpServletRequest request){
        //向数据模型放数据
        map.put("testName", "world");
        Student stu1 = new Student();
//        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>(); stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据
        map.put("stus",stus);
        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
//        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        //向数据模型放数据
        map.put("stu1",stu1);
        //向数据模型放数据
        map.put("stuMap",stuMap);

        request.setAttribute("attr1", "test");
        HttpSession session = request.getSession();
        session.setAttribute("session1", "user1");
        //返回模板文件名称
        return "test1";
    }
}
