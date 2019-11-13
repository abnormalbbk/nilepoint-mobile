package com.nilepoint.monitorevaluatemobile.dtn;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashaw on 11/21/17.
 *
 * This class will track DTN communication to devices across all convergence layers.
 *
 */

public class MobileDeviceRegistry {
    static MobileDeviceRegistry instance;

    static {
        instance = new MobileDeviceRegistry();
    }

    public Map<Node, MobileDevice> nodeDeviceMap = new HashMap<>();
    Map<MobileDevice, Node> mobileDeviceNodeMap = new HashMap<>();
    Map<String, MobileDevice> mobileDeviceMap = new HashMap<>();
    Map<MobileDevice, Photo> devicePhotoMap = new HashMap<>();
    Map<MobileDevice, List<String>> bundleTracker = new HashMap<>();

    public void track(MobileDevice device, Node node){
        nodeDeviceMap.put(node, device);
        mobileDeviceMap.put(device.getId(),device);
        bundleTracker.put(device,new ArrayList<String>());
        mobileDeviceNodeMap.put(device, node);
    }
    public void untrack(MobileDevice device, Node node){
        System.out.println(String.format("Untracking device %s and node %s",device, node));
        nodeDeviceMap.remove(node);
        mobileDeviceMap.remove(device.getId());
        bundleTracker.remove(device);
        mobileDeviceNodeMap.remove(device);
    }
    public void untrack(Node node){
        MobileDevice device =  nodeDeviceMap.get(node);
        untrack(device, node);
    }

    public boolean isTracked(MobileDevice device){
        return mobileDeviceNodeMap.keySet().contains(device);
    }

    public boolean track(MobileDevice device, Bundle bundle){
        List<String> bundleIds = bundleTracker.get(device);

        if (bundleIds.contains(bundle.getId())){
            boolean containsId = bundleIds.contains(bundle.getId());

            bundleIds.add(bundle.getId());

            return containsId;
        }

        return false;
    }

    public static MobileDeviceRegistry getInstance() {
        return instance;
    }

    public MobileDevice findById(String id){
        return mobileDeviceMap.get(id);
    }
    public Node findNodeByDevice(MobileDevice device){
        return mobileDeviceNodeMap.get(device);
    }

    public Photo getPhoto(MobileDevice device){
        return devicePhotoMap.get(device);
    }

    public void addPhoto(MobileDevice device, Photo photo){
        devicePhotoMap.put(device, photo);
    }

    public MobileDevice findByNode(Node node){
        return nodeDeviceMap.get(node);
    }
}
