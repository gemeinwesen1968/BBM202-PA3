import java.io.File;

public class Main {
    public static void main(String[] args) {
        File xmlFile = new File(args[0]);
        AlienFlora f = new AlienFlora(xmlFile);

        System.out.println("##Start Reading Flora Genomes##");
        f.readGenomes();
        System.out.println("##Reading Flora Genomes Completed##");


        System.out.println("##Start Evaluating Possible Evolutions##");
        f.evaluateEvolutions();
        System.out.println("##Evaluated Possible Evolutions##");

        System.out.println("##Start Evaluating Possible Adaptations##");
        f.evaluateAdaptations();
        System.out.print("##Evaluated Possible Adaptations##");
    }
}
