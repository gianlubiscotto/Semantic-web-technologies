import java.io.FileOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;

/*Questo file costruisce un dataset di conoscenze coerenti con FOAF e lo esporta
 * in formato JSON-LD. Il dataset verrà poi utilizzato in "linking_friends.java"
 * in cui grazie alla proprietà foaf:nick sarà possibile collegare gli amici
 * agli individui nell'ontologia di partenza.
 */

public class friendDestiny {
   
	private static String NS = "http://achieveme.altervista.org/Gianluca#";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        Model model = ModelFactory.createDefaultModel();

        //creazione risorse
        Resource resME = model.createResource(NS+"me");
        Resource resAC = model.createResource(NS + "Adelmo_Cesaritti");
        Resource resFF = model.createResource(NS + "Federico_Frisoli");
        Resource resMB = model.createResource(NS + "Mattia_Bonanno");
        Resource resRR = model.createResource(NS + "Riccardo_Runcino");
        Resource resFZ = model.createResource(NS + "Francesco_Zuk");
        
        //specifica persone
        resME.addProperty(RDF.type,FOAF.Person);
        resAC.addProperty(RDF.type,FOAF.Person);
        resFF.addProperty(RDF.type,FOAF.Person);
        resMB.addProperty(RDF.type,FOAF.Person);
        resRR.addProperty(RDF.type,FOAF.Person);
        resFZ.addProperty(RDF.type,FOAF.Person);
        
        //aggiunta nomi
        resME.addLiteral(FOAF.givenName, "Gianluca");
        resME.addLiteral(FOAF.familyName, "Ceccoli");
        resAC.addLiteral(FOAF.givenName, "Adelmo");
        resAC.addLiteral(FOAF.familyName, "Cesaritti");
        resFF.addLiteral(FOAF.givenName, "Federico");
        resFF.addLiteral(FOAF.familyName, "Frisoli");
        resMB.addLiteral(FOAF.givenName, "Mattia");
        resMB.addLiteral(FOAF.familyName, "Bonanno");
        resRR.addLiteral(FOAF.givenName, "Riccardo");
        resRR.addLiteral(FOAF.familyName, "Runcino");
        resFZ.addLiteral(FOAF.givenName, "Francesco");
        resFZ.addLiteral(FOAF.familyName, "Zuk");
        
        //aggiunta nickname
        resME.addLiteral(FOAF.nick, "Gianlubiscotto");
        resAC.addLiteral(FOAF.nick, "Captain_Dimonio");
        resFF.addLiteral(FOAF.nick, "GwinbleiddFri");
        resMB.addLiteral(FOAF.nick, "Tred-3");
        resRR.addLiteral(FOAF.nick, "Rickla");
        resFZ.addLiteral(FOAF.nick, "Lucjfero");

        //aggiunta conoscenze
        resME.addProperty(FOAF.knows, resAC);
        resME.addProperty(FOAF.knows, resFF);
        resME.addProperty(FOAF.knows, resMB);
        resME.addProperty(FOAF.knows, resRR);
        resME.addProperty(FOAF.knows, resFZ);
        
        try {
            model.write(new FileOutputStream("friends_jsonjena.json"), "JSON-LD");
        } catch (Exception e) {
        	System.out.println(e);
        }
        model.write(System.out,"N3");
        
	}

}
