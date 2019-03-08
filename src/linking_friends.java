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

/*Questo file consente di collegare il dataset di conoscenze all'ontologia
 * mediante matching del nickname che ogni persona possiede.
 * Nell'ontologia sono stati inseriti gli alter ego di ognunp dei conoscenti 
 * e sono stati chiamati come le proprietà foaf:nick di ognuno.
 * A questo punto i due domini vengono collegati mediante una proprietà
 * che indica la corrispondenza tra giocatore e personaggio giocato. 
 */

public class linking_friends {
	private static String fnameschema = "destiny_extended.rdf";
	private static String NS = "http://www.semanticweb.org/gianluca/ontologies/2018/11/destiny#";
	private static String ME = "http://achieveme.altervista.org/Gianluca#";
	
	public static void main(String[] args) {
		
		//modello degli amici
		Model friend_schema = ModelFactory.createDefaultModel();
		//modello dell'ontologia
		Model protege = FileManager.get().loadModel(fnameschema);
		
		Property plays = protege.createProperty(NS + "plays");
		Property isPlayedBy = protege.createProperty(NS + "isPlayedBy");
		plays.addProperty(OWL.inverseOf, isPlayedBy);
		isPlayedBy.addProperty(OWL.inverseOf, plays);
		
		String prefix = "prefix destiny: <" + NS + ">\n" +
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n" +
                "prefix yago: <http://dbpedia.org/class/yago/>\n" +
                "prefix dbo: <http://dbpedia.org/ontology/>\n" +
                "prefix foaf: <http://xmlns.com/foaf/0.1/>" +
                "prefix bif: <bif:>";
        
        try {
            friend_schema.read(new FileInputStream("friends_jsonjena.json"), ME, "JSON-LD");
        } catch (Exception e) {
        	System.out.println(e);
        	}
      
        //query alla mia ontologia per trovare i guardiani
	    List<String> ont_ans = showOntologyQuery( protege,
	               prefix + "select ?individual where{ ?individual a destiny:Guardian}");

	    //per ogni persona conosciuta guardo se esiste un corrispondente nickname nell'ontologia
        for (Statement stmt : friend_schema.listStatements(friend_schema.getResource(ME+"me"), FOAF.knows, (RDFNode)null).toList()) {
            RDFNode friend = stmt.getObject();
            Resource temp = friend_schema.getResource(friend.toString());
            String nick = temp.getProperty(FOAF.nick).getObject().toString();
		    for(int i=0;i<ont_ans.size();i++) {
		    	if(nick.matches(ont_ans.get(i))) {
		    		System.out.println(ME+nick+" \t|| destiny:plays || "+NS+ont_ans.get(i));
		    		temp.addProperty(plays, NS+ont_ans.get(i));
		    		protege.getResource(NS+ont_ans.get(i)).addProperty(isPlayedBy, temp.toString());
		    		ont_ans.remove(i);
		    		break;
		    	}
		    }
		    
		}
        System.out.println();
        friend_schema.write(System.out, "TURTLE");
        try {
            protege.write(new FileOutputStream("destiny_extended_friends.rdf"), "RDF/XML-ABBREV");
        } catch (Exception e) {
      	  System.out.println(e);
        }
        try {
            friend_schema.write(new FileOutputStream("friends_jena_extended.rdf"), "RDF/XML-ABBREV");
        } catch (Exception e) {
      	  System.out.println(e);
        }
	}
	
	protected static List<String> showOntologyQuery( Model m, String q ) {
		  List<String> answer = new ArrayList<String>();      
	      Query query = QueryFactory.create( q );
	      QueryExecution qexec = QueryExecutionFactory.create( query, m );
	      try {
	    	  ResultSetRewindable result = ResultSetFactory.makeRewindable( qexec.execSelect() );
			  //System.out.println(ResultSetFormatter.asText(result));
			  result.reset(); // back to the start
			  while (result.hasNext()) {
				  QuerySolution temp = result.next();
				  String res = temp.toString().substring(temp.toString().indexOf("<")+1, temp.toString().indexOf(">"));
				  res = res.replaceAll(NS, "");
				  answer.add(res);
			  }
	      }
	      finally {
	          qexec.close();
	      }
	      return answer;

	  }
	}
