import java.util.*;

public class GenomeCluster {
    public Map<String, Genome> genomeMap = new HashMap<>();

    public void addGenome(Genome genome) {
        genomeMap.put(genome.id, genome);
    }

    public boolean contains(String genomeId) {
        return genomeMap.containsKey(genomeId);
    }

    public Genome getMinEvolutionGenome() {
        Genome minGenome = null;
        for (Genome genome : genomeMap.values()) {
            if (minGenome == null || genome.evolutionFactor < minGenome.evolutionFactor) {
                minGenome = genome;
            }
        }
        return minGenome;
    }

    private static class Node {
        String id;
        int distance;

        public Node(String id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }
    public int dijkstra(String startId, String endId) {
        if (!genomeMap.containsKey(startId) || !genomeMap.containsKey(endId)) {
            return -1;
        }

        if (startId.equals(endId)) {
            return 0;
        }

        Map<String, Integer> distances = new HashMap<>();
        for (String genomeId : genomeMap.keySet()) {
            distances.put(genomeId, Integer.MAX_VALUE);
        }
        distances.put(startId, 0);

        PriorityQueue<Node> pq = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.distance)
        );
        pq.add(new Node(startId, 0));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentId = current.id;

            if (currentId.equals(endId)) {
                return current.distance;
            }

            if (visited.contains(currentId)) {
                continue;
            }

            visited.add(currentId);

            Genome currentGenome = genomeMap.get(currentId);
            for (Genome.Link link : currentGenome.links) {
                if (!genomeMap.containsKey(link.target)) {
                    continue;
                }

                int newDistance = distances.get(currentId) + link.adaptationFactor;

                if (newDistance < distances.get(link.target)) {
                    distances.put(link.target, newDistance);
                    pq.add(new Node(link.target, newDistance));
                }
            }

            for (Genome genome : genomeMap.values()) {
                for (Genome.Link link : genome.links) {
                    if (link.target.equals(currentId) && !visited.contains(genome.id)) {
                        int newDistance = distances.get(currentId) + link.adaptationFactor;

                        if (newDistance < distances.get(genome.id)) {
                            distances.put(genome.id, newDistance);
                            pq.add(new Node(genome.id, newDistance));
                        }
                    }
                }
            }
        }

        return -1;
    }
}
