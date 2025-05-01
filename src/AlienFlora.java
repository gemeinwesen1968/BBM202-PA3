import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class AlienFlora {
    private final File xmlFile;
    private final Map<String, Genome> allGenomes = new HashMap<>();
    private final List<GenomeCluster> clusters = new ArrayList<>();
    private final List<Double> possibleEvolutionResults = new ArrayList<>();
    private final List<Integer> adaptationResults = new ArrayList<>();
    public AlienFlora(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public void readGenomes() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList genomeList = doc.getElementsByTagName("genome");
            for (int i = 0; i < genomeList.getLength(); i++) {
                Node genomeNode = genomeList.item(i);

                if (genomeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element genomeELement = (Element) genomeNode;

                    String id = genomeELement.getElementsByTagName("id").item(0).getTextContent();
                    int evolutionFactor = Integer.parseInt(
                            genomeELement.getElementsByTagName("evolutionFactor").item(0).getTextContent()
                    );
                    Genome genome = new Genome(id, evolutionFactor);

                    NodeList linkList = genomeELement.getElementsByTagName("link");
                    for (int j = 0; j < linkList.getLength(); j++) {
                        Element linkElement = (Element) linkList.item(j);
                        String target = linkElement.getElementsByTagName("target").item(0).getTextContent();
                        int adaptationFactor = Integer.parseInt(
                                linkElement.getElementsByTagName("adaptationFactor").item(0).getTextContent()
                        );

                        genome.addLink(target, adaptationFactor);
                    }

                    allGenomes.put(id, genome);
                }
            }

            createClusters();

            System.out.println("Number of Genome Clusters: " + clusters.size());
            List<List<String>> clusterGenomeIds = new ArrayList<>();
            for (GenomeCluster cluster : clusters) {
                List<String> genomeIds = new ArrayList<>(cluster.genomeMap.keySet());
                clusterGenomeIds.add(genomeIds);
            }
            System.out.println("For the Genomes: " + clusterGenomeIds);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createClusters() {
        Set<String> visited = new HashSet<>();

        for (String genomeId : allGenomes.keySet()) {
            if (!visited.contains(genomeId)) {
                GenomeCluster cluster = new GenomeCluster();
                dfs(genomeId, visited, cluster);
                clusters.add(cluster);
            }
        }
    }

    private void dfs(String genomeId, Set<String> visited, GenomeCluster cluster) {
        visited.add(genomeId);
        Genome genome = allGenomes.get(genomeId);
        cluster.addGenome(genome);

        for (Genome.Link link : genome.links) {
            if (!visited.contains(link.target) && allGenomes.containsKey(link.target)) {
                dfs(link.target, visited, cluster);
            }
        }

        for (Genome otherGenome : allGenomes.values()) {
            for (Genome.Link link : otherGenome.links) {
                if (link.target.equals(genomeId) && !visited.contains(otherGenome.id)) {
                    dfs(otherGenome.id, visited, cluster);
                }
            }
        }
    }

    public void evaluateEvolutions() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            int numOfCertified = 0;
            Node evolutionPairsNode = doc.getElementsByTagName("possibleEvolutionPairs").item(0);

            NodeList pairList = ((Element) evolutionPairsNode).getElementsByTagName("pair");
            for (int i = 0; i < pairList.getLength(); i++) {
                Node pair = pairList.item(i);
                if (pair.getNodeType() == Node.ELEMENT_NODE) {
                    Element genomeELement = (Element) pair;
                    String firstId = genomeELement.getElementsByTagName("firstId").item(0).getTextContent();
                    String secondId = genomeELement.getElementsByTagName("secondId").item(0).getTextContent();

                    GenomeCluster firstIdCluster = findCluster(firstId);
                    GenomeCluster secondIdCluster = findCluster(secondId);
                    double result;
                    if (firstIdCluster == secondIdCluster) {
                        result = -1.0;
                    } else {
                        assert firstIdCluster != null;
                        assert secondIdCluster != null;
                        result = (double) (firstIdCluster.getMinEvolutionGenome().evolutionFactor + secondIdCluster.getMinEvolutionGenome().evolutionFactor) / 2;
                        numOfCertified++;
                    }
                    possibleEvolutionResults.add(result);
                }
            }

            System.out.println("Number of Possible Evolutions: " + pairList.getLength());
            System.out.println("Number of Certified Evolution: " + numOfCertified);
            System.out.println("Evolution Factor for Each Evolution Pair: " + possibleEvolutionResults);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GenomeCluster findCluster(String id) {
        for (GenomeCluster cluster : clusters) {
            if (cluster.contains(id)) {
                return cluster;
            }
        }
        return null;
    }

    public void evaluateAdaptations() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            int numOfCertified = 0;
            Node adaptationPairsNode = doc.getElementsByTagName("possibleAdaptationPairs").item(0);

            NodeList pairList = ((Element) adaptationPairsNode).getElementsByTagName("pair");
            for (int i = 0; i < pairList.getLength(); i++) {
                Node pair = pairList.item(i);
                if (pair.getNodeType() == Node.ELEMENT_NODE) {
                    Element genomeELement = (Element) pair;
                    String firstId = genomeELement.getElementsByTagName("firstId").item(0).getTextContent();
                    String secondId = genomeELement.getElementsByTagName("secondId").item(0).getTextContent();

                    GenomeCluster firstIdCluster = findCluster(firstId);
                    GenomeCluster secondIdCluster = findCluster(secondId);

                    if (firstIdCluster == secondIdCluster) {
                        assert firstIdCluster != null;
                        adaptationResults.add(firstIdCluster.dijkstra(firstId, secondId));
                        numOfCertified++;
                    } else {
                        adaptationResults.add(-1);
                    }
                }
            }

            System.out.println("Number of Possible Adaptations: " + pairList.getLength());
            System.out.println("Number of Certified Adaptations: " + numOfCertified);
            System.out.println("Adaptation Factor for Each Adaptation Pair: " + adaptationResults);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
