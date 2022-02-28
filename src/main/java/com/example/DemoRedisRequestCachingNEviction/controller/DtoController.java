package com.example.DemoRedisRequestCachingNEviction.controller;

import com.example.DemoRedisRequestCachingNEviction.RequestResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/dtos")
@OpenAPIDefinition( info = @Info(title = "Demo DTO controller", version = "1.0", description = "Swagger DTO details"))
@Tag(name = "DEMO_DTO")
public class DtoController {
    @GetMapping(value = "/getDto")
    @ResponseBody
    public RequestResponse getDTO(){
        RequestResponse requestResponse = new RequestResponse();
        requestResponse.setMessage("Got DTO");
        requestResponse.setStatus(HttpStatus.OK);
        return requestResponse;
    }
}
