/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.commons.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel;
import org.apache.causeway.commons.graph.GraphUtils.GraphKernel.GraphCharacteristic;

class GraphUtilsTest {

    @Test
    void subgraph() {
        var graph = new GraphKernel(4, ImmutableEnumSet.noneOf(GraphCharacteristic.class));
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);

        assertFalse(graph.isUndirected());
        assertEquals(4, graph.nodeCount());
        assertEquals(3, graph.edgeCount());

        // identity
        var subgraphId = graph.subGraph(new int[] {0, 1, 2, 3});
        assertFalse(subgraphId.isUndirected());
        assertEquals(4, subgraphId.nodeCount());
        assertEquals(3, subgraphId.edgeCount());

        // disjoint
        var subgraphDisjointNoEdges = graph.subGraph(new int[] {0, 3});
        assertFalse(subgraphDisjointNoEdges.isUndirected());
        assertEquals(2, subgraphDisjointNoEdges.nodeCount());
        assertEquals(0, subgraphDisjointNoEdges.edgeCount());

        // subgraph w/ 3 nodes, reordered
        var subgraph3 = graph.subGraph(new int[] {3, 1, 2});
        assertFalse(subgraph3.isUndirected());
        assertEquals(3, subgraph3.nodeCount());
        assertEquals(2, subgraph3.edgeCount());

    }

}
