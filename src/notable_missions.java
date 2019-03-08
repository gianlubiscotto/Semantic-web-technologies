import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/*In questo file, i personaggi giocati vengono estesi con 
 * una missione da svolgere (luogo da difendere
 * e da quale nemico) sulla base della corrispondenza della razza e dell'abilità
 * con quelle dei "Notable Guardians" all'interno dell'ontologia.
 */

public class notable_missions {

	private static String fnameschema = "destiny_extended_friends.rdf";
	private static String ME = "http://achieveme.altervista.org/Gianluca#";
	private static String NS = "http://www.semanticweb.org/gianluca/ontologies/2018/11/destiny#";

	public static void main(String[] args) {
		
		String prefix = "prefix destiny: <" + NS + ">\n" +
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n" +
                "prefix dbo: <http://dbpedia.org/ontology/>\n" +
                "prefix foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "prefix me: <"+ME+">\n" +
                "prefix bif: <bif:>";
		
		//modello degli amici
		Model friend_schema = ModelFactory.createDefaultModel();
		try {
            friend_schema.read(new FileInputStream("friends_jena_extended.rdf"), ME, "RDF/XML-ABBREV");
        } catch (Exception e) {
        	System.out.println(e);
        	}
		//modello dell'ontologia
		Model protege = FileManager.get().loadModel(fnameschema);
		
		Property plays = friend_schema.getProperty(NS+"plays");
		Property defends = protege.getProperty(NS+"defends");
		Property defendedBy = protege.getProperty(NS+"defendedBy");
		Property fightAgainst = protege.getProperty(NS+"fightAgainst");
		Property foughtBy = protege.getProperty(NS+"foughtBy");

		
		String race_ans = null;
		List<String> type_ans = new ArrayList<String>();
		String subclass = null;
		String location_ans = null;
		String enemy_ans = null;
		
		for (Statement stmt : friend_schema.listStatements(friend_schema.getResource(ME+"me"), FOAF.knows, (RDFNode)null).toList()) {
            RDFNode friend = stmt.getObject();
            Resource temp = friend_schema.getResource(friend.toString());
            String character = temp.getProperty(plays).getObject().toString();
            character = character.replaceAll(NS, "");

            //per ogni personaggio salvo la razza e la sottoclasse
		
			//razza
			race_ans = showOntologyQuery(protege, prefix + 
					"select ?race where {destiny:"+character+" destiny:hasRace ?race}");
			
			//lista di tipi
			type_ans = showURIQuery(protege, prefix + 
					"select ?type ?sub where {destiny:"+character+" a ?type}");
			//se in almeno uno dei tipi c'è la sottoclasse, la prendo altrimenti errore, giocatore senza sottoclasse
			for(int j=0;j<type_ans.size();j++) {
				if(type_ans.get(j).contains("Subclass")) {
					subclass = type_ans.get(j);
					subclass = subclass.substring(subclass.indexOf("<")+1, subclass.indexOf(">"));
					subclass = subclass.replaceAll(NS, "");
					break;
				}
			}
			if(subclass == null) {
				System.err.println("Sottoclasse non trovata");
				System.exit(0);
			}
			
			System.out.println(NS+character + "\t|| destiny:hasRace || " + NS+race_ans);
			System.out.println(NS+character + "\t|| a || " + "destiny:"+subclass);
			location_ans = showOntologyQuery(protege, prefix + 
					"select ?location where {?guardian a destiny:NotableGuardian."
					+ "?guardian a destiny:"+subclass+"."
							+ "?guardian a destiny:"+race_ans+"Character."
									+ "?guardian destiny:defends ?location}");
			enemy_ans = showOntologyQuery(protege, prefix + 
					"select ?enemy where {?guardian a destiny:NotableGuardian."
					+ "?guardian a destiny:"+subclass+"."
							+ "?guardian a destiny:"+race_ans+"Character."
									+ "?guardian destiny:fightAgainst ?enemy}");
			System.out.println("The notable guardian who is an "+race_ans+" "+subclass+" must defend "+" "+
									location_ans+" from "+enemy_ans+", so...");
			
			Resource player = protege.getResource(NS+character);
			Resource planet = protege.getResource(NS+location_ans);
			Resource army = protege.getResource(NS+enemy_ans);
			
			player.addProperty(defends,planet);
			player.addProperty(fightAgainst,army);
			planet.addProperty(defendedBy, player);
			army.addProperty(foughtBy, player);
			
			System.out.println(player.getProperty(defends).toString());
			System.out.println(player.getProperty(fightAgainst).toString()+"\n");
		
		}
		
		 try {
	          protege.write(new FileOutputStream("destiny_fully_inferred.rdf"), "RDF/XML-ABBREV");
	      } catch (Exception e) {
	    	  System.out.println(e);
	      }
				
	}
	
	 protected static String showOntologyQuery( Model m, String q ) {
		 String res=null; 
	     Query query = QueryFactory.create( q );
	     QueryExecution qexec = QueryExecutionFactory.create( query, m );
	     try {
	    	 ResultSetRewindable result = ResultSetFactory.makeRewindable( qexec.execSelect() );
			 //System.out.println(ResultSetFormatter.asText(result));
			 result.reset(); // back to the start
			 while (result.hasNext()) {
				 QuerySolution temp = result.next();
				 res = temp.toString().substring(temp.toString().indexOf("<")+1, temp.toString().indexOf(">"));
				 res = res.replaceAll(NS, "");
			  }
	      }
	      finally {
	          qexec.close();
	      }
	      return res;

	  }
	 protected static List<String> showURIQuery( Model m, String q ) {
		 List<String> ans=new ArrayList<String>();
	     Query query = QueryFactory.create( q );
	     QueryExecution qexec = QueryExecutionFactory.create( query, m );
	     try {
	    	 ResultSetRewindable result = ResultSetFactory.makeRewindable( qexec.execSelect() );
			 //System.out.println(ResultSetFormatter.asText(result));
			 result.reset(); // back to the start
			 while (result.hasNext()) {
				 QuerySolution temp = result.next();
				 ans.add(temp.toString());
			  }
	      }
	      finally {
	          qexec.close();
	      }
	     return ans;
	  }

}
