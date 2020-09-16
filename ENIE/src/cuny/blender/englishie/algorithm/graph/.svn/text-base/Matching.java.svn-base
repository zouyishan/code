/*
 * This file is part of the TimeFinder project.
 * Visit http://www.timefinder.de for more information.
 * Copyright 2008 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cuny.blender.englishie.algorithm.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Matching {

    private Set<Edge> edges;
    private float totalSum;
    private float floatMax;

    public Matching(int cap) {
        edges = new HashSet<Edge>(cap);
        floatMax = Float.MAX_VALUE;
    }

    public Matching() {
        this(10);
    }

    public void setFloatMax(float floatMax) {
        this.floatMax = floatMax;
    }

    public boolean addEdge(int x, int y, float weight) {
        return addEdge(new UndirectedEdge(x, y, weight));
    }

    public boolean addEdge(Edge edge) {
        boolean ret = edges.add(edge);
        if (ret) {
            totalSum += edge.getWeight();
        }
        return ret;
    }

    public Collection<? extends Edge> getEdges() {
        return edges;
    }

    public float getTotalSum() {
        return totalSum;
    }

    public int[][] toBipartiteArrayModY(int offset) {
        // We assume that all edges in the matching are x, y
        int maxColumn = -1;
        for (Edge e : edges) {
            if (e.getX() > maxColumn) {
                maxColumn = e.getX();
            }
        }
        if (maxColumn < 0) {
            return null;
        }

        int[][] matrix = new int[maxColumn + 1][];

        for (Edge e : edges) {
            if (e.getWeight() >= floatMax) {
                continue;
            }

            int y = e.getY();
            y %= offset;
            matrix[e.getX()] = new int[]{y, e.getX()};
        }

        return matrix;
    }

    public int[][] toBipartiteArrayModX(int offset) {
        // We assume that all edges in the matching are y, x
        int maxColumn = -1;
        for (Edge e : edges) {
            if (e.getY() > maxColumn) {
                maxColumn = e.getY();
            }
        }
        if (maxColumn < 0) {
            return null;
        }

        int[][] matrix = new int[maxColumn + 1][];

        for (Edge e : edges) {
            if (e.getWeight() >= floatMax) {
                continue;
            }

            int x = e.getX();
            x %= offset;
            matrix[e.getY()] = new int[]{x, e.getY()};
        }

        return matrix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Edge e : edges) {
            sb.append(e.toString());
            sb.append("<br />\n");
        }

        return sb.toString();
    }
}
