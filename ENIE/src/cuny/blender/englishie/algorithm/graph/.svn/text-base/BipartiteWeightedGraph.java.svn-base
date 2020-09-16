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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class implements a bi-partite, undirected, weighted Graph.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BipartiteWeightedGraph {

    // x = 0, 1, 2; y = 0, 1
    // now xList has 3 entries where the first parameter (integer)
    // is the index of an y node and the second parameter (float)
    // is the weight
    private List<Map<Integer, Float>> xList;
    private List<Map<Integer, Float>> yList;

    public BipartiteWeightedGraph() {
        xList = new ArrayList<Map<Integer, Float>>();
        yList = new ArrayList<Map<Integer, Float>>();
    }

    public BipartiteWeightedGraph(int xNodes, int yNodes) {
        this();
        addXNodes(xNodes);
        addYNodes(yNodes);
    }

    public int getYNodes() {
        return yList.size();
    }

    public int getXNodes() {
        return xList.size();
    }

    /**
     * Return all neighbor nodes (from Y) of the specified x node.
     */
    public Set<Integer> getX(int xNode) {
        return xList.get(xNode).keySet();
    }

    /**
     * Return all neighbor nodes (from X) of the specified y node.
     */
    public Set<Integer> getY(int yNode) {
        return yList.get(yNode).keySet();
    }

    public void addXNodes(int number) {
        for (int i = 0; i < number; i++) {
            xList.add(new HashMap<Integer, Float>());
        }
    }

    public void addYNodes(int number) {
        for (int i = 0; i < number; i++) {
            yList.add(new HashMap<Integer, Float>());
        }
    }

    public Float getConnection(int xNode, int yNode) {
        return xList.get(xNode).get(yNode);
    }

    public void setConnection(int xNode, int yNode, float weight) {
        xList.get(xNode).put(yNode, weight);
        yList.get(yNode).put(xNode, weight);
    }

    public boolean removeConnection(int xNode, int yNode) {
        boolean ret = xList.get(xNode).remove(yNode) != null;
        if (ret) {
            yList.get(yNode).remove(xNode);
        }
        return ret;
    }

    /**
     * This method removes all edges from the graph.
     * Nodes will stay.
     */
    public void clear() {
        for (Map s : xList) {
            s.clear();
        }
        for (Map s : yList) {
            s.clear();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map<Integer, Float> map : yList) {
            boolean firstValue = true;
            // sort against int to have a better overview
            for (Entry<Integer, Float> e : new TreeMap<Integer, Float>(map).entrySet()) {
                if (!firstValue) {
                    sb.append(", ");
                }
                firstValue = false;
                sb.append(e.getKey());
                sb.append(':');
                sb.append(e.getValue());
            }
            sb.append("<br />\n");
        }

        return sb.toString();
    }
}