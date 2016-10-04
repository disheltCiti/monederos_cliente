package citi.g500.wrappers;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by ftorres on 22/09/2016.
 */
public class BeaconStructure {

    private final HashMap<String, Beacon> structure;
    private final HashMap<String, LinkedList<Double>> distances;
    private final int monitorizationWindow;

    public BeaconStructure(int monitorizationWindow) {
        this.structure = new HashMap();
        this.distances = new HashMap();
        this.monitorizationWindow = monitorizationWindow;
    }

    public void addBeacon(Beacon beacon) {
        String id = beacon.getUui() + ";" + beacon.getMajor() + ";" + beacon.getMinor();
        this.structure.put(id, beacon);
        if (this.distances.containsKey(id)) {
            //Keeping the window size
            if (this.distances.get(id).size() >= this.monitorizationWindow) {
                this.distances.get(id).remove();
            }
            this.distances.get(id).add(beacon.getDistance());
        } else {
            this.distances.put(id, new LinkedList());
            this.distances.get(id).add(beacon.getDistance());
        }
    }

    public Beacon getClosestBeacon() {
        List<Map.Entry<String, Beacon>> list =
                new LinkedList<Map.Entry<String, Beacon>>(structure.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Beacon>>() {
            public int compare(Map.Entry<String, Beacon> o1, Map.Entry<String, Beacon> o2) {
                if (o1.getValue().getDistance() > o2.getValue().getDistance()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return list.get(0).getValue();
    }

    public int getSize() {
        return this.structure.size();
    }

    @Override
    public String toString() {
        String out = "Structure:\n";
        for (Map.Entry<String, Beacon> entry : structure.entrySet()) {
            out += (entry.getKey() + " => " + entry.getValue() + "\n");
        }
        return out;
    }

    public Beacon getClosestBeaconWCentrality() {
        Beacon minBeacon = null;
        double minDistance = -1;
        double distanceAcumulator = 0;
        for (Map.Entry<String, LinkedList<Double>> entry : distances.entrySet()) {
            for (double distance : entry.getValue()) {
                distanceAcumulator += distance;
                distanceAcumulator /= this.distances.size();
            }
            if (minDistance == -1 || minDistance > distanceAcumulator) {
                minDistance = distanceAcumulator;
                minBeacon = this.structure.get(entry.getKey());
            }
        }
        return minBeacon;
    }
}
