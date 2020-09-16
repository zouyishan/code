/*
 * This file is part of the TimeFinder project.
 * Visit http://www.timefinder.de for more information.
 * Copyright 2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cuny.blender.englishie.algorithm.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class implements a bi-partite, undirected, weighted Graph.
 * Currently only a helper class for the PathGrowingAlgorithm.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class WeightedGraph {

    private Set<Integer> nodesWithMoreThanOneEdge;
    private Map<Integer, Float>[] adjacentList;
    private int noOfEdges = 0;

    public WeightedGraph(int nodes) {
        adjacentList = new HashMap[nodes];
        nodesWithMoreThanOneEdge = new HashSet<Integer>();
    }

    public boolean addEdge(int x, int y, float weight) {
        Map<Integer, Float> map = adjacentList[x];
        if (map == null) {
            map = new HashMap<Integer, Float>();
            adjacentList[x] = map;
        }
        boolean ret = map.put(y, weight) == null;

        map = adjacentList[y];
        if (map == null) {
            map = new HashMap<Integer, Float>();
            adjacentList[y] = map;
        }
        map.put(x, weight);

        if (ret) {
            nodesWithMoreThanOneEdge.add(x);
            nodesWithMoreThanOneEdge.add(y);
            noOfEdges++;
        }

        return ret;
    }

    /**
     * This method returns a node which has at least one neighbor node.
     * @return -1 if the degree of all nodes is 0
     */
    public int getOneNodeWithAnEdge() {
        if (nodesWithMoreThanOneEdge.size() > 0) {
            return nodesWithMoreThanOneEdge.iterator().next();
        } else {
            return -1;
        }
    }

    public boolean removeEdge(int x, int y) {
        if (adjacentList[x] == null) {
            return false;
        }
        if (adjacentList[x].remove(y) == null) {
            return false;
        }

        noOfEdges--;
        if (adjacentList[x].size() == 0) {
            nodesWithMoreThanOneEdge.remove(x);
        }

        adjacentList[y].remove(x);
        if (adjacentList[y].size() == 0) {
            nodesWithMoreThanOneEdge.remove(y);
        }
        return true;
    }

    public int getNoOfEdges() {
        return noOfEdges;
    }

    public Map<Integer, Float> getNeighbors(int x) {
        return adjacentList[x];
    }

    public boolean removeNodeAndNeighbors(int x) {
        boolean ret = nodesWithMoreThanOneEdge.remove(x);
        if (!ret) {
            return false;
        }
        for (Integer n : adjacentList[x].keySet()) {
            adjacentList[n].remove(x);
            if (adjacentList[n].size() < 1) {
                nodesWithMoreThanOneEdge.remove(n);
            }
            noOfEdges--;
        }
        assert noOfEdges >= 0;
        adjacentList[x].clear();
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int n = 0; n < adjacentList.length; n++) {
            boolean firstValue = true;
            Map<Integer, Float> map = adjacentList[n];
            sb.append(n);
            sb.append(':');
            // sort against int to have a better overview
            for (Entry<Integer, Float> entry : new TreeMap<Integer, Float>(map).entrySet()) {
                if (!firstValue) {
                    sb.append(", ");
                }
                firstValue = false;
                sb.append('(');
                sb.append(entry.getKey());
                sb.append(';');
                sb.append(entry.getValue());
                sb.append(')');
            }
            sb.append("<br />\n");
        }

        return sb.toString();
    }
}