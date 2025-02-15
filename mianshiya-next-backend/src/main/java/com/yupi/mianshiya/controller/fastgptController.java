package com.yupi.mianshiya.controller;

import com.yupi.mianshiya.fastgpt.FastGPTClient;
import com.yupi.mianshiya.model.entity.User;
import com.yupi.mianshiya.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ai接口
 */
@RestController
@RequestMapping("/chat")
@Slf4j
@AllArgsConstructor
public class fastgptController {
    private final FastGPTClient fastGPTClient;
    private final UserService userService;


}
