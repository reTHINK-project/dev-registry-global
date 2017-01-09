package com.MVC.Service;

import com.MVC.dht.DHTManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by Half-Blood on 1/4/2017.
 */
@RestController
@RequestMapping("/Entity")
public class EntityService {
    @Autowired
    private DHTManager dhtManager = null;

    public String getEntitybyGUID(String GUID) throws IOException, ClassNotFoundException {
        return dhtManager.getInstance().get(GUID);
    }

    public void insertEntity(String jwt, String GUID) throws IOException {
        dhtManager.getInstance().put(GUID, jwt);
    }

    public void updateEntity(String jwt, String GUID) throws IOException {
        this.dhtManager.put(GUID, jwt);
    }


}

