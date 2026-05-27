package com.example.campusnavigator.routing;

import android.util.Log;

import com.example.campusnavigator.firebase.model.ConnectionModel;
import com.example.campusnavigator.firebase.model.LocationModel;

import java.util.*;

public class PathFinder {

    private final Map<String, LocationModel> locationMap = new HashMap<>();
    private final Map<String, List<ConnectionModel>> adjacency = new HashMap<>();

    public PathFinder() { }

    // Call this after Firebase load
    // locations: list of LocationModel (with getId() set)
    // connections: list of ConnectionModel (must have sourceId/destId set)
    public void buildGraph(List<LocationModel> locations, List<ConnectionModel> connections) {

        locationMap.clear();
        adjacency.clear();

        // Build locationMap using Firestore ID
        for (LocationModel loc : locations) {
            if (loc != null && loc.getId() != null) {
                locationMap.put(loc.getId(), loc);
            }
        }

        // Build adjacency using Firestore IDs (sourceId/destId)
        for (ConnectionModel conn : connections) {
            if (conn == null) continue;

            String srcId = conn.getSourceId();
            String dstId = conn.getDestId();

            if (srcId == null || dstId == null) continue;

            srcId = srcId.trim();
            dstId = dstId.trim();

            adjacency.putIfAbsent(srcId, new ArrayList<>());
            adjacency.putIfAbsent(dstId, new ArrayList<>());

            adjacency.get(srcId).add(conn);
            adjacency.get(dstId).add(conn); // undirected
        }

        Log.d("PathFinder", "Graph built. Locations=" + locationMap.size() +
                " AdjacencyNodes=" + adjacency.size());
    }

    // Now pathFinder uses Firestore IDs correctly
    public List<String> findShortestPath(String startId, String endId) {

        if (!locationMap.containsKey(startId) || !locationMap.containsKey(endId)) {
            Log.e("PathFinder", "StartId or EndId not found");
            return null;
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();

        for (String id : locationMap.keySet()) dist.put(id, Double.MAX_VALUE);

        dist.put(startId, 0.0);

        PriorityQueue<String> pq =
                new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(startId);

        while (!pq.isEmpty()) {
            String current = pq.poll();

            if (current == null) break;

            // If current distance is still MAX, no point in expanding
            if (dist.get(current) == Double.MAX_VALUE) continue;

            if (!adjacency.containsKey(current)) continue;

            for (ConnectionModel conn : adjacency.get(current)) {

                // Determine neighbor ID by comparing conn's source/dest IDs
                String neighbor = null;
                if (current.equals(conn.getSourceId())) {
                    neighbor = conn.getDestId();
                } else if (current.equals(conn.getDestId())) {
                    neighbor = conn.getSourceId();
                } else {
                    continue; // shouldn't happen but safe-guard
                }

                if (neighbor == null) continue;

                double newDist = dist.get(current) + conn.getDistanceMeters();

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        if (!prev.containsKey(endId) && !endId.equals(startId)) {
            Log.e("PathFinder", "No path found");
            return null;
        }

        List<String> path = new ArrayList<>();
        String step = endId;

        // If start==end, return single-element list
        while (step != null) {
            path.add(step);
            step = prev.get(step);
        }

        Collections.reverse(path);
        return path;
    }
}
