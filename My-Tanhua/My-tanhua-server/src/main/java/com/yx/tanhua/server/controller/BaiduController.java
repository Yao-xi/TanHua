package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.BaiduService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("baidu")
@Slf4j
public class BaiduController {

    @Autowired
    private BaiduService baiduService;

    /**
     * 更新位置
     *
     * @param param
     * @return
     */
    @PostMapping("location")
    public ResponseEntity<Void> updateLocation(@RequestBody Map<String, Object> param) {
        try {
            // 经度
            // Double longitude = Double.valueOf(param.get("longitude").toString());
            // TODO 2021-02-09 23:16 Yaoxi 测试数据
            Double longitude = 121.512253;
            // 纬度
            // Double latitude = Double.valueOf(param.get("latitude").toString());
            // TODO 2021-02-09 23:16 Yaoxi 测试数据
            Double latitude = 31.24094;
            // 地址
            String address = param.get("addrStr").toString();
            log.debug("更新位置~ longitude="+longitude+" latitude="+latitude+" address="+address);
            Boolean bool = this.baiduService.updateLocation(longitude, latitude, address);
            if (bool) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}