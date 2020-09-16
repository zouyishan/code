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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Used for the augmenting tree in KuhnMunkresAlgorithm
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class SimpleTree {

    private static final int COLLECTION_CAPACITY = 10;
    private int high = 0;
    private MainNode mainNode;

    public SimpleTree() {
        mainNode = new MainNode();
    }

    public void setRoots(Collection<Integer> coll) {
        assert high == 0;
        SimpleNode lowerNode;
        
        for (int value : coll) {
            lowerNode = createNode(value, null);
            mainNode.currentMap.put(value, lowerNode);
        }
    }

    /**
     * This method adds a node to the tree. The value should be the id of the 
     * node.
     */
    public void addBranches(int value, Collection<Integer> coll) {
        SimpleNode lowerNode = mainNode.currentMap.get(value);
        assert lowerNode != null;

        if (mainNode.higher == null) {
            mainNode.higher = new MainNode();
            mainNode.higher.lower = mainNode;
        }

        HashMap<Integer, SimpleNode> neighbors = mainNode.higher.currentMap;

        for (int n : coll) {
            neighbors.put(n, createNode(n, lowerNode));
        }
    }

    private final SimpleNode createNode(int value, SimpleNode lowerNode) {
        SimpleNode n = new SimpleNode(value);
        n.lower = lowerNode;
        return n;
    }

    public void higher() {
        high++;
        assert high > 0;
        assert mainNode.higher != null;
        mainNode = mainNode.higher;
    }

    public void lower() {
        high--;
        assert high >= 0;
        assert mainNode.lower != null;
        mainNode = mainNode.lower;
    }

    public Set<Integer> getNeighbors() {
        assert mainNode.currentMap != null;
        return mainNode.currentMap.keySet();
    }

    public Iterator<Integer> getToRootIterator(final int value) {
        assert mainNode.currentMap.get(value) != null;

        return new Iterator<Integer>() {

            SimpleNode node = mainNode.currentMap.get(value);

            public boolean hasNext() {
                return node.lower != null;
            }

            public Integer next() {
                assert node.lower != null : "Can't go lower than the root of the tree";
                node = node.lower;
                return node.currentVal;
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    private static class SimpleNode {

        int currentVal;
        SimpleNode lower;
        SimpleNode neighbor;

        SimpleNode(int val) {
            currentVal = val;
        }
    }

    private static class MainNode {

        HashMap<Integer, SimpleNode> currentMap;
        MainNode higher;
        MainNode lower;

        MainNode() {
            currentMap = new HashMap<Integer, SimpleNode>(COLLECTION_CAPACITY);
        }
    }
}
