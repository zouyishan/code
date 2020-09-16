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

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class AssignmentHelper {

    // Until this floating number the following is true: f + 1 > f
    public static float LAST_GOOD_FLOAT = 1.6777214e7f;

    private static float calculateSum(float costMatrix[][], int assigment[][], float floatMax) {
        float sum = 0;
        for (int x = 0; x < costMatrix[0].length; x++) {
            if (assigment[x] != null) {
                sum += costMatrix[assigment[x][0]][x];
            } else {
                sum += floatMax;
            }
        }

        return sum;
    }

    public static float calculateSum(float costMatrix[][], int assigment[][]) {
        return calculateSum(costMatrix, assigment, getFloatMax(costMatrix.length));
    }

    /**
     * This method returns the maximal float where the following is valid:
     * n * floatMax + 1 > n * floatMax     
     */
    public static float getFloatMax(int n) {
        return (int) (LAST_GOOD_FLOAT / n);
    }
}
