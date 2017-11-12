package com.guild.api.demo.controller;

import com.guild.api.demo.model.Logistics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogisticsController {

    @GetMapping(value = "/logistics/{logisticsId}")
    public @ResponseBody Logistics getLogistics(@PathVariable String logisticsId) throws InterruptedException {
        Thread.sleep(1000);
        return new Logistics(logisticsId, "sf-express");
    }

}
